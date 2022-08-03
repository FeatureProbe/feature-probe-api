package com.featureprobe.api.auth;

import com.featureprobe.api.dto.BaseResponse;
import com.featureprobe.api.mapper.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String AUTHORITIES_CLAIM_NAME = "roles";

    private LoginFailureHandler loginFailureHandler;

    private LoginSuccessHandler loginSuccessHandler;

    UserPasswordAuthenticationProcessingFilter userPasswordAuthenticationProcessingFilter(
            AuthenticationManager authenticationManager) {
        UserPasswordAuthenticationProcessingFilter userPasswordAuthenticationProcessingFilter =
                new UserPasswordAuthenticationProcessingFilter();
        userPasswordAuthenticationProcessingFilter.setAuthenticationManager(authenticationManager);
        userPasswordAuthenticationProcessingFilter.setAuthenticationSuccessHandler(loginSuccessHandler);
        userPasswordAuthenticationProcessingFilter.setAuthenticationFailureHandler(loginFailureHandler);
        return userPasswordAuthenticationProcessingFilter;
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        AuthenticationEntryPoint authenticationEntryPoint = (httpServletRequest, httpServletResponse, e) ->
        {
            BaseResponse res = new BaseResponse(HttpStatus.UNAUTHORIZED.name().toLowerCase(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase());
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpServletResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
            httpServletResponse.getWriter().write(JsonMapper.toJSONString(res));
        };
        return authenticationEntryPoint;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.headers().frameOptions().disable();
        http.csrf().disable();
        http
            .formLogin()
                .loginProcessingUrl("/login")
                .and()
            .authorizeRequests()
                .antMatchers( "/login", "/v3/api-docs.yaml", "/server/**", "/actuator/**")
                .permitAll()
                .anyRequest().authenticated()
                .and()
            .exceptionHandling()
                .accessDeniedHandler(((httpServletRequest, httpServletResponse, e) ->
                        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value())))
                .authenticationEntryPoint(authenticationEntryPoint());
        http.addFilterBefore(userPasswordAuthenticationProcessingFilter(authenticationManager()),
                        UsernamePasswordAuthenticationFilter.class);
        http.oauth2ResourceServer().jwt().jwtAuthenticationConverter(authenticationConverter());
    }

    protected JwtAuthenticationConverter authenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("");
        authoritiesConverter.setAuthoritiesClaimName(AUTHORITIES_CLAIM_NAME);
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

}
