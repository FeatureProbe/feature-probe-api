package com.featureprobe.api.cache;

import com.featureprobe.api.cache.dto.SdkKeyResponse;
import com.featureprobe.api.cache.dto.ServerResponse;
import com.featureprobe.api.cache.service.ServerService;
import com.featureprobe.api.dao.entity.ChangeLog;
import com.featureprobe.api.dao.repository.ChangeLogRepository;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class LocalCache {

    ServerService serverService;

    ChangeLogRepository changeLogRepository;

    @Bean
    public LoadingCache<String, ServerResponse> toggleCache() {
        LoadingCache<String, ServerResponse> cache = CacheBuilder.newBuilder()
                .maximumSize(10000L)
                .build(new CacheLoader<String, ServerResponse>() {
                    @Override
                    public ServerResponse load(String key) throws Exception {
                        return serverService.queryServerTogglesByServerSdkKey(key);
                    }
                });
        return cache;
    }

    @Bean
    public LoadingCache<String, SdkKeyResponse> sdkKeyCache() {
        LoadingCache<String, SdkKeyResponse> cache = CacheBuilder.newBuilder()
                .maximumSize(1L)
                .build(new CacheLoader<String, SdkKeyResponse>() {
                    @Override
                    public SdkKeyResponse load(String key) throws Exception {
                        return serverService.queryAllSdkKeys();
                    }
                });
        return cache;
    }

    @Bean
    public LoadingCache<String, Long> maxChangeLogId() {
        LoadingCache<String, Long> cache = CacheBuilder.newBuilder()
                .maximumSize(1L)
                .build(new CacheLoader<String, Long>() {
                    @Override
                    public Long load(String key) throws Exception {
                        List<ChangeLog> changeLogs = changeLogRepository.findAll(PageRequest.of(0, 1,
                                Sort.Direction.DESC, "id")).getContent();
                        return CollectionUtils.isNotEmpty(changeLogs) ? changeLogs.get(0).getId() : 0;
                    }
                });
        return cache;
    }

}
