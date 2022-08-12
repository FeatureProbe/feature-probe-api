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
import org.hibernate.annotations.ParamDef;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(exclude = "environments")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "project")
@DynamicInsert
@EntityListeners(TenantEntityListener.class)
@ToString(callSuper = true, exclude = "environments")
@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "organizeId", type = "string")})
@Filter(name = "tenantFilter", condition = "organize_id = :organizeId")
@FilterDef(name = "deletedFilter", parameters = {@ParamDef(name = "deleted", type = "boolean")})
@Filter(name = "deletedFilter", condition = "deleted = :deleted")
public class Project extends AbstractAuditEntity implements TenantSupport, Serializable {

    @Column(name = "[key]")
    private String key;

    private String name;

    private String description;

    @Column(columnDefinition = "TINYINT")
    private boolean deleted;

    @Column(name = "organize_id")
    private Long organizeId;

    @OneToMany(mappedBy = "project", cascade = CascadeType.PERSIST)
    private List<Environment> environments;

}
