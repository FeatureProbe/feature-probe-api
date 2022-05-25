package com.featureprobe.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariationAccessCounter {

    @NotBlank
    private String value;

    @NotNull
    private Long count;
}
