package com.labdigitiser.network.model;

public class ApiPlant {

    private int id;
    private String description;
    private int is_active;
    private String created_at;
    private int company_id;
    private String companyName;
    private String company_name;
    private String plantName;
    private String short_code;
    private String plantType;
    private String name;
    private String type;

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return is_active == 1;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public int getCompanyId() {
        return company_id;
    }

    public String getCompanyName() {
        return company_name != null ? company_name : companyName;
    }

    public String getPlantName() {
        return name != null ? name : plantName;
    }

    public String getPlantType() {
        return type != null ? type : plantType;
    }

    public String getShortCode() {
        return short_code;
    }
}
