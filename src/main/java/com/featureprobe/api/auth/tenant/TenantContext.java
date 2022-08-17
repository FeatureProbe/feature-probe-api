package com.featureprobe.api.auth.tenant;

import com.featureprobe.api.dto.OrganizationMember;

public class TenantContext {

    private static ThreadLocal<String> currentTenant = new ThreadLocal<>();

    private static ThreadLocal<OrganizationMember> currentOrganization = new ThreadLocal<>();

    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    public static OrganizationMember getCurrentOrganization() {
        return currentOrganization.get();
    }

    public static void setCurrentTenant(String tenant) {
        currentTenant.set(tenant);
    }

    public static void setCurrentOrganization(OrganizationMember organizationMember) {
        currentOrganization.set(organizationMember);
    }

    public static void clear() {
        currentTenant.remove();
        currentOrganization.remove();
    }

}
