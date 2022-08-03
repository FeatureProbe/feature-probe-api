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
@DynamicInsert
@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "organizeId", type = "string")})
@Filter(name = "tenantFilter", condition = "organize_id = :organizeId")
@FilterDef(name = "deletedFilter", parameters = {@ParamDef(name = "deleted", type = "boolean")})
@Filter(name = "deletedFilter", condition = "deleted = :deleted")
public class Attribute extends AbstractAuditEntity implements TenantSupport {

    @Column(name = "project_key")
    private String projectKey;

    @Column(name = "[key]")
    private String key;

    private Boolean deleted;

    @Column(name = "organize_id")
    private String organizeId;

}
