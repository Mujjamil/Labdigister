package com.labdigitiser.network.model;

public class ApiReadingValue {

    private int id;
    private int parameter_id;
    private double value;
    private String parameter_name;
    private String unit;
    private String icon_name;

    public int getId() {
        return id;
    }

    public int getParameterId() {
        return parameter_id;
    }

    public double getValue() {
        return value;
    }

    public String getParameterName() {
        return parameter_name;
    }

    public String getUnit() {
        return unit;
    }

    public String getIconName() {
        return icon_name;
    }
}
