package com.featureprobe.api.mapper;


import com.featureprobe.api.dto.ToggleCreateRequest;
import com.featureprobe.api.dto.ToggleItemResponse;
import com.featureprobe.api.dto.ToggleResponse;
import com.featureprobe.api.dto.ToggleUpdateRequest;
import com.featureprobe.api.entity.Tag;
import com.featureprobe.api.entity.Toggle;
import com.featureprobe.api.model.Variation;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface ToggleMapper {

    ToggleMapper INSTANCE = Mappers.getMapper(ToggleMapper.class);
    ToggleItemResponse entityToItemResponse(Toggle toggle);

    @Mapping(target = "variations",
            expression = "java(toVariation(toggle.getVariations()))")
    @Mapping(target = "tags",
            expression = "java(toTagNames(toggle.getTags()))")
    ToggleResponse entityToResponse(Toggle toggle);

    default List<String> toTagNames(List<Tag> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return Collections.emptyList();
        }
        return tags.stream().map(Tag::getName).collect(Collectors.toList());
    }

    default List<Variation> toVariation(String variation) {
        return JsonMapper.toListObject(variation, Variation.class);
    }

    @Mapping(target = "variations", expression = "java(toVariationJson(toggleRequest.getVariations()))")
    @Mapping(target = "tags", ignore = true)
    Toggle requestToEntify(ToggleCreateRequest toggleRequest);

    default String toVariationJson(List<Variation> variations)  {
        return JsonMapper.toJSONString(variations);
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "tags", ignore = true)
    void mapEntity(ToggleUpdateRequest toggleRequest, @MappingTarget Toggle toggle);

}
