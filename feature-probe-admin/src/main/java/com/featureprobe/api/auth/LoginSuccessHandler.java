package com.featureprobe.api.auth;

import com.featureprobe.api.dto.CertificationUserResponse;
import com.featureprobe.api.base.util.JsonMapper;
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
        AuthenticatedMember principal = token.getPrincipal();
        String jwt = jwtHelper.createJwtForMember(principal);
        Long organizationId = CollectionUtils.isEmpty(principal.getOrganizations()) ? null :
                principal.getOrganizations().get(0).getId();
        response.getWriter().write(JsonMapper.toJSONString(new CertificationUserResponse(token.getAccount(),
                principal.getRole(), organizationId, jwt)));
    }

}
