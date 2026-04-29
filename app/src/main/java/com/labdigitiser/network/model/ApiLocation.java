package com.labdigitiser.network.model;

public class ApiLocation {

    private int id;
    private int plant_id;
    private String name;
    private String zone_label;
    private String zone_code;
    private int sort_order;
    private int is_active;

    public int getId() {
        return id;
    }

    public int getPlantId() {
        return plant_id;
    }

    public String getName() {
        return name;
    }

    public String getZoneLabel() {
        return zone_label;
    }

    public String getZoneCode() {
        return zone_code;
    }

    public int getSortOrder() {
        return sort_order;
    }

    public boolean isActive() {
        return is_active == 1;
    }
}
