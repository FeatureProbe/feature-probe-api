package com.featureprobe.api.cache.service;

import com.featureprobe.api.cache.model.ServerSegmentBuilder;
import com.featureprobe.api.cache.model.ServerToggleBuilder;
import com.featureprobe.api.cache.dto.SdkKeyResponse;
import com.featureprobe.api.cache.dto.ServerResponse;
import com.featureprobe.api.base.db.ExcludeTenant;
import com.featureprobe.api.dao.entity.Dictionary;
import com.featureprobe.api.dao.entity.Environment;
import com.featureprobe.api.dao.entity.Segment;
import com.featureprobe.api.dao.entity.Targeting;
import com.featureprobe.api.dao.entity.Toggle;
import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.dao.exception.ResourceNotFoundException;
import com.featureprobe.api.dao.repository.DictionaryRepository;
import com.featureprobe.api.dao.repository.EnvironmentRepository;
import com.featureprobe.api.dao.repository.SegmentRepository;
import com.featureprobe.api.dao.repository.TargetingRepository;
import com.featureprobe.api.dao.repository.ToggleRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@ExcludeTenant
public class ServerService {

    private EnvironmentRepository environmentRepository;

    private SegmentRepository segmentRepository;

    private ToggleRepository toggleRepository;

    private TargetingRepository targetingRepository;

    private DictionaryRepository dictionaryRepository;


    @PersistenceContext
    public EntityManager entityManager;

    public static final String SDK_KEY_DICTIONARY_KEY = "all_sdk_key_map";


    public SdkKeyResponse queryAllSdkKeys() {
        SdkKeyResponse sdkKeyResponse = new SdkKeyResponse();
        List<Environment> environments = environmentRepository.findAllByArchivedAndDeleted(false,
                false);
        environments.stream().forEach(environment -> sdkKeyResponse.put(environment.getClientSdkKey(),
                environment.getServerSdkKey()));
        Dictionary dictionary = dictionaryRepository.findByKey(SDK_KEY_DICTIONARY_KEY)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.DICTIONARY, SDK_KEY_DICTIONARY_KEY));
        sdkKeyResponse.setVersion(Long.parseLong(dictionary.getValue()));
        return sdkKeyResponse;
    }

    public ServerResponse queryServerTogglesByServerSdkKey(String serverSdkKey) {
        Environment environment = environmentRepository.findByServerSdkKeyOrClientSdkKey(serverSdkKey, serverSdkKey)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ENVIRONMENT, serverSdkKey));
        return new ServerResponse(queryTogglesBySdkKey(environment.getServerSdkKey()),
                querySegmentsBySdkKey(environment.getServerSdkKey()), environment.getVersion());
    }

    public String getSdkServerKey(String serverKeyOrClientKey) {
        return environmentRepository.findByServerSdkKeyOrClientSdkKey(serverKeyOrClientKey, serverKeyOrClientKey)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ENVIRONMENT, serverKeyOrClientKey))
                .getServerSdkKey();
    }


    private List<com.featureprobe.sdk.server.model.Toggle> queryTogglesBySdkKey(String serverSdkKey) {
        Environment environment = environmentRepository.findByServerSdkKey(serverSdkKey).get();
        if (Objects.isNull(environment)) {
            return Collections.emptyList();
        }
        List<Segment> segments = segmentRepository.findAllByProjectKeyAndOrganizationIdAndDeleted(
                environment.getProject().getKey(), environment.getOrganizationId(), false);
        List<Toggle> toggles = toggleRepository.findAllByProjectKeyAndOrganizationIdAndArchivedAndDeleted(
                environment.getProject().getKey(), environment.getOrganizationId(), false, false);
        Map<String, Targeting> targetingByKey = targetingRepository
                .findAllByProjectKeyAndEnvironmentKeyAndOrganizationIdAndDeleted(environment.getProject().getKey(),
                        environment.getKey(), environment.getOrganizationId(), false).stream()
                .collect(Collectors.toMap(Targeting::getToggleKey, Function.identity()));
        return toggles.stream().map(toggle -> {
            Targeting targeting = targetingByKey.get(toggle.getKey());
            try {
                return new ServerToggleBuilder().builder()
                        .key(toggle.getKey())
                        .disabled(targeting.getDisabled())
                        .version(targeting.getVersion())
                        .returnType(toggle.getReturnType())
                        .forClient(toggle.getClientAvailability())
                        .rules(targeting.getContent())
                        .segments(segments.stream().collect(Collectors.toMap(Segment::getKey, Function.identity())))
                        .build();
            } catch (Exception e) {
                log.warn("Build server toggle failed, server sdk key: {}, toggle key: {}, env key: {}",
                        serverSdkKey, targeting.getToggleKey(), targeting.getEnvironmentKey(), e);
                return null;
            }

        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<com.featureprobe.sdk.server.model.Segment> querySegmentsBySdkKey(String serverSdkKey) {
        Environment environment = environmentRepository.findByServerSdkKey(serverSdkKey).get();
        if (Objects.isNull(environment)) {
            return Collections.emptyList();
        }
        List<Segment> segments = segmentRepository.findAllByProjectKeyAndOrganizationIdAndDeleted(
                environment.getProject().getKey(), environment.getOrganizationId(), false);
        return segments.stream().map(segment -> {
            try {
                return new ServerSegmentBuilder().builder()
                        .uniqueId(segment.getUniqueKey())
                        .version(segment.getVersion())
                        .rules(segment.getRules())
                        .build();
            } catch (Exception e) {
                log.error("Build server segment failed, server sdk key: {}, segment key: {}",
                        serverSdkKey, segment.getKey(), e);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
