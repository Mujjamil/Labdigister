package com.labdigitiser.network.model;

import java.util.List;

public class CreateReadingRequest {

    private final int plant_id;
    private final int location_id;
    private final String reading_date;
    private final String reading_time;
    private final String shift;
    private final String notes;
    private final List<ParameterValueRequest> values;

    public CreateReadingRequest(
            int plantId,
            int locationId,
            String readingDate,
            String readingTime,
            String shift,
            String notes,
            List<ParameterValueRequest> parameterValues
    ) {
        this.plant_id = plantId;
        this.location_id = locationId;
        this.reading_date = readingDate;
        this.reading_time = readingTime;
        this.shift = shift;
        this.notes = notes;
        this.values = parameterValues;
    }

    public static class ParameterValueRequest {
        private final int parameter_id;
        private final String value;

        public ParameterValueRequest(int parameterId, String value) {
            this.parameter_id = parameterId;
            this.value = value;
        }
    }
}
