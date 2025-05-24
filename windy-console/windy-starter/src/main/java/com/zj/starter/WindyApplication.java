package com.zj.starter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author guyuelan
 * @since 2023/1/28
 */
@EnableScheduling
@ComponentScan("com.zj")
@MapperScan("com.zj.*.mapper")
@SpringBootApplication
@EnableEurekaClient
public class WindyApplication {

  public static void main(String[] args) {
    SpringApplication.run(WindyApplication.class, args);
  }

}
