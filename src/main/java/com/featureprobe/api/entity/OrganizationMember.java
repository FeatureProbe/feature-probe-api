package com.featureprobe.api.entity;


import com.featureprobe.api.base.entity.AbstractAuditEntity;
import com.featureprobe.api.base.enums.OrganizationRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "organization_member")
@DynamicInsert
public class OrganizationMember extends AbstractAuditEntity {

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "member_id")
    private Long memberId;

    @Enumerated(EnumType.STRING)
    private OrganizationRoleEnum role;

}
