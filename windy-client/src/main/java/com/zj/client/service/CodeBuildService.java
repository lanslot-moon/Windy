package com.zj.client.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback.Adapter;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.AuthConfigurations;
import com.github.dockerjava.api.model.PushResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.entity.dto.CodeBuildParamDto;
import com.zj.client.handler.pipeline.build.CodeBuildContext;
import com.zj.client.handler.pipeline.build.CodeBuildFactory;
import com.zj.client.handler.pipeline.build.ICodeBuilder;
import com.zj.client.handler.pipeline.executer.notify.PipelineEventFactory;
import com.zj.client.handler.pipeline.executer.vo.PipelineStatusEvent;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel.ResponseStatus;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.client.handler.pipeline.git.IGitProcessor;
import com.zj.common.entity.WindyConstants;
import com.zj.common.enums.DeployType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.utils.GitUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Slf4j
@Service
public class CodeBuildService {

    public static final int BUILD_SUCCESS = 0;
    public static final String BUILD_SUCCESS_TIPS = "构建成功";
    public static final String SUFFIX = "/";
    public static final String SPLIT_STRING = ":";
    public final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private final IGitProcessor gitProcessor;
    private final Executor executorService;
    private final GlobalEnvConfig globalEnvConfig;
    private final CodeBuildFactory codeBuildFactory;

    private final Map<String, QueryResponseModel> statusMap = new ConcurrentHashMap<>();

    public CodeBuildService(IGitProcessor gitProcessor, @Qualifier("gitOperatePool") Executor executorService,
                            GlobalEnvConfig globalEnvConfig, CodeBuildFactory codeBuildFactory) {
        this.gitProcessor = gitProcessor;
        this.executorService = executorService;
        this.globalEnvConfig = globalEnvConfig;
        this.codeBuildFactory = codeBuildFactory;
    }

    public void buildCode(CodeBuildParamDto codeBuildParam, TaskNode taskNode) {
        saveStatus(codeBuildParam.getRecordId(), ProcessStatus.RUNNING, "构建中", null);
        executorService.execute(() -> {
            try {
                //从git服务端拉取代码
                String gitUrl = codeBuildParam.getGitUrl();
                String serviceName = Optional.ofNullable(codeBuildParam.getServiceName())
                        .filter(StringUtils::isNoneBlank)
                        .orElseGet(() -> GitUtils.getServiceFromUrl(gitUrl));
                String pipelineWorkspace = globalEnvConfig.getPipelineWorkspace(serviceName, codeBuildParam.getPipelineId());

                //1 拉取代码到本地
                updateProcessMsg(taskNode, "拉取代码: " + gitUrl);
                updateProcessMsg(taskNode, "拉取分支: " + codeBuildParam.getBranches());
                pullCodeFrmGit(codeBuildParam, pipelineWorkspace);
                updateProcessMsg(taskNode, "拉取代码完成");

                //开始构建代码产物
                String pomPath = getTargetPomPath(pipelineWorkspace, codeBuildParam.getPomPath());
                ICodeBuilder codeBuilder = codeBuildFactory.getCodeBuilder(codeBuildParam.getCode());
                CodeBuildContext context = new CodeBuildContext();
                context.setBuildFile(pomPath);
                context.setTargetDir(pipelineWorkspace);
                context.setServiceName(serviceName);
                context.setVersion(codeBuildParam.getVersion());
                context.setBuildPath(codeBuildParam.getBuildVersion());
                Integer exitCode = codeBuilder.build(context, message -> {
                    if (StringUtils.isNotBlank(message)) {
                        notifyMessage(taskNode, message);
                    }
                });
                log.info("get maven exit code={}", exitCode);
                updateProcessMsg(taskNode, "代码产物构建完成 状态码: " + exitCode);

                //3 构建docker镜像
                String remoteImage = "";
                if (checkImageRepository(codeBuildParam)) {
                    updateProcessMsg(taskNode, "开始构建docker镜像");
                    String dockerFilePath = new File(pomPath).getParentFile().getPath() + File.separator + "docker"
                            + File.separator + "Dockerfile";
                    remoteImage = startBuildDocker(serviceName, dockerFilePath, codeBuildParam);
                    updateProcessMsg(taskNode, "构建docker镜像完成 镜像地址: " + remoteImage);
                }

                // 4处理构建结果
                String deployDirPath = new File(pomPath).getParentFile().getPath() + File.separator + WindyConstants.DEPLOY;
                handleBuildResult(codeBuildParam, exitCode, remoteImage, deployDirPath);
            } catch (Exception e) {
                log.error("buildCode error", e);
                saveStatus(codeBuildParam.getRecordId(), ProcessStatus.FAIL, e.getMessage(), null);
            }
        });
    }

    private boolean checkImageRepository(CodeBuildParamDto codeBuildParam) {
        if (Objects.equals(codeBuildParam.getDeployType(), DeployType.SSH.getType())) {
            return false;
        }
        return StringUtils.isNotBlank(codeBuildParam.getRepository()) && StringUtils.isNotBlank(
                codeBuildParam.getUser()) && StringUtils.isNotBlank(codeBuildParam.getPassword());
    }

