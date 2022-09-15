package com.featureprobe.api.service;

import com.featureprobe.api.auth.TokenHelper;
import com.featureprobe.api.base.enums.ApprovalStatusEnum;
import com.featureprobe.api.base.enums.ApprovalTypeEnum;
import com.featureprobe.api.base.enums.ResourceType;
import com.featureprobe.api.base.enums.SketchStatusEnum;
import com.featureprobe.api.base.exception.ResourceNotFoundException;
import com.featureprobe.api.dto.ApprovalRecordQueryRequest;
import com.featureprobe.api.dto.ApprovalRecordResponse;
import com.featureprobe.api.entity.ApprovalRecord;
import com.featureprobe.api.entity.Environment;
import com.featureprobe.api.entity.Project;
import com.featureprobe.api.entity.TargetingSketch;
import com.featureprobe.api.entity.Toggle;
import com.featureprobe.api.mapper.ApprovalRecordMapper;
import com.featureprobe.api.mapper.JsonMapper;
import com.featureprobe.api.repository.ApprovalRecordRepository;
import com.featureprobe.api.repository.EnvironmentRepository;
import com.featureprobe.api.repository.ProjectRepository;
import com.featureprobe.api.repository.TargetingSketchRepository;
import com.featureprobe.api.repository.ToggleRepository;
import com.featureprobe.api.util.PageRequestUtil;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.Predicate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class ApprovalRecordService {

    private ProjectRepository projectRepository;
    private EnvironmentRepository environmentRepository;
    private ToggleRepository toggleRepository;
    private ApprovalRecordRepository approvalRecordRepository;
    private TargetingSketchRepository targetingSketchRepository;

    @PersistenceContext
    public EntityManager entityManager;

    public Page<ApprovalRecordResponse> list(ApprovalRecordQueryRequest queryRequest) {
        Specification<ApprovalRecord> spec = buildListSpec(queryRequest);
        Pageable pageable = PageRequestUtil.toPageable(queryRequest, Sort.Direction.DESC, "createdTime");
        Page<ApprovalRecord> approvalRecords = approvalRecordRepository.findAll(spec, pageable);
        Page<ApprovalRecordResponse> res = approvalRecords.map(approvalRecord -> translateResponse(approvalRecord));
        List<ApprovalRecordResponse> sortedRes = res.getContent().stream()
                .sorted(Comparator.comparing(ApprovalRecordResponse::isLocked).reversed()).collect(Collectors.toList());
        return new PageImpl<>(sortedRes, pageable, res.getTotalElements());
    }

    private Specification<ApprovalRecord> buildListSpec(ApprovalRecordQueryRequest queryRequest) {
        return (root, query, cb) -> {
            Predicate p2 = cb.equal(root.get("submitBy"), TokenHelper.getAccount());
            Predicate p3 = cb.like(root.get("reviewers"), "%\"" + TokenHelper.getAccount() + "\"%");
            Predicate statusPredicate;
            if (isTargetingSketchStatus(queryRequest.getStatus())) {
                List<TargetingSketch> targetingSketches = targetingSketchRepository
                        .findAllByStatus(SketchStatusEnum.forValue(queryRequest.getStatus()));
                Set<Long> approvalIds = targetingSketches.stream()
                        .map(TargetingSketch::getApprovalId).collect(Collectors.toSet());
                Predicate p4 = root.get("id").in(approvalIds);
                Predicate p5 = cb.equal(root.get("status"), ApprovalStatusEnum.PASS);
                if(StringUtils.equals(SketchStatusEnum.CANCEL.name(), queryRequest.getStatus())) {
                    if (queryRequest.getType() == ApprovalTypeEnum.APPLY) {
                        statusPredicate = cb.and(p2, p4, p5);
                    } else {
                        statusPredicate = cb.and(p3, p4, p5);
                    }
                } else {
                    if (queryRequest.getType() == ApprovalTypeEnum.APPLY) {
                        statusPredicate = cb.and(p2, p4);
                    } else {
                        statusPredicate = cb.and(p3, p4);
                    }
                }
            } else {
                Predicate p1 = cb.equal(root.get("status"), ApprovalStatusEnum.forValue(queryRequest.getStatus()));
                if (queryRequest.getType() == ApprovalTypeEnum.APPLY) {
                    statusPredicate = cb.and(p1, p2);
                } else {
                    statusPredicate = cb.and(p1, p3);
                }
            }
            if (StringUtils.isNotBlank(queryRequest.getKeyword())) {
                List<Toggle> toggles = toggleRepository.findByNameLike(queryRequest.getKeyword());
                Set<String> toggleKeys = toggles.stream().map(Toggle::getKey).collect(Collectors.toSet());
                Predicate p4 = root.get("toggleKey").in(toggleKeys);
                Predicate p5 = cb.like(root.get("title"), "%" + queryRequest.getKeyword() + "%");
                query.where(statusPredicate, cb.or(p4, p5));
            } else {
                query.where(statusPredicate);
            }
            return query.getRestriction();
        };
    }

    private boolean isTargetingSketchStatus(String status) {
        return StringUtils.equals(status, SketchStatusEnum.RELEASE.name()) ||
                StringUtils.equals(status, SketchStatusEnum.CANCEL.name());
    }


    private ApprovalRecordResponse translateResponse(ApprovalRecord approvalRecord) {
        ApprovalRecordResponse approvalRecordResponse = ApprovalRecordMapper.INSTANCE.entityToResponse(approvalRecord);
        approvalRecordResponse.setProjectName(selectApprovalRecordProject(approvalRecord).getName());
        approvalRecordResponse.setEnvironmentName(selectApprovalRecordEnvironment(approvalRecord).getName());
        approvalRecordResponse.setToggleName(selectApprovalRecordToggle(approvalRecord).getName());
        approvalRecordResponse.setReviewers(JsonMapper.toListObject(approvalRecord.getReviewers(), String.class));
        approvalRecordResponse.setComment(approvalRecord.getComment());
        approvalRecordResponse.setApprovalTime(approvalRecord.getModifiedTime());
        Optional<TargetingSketch> targetingSketch = targetingSketchRepository.findByApprovalId(approvalRecord.getId());
        approvalRecordResponse.setSketchTime(targetingSketch.get().getModifiedTime());
        if(ApprovalStatusEnum.REJECT == approvalRecord.getStatus()) {
            approvalRecordResponse.setCancelTime(targetingSketch.get().getModifiedTime());
        }
        if (locked(targetingSketch.get())) {
            approvalRecordResponse.setLocked(true);
            approvalRecordResponse.setLockedTime(targetingSketch.get().getCreatedTime());
        }
        return approvalRecordResponse;
    }

    private boolean locked(TargetingSketch targetingSketch) {
        return targetingSketch.getStatus() == SketchStatusEnum.PENDING;
    }

    private Project selectApprovalRecordProject(ApprovalRecord approvalRecord) {
        return projectRepository.findByKey(approvalRecord.getProjectKey()).orElseThrow(() ->
                new ResourceNotFoundException(ResourceType.PROJECT, approvalRecord.getProjectKey()));
    }

    private Environment selectApprovalRecordEnvironment(ApprovalRecord approvalRecord) {
        return environmentRepository.findByProjectKeyAndKey(approvalRecord.getProjectKey(),
                approvalRecord.getEnvironmentKey()).orElseThrow(() ->
                new ResourceNotFoundException(ResourceType.ENVIRONMENT,
                        approvalRecord.getProjectKey() + "-" + approvalRecord.getEnvironmentKey()));
    }

    private Toggle selectApprovalRecordToggle(ApprovalRecord approvalRecord) {
        return toggleRepository.findByProjectKeyAndKey(approvalRecord.getProjectKey(),
                approvalRecord.getToggleKey()).orElseThrow(() ->
                new ResourceNotFoundException(ResourceType.TOGGLE,
                        approvalRecord.getProjectKey() + "-" + approvalRecord.getToggleKey()));
    }

}
