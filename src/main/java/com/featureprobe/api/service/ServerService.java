package com.featureprobe.api.service;

import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.exception.ResourceNotFoundException;
import com.featureprobe.api.dto.SdkKeyResponse;
import com.featureprobe.api.dto.ServerResponse;
import com.featureprobe.api.entity.Environment;
import com.featureprobe.api.entity.Segment;
import com.featureprobe.api.entity.Targeting;
import com.featureprobe.api.entity.Toggle;
import com.featureprobe.api.model.ServerSegmentBuilder;
import com.featureprobe.api.model.ServerToggleBuilder;
import com.featureprobe.api.repository.EnvironmentRepository;
import com.featureprobe.api.repository.SegmentRepository;
import com.featureprobe.api.repository.TargetingRepository;
import com.featureprobe.api.repository.ToggleRepository;
import com.featureprobe.api.service.aspect.ExcludeTenant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
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

    @PersistenceContext
    public EntityManager entityManager;

    @Cacheable(value="all_sdk_key_map")
    public SdkKeyResponse queryAllSdkKeys() {
        SdkKeyResponse sdkKeyResponse = new SdkKeyResponse();
        List<Environment> environments = environmentRepository.findAll();
        environments.stream().forEach(environment -> sdkKeyResponse.put(environment.getClientSdkKey(),
                environment.getServerSdkKey()));
        return sdkKeyResponse;
    }

    public ServerResponse queryServerTogglesByServerSdkKey(String serverSdkKey) {
        serverSdkKey = getSdkServerKey(serverSdkKey);
        return new ServerResponse(queryTogglesBySdkKey(serverSdkKey), querySegmentsBySdkKey(serverSdkKey));
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
        List<Segment> segments = segmentRepository.findAllByProjectKeyAndOrganizationId(
                environment.getProject().getKey(), environment.getOrganizationId());
        List<Toggle> toggles = toggleRepository.findAllByProjectKeyAndOrganizationId(environment.getProject().getKey(),
                environment.getOrganizationId());
        Map<String, Targeting> targetingByKey = targetingRepository
                .findAllByProjectKeyAndEnvironmentKeyAndOrganizationId(environment.getProject().getKey(),
                        environment.getKey(), environment.getOrganizationId()).stream()
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
        List<Segment> segments = segmentRepository.findAllByProjectKeyAndOrganizationId(
                environment.getProject().getKey(), environment.getOrganizationId());
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
