package com.featureprobe.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class UserPasswordAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

    protected UserPasswordAuthenticationProcessingFilter() {
        super("/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = request.getInputStream();
        String body = IOUtils.toString(is, StandardCharsets.UTF_8);
        Map<String, String> authParam = mapper.readValue(body, Map.class);
        String account = authParam.get("account");
        String password = authParam.get("password");
        return getAuthenticationManager().authenticate(new UserPasswordAuthenticationToken(account, password));
    }
}
