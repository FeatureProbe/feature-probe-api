package com.featureprobe.api.auth;

import com.featureprobe.api.dto.CertificationUserResponse;
import com.featureprobe.api.entity.Member;
import com.featureprobe.api.mapper.JsonMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@AllArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private JwtHelper jwtHelper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        UserPasswordAuthenticationToken token =
                (UserPasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        Member member = token.getPrincipal();
        String jwt = jwtHelper.createJwtForMember(member);
        Long organizeId = CollectionUtils.isEmpty(member.getOrganizes()) ? null : member.getOrganizes().get(0).getId();
        response.getWriter().write(JsonMapper.toJSONString(new CertificationUserResponse(token.getAccount(),
                        member.getRole().name(), organizeId, jwt)));
    }

}
