package com.featureprobe.api.dto;

import com.featureprobe.api.base.enums.ResponseCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BaseResponse {

    private String code;

    private String message;

    public BaseResponse(ResponseCodeEnum responseCode) {
        this.code = responseCode.code();
        this.message = responseCode.message();
    }

}