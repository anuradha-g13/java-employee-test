package com.reliaquest.api.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.reliaquest.api.model.Employee;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@Data
public class CacheConfig {

    @Value("${cache.size:500}")
    private Integer cacheSize;

    @Value("${cache.expire.time.min:10}")
    private Integer cacheExpireTime;

    @Bean
    public Cache<String, Employee> employeeCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterAccess(cacheExpireTime, TimeUnit.MINUTES)
                .build();
    }
}
