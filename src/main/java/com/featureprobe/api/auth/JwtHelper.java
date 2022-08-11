package com.featureprobe.api.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.featureprobe.api.dto.UserOrganize;
import com.featureprobe.api.entity.Member;
import com.featureprobe.api.entity.Organize;
import com.featureprobe.api.mapper.JsonMapper;
import com.featureprobe.api.service.OrganizeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private static final String ORGANIZE = "organizes";

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final OrganizeService organizeService;

    public String createJwtForMember(Member member) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Instant.now().toEpochMilli());
        calendar.add(Calendar.HOUR, 12);
        JWTCreator.Builder jwtBuilder = JWT.create().withSubject(member.getAccount());
        jwtBuilder.withClaim(ACCOUNT_KEY, member.getAccount());
        jwtBuilder.withClaim(USER_ID_KEY, member.getId());
        jwtBuilder.withClaim(ROLE_KEY, member.getRole().name());
        List<UserOrganize> organizes = new ArrayList<>();
        for(Organize organize: member.getOrganizes()) {
            UserOrganize userOrganize = organizeService.queryUserOrganize(organize.getId(), member.getId());
            organizes.add(new UserOrganize(organize.getId(), organize.getName(), userOrganize.getRole()));
        }
        Map<Long, UserOrganize> userOrganizeMap = organizes.stream().collect(Collectors
                .toMap(UserOrganize::getOrganizeId, Function.identity()));
        jwtBuilder.withClaim(ORGANIZE, JsonMapper.toJSONString(userOrganizeMap));
        return jwtBuilder
                .withNotBefore(new Date())
                .withExpiresAt(calendar.getTime())
                .sign(Algorithm.RSA256(publicKey, privateKey));
    }

}
