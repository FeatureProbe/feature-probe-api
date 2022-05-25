package com.featureprobe.api.entity;


import com.featureprobe.api.base.entity.AbstractAuditEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Where;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "project")
@DynamicInsert
@Where(clause = "deleted = 0")
@ToString(callSuper = true)
public class Project extends AbstractAuditEntity {

    @Column(name = "[key]")
    private String key;

    private String name;

    private String description;

    private Boolean deleted;

    @OneToMany(mappedBy = "project", cascade = CascadeType.PERSIST)
    private List<Environment> environments;
}
