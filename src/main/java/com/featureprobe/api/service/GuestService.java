package com.featureprobe.api.service;

import com.featureprobe.api.auth.tenant.TenantContext;
import com.featureprobe.api.base.config.JWTConfig;
import com.featureprobe.api.base.enums.RoleEnum;
import com.featureprobe.api.dto.ProjectCreateRequest;
import com.featureprobe.api.entity.Member;
import com.featureprobe.api.entity.Organization;
import com.featureprobe.api.repository.MemberRepository;
import com.featureprobe.api.service.aspect.ExcludeTenant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@ExcludeTenant
@AllArgsConstructor
@Service
public class GuestService {

    JWTConfig JWTConfig;

    private MemberRepository memberRepository;

    @PersistenceContext
    public EntityManager entityManager;

    private ProjectService projectService;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String GUEST_INIT_PROJECT_KEY = "My_Project";

    private static final String DEMO_INIT_DATA_FILE_PATH = "db/demo_init_data.sql";

    @Transactional(rollbackFor = Exception.class)
    public Member initGuest(String account, String source) {
        Member createdMember = new Member();
        createdMember.setAccount(account);
        createdMember.setPassword(passwordEncoder.encode(JWTConfig.getGuestDefaultPassword()));
        createdMember.setRole(RoleEnum.ADMIN);
        List<Organization> organizations = new ArrayList<>(1);
        organizations.add(new Organization(account));
        createdMember.setOrganizations(organizations);
        createdMember.setSource(source);
        Member savedMember = memberRepository.save(createdMember);
        SecurityContextHolder.setContext(new SecurityContextImpl(new JwtAuthenticationToken(Jwt.withTokenValue("_")
                .claim("userId", savedMember.getId()).claim("account", savedMember.getAccount())
                .claim("role", savedMember.getRole().name())
                .header("iss", "")
                .build())));
        Organization organization = savedMember.getOrganizations().get(0);
        initProjectEnvironment(String.valueOf(organization.getId()), GUEST_INIT_PROJECT_KEY);
        initToggles(organization.getId(), savedMember.getId());
        return savedMember;
    }

    private void initProjectEnvironment(String tenantId, String projectName) {
        TenantContext.setCurrentTenant(tenantId);
        projectService.create(new ProjectCreateRequest(projectName,
                projectName, ""));
    }

    private void initToggles(Long tenantId, Long userId) {
        try {
            ClassPathResource classPathResource = new ClassPathResource(DEMO_INIT_DATA_FILE_PATH);
            BufferedReader br = new BufferedReader(new InputStreamReader(classPathResource.getInputStream()));
            String sql;
            while (StringUtils.isNotBlank((sql = br.readLine()))) {
                sql = sql.replace("${organization_id}", String.valueOf(tenantId))
                        .replace("${project_key}", GUEST_INIT_PROJECT_KEY)
                        .replace("${user_id}", String.valueOf(userId));
                executeSQL(sql);
            }
        } catch (IOException e) {
            log.error("Demo init toggles error.", e);
        }
    }

    private void executeSQL(String sql) {
        Query query = entityManager.createNativeQuery(sql);
        query.executeUpdate();
    }
}
