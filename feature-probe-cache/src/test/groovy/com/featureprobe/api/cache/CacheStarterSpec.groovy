package com.featureprobe.api.cache

import com.featureprobe.api.base.enums.ChangeLogType
import com.featureprobe.api.cache.dto.SdkKeyResponse
import com.featureprobe.api.cache.dto.ServerResponse
import com.featureprobe.api.cache.service.CacheService
import com.featureprobe.api.dao.entity.ChangeLog
import com.featureprobe.api.dao.repository.ChangeLogRepository
import com.featureprobe.sdk.server.model.Segment
import com.featureprobe.sdk.server.model.Toggle
import com.google.common.cache.LoadingCache
import spock.lang.Specification


class CacheStarterSpec extends Specification {

    LoadingCache toggleCache;

    LoadingCache sdkKeyCache;

    LoadingCache maxChangeLogIdCache;

    CacheService cacheService

    ChangeLogRepository changeLogRepository

    CacheStarter cacheStarter

    def setup() {
        toggleCache = Mock(LoadingCache)
        sdkKeyCache = Mock(LoadingCache)
        maxChangeLogIdCache = Mock(LoadingCache)
        changeLogRepository = Mock(ChangeLogRepository)
        cacheService = new CacheService(toggleCache, sdkKeyCache, maxChangeLogIdCache)
        cacheStarter = new CacheStarter(cacheService, changeLogRepository)
    }

    def "init cache" () {
        when:
        cacheStarter.init()
        then:
        1 * maxChangeLogIdCache.get("MAX_CHANGE_LOG_ID_CACHE_KEY") >> 1L
        1 * sdkKeyCache.get("SDK_KEYS_CACHE_KEY") >> new SdkKeyResponse(version: 1, clientKeyToServerKey: ["client-123" : "server-123", "client-234": "server-234"])
        1 * toggleCache.get("server-123") >> new ServerResponse([new Toggle()], [new Segment()], 1)
        1 * toggleCache.get("server-234") >> new ServerResponse([new Toggle()], [new Segment()], 1)
    }

    def "handle change log" () {
        when:
        cacheStarter.handleChangeLog()
        then:
        1 * maxChangeLogIdCache.get("MAX_CHANGE_LOG_ID_CACHE_KEY") >> 1L
        1 * changeLogRepository.findAllByIdGreaterThanOrderByIdAsc(1L) >>
                [new ChangeLog(id: 2, type: ChangeLogType.ADD, serverSdkKey: "server-222"),
                 new ChangeLog(id: 3, type: ChangeLogType.CHANGE, serverSdkKey: "server-333"),
                 new ChangeLog(id: 4, type: ChangeLogType.DELETE, serverSdkKey: "server-444")]
        1 * maxChangeLogIdCache.put("MAX_CHANGE_LOG_ID_CACHE_KEY", 4)
        1 * sdkKeyCache.refresh("SDK_KEYS_CACHE_KEY")
        1 * toggleCache.refresh("server-222")
        1 * toggleCache.refresh("server-333")
        1 * toggleCache.invalidate("server-444")
    }
}

