package com.labdigitiser.network.model;

import com.google.gson.annotations.SerializedName;

public class ApiParameter {

    private int id;
    @SerializedName("plant_id")
    private int plantId;
    private String name;
    private String unit;
    private String category;
    @SerializedName("icon_name")
    private String iconName;
    @SerializedName("sort_order")
    private int sortOrder;
    @SerializedName("is_active")
    private int isActive;

    public int getId() {
        return id;
    }

    public int getPlantId() {
        return plantId;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public String getCategory() {
        return category;
    }

    public String getIconName() {
        return iconName;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public boolean isActive() {
        return isActive == 1;
    }
}
