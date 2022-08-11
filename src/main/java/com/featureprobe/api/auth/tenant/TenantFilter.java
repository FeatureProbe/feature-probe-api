package com.featureprobe.api.auth.tenant;

import com.featureprobe.api.auth.TokenHelper;
import com.featureprobe.api.base.config.AppConfig;
import com.featureprobe.api.dto.BaseResponse;
import com.featureprobe.api.dto.UserOrganize;
import com.featureprobe.api.mapper.JsonMapper;
import com.featureprobe.api.service.OrganizeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@AllArgsConstructor
public class TenantFilter implements Filter {

    private static final String TENANT_HEADER = "X-OrganizeID";

    private static final String ORGANIZE_ID_MISS_ERROR_MSG = "No OrganizeID supplied";

    private OrganizeService organizeService;

    private AppConfig appConfig;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String requestURI = request.getRequestURI();
        if (!appConfig.getExcludeTenantUri().contains(requestURI)) {
            String tenantHeader = request.getHeader(TENANT_HEADER);
            try {
                if (StringUtils.isNotBlank(tenantHeader)) {
                    UserOrganize userOrganize = organizeService.queryUserOrganize(Long.parseLong(tenantHeader),
                            TokenHelper.getUserId());
                    TenantContext.setCurrentTenant(tenantHeader);
                    TenantContext.setCurrentOrganize(userOrganize);
                } else {
                    tenantErrorResponse(response);
                    return;
                }
            } catch (Exception e) {
                log.error("Tenant Filter Error.", e);
                organizeErrorResponse(response);
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void tenantErrorResponse(HttpServletResponse response) throws IOException {
        BaseResponse res = new BaseResponse(HttpStatus.BAD_REQUEST.name().toLowerCase(), ORGANIZE_ID_MISS_ERROR_MSG);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(JsonMapper.toJSONString(res));
        response.getWriter().flush();
    }

    private void organizeErrorResponse(HttpServletResponse response) throws IOException {
        BaseResponse res = new BaseResponse(HttpStatus.FORBIDDEN.name().toLowerCase(),
                HttpStatus.FORBIDDEN.getReasonPhrase());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(JsonMapper.toJSONString(res));
        response.getWriter().flush();
    }
}
