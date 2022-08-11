package com.featureprobe.api.base.config;

import com.featureprobe.api.auth.tenant.TenantContext;
import com.featureprobe.api.entity.TenantSupport;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class TenantEntityListener {

    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof TenantSupport) {
            ((TenantSupport) entity).setOrganizeId(Long.parseLong(TenantContext.getCurrentTenant()));
        }
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof TenantSupport) {
            ((TenantSupport) entity).setOrganizeId(Long.parseLong(TenantContext.getCurrentTenant()));
        }
    }

}
