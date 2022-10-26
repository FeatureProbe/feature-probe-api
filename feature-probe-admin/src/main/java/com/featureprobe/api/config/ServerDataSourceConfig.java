package com.featureprobe.api.config;

import com.featureprobe.api.cache.ICache;
import com.featureprobe.api.cache.MemoryCache;
import com.featureprobe.api.dao.repository.ChangeLogRepository;
import com.featureprobe.api.server.CacheServerDataSource;
import com.featureprobe.api.server.DBServerDataSource;
import com.featureprobe.api.server.ServerDataSource;
import com.featureprobe.api.service.BaseServerService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class ServerDataSourceConfig {

    private static final String SERVER_DATA_SOURCE_DB = "DB";

    private static final String SERVER_DATA_SOURCE_CACHE = "MEMORY";

    AppConfig appConfig;

    ChangeLogRepository changeLogRepository;

    BaseServerService baseServerService;



    @Bean
    public ServerDataSource serverDataSource() {
        String serverDataSource = appConfig.getServerDataSource();
        if (StringUtils.isNotBlank(serverDataSource) && StringUtils.equalsIgnoreCase(serverDataSource,
                SERVER_DATA_SOURCE_DB)) {
            return new DBServerDataSource(baseServerService);
        } else if (StringUtils.isNotBlank(serverDataSource) && StringUtils.equalsIgnoreCase(serverDataSource,
                SERVER_DATA_SOURCE_CACHE)) {
            MemoryCache<String, byte[]> cache = MemoryCache.createArrayByteCache(600);
            return new CacheServerDataSource(cache, changeLogRepository, baseServerService);
        } else {
            return new DBServerDataSource(baseServerService);
        }
    }

}
