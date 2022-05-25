package com.featureprobe.api.base.exception;


import com.featureprobe.api.base.constants.ResponseCode;
import com.featureprobe.api.dto.ErrorResponse;
import com.featureprobe.api.util.I18nUtil;
import com.featureprobe.api.mapper.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@AllArgsConstructor
@ControllerAdvice
@Slf4j
public class WebExceptionAspect {

    I18nUtil i18nUtil;

    @ExceptionHandler(value = ResourceNotFoundException.class)
    public void resourceNotFoundHandler(HttpServletResponse response, ResourceNotFoundException e)
            throws IOException {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String resourceNameMessage = i18nUtil.getResourceNameMessage(e.resourceType);
        String resourceNotFoundMessage = i18nUtil.get(ResponseCode.NOT_FOUND.messageKey(),
                new Object[]{resourceNameMessage, e.resourceKey});

        response.getWriter().write(toErrorResponse(ResponseCode.NOT_FOUND, resourceNotFoundMessage));
    }

    @ExceptionHandler(value = ResourceConflictException.class)
    public void resourceConflictHandler(HttpServletResponse response, ResourceConflictException e)
            throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(toErrorResponse(ResponseCode.CONFLICT));
    }

    @ExceptionHandler(value = PasswordErrorException.class)
    public void passwordErrorHandler(HttpServletResponse response, PasswordErrorException e)
            throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(toErrorResponse(ResponseCode.INVALID_REQUEST));
    }

    private String toErrorResponse(ResponseCode resourceCode) {
        return toErrorResponse(resourceCode, i18nUtil.get(resourceCode.messageKey()));
    }

    private String toErrorResponse(ResponseCode responseCode, String message) {
        return JsonMapper.toJSONString(new ErrorResponse(responseCode.code(), message));
    }

}
