package com.featureprobe.api.dto;

import com.featureprobe.api.base.constants.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {

    private String code;

    private String message;

}