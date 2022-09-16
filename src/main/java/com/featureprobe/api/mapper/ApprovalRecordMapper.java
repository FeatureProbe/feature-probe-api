package com.featureprobe.api.mapper;

import com.featureprobe.api.dto.ApprovalRecordResponse;
import com.featureprobe.api.entity.ApprovalRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ApprovalRecordMapper extends BaseMapper {

    ApprovalRecordMapper INSTANCE = Mappers.getMapper(ApprovalRecordMapper.class);

    @Mapping(target = "reviewers", ignore = true)
    ApprovalRecordResponse entityToResponse(ApprovalRecord approvalRecord);
}
