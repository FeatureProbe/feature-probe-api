package com.featureprobe.api.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.featureprobe.api.base.exception.ForbiddenException;
import com.featureprobe.api.entity.Member;
import com.featureprobe.api.mapper.JsonMapper;
import com.featureprobe.api.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.Base64Utils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

@Slf4j
public class JWTAuthenticationFilter extends BasicAuthenticationFilter {

    MemberRepository memberRepository;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager, MemberRepository memberRepository) {
        super(authenticationManager);
        this.memberRepository = memberRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        final String authHeader = request.getHeader(JWTUtils.AUTH_HEADER_KEY);
        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith(JWTUtils.TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }
        try {
            final String token = authHeader.substring(7);
            String payload = JWT.decode(token).getPayload();
            byte[] decode = Base64Utils.decode(payload.getBytes(StandardCharsets.UTF_8));
            String user = new String(decode);
            Long userId = Long.parseLong((String)JsonMapper.toObject(user, Map.class).get("userId"));
            Member member = memberRepository.findById(userId).orElseThrow(() -> new ForbiddenException());
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256("2111212")).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            Date expiresAt = decodedJWT.getExpiresAt();
            if (new Date().after(expiresAt)) {
                // Token 失效
                log.error("Token has expired");
                throw new ForbiddenException();
            }
            UserPasswordAuthenticationToken userPasswordAuthenticationToken =
                    new UserPasswordAuthenticationToken(member,
                            Arrays.asList(new SimpleGrantedAuthority(member.getRole().name())));
            SecurityContextHolder.getContext().setAuthentication(userPasswordAuthenticationToken);
        }catch (Exception e) {
            log.error("Token is forbidden", e);
            throw new ForbiddenException();
        }
        chain.doFilter(request, response);
    }
}
