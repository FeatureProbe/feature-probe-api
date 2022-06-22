package com.featureprobe.api.util;

import com.featureprobe.api.dto.PaginationRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageRequestUtil {

    public static Pageable toPageable(PaginationRequest pageRequest, Sort.Direction direction, String sortBy) {
        return PageRequest.of(pageRequest.getPageIndex(), pageRequest.getPageSize(),
                direction, sortBy);
    }
}
