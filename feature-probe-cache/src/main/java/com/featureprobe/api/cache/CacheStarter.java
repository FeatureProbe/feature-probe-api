package com.featureprobe.api.cache;

import com.featureprobe.api.cache.dto.SdkKeyResponse;
import com.featureprobe.api.cache.service.CacheService;
import com.featureprobe.api.dao.entity.ChangeLog;
import com.featureprobe.api.base.enums.ChangeLogType;
import com.featureprobe.api.dao.repository.ChangeLogRepository;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
@Slf4j
public class CacheStarter {

    private CacheService cacheService;

    private ChangeLogRepository changeLogRepository;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("FeatureProbe-Cache-Updater-%d")
                    .setPriority(Thread.MIN_PRIORITY)
                    .build());

    @PostConstruct
    public void init() throws ExecutionException {
        StopWatch watcher = new StopWatch();
        watcher.start();
        log.info("FeatureProbe API start initialization cache .");
        cacheService.queryMaxChangeLogId();
        SdkKeyResponse sdkKeyResponse = cacheService.queryAllSdkKeysFromCache();
        for (String serverSdkKey : sdkKeyResponse.getClientKeyToServerKey().values()) {
            cacheService.queryServerTogglesByServerSdkKeyFromCache(serverSdkKey);
        }
        watcher.stop();
        log.info("FeatureProbe API initialization cache finished . Time : -----"
                + watcher.getTime(TimeUnit.SECONDS) + " s");
        scheduler.scheduleAtFixedRate(this::handleChangeLog, 0L, 200, TimeUnit.MILLISECONDS);
    }

    private void handleChangeLog() {
        try {
            List<ChangeLog> changeLogs = changeLogRepository
                    .findAllByIdGreaterThanOrderByIdAsc(cacheService.queryMaxChangeLogId());
            if (CollectionUtils.isNotEmpty(changeLogs)) {
                cacheService.refreshMaxChangeLogId(changeLogs.get(changeLogs.size() - 1).getId());
                Map<String, ChangeLogType> logGroup = new HashMap<>();
                boolean isUpdateSdkKey = false;
                for (ChangeLog changeLog : changeLogs) {
                    logGroup.put(changeLog.getServerSdkKey(), changeLog.getType());
                    if (changeLog.getType() == ChangeLogType.ADD || changeLog.getType() == ChangeLogType.DELETE) {
                        isUpdateSdkKey = true;
                    }
                }
                if (isUpdateSdkKey) {
                    cacheService.refreshSdkKeyCache();
                }
                for (String serverSdkKey : logGroup.keySet()) {
                    refreshCache(serverSdkKey, logGroup.get(serverSdkKey));
                }
            }
        } catch (Exception e) {

        }
    }

    private void refreshCache(String serverSdkKey, ChangeLogType type) {
        switch (type) {
            case ADD:
            case CHANGE:
                cacheService.refreshToggleCache(serverSdkKey);
                break;
            case DELETE:
                cacheService.removeToggleCache(serverSdkKey);
                break;
            default:
                break;
        }
    }

}
