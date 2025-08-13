package com.zj.notify.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HybridFactory implements ApplicationContextAware {

    private static BeanFactory context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
        log.info("ApplicationContext has been set successfully");
    }


    public static <T> T get(Class<T> clazz, T defaultInstance) {
        // 优先使用SPI
        T instance = SpiFactory.get(clazz, null);
        if (instance != null) {
            return instance;
        }

        // SPI没有则尝试Spring容器
        if (context != null) {
            try {
                return context.getBean(clazz);
            } catch (Exception ignored) {
                log.error("HybridFactory get 找不到bean: {}", clazz.getName());
            }
        }

        // 都没有则使用默认实现
        return defaultInstance;
    }
}
