package com.featureprobe.api.mapper;

import com.featureprobe.api.dto.MemberCreateRequest;
import com.featureprobe.api.dto.MemberResponse;
import com.featureprobe.api.dto.MemberUpdateRequest;
import com.featureprobe.api.entity.Member;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Mapper
public interface MemberMapper {

    MemberMapper INSTANCE = Mappers.getMapper(MemberMapper.class);

    @Mapping(target = "role", expression = "java(member.getRole().name())")
    @Mapping(target = "account", expression = "java(member.getAccount())")
    @Mapping(target = "createdBy", expression = "java(member.getCreatedBy())")
    @Mapping(target = "visitedTime", expression = "java(member.getVisitedTime())")
    MemberResponse entityToResponse(Member member);

    @Mapping(target = "password", expression = "java(toPasswordEncrypt(updateRequest.getPassword()))")
    @Mapping(target = "account", expression = "java(updateRequest.getAccount())")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void mapEntity(MemberUpdateRequest updateRequest, @MappingTarget Member member);

    default String toPasswordEncrypt(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }
}
