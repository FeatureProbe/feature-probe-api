package com.featureprobe.api.controller;


import com.featureprobe.api.auth.TokenHelper;
import com.featureprobe.api.base.doc.DefaultApiResponses;
import com.featureprobe.api.dto.MemberCreateRequest;
import com.featureprobe.api.dto.MemberDeleteRequest;
import com.featureprobe.api.dto.MemberModifyPasswordRequest;
import com.featureprobe.api.dto.MemberResponse;
import com.featureprobe.api.dto.MemberSearchRequest;
import com.featureprobe.api.dto.MemberUpdateRequest;
import com.featureprobe.api.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/members")
@DefaultApiResponses
@Tag(name = "Members", description = "Using the members API, you can create, destroy, and manage members")
public class MemberController {

    private MemberService memberService;

    @GetMapping("/current")
    @Operation(summary = "Current login member", description = "")
    public MemberResponse currentLoginMember() {
        return new MemberResponse(TokenHelper.getAccount(), TokenHelper.getRole());
    }

    @PostMapping
    @Operation(summary = "Create member", description = "Create a new member")
    public List<MemberResponse> create(@Validated @RequestBody MemberCreateRequest createRequest) {
        return memberService.create(createRequest);
    }

    @GetMapping
    @Operation(summary = "List Member", description = "Get a list of all member")
    public Page<MemberResponse> list(MemberSearchRequest searchRequest) {
        return memberService.query(searchRequest);
    }

    @PatchMapping
    @Operation(summary = "Update member", description = "Update a member")
    public MemberResponse update(@Validated @RequestBody MemberUpdateRequest updateRequest) {
        return memberService.update(updateRequest);
    }

    @PatchMapping("/modifyPassword")
    @Operation(summary = "Modify member password", description = "Modify a member password")
    public MemberResponse modifyPassword(@Validated @RequestBody MemberModifyPasswordRequest modifyPasswordRequest) {
        return memberService.modifyPassword(modifyPasswordRequest);
    }

    @DeleteMapping
    @Operation(summary = "Delete member", description = "Logical delete a member")
    public MemberResponse delete(@Validated @RequestBody MemberDeleteRequest deleteRequest) {
        return memberService.delete(deleteRequest.getAccount());
    }

    @GetMapping("/query")
    @Operation(summary = "Get member", description = "Get a single member by account.")
    public MemberResponse query(String account) {
        return memberService.queryByAccount(account);
    }
}
