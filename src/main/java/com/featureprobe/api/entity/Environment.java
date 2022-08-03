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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "environment")
@DynamicInsert
@ToString(callSuper = true)
@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "organizeId", type = "string")})
@Filter(name = "tenantFilter", condition = "organize_id = :organizeId")
@FilterDef(name = "deletedFilter", parameters = {@ParamDef(name = "deleted", type = "boolean")})
@Filter(name = "deletedFilter", condition = "deleted = :deleted")
public class Environment extends AbstractAuditEntity implements TenantSupport {

    private String name;

    @Column(name = "[key]")
    private String key;

    @Column(name = "server_sdk_key")
    private String serverSdkKey;

    @Column(name = "client_sdk_key")
    private String clientSdkKey;

    private Boolean deleted;

    @Column(name = "organize_id")
    private String organizeId;

    @ManyToOne
    @JoinColumn(name = "project_key", referencedColumnName = "key")
    private Project project;
}
