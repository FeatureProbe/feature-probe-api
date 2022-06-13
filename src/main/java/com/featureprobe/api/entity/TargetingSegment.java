package com.featureprobe.api.entity;


import com.featureprobe.api.base.entity.AbstractAuditEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "targeting_segment")
@DynamicInsert
@ToString(callSuper = true)
public class TargetingSegment extends AbstractAuditEntity {


    @Column(name = "targeting_id")
    private Long targetingId;

    @Column(name = "segment_key")
    private String segmentKey;

    @Column(name = "project_key")
    private String projectKey;

}
