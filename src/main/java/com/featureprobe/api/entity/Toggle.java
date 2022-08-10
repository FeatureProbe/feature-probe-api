package com.featureprobe.api.entity;

import com.featureprobe.api.base.config.TenantEntityListener;
import com.featureprobe.api.base.entity.AbstractAuditEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "toggle")
@DynamicInsert
@ToString(callSuper = true)
@EntityListeners(TenantEntityListener.class)
@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "organizeId", type = "long")})
@Filter(name = "tenantFilter", condition = "organize_id = :organizeId")
@FilterDef(name = "deletedFilter", parameters = {@ParamDef(name = "deleted", type = "boolean")})
@Filter(name = "deletedFilter", condition = "deleted = :deleted")
public class Toggle extends AbstractAuditEntity implements TenantSupport {

    private String name;

    @Column(name = "[key]")
    private String key;

    @Column(name = "description" , columnDefinition = "TEXT")
    private String desc;

    @Column(name = "return_type", columnDefinition = "CHAR")
    private String returnType;

    @Column(name = "disabled_serve")
    private Integer disabledServe;

    @Column(columnDefinition = "TEXT")
    private String variations;

    @Column(name = "project_key")
    private String projectKey;

    @Column(name = "client_availability", columnDefinition = "TINYINT")
    private Boolean clientAvailability;

    @Column(columnDefinition = "TINYINT")
    private Boolean archived;

    @Column(columnDefinition = "TINYINT")
    private Boolean deleted;

    @Column(name = "organize_id")
    private Long organizeId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "toggle_tag", joinColumns = {@JoinColumn(name = "toggle_key", referencedColumnName = "key")},
            inverseJoinColumns = {@JoinColumn(name = "tag_id", referencedColumnName = "id")})
    @Fetch(FetchMode.JOIN)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Tag> tags = new HashSet<>();

}
