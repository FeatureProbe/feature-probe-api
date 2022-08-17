package com.featureprobe.api.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.featureprobe.api.dto.OrganizationMember;
import com.featureprobe.api.entity.Member;
import com.featureprobe.api.entity.Organization;
import com.featureprobe.api.mapper.JsonMapper;
import com.featureprobe.api.service.OrganizationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class JwtHelper {

    private static final String ACCOUNT_KEY = "account";
    private static final String USER_ID_KEY = "userId";
    private static final String ROLE_KEY = "role";
    private static final String ORGANIZATIONS = "organizations";
    public static final String AUTHORITIES_CLAIM_NAME = "roles";

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final OrganizationService organizationService;

    public String createJwtForMember(Member member) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Instant.now().toEpochMilli());
        calendar.add(Calendar.HOUR, 12);
        JWTCreator.Builder jwtBuilder = JWT.create().withSubject(member.getAccount());
        jwtBuilder.withClaim(ACCOUNT_KEY, member.getAccount());
        jwtBuilder.withClaim(USER_ID_KEY, member.getId());
        jwtBuilder.withClaim(ROLE_KEY, member.getRole().name());
        List<OrganizationMember> organizations = new ArrayList<>();
        for(Organization organization : member.getOrganizations()) {
            OrganizationMember organizationMember = organizationService.queryOrganizationMember(organization.getId(),
                    member.getId());
            organizations.add(new OrganizationMember(organization.getId(), organization.getName(),
                    organizationMember.getRole()));
        }
        Map<Long, OrganizationMember> organizationMemberMap = organizations.stream().collect(Collectors
                .toMap(OrganizationMember::getOrganizationId, Function.identity()));
        jwtBuilder.withClaim(ORGANIZATIONS, JsonMapper.toJSONString(organizationMemberMap));
        if (CollectionUtils.isNotEmpty(organizations)) {
            jwtBuilder.withClaim(AUTHORITIES_CLAIM_NAME, organizations.get(0).getRoleName());
        }
        return jwtBuilder
                .withNotBefore(new Date())
                .withExpiresAt(calendar.getTime())
                .sign(Algorithm.RSA256(publicKey, privateKey));
    }

}
