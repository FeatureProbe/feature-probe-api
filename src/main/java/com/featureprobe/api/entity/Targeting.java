package com.featureprobe.api.entity;

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

    private Boolean disabled;

    private String content;

    private Boolean deleted;

    @Column(name = "organize_id")
    private String organizeId;

}
