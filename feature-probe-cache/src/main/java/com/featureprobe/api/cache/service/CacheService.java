package com.featureprobe.api.cache.service;

import com.featureprobe.api.cache.dto.SdkKeyResponse;
import com.featureprobe.api.cache.dto.ServerResponse;
import com.featureprobe.api.base.db.ExcludeTenant;
import com.google.common.cache.LoadingCache;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@AllArgsConstructor
@ExcludeTenant
public class CacheService {

    public static final String SDK_KEYS_CACHE_KEY = "SDK_KEYS_CACHE_KEY";

    public static final String MAX_CHANGE_LOG_ID_CACHE_KEY = "MAX_CHANGE_LOG_ID_CACHE_KEY";

    private LoadingCache<String, ServerResponse> toggleCache;

    private LoadingCache<String, SdkKeyResponse> sdkKeyCache;

    private LoadingCache<String, Long> maxChangeLogIdCache;

    public ServerResponse queryServerTogglesByServerSdkKeyFromCache(String serverSdkKey) throws ExecutionException {
        return toggleCache.get(serverSdkKey);
    }

    public SdkKeyResponse queryAllSdkKeysFromCache() throws ExecutionException {
        return sdkKeyCache.get(SDK_KEYS_CACHE_KEY);
    }

    public Long queryMaxChangeLogId() throws ExecutionException {
        return maxChangeLogIdCache.get(MAX_CHANGE_LOG_ID_CACHE_KEY);
    }

    public void refreshMaxChangeLogId(Long id) {
        maxChangeLogIdCache.put(MAX_CHANGE_LOG_ID_CACHE_KEY, id);
    }

    public void refreshSdkKeyCache() {
        sdkKeyCache.refresh(SDK_KEYS_CACHE_KEY);
    }

    public void refreshToggleCache(String serverSdkKey) {
        toggleCache.refresh(serverSdkKey);
    }

    public void removeToggleCache(String serverSdkKey) {
        toggleCache.invalidate(serverSdkKey);
    }

}
