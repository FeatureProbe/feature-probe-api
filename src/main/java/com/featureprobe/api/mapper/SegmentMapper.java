package com.featureprobe.api.mapper;

import com.featureprobe.api.dto.SegmentResponse;
import com.featureprobe.api.entity.Segment;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SegmentMapper {

    SegmentMapper INSTANCE = Mappers.getMapper(SegmentMapper.class);

    SegmentResponse entityToResponse(Segment segment);
}
