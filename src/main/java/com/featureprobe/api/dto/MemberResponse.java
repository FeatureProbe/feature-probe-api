package com.featureprobe.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberResponse {

    private String account;

    private String role;

    private String createdBy;

    private Date visitedTime;

    public MemberResponse(String account, String role) {
        this.account = account;
        this.role = role;
    }

}
