package com.labdigitiser.network.model;

public class ApiDashboardEntry {

    private int id;
    private String reading_date;
    private String reading_time;
    private String shift;
    private String location;
    private String submitted_by;
    private String record_id;

    public int getId() {
        return id;
    }

    public String getReadingDate() {
        return reading_date;
    }

    public String getReadingTime() {
        return reading_time;
    }

    public String getShift() {
        return shift;
    }

    public String getLocation() {
        return location;
    }

    public String getSubmittedBy() {
        return submitted_by;
    }

    public String getRecordId() {
        return record_id;
    }
}
