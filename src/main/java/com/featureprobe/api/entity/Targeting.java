package com.featureprobe.api.entity;

import com.featureprobe.api.base.config.TenantEntityListener;
import com.featureprobe.api.base.entity.AbstractAuditEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;
import javax.persistence.Version;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "targeting")
@DynamicInsert
@ToString(callSuper = true)
@EntityListeners(TenantEntityListener.class)
@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "organizeId", type = "string")})
@Filter(name = "tenantFilter", condition = "organize_id = :organizeId")
@FilterDef(name = "deletedFilter", parameters = {@ParamDef(name = "deleted", type = "boolean")})
@Filter(name = "deletedFilter", condition = "deleted = :deleted")
public class Targeting extends AbstractAuditEntity implements TenantSupport {

    @Column(name = "toggle_key")
    private String toggleKey;

    @Column(name = "environment_key")
    private String environmentKey;

    @Column(name = "project_key")
    private String projectKey;

    @Version
    private Long version;

    @Column(columnDefinition = "TINYINT")
    private Boolean disabled;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TINYINT")
    private boolean deleted;

    @Column(name = "organize_id")
    private Long organizeId;

}