    private void handleBuildResult(CodeBuildParamDto codeBuildParamDto, Integer exitCode, String remoteImage,
                                   String deployDirPath) {
        ProcessStatus result = Optional.of(exitCode).filter(code -> Objects.equals(BUILD_SUCCESS, code))
                .map(code -> ProcessStatus.SUCCESS).orElse(ProcessStatus.FAIL);
        Map<String, Object> context = new HashMap<>();
        context.put(WindyConstants.IMAGE_NAME, remoteImage);
        context.put(WindyConstants.BUILD_FILE_PATH, deployDirPath);
        saveStatus(codeBuildParamDto.getRecordId(), result, BUILD_SUCCESS_TIPS, context);
    }

    /**
     * 只有构建消息才需要运行日志
     */
    private void notifyMessage(TaskNode taskNode, String line) {
        QueryResponseModel model = statusMap.get(taskNode.getRecordId());
        if (model.getMessage().size() >= 400) {
            model.getMessage().remove(0);
        }

        if (!line.contains("Progress") && !line.contains("Downloading") && !line.contains("Downloaded")) {
            model.addMessage(line);
        }

        PipelineStatusEvent statusEvent = PipelineStatusEvent.builder()
                .taskNode(taskNode)
                .processStatus(ProcessStatus.exchange(model.getStatus()))
                .errorMsg(model.getMessage())
                .context(model.getContext())
                .build();
        PipelineEventFactory.sendNotifyEvent(statusEvent);
    }

    private String startBuildDocker(String serviceName, String dockerFilePath, CodeBuildParamDto param)
            throws InterruptedException {
        String version = Optional.ofNullable(param.getVersion()).filter(StringUtils::isNoneBlank).orElseGet(() -> dateFormat.format(new Date()));
        File dockerFile = new File(dockerFilePath);
        return buildDocker(serviceName.toLowerCase(), version, dockerFile, param);
    }

    private void pullCodeFrmGit(CodeBuildParamDto codeBuildParamDto, String pipelineWorkspace)
            throws Exception {
        if (codeBuildParamDto.isPublish()) {
            gitProcessor.createTempBranch(codeBuildParamDto, codeBuildParamDto.getBranches(), pipelineWorkspace);
        } else {
            String branch = codeBuildParamDto.getBranches().stream().findFirst().orElse(null);
            gitProcessor.pullCodeFromGit(codeBuildParamDto, branch, pipelineWorkspace);
        }
    }

    private void updateProcessMsg(TaskNode taskNode, String message) {
        notifyMessage(taskNode, "======= " + message);
    }

    private void saveStatus(String recordId, ProcessStatus status, String message, Map<String, Object> context) {
        QueryResponseModel model = Optional.ofNullable(statusMap.get(recordId)).orElse(new QueryResponseModel());
        model.setStatus(status.getType());
        model.setContext(context);
        model.addMessage(message);
        model.setData(new ResponseStatus(status.getType()));
        statusMap.put(recordId, model);
    }

    private String getTargetPomPath(String pipelineWorkspace, String configPath) {
        return pipelineWorkspace + File.separator + configPath;
    }

    public QueryResponseModel getRecordStatus(String recordId) {
        return statusMap.get(recordId);
    }

    public String buildDocker(String imageName, String version, File dockerfile,
                              CodeBuildParamDto param) throws InterruptedException {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
        BuildImageResultCallback callback = new BuildImageResultCallback() {
            @Override
            public void onStart(Closeable stream) {
                super.onStart(stream);
                log.info("start build docker image recordId={}", param.getRecordId());
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                throwable.printStackTrace();
                saveStatus(param.getRecordId(), ProcessStatus.FAIL, "镜像构建失败", null);
            }
        };

        //构建镜像
        String repository = param.getRepository();
        String imageUrl = repository.endsWith(SUFFIX) ? repository : repository + SUFFIX;
        String tag = imageName + SPLIT_STRING + version;
        String imageRepository = imageUrl + tag;
        log.info("docker imageRepository ={}", imageRepository);
        // 设置登陆远程仓库的用户信息
        AuthConfig authConfig = new AuthConfig().withRegistryAddress(repository)
                .withUsername(param.getUser()).withPassword(param.getPassword());
        AuthConfigurations authConfigs = new AuthConfigurations();
        authConfigs.addConfig(authConfig);
        dockerClient.buildImageCmd().withDockerfile(dockerfile)
                .withBuildAuthConfigs(authConfigs)
                .withTags(Collections.singleton(imageRepository)).exec(callback).awaitImageId();

        // 将镜像推送到远程镜像仓库
        Adapter<PushResponseItem> responseItemAdapter = dockerClient.pushImageCmd(imageRepository)
                .withAuthConfig(authConfig).start();
        responseItemAdapter.awaitCompletion();
        return imageRepository;
    }
}
