package com.featureprobe.api.entity;

import com.featureprobe.api.base.entity.AbstractAuditEntity;
import com.featureprobe.api.base.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Where;
import org.springframework.security.core.AuthenticatedPrincipal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "member")
@Where(clause = "deleted = 0")
@DynamicInsert
public class Member extends AbstractAuditEntity implements AuthenticatedPrincipal {

    private String account;

    private String password;

    @Column(name = "visited_time")
    private Date visitedTime;

    @Enumerated(EnumType.STRING)
    private RoleEnum role;

    private Boolean deleted;

    @Override
    public String getName() {
        return account;
    }
}
