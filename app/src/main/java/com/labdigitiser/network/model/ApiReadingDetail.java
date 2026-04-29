package com.labdigitiser.network.model;

import java.util.List;

public class ApiReadingDetail {

    private int id;
    private String reading_date;
    private String reading_time;
    private String shift;
    private String notes;
    private String created_at;
    private int plant_id;
    private int location_id;
    private int submitted_by;
    private String location_name;
    private String zone_label;
    private String zone_code;
    private String submitted_by_name;
    private String plant_name;
    private String plant_type;
    private String company_name;
    private String record_id;
    private List<ApiReadingValue> values;

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

    public String getNotes() {
        return notes;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public int getPlantId() {
        return plant_id;
    }

    public int getLocationId() {
        return location_id;
    }

    public int getSubmittedBy() {
        return submitted_by;
    }

    public String getLocationName() {
        return location_name;
    }

    public String getZoneLabel() {
        return zone_label;
    }

    public String getZoneCode() {
        return zone_code;
    }

    public String getSubmittedByName() {
        return submitted_by_name;
    }

    public String getPlantName() {
        return plant_name;
    }

    public String getPlantType() {
        return plant_type;
    }

    public String getCompanyName() {
        return company_name;
    }

    public String getRecordId() {
        return record_id;
    }

    public List<ApiReadingValue> getValues() {
        return values;
    }
}
