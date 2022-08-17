package com.featureprobe.api.service;

import com.featureprobe.api.auth.tenant.TenantContext;
import com.featureprobe.api.base.config.AppConfig;
import com.featureprobe.api.base.enums.RoleEnum;
import com.featureprobe.api.dto.ProjectCreateRequest;
import com.featureprobe.api.entity.Member;
import com.featureprobe.api.entity.Organization;
import com.featureprobe.api.repository.MemberRepository;
import com.featureprobe.api.service.aspect.ExcludeTenant;
import lombok.AllArgsConstructor;
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
import java.util.ArrayList;
import java.util.List;

@ExcludeTenant
@AllArgsConstructor
@Service
public class GuestService {

    AppConfig appConfig;

    private MemberRepository memberRepository;

    @PersistenceContext
    public EntityManager entityManager;

    private ProjectService projectService;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String GUEST_INIT_PROJECT_KEY = "My_Project";

    private static final String[] toggleSql= {"INSERT INTO `toggle` (`organization_id`, `name`, `key`, " +
            "`description`, `return_type`, `disabled_serve`, `variations`, `project_key`, `archived`, " +
            "`client_availability`, `deleted`, `modified_by`, `created_by`, `created_time`, `modified_time`) " +
            "VALUES (${organization_id}, 'feature toggle01', 'feature_toggle01', '', 'boolean', 0, " +
            "'[{\\\"value\\\":\\\"false\\\", \\\"name\\\":\\\"不可见\\\"," +
            "\\\"description\\\":\\\"运营活动对用户不可见\\\"},{\\\"value\\\":" +
            "\\\"true\\\",\\\"name\\\":\\\"可见\\\",\\\"description\\\":\\\"运营活动对用户可见\\\"}]', " +
            "'${project_key}', 0, 1, 0, ${user_id}, ${user_id}, now(), now())",

        "INSERT INTO `targeting` (`organization_id`, `toggle_key`, `environment_key`, `project_key`, `version`, " +
                "`disabled`, `content`, `deleted`, `modified_by`, `created_by`, `created_time`, " +
                "`modified_time`) VALUES (${organization_id}, 'feature_toggle01', 'online', '${project_key}', " +
                "1, 1, '{\\\"rules\\\":[],\\\"disabledServe\\\":{\\\"select\\\":0},\\\"defaultServe\\\":" +
                "{\\\"select\\\":1},\\\"variations\\\":[{\\\"value\\\":\\\"false\\\",\\\"name\\\":" +
                "\\\"不可见\\\",\\\"description\\\":\\\"运营活动对用户不可见\\\"},{\\\"value\\\":\\\"true\\\"," +
                "\\\"name\\\":\\\"可见\\\",\\\"description\\\":\\\"运营活动对用户可见\\\"}]}', 0, " +
                "${user_id}, ${user_id}, now(), now())",

        "INSERT INTO `variation_history` (`organization_id`, `project_key`, `toggle_key`, `environment_key`, " +
                "`toggle_version`, `value`, `value_index`, `name`) VALUES (${organization_id}, '${project_key}', " +
                "'feature_toggle01', 'online', 1, 'true', 1, '可见')",

        "INSERT INTO `variation_history` (`organization_id`, `project_key`, `toggle_key`, `environment_key`, " +
                "`toggle_version`, `value`, `value_index`, `name`) VALUES (${organization_id}, '${project_key}', " +
                "'feature_toggle01', 'online', 1, 'false', 0, '不可见')",

        "INSERT INTO `targeting_version` (`organization_id`, `project_key`, `environment_key`, `toggle_key`, " +
                "`comment`, `content`, `disabled`, `version`, `deleted`, `modified_time`, `created_by`, " +
                "`created_time`, `modified_by`) VALUES (${organization_id}, '${project_key}', 'online', " +
                "'feature_toggle01', '', '{\\\"rules\\\":[],\\\"disabledServe\\\":{\\\"select\\\":0}," +
                "\\\"defaultServe\\\":{\\\"select\\\":1},\\\"variations\\\":[{\\\"value\\\":\\\"false\\\"," +
                "\\\"name\\\":\\\"不可见\\\",\\\"description\\\":\\\"运营活动对用户不可见\\\"}," +
                "{\\\"value\\\":\\\"true\\\",\\\"name\\\":\\\"可见\\\",\\\"description\\\":\\\"" +
                "运营活动对用户可见\\\"}]}', 1, 1, 0, now(), ${user_id}, now(), ${user_id})",


        "INSERT INTO `toggle` (`organization_id`, `name`, `key`, `description`, `return_type`, `disabled_serve`, " +
                "`variations`, `project_key`, `archived`, `client_availability`, `deleted`, `modified_by`, " +
                "`created_by`, `created_time`, `modified_time`) VALUES (${organization_id}, 'feature toggle02', " +
                "'feature_toggle02', '', 'boolean', 0, '[{\\\"value\\\":\\\"false\\\",\\\"name\\\":\\\"" +
                "不可见\\\",\\\"description\\\":\\\"\\\"},{\\\"value\\\":\\\"true\\\",\\\"name\\\":" +
                "\\\"可见\\\",\\\"description\\\":\\\"\\\"}]', '${project_key}', " +
                "0, 1, 0, ${user_id}, ${user_id}, now(), now())",

        "INSERT INTO `targeting` (`organization_id`, `toggle_key`, `environment_key`, `project_key`, `version`, " +
                "`disabled`, `content`, `deleted`, `modified_by`, `created_by`, `created_time`, `modified_time`) " +
                "VALUES (${organization_id}, 'feature_toggle02', 'online', '${project_key}', 1, 0, " +
                "'{\\\"rules\\\":[{\\\"conditions\\\":[{\\\"type\\\":\\\"string\\\"," +
                "\\\"subject\\\":\\\"userId\\\",\\\"predicate\\\":\\\"is one of\\\"," +
                "\\\"objects\\\":[\\\"00001\\\",\\\"00002\\\"],\\\"segmentType\\\":false," +
                "\\\"numberType\\\":false,\\\"datetimeType\\\":false,\\\"semVerType\\\":false}]," +
                "\\\"name\\\":\\\"测试组可见\\\",\\\"serve\\\":{\\\"select\\\":1},\\\"notEmptyConditions\\\":true}]," +
                "\\\"disabledServe\\\":{\\\"select\\\":0},\\\"defaultServe\\\":{\\\"select\\\":0}," +
                "\\\"variations\\\":[{\\\"value\\\":\\\"false\\\",\\\"name\\\":\\\"不可见\\\"," +
                "\\\"description\\\":\\\"\\\"},{\\\"value\\\":\\\"true\\\",\\\"name\\\":\\\"可见\\\"," +
                "\\\"description\\\":\\\"\\\"}]}', 0, ${user_id}, ${user_id}, now(), now())",

        "INSERT INTO `variation_history` (`organization_id`, `project_key`, `toggle_key`, `environment_key`, " +
                "`toggle_version`, `value`, `value_index`, `name`) VALUES (${organization_id}, '${project_key}', " +
                "'feature_toggle02', 'online', 1, 'true', 1, '可见')",

        "INSERT INTO `variation_history` (`organization_id`, `project_key`, `toggle_key`, `environment_key`, " +
                "`toggle_version`, `value`, `value_index`, `name`) VALUES (${organization_id}, '${project_key}', " +
                "'feature_toggle02', 'online', 1, 'false', 0, '不可见')",

        "INSERT INTO `targeting_version` (`organization_id`, `project_key`, `environment_key`, `toggle_key`, " +
                "`comment`, `content`, `disabled`, `version`, `deleted`, `modified_time`, `created_by`, " +
                "`created_time`, `modified_by`) VALUES (${organization_id}, '${project_key}', 'online', " +
                "'feature_toggle02', '', '{\\\"rules\\\":[{\\\"conditions\\\":[{\\\"type\\\":\\\"string\\\"," +
                "\\\"subject\\\":\\\"userId\\\",\\\"predicate\\\":\\\"is one of\\\"," +
                "\\\"objects\\\":[\\\"00001\\\",\\\"00002\\\"],\\\"segmentType\\\":false," +
                "\\\"numberType\\\":false,\\\"datetimeType\\\":false,\\\"semVerType\\\":false}]," +
                "\\\"name\\\":\\\"测试组可见\\\",\\\"serve\\\":{\\\"select\\\":1}," +
                "\\\"notEmptyConditions\\\":true}],\\\"disabledServe\\\":{\\\"select\\\":0}," +
                "\\\"defaultServe\\\":{\\\"select\\\":0},\\\"variations\\\":[{\\\"value\\\":\\\"false\\\"," +
                "\\\"name\\\":\\\"不可见\\\",\\\"description\\\":\\\"\\\"},{\\\"value\\\":\\\"true\\\"," +
                "\\\"name\\\":\\\"可见\\\",\\\"description\\\":\\\"\\\"}]}', 0, 1, 0,  now(), ${user_id}, " +
                "now(), ${user_id})",

        "INSERT INTO `toggle` (`organization_id`, `name`, `key`, `description`, `return_type`, `disabled_serve`, " +
                "`variations`, `project_key`, `archived`, `client_availability`, `deleted`, `modified_by`, " +
                "`created_by`, `created_time`, `modified_time`) VALUES (${organization_id}, 'feature toggle03', " +
                "'feature_toggle03', '', 'boolean', 0, '[{\\\"value\\\":\\\"false\\\",\\\"name\\\":\\\"不可见\\\"," +
                "\\\"description\\\":\\\"\\\"},{\\\"value\\\":\\\"true\\\",\\\"name\\\":\\\"可见\\\"," +
                "\\\"description\\\":\\\"\\\"}]', '${project_key}', 0, 1, 0, ${user_id}, ${user_id}, now(), now())",

        "INSERT INTO `targeting` (`organization_id`, `toggle_key`, `environment_key`, `project_key`, `version`, " +
                "`disabled`, `content`, `deleted`, `modified_by`, `created_by`, `created_time`, `modified_time`) " +
                "VALUES (${organization_id}, 'feature_toggle03', 'online', '${project_key}', 1, 0, " +
                "'{\\\"rules\\\":[], \\\"disabledServe\\\":{\\\"select\\\":0},\\\"defaultServe\\\":" +
                "{\\\"split\\\":[6000,4000]}, \\\"variations\\\":[{\\\"value\\\":\\\"false\\\",\\\"name\\\":" +
                "\\\"不可见\\\",\\\"description\\\":\\\"运营活动对用户不可见\\\"},{\\\"value\\\":\\\"true\\\"," +
                "\\\"name\\\":\\\"可见\\\",\\\"description\\\":\\\"运营活动对用户可见\\\"}]}', 0,  " +
                "${user_id}, ${user_id}, now(), now())",

        "INSERT INTO `variation_history` (`organization_id`, `project_key`, `toggle_key`, `environment_key`, " +
                "`toggle_version`, `value`, `value_index`, `name`) VALUES (${organization_id}, '${project_key}', " +
                "'feature_toggle03', 'online', 1, 'true', 1, '可见')",

        "INSERT INTO `variation_history` (`organization_id`, `project_key`, `toggle_key`, `environment_key`, " +
                "`toggle_version`, `value`, `value_index`, `name`) VALUES (${organization_id}, '${project_key}', " +
                "'feature_toggle03', 'online', 1, 'false', 0, '不可见')",

        "INSERT INTO `targeting_version` (`organization_id`, `project_key`, `environment_key`, `toggle_key`, " +
                "`comment`, `content`, `disabled`, `version`, `deleted`, `modified_time`, `created_by`, " +
                "`created_time`, `modified_by`) VALUES (${organization_id}, '${project_key}', 'online', " +
                "'feature_toggle03', '', '{\\\"rules\\\":[],\\\"disabledServe\\\":{\\\"select\\\":0}," +
                "\\\"defaultServe\\\":{\\\"split\\\":[8000,2000]},\\\"variations\\\":[{\\\"value\\\":\\\"false\\\"," +
                "\\\"name\\\":\\\"不可见\\\",\\\"description\\\":\\\"运营活动对用户不可见\\\"},{\\\"value\\\"" +
                ":\\\"true\\\",\\\"name\\\":\\\"可见\\\",\\\"description\\\":\\\"运营活动对用户可见\\\"}]}', 0, " +
                "1, 0, now(), ${user_id}, now(), ${user_id})",

        "INSERT INTO `toggle` (`organization_id`, `name`, `key`, `description`, `return_type`, `disabled_serve`, " +
                "`variations`, `project_key`, `archived`, `client_availability`, `deleted`, `modified_by`, " +
                "`created_by`, `created_time`, `modified_time`) VALUES (${organization_id}, 'feature toggle04', " +
                "'feature_toggle04', '', 'number', 1, '[{\\\"value\\\":\\\"10\\\",\\\"name\\\":\\\"$10\\\"," +
                "\\\"description\\\":\\\"商品价格为10美元\\\"},{\\\"value\\\":\\\"20\\\",\\\"name\\\":\\\"$20\\\"," +
                "\\\"description\\\":\\\"商品价格为10美元\\\"}]', '${project_key}', 0, 1, 0, ${user_id}, " +
                "${user_id}, now(), now())",

        "INSERT INTO `targeting` (`organization_id`, `toggle_key`, `environment_key`, `project_key`, `version`, " +
                "`disabled`, `content`, `deleted`, `modified_by`, `created_by`, `created_time`, `modified_time`) " +
                "VALUES (${organization_id}, 'feature_toggle04', 'online', '${project_key}', 1, 0, " +
                "'{\\\"rules\\\":[{\\\"conditions\\\":[{\\\"type\\\":\\\"string\\\"," +
                "\\\"subject\\\":\\\"userId\\\",\\\"predicate\\\":\\\"is one of\\\"," +
                "\\\"objects\\\":[\\\"00001\\\",\\\"00002\\\"],\\\"segmentType\\\":false," +
                "\\\"numberType\\\":false,\\\"datetimeType\\\":false,\\\"semVerType\\\":false}]," +
                "\\\"name\\\":\\\"00001, 00002 用户商品价格为$10\\\",\\\"serve\\\":{\\\"select\\\":0}," +
                "\\\"notEmptyConditions\\\":true},{\\\"conditions\\\":[{\\\"type\\\":\\\"string\\\"," +
                "\\\"subject\\\":\\\"userId\\\",\\\"predicate\\\":\\\"is one of\\\"," +
                "\\\"objects\\\":[\\\"00003\\\"],\\\"segmentType\\\":false,\\\"numberType\\\":false," +
                "\\\"datetimeType\\\":false,\\\"semVerType\\\":false}],\\\"name\\\":\\\"00003 用户商品价格为$20\\\"," +
                "\\\"serve\\\":{\\\"select\\\":1},\\\"notEmptyConditions\\\":true}]," +
                "\\\"disabledServe\\\":{\\\"select\\\":1},\\\"defaultServe\\\":{\\\"select\\\":1}," +
                "\\\"variations\\\":[{\\\"value\\\":\\\"10\\\",\\\"name\\\":\\\"$10\\\"," +
                "\\\"description\\\":\\\"商品价格为10美元\\\"},{\\\"value\\\":\\\"20\\\",\\\"name\\\":\\\"$20\\\"," +
                "\\\"description\\\":\\\"商品价格为20美元\\\"}]}', 0, ${user_id}, ${user_id}, now(), now())",

        "INSERT INTO `variation_history` (`organization_id`, `project_key`, `toggle_key`, `environment_key`, " +
                "`toggle_version`, `value`, `value_index`, `name`) VALUES (${organization_id}, '${project_key}', " +
                "'feature_toggle04', 'online', 1, '20', 1, '$20')",

        "INSERT INTO `variation_history` (`organization_id`, `project_key`, `toggle_key`, `environment_key`, " +
                "`toggle_version`, `value`, `value_index`, `name`) VALUES (${organization_id}, " +
                "'${project_key}', 'feature_toggle04', 'online', 1, '10', 0, '$10')",

        "INSERT INTO `targeting_version` (`organization_id`, `project_key`, `environment_key`, `toggle_key`, " +
                "`comment`, `content`, `disabled`, `version`, `deleted`, `modified_time`, `created_by`, " +
                "`created_time`, `modified_by`) VALUES (${organization_id}, '${project_key}', 'online', " +
                "'feature_toggle04', '', '{\\\"rules\\\":[{\\\"conditions\\\":[{\\\"type\\\":\\\"string\\\"," +
                "\\\"subject\\\":\\\"userId\\\",\\\"predicate\\\":\\\"is one of\\\",\\\"objects\\\":[\\\"00001\\\"," +
                "\\\"00002\\\"],\\\"segmentType\\\":false,\\\"numberType\\\":false,\\\"datetimeType\\\":false," +
                "\\\"semVerType\\\":false}],\\\"name\\\":\\\"00001, 00002 用户商品价格为$10\\\"," +
                "\\\"serve\\\":{\\\"select\\\":0},\\\"notEmptyConditions\\\":true}," +
                "{\\\"conditions\\\":[{\\\"type\\\":\\\"string\\\",\\\"subject\\\":\\\"userId\\\"," +
                "\\\"predicate\\\":\\\"is one of\\\",\\\"objects\\\":[\\\"00003\\\"],\\\"segmentType\\\":false," +
                "\\\"numberType\\\":false,\\\"datetimeType\\\":false,\\\"semVerType\\\":false}]," +
                "\\\"name\\\":\\\"00003 用户商品价格为$20\\\",\\\"serve\\\":{\\\"select\\\":1}," +
                "\\\"notEmptyConditions\\\":true}],\\\"disabledServe\\\":{\\\"select\\\":1}," +
                "\\\"defaultServe\\\":{\\\"select\\\":1},\\\"variations\\\":[{\\\"value\\\":\\\"10\\\"," +
                "\\\"name\\\":\\\"$10\\\",\\\"description\\\":\\\"商品价格为10美元\\\"},{\\\"value\\\":\\\"20\\\"," +
                "\\\"name\\\":\\\"$20\\\",\\\"description\\\":\\\"商品价格为20美元\\\"}]}', 0, 1, 0, now(), " +
                "${user_id}, now(), ${user_id})"
    };


