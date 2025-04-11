package com.zj.common.adapter.invoker.eureka;

import com.alibaba.fastjson.JSON;
import com.zj.common.adapter.discover.DiscoverService;
import com.zj.common.adapter.discover.ServiceInstance;
import com.zj.common.adapter.invoker.IClientInvoker;
import com.zj.common.entity.dto.ClientCollectDto;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.common.entity.dto.StopDispatch;
import com.zj.common.entity.service.ToolLoadResult;
import com.zj.common.entity.service.ToolVersionDto;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public class EurekaClientInvokerAdapter extends BaseEurekaAdapter implements IClientInvoker {

    private static final String DISPATCH_GENERATE_TASK = "http://WindyClient/v1/client/dispatch/generate";
    private static final String LOAD_LANGUAGE_VERSION = "http://%s/v1/devops/client/load/build/version";
    private static final String DISPATCH_PIPELINE_TASK = "http://WindyClient/v1/client/dispatch/pipeline";
    private static final String DISPATCH_FEATURE_TASK = "http://WindyClient/v1/client/dispatch/feature";
    private static final String MASTER_NOTIFY_CLIENT_STOP = "http://%s/v1/client/task/stop";
    public static final String CLIENT_MONITOR_URL = "http://%s/v1/devops/client/instance";

    private final DiscoverService discoverService;

    public EurekaClientInvokerAdapter(DiscoverService discoverService, RestTemplate restTemplate) {
        super(restTemplate);
        this.discoverService = discoverService;
    }

    @Override
    public boolean runGenerateTask(Object generateInfo) {
        ResponseEntity<String> response = requestPost(DISPATCH_GENERATE_TASK, generateInfo);
        if (Objects.isNull(response)) {
            return false;
        }
        log.info("run generate response result ={}", response.getBody());
        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public boolean runPipelineTask(Object pipelineTask, boolean isRequestSingle, String singleIp) {
        if (isRequestSingle) {
            Optional<ServiceInstance> optional = discoverService.getServiceInstances(DiscoverService.WINDY_CLIENT)
                    .stream()
                    .filter(service -> Objects.equals(service.getIp(), singleIp))
                    .findAny();
            if (!optional.isPresent()) {
                log.info("send single request error, service not find ={}", singleIp);
                return false;
            }
            String url = DISPATCH_PIPELINE_TASK.replace(DiscoverService.WINDY_CLIENT, optional.get().getHost());
            Response response = postWithIp(url, pipelineTask);
            if (Objects.nonNull(response)) {
                response.close();
            }
            return Optional.ofNullable(response).map(Response::isSuccessful).orElse(false);
        }

        ResponseEntity<String> response = requestPost(DISPATCH_PIPELINE_TASK, pipelineTask);
        if (Objects.isNull(response)) {
            return false;
        }
        log.info("run pipeline response result ={}", response.getBody());
        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public boolean runFeatureTask(Object featureTask) {
        ResponseEntity<String> response = requestPost(DISPATCH_FEATURE_TASK, featureTask);
        if (Objects.isNull(response)) {
            return false;
        }
        log.info("run feature response status result ={}", response.getBody());
        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public void stopTaskLoopQuery(StopDispatch stopDispatch) {
        CompletableFuture.runAsync(() -> {
            List<ServiceInstance> windyClientInstances = discoverService.getWindyClientInstances();
            windyClientInstances.forEach(serviceInstance -> {
                String url = String.format(MASTER_NOTIFY_CLIENT_STOP, serviceInstance.getHost());
                log.info("start stop loop query task status targetId={} targetType={}", stopDispatch.getTargetId(),
                        stopDispatch.getLogType());
                Response response = postWithIp(url, stopDispatch);
                if (Objects.nonNull(response)) {
                    response.close();
                }
            });
        });
    }

    @Override
    public List<ToolLoadResult> loadBuildTool(ToolVersionDto toolVersion) {
        List<ServiceInstance> serviceInstances = discoverService.getServiceInstances(DiscoverService.WINDY_CLIENT);
        return serviceInstances.stream().map(service -> {
            ToolLoadResult toolLoadResult = new ToolLoadResult();
            toolLoadResult.setNodeIP(service.getIp());
            try {
                String url = String.format(LOAD_LANGUAGE_VERSION, service.getHost());
                Response response = postWithIp(url, toolVersion);
                if (Objects.isNull(response)) {
                    toolLoadResult.setSuccess(false);
                    return toolLoadResult;
                }
                String resultString = response.body().string();
                log.info("get support versions = {}", resultString);
                ResponseMeta responseMeta = JSON.parseObject(resultString, ResponseMeta.class);
                Boolean loadResult = Optional.ofNullable(responseMeta).map(res -> JSON.parseObject(JSON.toJSONString(res.getData()),
                        ToolLoadResult.class)).map(ToolLoadResult::getSuccess).orElse(false);
                toolLoadResult.setSuccess(loadResult);
                response.close();
            } catch (Exception e) {
                log.info("request client load tool error", e);
                toolLoadResult.setSuccess(false);
            }
            return toolLoadResult;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ClientCollectDto> requestClientMonitor() {
        List<ServiceInstance> serviceInstances = discoverService.getServiceInstances(DiscoverService.WINDY_CLIENT);
        return serviceInstances.stream().map(service -> {
            String url = String.format(CLIENT_MONITOR_URL, service.getHost());
            log.info(" start request client monitor data url = {}", url);
            Response response = getWithIp(url);
            if (Objects.isNull(response)) {
                return null;
            }
            try {
                String resultString = response.body().string();
                log.info("request client monitor result={}", resultString);
                ResponseMeta result = JSON.parseObject(resultString, ResponseMeta.class);
                response.close();
                return JSON.parseObject(JSON.toJSONString(result.getData()), ClientCollectDto.class);
            } catch (IOException e) {
                log.info("handle client monitor result error");
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
