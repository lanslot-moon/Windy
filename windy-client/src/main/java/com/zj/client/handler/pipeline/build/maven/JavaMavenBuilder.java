package com.zj.client.handler.pipeline.build.maven;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.handler.pipeline.build.CodeBuildContext;
import com.zj.client.handler.pipeline.build.IBuildNotifyListener;
import com.zj.client.handler.pipeline.build.ICodeBuilder;
import com.zj.common.entity.WindyConstants;
import com.zj.common.enums.ToolType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.shared.invoker.*;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Slf4j
@Component
public class JavaMavenBuilder implements ICodeBuilder {
    public static final String SH_COMMAND_FORMAT = "nohup java -jar %s > app.log 2>&1 &";
    private final GlobalEnvConfig globalEnvConfig;
    private List<String> templateShell;

    public JavaMavenBuilder(GlobalEnvConfig globalEnvConfig) {
        this.globalEnvConfig = globalEnvConfig;
        try {
            URL resourceURL = ResourceUtils.getURL("classpath:start.sh");
            InputStream inputStream = resourceURL.openStream();
            templateShell = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
            inputStream.close();
        } catch (Exception e) {
            log.warn("load template sh file error", e);
        }
    }

    @Override
    public String codeType() {
        return ToolType.JAVA.getType();
    }

    @Override
    public Integer build(CodeBuildContext context, IBuildNotifyListener notifyListener) {
        try {
            log.info("start build java code = {}", JSON.toJSONString(context));
            return buildMaven(context, notifyListener::notifyMessage);
        } catch (Exception e) {
            log.info("execute maven error", e);
        }
        return -1;
    }

    public Integer buildMaven(CodeBuildContext context, InvocationOutputHandler outputHandler)
            throws IOException, MavenInvocationException {
        // 创建一个Maven构建请求
        InvocationRequest ideaRequest = new DefaultInvocationRequest();
        // 设置构建的根目录
        ideaRequest.setBaseDirectory(new File(context.getTargetDir()));
        // 设置也构建依赖的项目
        ideaRequest.setAlsoMakeDependents(true);
        // 设置构建的目标，这里使用的是Maven的"package"目标
        ideaRequest.setGoals(Collections.singletonList("package"));

        // 动态版本号传递，如果没有传递，则使用pom中的版本
        Properties properties = new Properties();
        // 如果上下文中有版本号且不为空，则将其设置为"revision"属性
        Optional.ofNullable(context.getVersion())
                .filter(StringUtils::isNoneBlank).ifPresent(dynamicVersion -> properties.setProperty("revision", dynamicVersion));
        ideaRequest.setProperties(properties);

        // 获取Maven的安装目录，如果没有传递，则使用全局配置中的Maven路径
        String mavenDir = Optional.ofNullable(context.getBuildPath())
                .filter(StringUtils::isNoneBlank)
                .orElseGet(globalEnvConfig::getMavenPath);
        // 检查Maven路径是否为空，如果为空则抛出异常
        Preconditions.checkNotNull(mavenDir, "maven path can not find , consider to fix it");
        // 创建Maven调用器
        Invoker mavenInvoker = new DefaultInvoker();
        // 设置Maven的安装目录
        mavenInvoker.setMavenHome(new File(mavenDir));
        // 设置输出处理器，用于处理构建过程中的输出信息
        mavenInvoker.setOutputHandler(outputHandler);
        // 执行Maven构建请求
        InvocationResult ideaResult = mavenInvoker.execute(ideaRequest);
        // 获取POM文件
        File pomFile = new File(context.getBuildFile());
        // 将构建的JAR文件复制到部署目录
        copyJar2DeployDir(pomFile);
        // 返回构建的退出码
        return ideaResult.getExitCode();
    }

    /**
     * 将jar文件拷贝到部署目录
     */
    private void copyJar2DeployDir(File pomFile) throws IOException {
        Collection<File> files = FileUtils.listFiles(pomFile.getParentFile(), new String[]{"jar"}, true);
        File jarFile = files.stream().findFirst().orElse(null);
        if (Objects.isNull(jarFile)) {
            throw new ApiException(ErrorCode.NOT_FIND_JAR);
        }

        //ssh镜像部署
        String destDir = pomFile.getParentFile().getPath() + File.separator + WindyConstants.DEPLOY;
        File dir = new File(destDir);
        createSHFileIfNeed(jarFile.getName(), destDir, dir);
        FileUtils.copyToDirectory(jarFile, dir);

        //docker镜像部署
        String dockerDir = pomFile.getParentFile().getPath() + File.separator + WindyConstants.DOCKER;
        File dockerDirFile = new File(dockerDir);
        createSHFileIfNeed(jarFile.getName(), dockerDir, dockerDirFile);
        FileUtils.copyToDirectory(jarFile, dockerDirFile);
    }

    private void createSHFileIfNeed(String jarName, String destDir, File dir) throws IOException {
        // 检查目录是否存在，如果不存在则创建目录
        if (!dir.exists()) {
            boolean result = dir.mkdirs();
            // 记录创建目录的结果
            log.debug("creat sh file parent path result={}", result);
        }

        // 在指定目录下查找所有扩展名为.sh的文件
        Collection<File> shFiles = FileUtils.listFiles(dir, new String[]{"sh"}, false);
        // 如果找到了.sh文件，则记录日志并返回，不创建默认的.sh文件
        if (CollectionUtils.isNotEmpty(shFiles)) {
            log.info("destination dir={} hava sh file, not create default sh file", destDir);
            return;
        }
        // 如果没有找到.sh文件，则创建默认的.sh文件
        createDefaultSHFile(destDir, jarName);
    }

    /**
     * 用于创建默认的Shell脚本文件
     */
    private void createDefaultSHFile(String destDir, String name) {
        try {
            // 创建目标文件对象，文件路径为destDir目录下的start.sh
            File destFile = new File(destDir + File.separator + "start.sh");
            // 创建一个列表来存储命令，初始内容为templateShell中的命令
            List<String> commands = new ArrayList<>(templateShell);
            // 使用String.format方法格式化命令字符串，将name参数插入到命令模板中
            String command = String.format(SH_COMMAND_FORMAT, name);
            // 将格式化后的命令添加到命令列表中
            commands.add(command);
            // 使用FileUtils.writeLines方法将命令列表写入到目标文件中
            // 参数分别为：目标文件、字符编码、命令列表、行分隔符、是否追加到文件末尾
            FileUtils.writeLines(destFile, StandardCharsets.UTF_8.name(), commands, "\r\n", true);
        } catch (IOException e) {
            // 捕获IOException异常，并记录错误日志
            log.error("write event to file error", e);
        }
    }
}
