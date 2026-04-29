package com.labdigitiser.network.model;

import java.util.List;

public class ApiDashboardData {

    private ApiStats stats;
    private List<ApiActivityPoint> activity_last_7_days;
    private List<ApiDashboardEntry> recent_entries;

    public ApiStats getStats() {
        return stats;
    }

    public List<ApiActivityPoint> getActivityLast7Days() {
        return activity_last_7_days;
    }

    public List<ApiDashboardEntry> getRecentEntries() {
        return recent_entries;
    }
}
