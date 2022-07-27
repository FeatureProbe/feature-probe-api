package com.featureprobe.api.auth;

import com.featureprobe.api.base.config.ConfigProperties;
import com.featureprobe.api.dto.BaseResponse;
import com.featureprobe.api.mapper.JsonMapper;
import com.featureprobe.api.repository.MemberRepository;
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
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private ConfigProperties configProperties;

    private LoginFailureHandler loginFailureHandler;

    private LoginSuccessHandler loginSuccessHandler;

    private CustomLogoutSuccessHandler customLogoutSuccessHandler;

    private MemberRepository memberRepository;

    UserPasswordAuthenticationProcessingFilter userPasswordAuthenticationProcessingFilter(
            AuthenticationManager authenticationManager) {
        UserPasswordAuthenticationProcessingFilter userPasswordAuthenticationProcessingFilter =
                new UserPasswordAuthenticationProcessingFilter();
        userPasswordAuthenticationProcessingFilter.setAuthenticationManager(authenticationManager);
        userPasswordAuthenticationProcessingFilter.setAuthenticationSuccessHandler(loginSuccessHandler);
        userPasswordAuthenticationProcessingFilter.setAuthenticationFailureHandler(loginFailureHandler);
        return userPasswordAuthenticationProcessingFilter;
    }

    JWTAuthenticationFilter jwtAuthenticationFilter(AuthenticationManager authenticationManager,
                                                    MemberRepository memberRepository) {
        JWTAuthenticationFilter jwtAuthenticationFilter =
                new JWTAuthenticationFilter(authenticationManager, memberRepository, configProperties.getSecret());
        return jwtAuthenticationFilter;
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
        http.headers().frameOptions().disable();
        http.csrf().disable();
        http
            .formLogin()
                .loginProcessingUrl("/login")
                .and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessHandler(customLogoutSuccessHandler)
                .and()
            .authorizeRequests()
                .antMatchers( "/login", "/logout", "/v3/api-docs.yaml", "/server/**", "/actuator/**")
                .permitAll()
                .anyRequest().authenticated()
                .and()
            .exceptionHandling()
                .accessDeniedHandler(((httpServletRequest, httpServletResponse, e) ->
                        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value())))
                .authenticationEntryPoint(authenticationEntryPoint());
        http.addFilter(jwtAuthenticationFilter(authenticationManager(), memberRepository))
                .addFilterBefore(userPasswordAuthenticationProcessingFilter(authenticationManager()), 
                        UsernamePasswordAuthenticationFilter.class);
    }

}
