package com.featureprobe.api.entity;


import com.featureprobe.api.base.entity.AbstractAuditEntity;
import com.featureprobe.api.base.enums.OrganizeRoleEnum;
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
@Table(name = "organize_user")
@DynamicInsert
public class OrganizeUser extends AbstractAuditEntity {

    @Column(name = "organize_id")
    private Long organizeId;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    private OrganizeRoleEnum role;

}
