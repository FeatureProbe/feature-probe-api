package com.featureprobe.api.auth.tenant;

import com.featureprobe.api.dto.UserOrganize;

public class TenantContext {

    private static ThreadLocal<String> currentTenant = new ThreadLocal<>();

    private static ThreadLocal<UserOrganize> currentOrganize = new ThreadLocal<>();

    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    public static UserOrganize getCurrentOrganize() {
        return currentOrganize.get();
    }

    public static void setCurrentTenant(String tenant) {
        currentTenant.set(tenant);
    }

    public static void setCurrentOrganize(UserOrganize userOrganize) {
        currentOrganize.set(userOrganize);
    }

    public static void clear() {
        currentTenant.remove();
        currentOrganize.remove();
    }

}
