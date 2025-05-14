package com.lazzen.hec;

import org.apache.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;

/**
 * @author caszhou
 * @date 2023/4/19
 */
@SpringBootApplication(exclude = {DruidDataSourceAutoConfigure.class, SpringBootConfiguration.class})
public class LazzenHecServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LazzenHecServiceApplication.class, args);
    }
}
