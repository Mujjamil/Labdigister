package com.labdigitiser.network.model;

public class ApiStats {

    private int total_records;
    private int today_entries;
    private int week_entries;
    private int active_locations;

    public int getTotalRecords() {
        return total_records;
    }

    public int getTodayEntries() {
        return today_entries;
    }

    public int getWeekEntries() {
        return week_entries;
    }

    public int getActiveLocations() {
        return active_locations;
    }
}
