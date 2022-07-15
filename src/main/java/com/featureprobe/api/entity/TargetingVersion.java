package com.featureprobe.api.entity;

import com.featureprobe.api.base.entity.AbstractAuditEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "targeting_version")
@Where(clause = "deleted = 0")
@DynamicInsert
@ToString(callSuper = true)
public class TargetingVersion extends AbstractAuditEntity {

    @Column(name = "project_key")
    private String projectKey;

    @Column(name = "environment_key")
    private String environmentKey;

    @Column(name = "toggle_key")
    private String toggleKey;

    private String comment;

    private String content;

    private Boolean disabled;

    private Long version;

}
