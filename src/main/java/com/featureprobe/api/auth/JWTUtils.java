package com.featureprobe.api.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.featureprobe.api.entity.Member;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class JWTUtils {

    public static final String AUTH_HEADER_KEY = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    private static final String ISS_KEY = "iss";
    private static final String ISS_VALUE = "https://featureprobe.com";
    private static final String ACCOUNT_KEY = "account";
    private static final String USER_ID_KEY = "userId";
    private static final String ROLE_KEY = "role";

    public static String getToken(Member member) {
        LocalDateTime now = LocalDateTime.now();
        return JWT.create()
                .withExpiresAt(LocalDateTime.now().plusMinutes(30L).atZone(ZoneId.systemDefault()).toInstant())
                .withClaim(ISS_KEY, ISS_VALUE)
                .withClaim(ACCOUNT_KEY, member.getAccount())
                .withClaim(USER_ID_KEY, member.getId())
                .withClaim(ROLE_KEY, member.getRole().name())
                .sign(Algorithm.HMAC256(member.getPassword()));
    }

}
