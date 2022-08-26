package com.featureprobe.api.service

import com.featureprobe.api.base.config.AppConfig
import com.featureprobe.api.base.enums.RoleEnum
import com.featureprobe.api.entity.Environment
import com.featureprobe.api.entity.Member
import com.featureprobe.api.entity.Organization
import com.featureprobe.api.entity.Project
import com.featureprobe.api.repository.EnvironmentRepository
import com.featureprobe.api.repository.MemberRepository
import com.featureprobe.api.repository.ProjectRepository
import com.featureprobe.sdk.server.FeatureProbe
import org.hibernate.internal.SessionImpl
import org.hibernate.query.internal.QueryImpl
import org.hibernate.query.spi.NativeQueryImplementor
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.Query

class GuestServiceSpec extends Specification{

    AppConfig appConfig

    MemberRepository memberRepository

    ProjectRepository projectRepository

    EnvironmentRepository environmentRepository

    EntityManager entityManager

    ProjectService projectService

    GuestService guestService

    def setup() {
        appConfig = new AppConfig()
        appConfig.setGuestDefaultPassword("Password")
        memberRepository = Mock(MemberRepository)
        entityManager = Mock(SessionImpl)
        projectRepository = Mock(ProjectRepository)
        environmentRepository = Mock(EnvironmentRepository)
        FeatureProbe featureProbe = new FeatureProbe("_")
        projectService = new ProjectService(projectRepository, environmentRepository, featureProbe, entityManager)
        guestService = new GuestService(appConfig, memberRepository, entityManager, projectService)
    }

    def "init guest data"() {
        given:
        Query query = Mock(NativeQueryImplementor)
        when:
        def guest = guestService.initGuest("Admin")
        then:
        1 * memberRepository.save(_) >> new Member(id: 1, account: "Admin", role: RoleEnum.ADMIN, organizations: [new Organization(id: 1)])
        1 * projectRepository.count() >> 2
        1 * projectRepository.save(_) >> new Project(name: "projectName", key: "projectKey",
                environments: [new Environment()])
        entityManager.createNativeQuery(_) >> query
        20 * query.executeUpdate()
    }

}