    @Transactional(rollbackFor = Exception.class)
    public Member initGuest(String account) {
        Member createdMember = new Member();
        createdMember.setAccount(account);
        createdMember.setPassword(passwordEncoder.encode(appConfig.getGuestDefaultPassword()));
        createdMember.setRole(RoleEnum.ADMIN);
        List<Organization> organizations = new ArrayList<>(1);
        organizations.add(new Organization(account));
        createdMember.setOrganizations(organizations);
        Member savedMember = memberRepository.save(createdMember);
        SecurityContextHolder.setContext(new SecurityContextImpl(new JwtAuthenticationToken(Jwt.withTokenValue("_")
                .claim("userId", savedMember.getId()).claim("account", savedMember.getAccount())
                .claim("role", savedMember.getRole().name())
                .header("iss", "")
                .build())));
        Organization organization = savedMember.getOrganizations().get(0);
        initProjectEnvironment(String.valueOf(organization.getId()), GUEST_INIT_PROJECT_KEY);
        initToggles(organization.getId(), savedMember.getId(), account);
        return savedMember;
    }

    private void initProjectEnvironment(String tenantId, String projectName) {
        TenantContext.setCurrentTenant(tenantId);
        projectService.create(new ProjectCreateRequest(projectName,
                projectName, ""));
    }

    private void initToggles(Long tenantId, Long userId, String account) {
        for(String str : toggleSql) {
            String sql = str.replace("${organization_id}", String.valueOf(tenantId))
                    .replace("${project_key}", GUEST_INIT_PROJECT_KEY)
                    .replace("${user_id}", String.valueOf(userId));
            Query query = entityManager.createNativeQuery(sql);
            query.executeUpdate();
        }
    }

}
