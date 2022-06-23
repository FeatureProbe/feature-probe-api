package com.featureprobe.api.mapper;

import com.featureprobe.api.dto.TargetingVersionResponse;
import com.featureprobe.api.entity.TargetingVersion;
import com.featureprobe.api.model.TargetingContent;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TargetingVersionMapper {

    TargetingVersionMapper INSTANCE = Mappers.getMapper(TargetingVersionMapper.class);

    @Mapping(target = "content",
            expression = "java(toTargetingContent(targetingVersion.getContent()))")
    TargetingVersionResponse entityToResponse(TargetingVersion targetingVersion);

    default TargetingContent toTargetingContent(String content) {
        if (StringUtils.isNotBlank(content)) {
            return JsonMapper.toObject(content, TargetingContent.class);
        }
        return null;
    }

}
