package com.zj.metrics.schedule;

import com.zj.common.adapter.pool.WindyThreadPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class PoolConfiguration {

    @Bean("metricParserPool")
    public Executor getMetricParserPool() {
        WindyThreadPool windyThreadPool = new WindyThreadPool();
        windyThreadPool.setCorePoolSize(2);
        windyThreadPool.setMaxPoolSize(60);
        windyThreadPool.setAllowCoreThreadTimeOut(true);
        windyThreadPool.setQueueSize(1000);
        windyThreadPool.setThreadNamePrefix("metric-");
        windyThreadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return windyThreadPool;
    }
}
