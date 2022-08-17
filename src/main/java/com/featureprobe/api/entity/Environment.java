package com.featureprobe.api.entity;

import com.featureprobe.api.base.config.TenantEntityListener;
import com.featureprobe.api.base.entity.AbstractAuditEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.ParamDef;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@EqualsAndHashCode(exclude = "project")
@ToString(callSuper = true, exclude = "project")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "environment")
@DynamicInsert
@EntityListeners(TenantEntityListener.class)
@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "organizationId", type = "long")})
@Filter(name = "tenantFilter", condition = "organization_id = :organizationId")
@FilterDef(name = "deletedFilter", parameters = {@ParamDef(name = "deleted", type = "boolean")})
@Filter(name = "deletedFilter", condition = "deleted = :deleted")
public class Environment extends AbstractAuditEntity implements TenantSupport, Serializable {

    private String name;

    @Column(name = "[key]")
    private String key;

    @Column(name = "server_sdk_key")
    private String serverSdkKey;

    @Column(name = "client_sdk_key")
    private String clientSdkKey;

    @Column(columnDefinition = "TINYINT")
    private boolean deleted;

    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(column=@JoinColumn(name ="organization_id", referencedColumnName ="organization_id")),
            @JoinColumnOrFormula(column=@JoinColumn(name ="project_key", referencedColumnName ="key"))})
    private Project project;


    @Override
    public Long getOrganizationId() {
        return project.getOrganizationId();
    }

    @Override
    public void setOrganizationId(Long organizationId) {
        project.setOrganizationId(organizationId);
    }
}
