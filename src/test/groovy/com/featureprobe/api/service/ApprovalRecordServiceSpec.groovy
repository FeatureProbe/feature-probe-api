package com.featureprobe.api.service

import com.featureprobe.api.base.enums.ApprovalStatusEnum
import com.featureprobe.api.base.enums.ApprovalTypeEnum
import com.featureprobe.api.dto.ApprovalRecordQueryRequest
import com.featureprobe.api.entity.ApprovalRecord
import com.featureprobe.api.entity.Environment
import com.featureprobe.api.entity.Project
import com.featureprobe.api.entity.TargetingSketch
import com.featureprobe.api.entity.Toggle
import com.featureprobe.api.repository.ApprovalRecordRepository
import com.featureprobe.api.repository.EnvironmentRepository
import com.featureprobe.api.repository.ProjectRepository
import com.featureprobe.api.repository.TargetingSketchRepository
import com.featureprobe.api.repository.ToggleRepository
import org.hibernate.internal.SessionImpl
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import spock.lang.Specification

import javax.persistence.EntityManager

class ApprovalRecordServiceSpec extends Specification {

    ApprovalRecordService approvalRecordService
    ProjectRepository projectRepository
    EnvironmentRepository environmentRepository
    ToggleRepository toggleRepository
    ApprovalRecordRepository approvalRecordRepository
    TargetingSketchRepository targetingSketchRepository
    ApprovalRecord approvalRecord
    EntityManager entityManager

    def setup() {
        projectRepository = Mock(ProjectRepository)
        environmentRepository = Mock(EnvironmentRepository)
        toggleRepository = Mock(ToggleRepository)
        approvalRecordRepository = Mock(ApprovalRecordRepository)
        targetingSketchRepository = Mock(TargetingSketchRepository)
        entityManager = Mock(SessionImpl)
        approvalRecordService = new ApprovalRecordService(projectRepository, environmentRepository, toggleRepository,
                approvalRecordRepository, targetingSketchRepository, entityManager)
        approvalRecord = new ApprovalRecord(id: 1, organizationId: -1, projectKey: "projectKey",
                environmentKey: "environmentKey", toggleKey: "toggleKey", submitBy: "Admin", approvedBy: "Test", reviewers: "[\"manager\"]", status: ApprovalStatusEnum.PENDING, title: "title")
    }

    def "Query approval record list"() {
        when:
        def list = approvalRecordService.list(new ApprovalRecordQueryRequest(keyword: "test", status: "PENDING", type: ApprovalTypeEnum.APPLY))
        then:
        1 * approvalRecordRepository.findAll(_, _) >> new PageImpl<>([approvalRecord], Pageable.ofSize(1), 1)
        1 * projectRepository.findByKey("projectKey") >> Optional.of(new Project(name: "projectName"))
        1 * environmentRepository.findByProjectKeyAndKey("projectKey", "environmentKey") >> Optional.of(new Environment(name: "environmentName"))
        1 * toggleRepository.findByProjectKeyAndKey("projectKey", "toggleKey") >> Optional.of(new Toggle(name: "toggleName"))
        1 * targetingSketchRepository.findByApprovalId(1) >> Optional.of(new TargetingSketch(modifiedTime: new Date()))
        1 == list.content.size()
    }

}

