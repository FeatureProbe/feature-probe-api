package com.featureprobe.api.dao.entity;

import com.featureprobe.api.base.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "member")
@DynamicInsert
@FilterDef(name = "deletedFilter", parameters = {@ParamDef(name = "deleted", type = "boolean")})
@Filter(name = "deletedFilter", condition = "deleted = :deleted")
@EqualsAndHashCode
public class Member extends AbstractAuditEntity {

    private String account;

    private String password;

    @Column(name = "visited_time")
    private Date visitedTime;

    @Enumerated(EnumType.STRING)
    private RoleEnum role;

    @Column(columnDefinition = "TINYINT")
    private Boolean deleted;

    private String source;

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinTable(name = "organization_member", joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "organization_id"))
    @Fetch(FetchMode.JOIN)
    private List<Organization> organizations = new ArrayList<>();

    public Member(Long id, String account) {
        super.setId(id);
        this.account = account;
    }
}
