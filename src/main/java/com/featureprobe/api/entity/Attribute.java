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
@Table(name = "attribute")
@ToString(callSuper = true)
@Where(clause = "deleted = 0")
@DynamicInsert
public class Attribute extends AbstractAuditEntity {

    @Column(name = "project_key")
    private String projectKey;

    @Column(name = "[key]")
    private String key;

    private Boolean deleted;

}
