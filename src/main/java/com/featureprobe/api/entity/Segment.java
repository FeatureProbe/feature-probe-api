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
import javax.persistence.Version;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "segment")
@DynamicInsert
@ToString(callSuper = true)
@Where(clause = "deleted = 0")
public class Segment extends AbstractAuditEntity {

    private String name;

    @Column(name = "[key]")
    private String key;

    private String description;

    @Column(name = "project_key")
    private String projectKey;

    @Version
    private Long version;

    private String rules;

    private Boolean deleted;

}
