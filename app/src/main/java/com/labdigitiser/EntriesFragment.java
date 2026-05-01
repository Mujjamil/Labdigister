package com.labdigitiser;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.labdigitiser.network.WebsiteRepository;
import com.labdigitiser.network.model.ApiLocation;
import com.labdigitiser.network.model.ApiParameter;
import com.labdigitiser.network.model.ApiPlant;
import com.labdigitiser.network.model.ApiResponse;
import com.labdigitiser.network.model.ApiSessionData;
import com.labdigitiser.network.model.ApiUser;
import com.labdigitiser.network.model.ApiWriteResponse;
import com.labdigitiser.network.model.CreateReadingRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EntriesFragment extends Fragment {

    private static final int TOTAL_STEPS = 4;

    private WebsiteRepository websiteRepository;
    private TextView headerSubtitleText;
    private TextView memberNameText;
    private TextView stepIndicatorText;
    private TextView zoneText;
    private TextView shiftText;
    private TextView parameterEmptyText;
    private TextView reviewText;
    private TextView statusText;
    private Spinner plantsSpinner;
    private Spinner locationsSpinner;
    private LinearLayout parameterContainer;
    private EditText dateEdit;
    private EditText timeEdit;
    private EditText notesEdit;
    private MaterialButton reloadButton;
    private MaterialButton stepOneNextButton;
    private MaterialButton stepTwoBackButton;
    private MaterialButton stepTwoNextButton;
    private MaterialButton stepThreeBackButton;
    private MaterialButton stepThreeNextButton;
    private MaterialButton stepFourBackButton;
    private MaterialButton stepFourNextButton;
    private LinearLayout stepOnePanel;
    private LinearLayout stepTwoPanel;
    private LinearLayout stepThreePanel;
    private LinearLayout stepFourPanel;

    private final List<ApiPlant> plants = new ArrayList<>();
    private final List<ApiLocation> locations = new ArrayList<>();
    private final List<ApiParameter> parameters = new ArrayList<>();
    private final Map<Integer, CheckBox> parameterChecks = new LinkedHashMap<>();
    private final Map<Integer, EditText> parameterInputs = new LinkedHashMap<>();

    private ApiUser currentUser;
    private int currentStep = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entries, container, false);
        websiteRepository = new WebsiteRepository(requireContext());
        bindViews(view);
        seedTimestamp();
        bindActions();
        loadEntryData();
        showStep(1);
        return view;
    }

    private void bindViews(View view) {
        headerSubtitleText = view.findViewById(R.id.tv_entry_header_subtitle);
        memberNameText = view.findViewById(R.id.tv_entry_member_name);
        stepIndicatorText = view.findViewById(R.id.tv_entry_step_indicator);
        zoneText = view.findViewById(R.id.tv_entry_zone);
        shiftText = view.findViewById(R.id.tv_entry_shift);
        parameterEmptyText = view.findViewById(R.id.tv_entry_parameter_empty);
        reviewText = view.findViewById(R.id.tv_entry_summary);
        statusText = view.findViewById(R.id.tv_entry_status);
        plantsSpinner = view.findViewById(R.id.spinner_plants);
        locationsSpinner = view.findViewById(R.id.spinner_locations);
        parameterContainer = view.findViewById(R.id.container_parameter_fields);
        dateEdit = view.findViewById(R.id.edit_entry_date);
        timeEdit = view.findViewById(R.id.edit_entry_time);
        notesEdit = view.findViewById(R.id.edit_entry_notes);
        reloadButton = view.findViewById(R.id.button_reload_entry_data);
        stepOneNextButton = view.findViewById(R.id.button_step1_next);
        stepTwoBackButton = view.findViewById(R.id.button_step2_back);
        stepTwoNextButton = view.findViewById(R.id.button_step2_next);
        stepThreeBackButton = view.findViewById(R.id.button_step3_back);
        stepThreeNextButton = view.findViewById(R.id.button_step3_next);
        stepFourBackButton = view.findViewById(R.id.button_step4_back);
        stepFourNextButton = view.findViewById(R.id.button_step4_next);
        stepOnePanel = view.findViewById(R.id.panel_step_1);
        stepTwoPanel = view.findViewById(R.id.panel_step_2);
        stepThreePanel = view.findViewById(R.id.panel_step_3);
        stepFourPanel = view.findViewById(R.id.panel_step_4);
    }

    private void seedTimestamp() {
        Date now = new Date();
        dateEdit.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now));
        timeEdit.setText(new SimpleDateFormat("HH:mm", Locale.US).format(now));
        shiftText.setText(detectShift(timeEdit.getText() == null ? "" : timeEdit.getText().toString().trim()));
    }

    private void bindActions() {
        stepOneNextButton.setOnClickListener(v -> handleStepOneNext());
        stepTwoBackButton.setOnClickListener(v -> showStep(1));
        stepTwoNextButton.setOnClickListener(v -> handleStepTwoNext());
        stepThreeBackButton.setOnClickListener(v -> showStep(2));
        stepThreeNextButton.setOnClickListener(v -> handleStepThreeNext());
        stepFourBackButton.setOnClickListener(v -> showStep(3));
        stepFourNextButton.setOnClickListener(v -> submitReading());
        reloadButton.setOnClickListener(v -> loadEntryData());

        plantsSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                ApiPlant plant = getSelectedPlant();
                if (plant == null) {
                    return;
                }
                websiteRepository.selectPlant(String.valueOf(plant.getId()), plant.getPlantName());
                headerSubtitleText.setText("Selected plant: " + plant.getCompanyName() + " / " + plant.getPlantName());
                loadLocationsAndParameters();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        locationsSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                ApiLocation location = getSelectedLocation();
                if (location == null) {
                    zoneText.setText("Select a location");
                    return;
                }
                zoneText.setText(location.getZoneLabel() + " / " + location.getZoneCode());
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                zoneText.setText("Select a location");
            }
        });

        timeEdit.addTextChangedListener(new SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                shiftText.setText(detectShift(s == null ? "" : s.toString().trim()));
            }
        });
    }

    private void loadEntryData() {
        setLoadingState(true, "Loading member session and assigned workflow...");
        websiteRepository.getCurrentUser().enqueue(new Callback<ApiResponse<ApiSessionData>>() {
            @Override
            public void onResponse(Call<ApiResponse<ApiSessionData>> call, Response<ApiResponse<ApiSessionData>> response) {
                if (!isAdded()) {
                    return;
                }

                ApiResponse<ApiSessionData> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                    setLoadingState(false, body != null ? body.getReadableMessage() : "Unable to load member session.");
                    return;
                }

                ApiSessionData data = body.getData();
                currentUser = data.getUser();
                bindSessionData(data);
                setLoadingState(false, "Member workflow loaded.");
            }

            @Override
            public void onFailure(Call<ApiResponse<ApiSessionData>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                setLoadingState(false, t.getMessage());
            }
        });
    }

    private void bindSessionData(ApiSessionData sessionData) {
        if (sessionData.getUser() != null) {
            memberNameText.setText(sessionData.getUser().getName() + " (" + sessionData.getUser().getRole() + ")");
        }

        plants.clear();
        if (sessionData.getPlants() != null) {
            plants.addAll(sessionData.getPlants());
        }
        bindPlants();
    }

    private void bindPlants() {
        List<String> labels = new ArrayList<>();
        int selectedIndex = 0;
        String selectedPlantId = websiteRepository.getSelectedPlantId();

        for (int i = 0; i < plants.size(); i++) {
            ApiPlant plant = plants.get(i);
            labels.add(plant.getCompanyName() + " / " + plant.getPlantName() + " (" + plant.getPlantType() + ")");
            if (selectedPlantId != null && selectedPlantId.equals(String.valueOf(plant.getId()))) {
                selectedIndex = i;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                labels
        );
        plantsSpinner.setAdapter(adapter);

        if (!plants.isEmpty()) {
            plantsSpinner.setSelection(selectedIndex);
            ApiPlant plant = plants.get(selectedIndex);
            websiteRepository.selectPlant(String.valueOf(plant.getId()), plant.getPlantName());
            if (plants.size() > 1) {
                headerSubtitleText.setText("Select the working plant first, then follow the website's 5-step entry flow.");
            } else {
                headerSubtitleText.setText("Assigned plant ready. Follow the website's 5-step entry flow.");
            }
            loadLocationsAndParameters();
        } else {
            headerSubtitleText.setText("No plants assigned to this member.");
            locations.clear();
            parameters.clear();
            bindLocations();
            bindParameterRows();
        }
    }

    private void loadLocationsAndParameters() {
        if (websiteRepository.getSelectedPlantId() == null) {
            return;
        }

        setLoadingState(true, "Loading plant locations...");
        websiteRepository.getPlantLocations().enqueue(new Callback<ApiResponse<List<ApiLocation>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ApiLocation>>> call, Response<ApiResponse<List<ApiLocation>>> response) {
                if (!isAdded()) {
                    return;
                }

                ApiResponse<List<ApiLocation>> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                    setLoadingState(false, body != null ? body.getReadableMessage() : "Unable to load locations.");
                    return;
                }

                locations.clear();
                if (body.getData() != null) {
                    locations.addAll(body.getData());
                }
                bindLocations();
                loadParameters();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ApiLocation>>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                setLoadingState(false, t.getMessage());
            }
        });
    }

    private void bindLocations() {
        List<String> labels = new ArrayList<>();
        for (ApiLocation location : locations) {
            labels.add(location.getName() + " (" + location.getZoneLabel() + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                labels
        );
        locationsSpinner.setAdapter(adapter);

        if (locations.isEmpty()) {
            zoneText.setText("No locations available for this plant.");
        }
    }

    private void loadParameters() {
        setLoadingState(true, "Loading parameters...");
        websiteRepository.getPlantParameters().enqueue(new Callback<ApiResponse<List<ApiParameter>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ApiParameter>>> call, Response<ApiResponse<List<ApiParameter>>> response) {
                if (!isAdded()) {
                    return;
                }

                ApiResponse<List<ApiParameter>> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                    setLoadingState(false, body != null ? body.getReadableMessage() : "Unable to load parameters.");
                    return;
                }

                parameters.clear();
                if (body.getData() != null) {
                    parameters.addAll(body.getData());
                }
                bindParameterRows();
                setLoadingState(false, "Plant parameters ready.");
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ApiParameter>>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                setLoadingState(false, t.getMessage());
            }
        });
    }

    private void bindParameterRows() {
        parameterContainer.removeAllViews();
        parameterChecks.clear();
        parameterInputs.clear();

        if (parameters.isEmpty()) {
            parameterEmptyText.setVisibility(View.VISIBLE);
            parameterEmptyText.setText("No parameters found for this plant.");
            return;
        }

        parameterEmptyText.setVisibility(View.GONE);

        for (ApiParameter parameter : parameters) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            rowParams.bottomMargin = 18;
            row.setLayoutParams(rowParams);

            CheckBox checkBox = new CheckBox(requireContext());
            checkBox.setText(parameter.getName());
            checkBox.setTextColor(getResources().getColor(R.color.text_dark, null));

            TextView subLabel = new TextView(requireContext());
            LinearLayout.LayoutParams subLabelParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            subLabelParams.topMargin = 4;
            subLabel.setLayoutParams(subLabelParams);
            subLabel.setText(buildParameterMeta(parameter));
            subLabel.setTextColor(getResources().getColor(R.color.text_muted, null));
            subLabel.setTextSize(12f);

            EditText input = new EditText(requireContext());
            LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            inputParams.topMargin = 8;
            input.setLayoutParams(inputParams);
            input.setBackgroundResource(R.drawable.bg_input_small);
            input.setHint("Enter " + parameter.getName());
            input.setPadding(28, 24, 28, 24);
            input.setTextColor(getResources().getColor(R.color.text_dark, null));
            input.setHintTextColor(getResources().getColor(R.color.text_hint, null));

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                input.setEnabled(isChecked);
                input.setAlpha(isChecked ? 1.0f : 0.5f);
                if (!isChecked) {
                    input.setText("");
                }
            });

            input.setEnabled(false);
            input.setAlpha(0.5f);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

            row.addView(checkBox);
            row.addView(subLabel);
            row.addView(input);
            parameterChecks.put(parameter.getId(), checkBox);
            parameterInputs.put(parameter.getId(), input);
            parameterContainer.addView(row);
        }
    }

    private void handleStepOneNext() {
        if (plants.isEmpty()) {
            Toast.makeText(requireContext(), "No plants assigned.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (locations.isEmpty()) {
            Toast.makeText(requireContext(), "No locations available.", Toast.LENGTH_SHORT).show();
            return;
        }
        showStep(2);
    }

    private void handleStepTwoNext() {
        String date = valueOf(dateEdit);
        String time = valueOf(timeEdit);

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(time)) {
            Toast.makeText(requireContext(), "Date and time are required.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidDate(date)) {
            dateEdit.setError("Use YYYY-MM-DD");
            return;
        }
        if (!isValidTime(time)) {
            timeEdit.setError("Use HH:MM in 24-hour format");
            return;
        }
        shiftText.setText(detectShift(time));
        showStep(3);
    }

    private void handleStepThreeNext() {
        List<CreateReadingRequest.ParameterValueRequest> values = collectParameterValues(false);
        if (values.isEmpty()) {
            Toast.makeText(requireContext(), "Select at least one parameter and enter its value.", Toast.LENGTH_LONG).show();
            return;
        }
        buildReview();
        showStep(4);
    }

    private void buildReview() {
        ApiPlant plant = getSelectedPlant();
        ApiLocation location = getSelectedLocation();
        StringBuilder builder = new StringBuilder();

        builder.append("Company: ").append(plant != null ? plant.getCompanyName() : "-").append("\n");
        builder.append("Plant: ").append(plant != null ? plant.getPlantName() : "-").append("\n");
        builder.append("Location: ").append(location != null ? location.getName() : "-").append("\n");
        builder.append("Zone: ").append(location != null ? location.getZoneLabel() + " / " + location.getZoneCode() : "-").append("\n");
        builder.append("Date: ").append(valueOf(dateEdit)).append("\n");
        builder.append("Time: ").append(valueOf(timeEdit)).append("\n");
        builder.append("Shift: ").append(detectShift(valueOf(timeEdit))).append("\n");
        builder.append("Submitted By: ").append(currentUser != null ? currentUser.getName() : "-").append("\n");

        String notes = valueOf(notesEdit);
        if (!TextUtils.isEmpty(notes)) {
            builder.append("Notes: ").append(notes).append("\n");
        }

        builder.append("\nParameters:\n");
        for (ApiParameter parameter : parameters) {
            CheckBox checkBox = parameterChecks.get(parameter.getId());
            EditText input = parameterInputs.get(parameter.getId());
            if (checkBox != null && checkBox.isChecked() && input != null && !TextUtils.isEmpty(valueOf(input))) {
                builder.append("- ")
                        .append(parameter.getName())
                        .append(": ")
                        .append(valueOf(input))
                        .append(formatUnit(parameter.getUnit()))
                        .append("\n");
            }
        }

        reviewText.setText(builder.toString().trim());
    }

    private void submitReading() {
        ApiPlant plant = getSelectedPlant();
        ApiLocation location = getSelectedLocation();
        if (plant == null || location == null) {
            Toast.makeText(requireContext(), "Plant or location missing.", Toast.LENGTH_LONG).show();
            return;
        }

        List<CreateReadingRequest.ParameterValueRequest> parameterValues = collectParameterValues(true);
        if (parameterValues.isEmpty()) {
            Toast.makeText(requireContext(), "No parameter values selected.", Toast.LENGTH_LONG).show();
            return;
        }

        stepFourNextButton.setEnabled(false);
        statusText.setText("Submitting entry to the shared backend...");

        String readingDate = valueOf(dateEdit);
        String readingTime = normaliseTime(valueOf(timeEdit));
        String shift = detectShift(readingTime);
        String notes = valueOf(notesEdit);

        CreateReadingRequest request = new CreateReadingRequest(
                plant.getId(),
                location.getId(),
                readingDate,
                readingTime,
                shift,
                TextUtils.isEmpty(notes) ? null : notes,
                parameterValues
        );

        websiteRepository.createReading(request).enqueue(new Callback<ApiWriteResponse>() {
            @Override
            public void onResponse(Call<ApiWriteResponse> call, Response<ApiWriteResponse> response) {
                if (!isAdded()) {
                    return;
                }

                stepFourNextButton.setEnabled(true);
                ApiWriteResponse body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                    String message = body != null && body.getMessage() != null ? body.getMessage() : "Submission failed.";
                    statusText.setText(message);
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                    return;
                }

                String message = body.getMessage() == null ? "Entry submitted." : body.getMessage();
                statusText.setText(message + " Admin can now see this on the website.");
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                clearWizardAfterSubmit();
            }

            @Override
            public void onFailure(Call<ApiWriteResponse> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }

                stepFourNextButton.setEnabled(true);
                statusText.setText(t.getMessage());
                Toast.makeText(requireContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private List<CreateReadingRequest.ParameterValueRequest> collectParameterValues(boolean strict) {
        List<CreateReadingRequest.ParameterValueRequest> values = new ArrayList<>();
        for (ApiParameter parameter : parameters) {
            CheckBox checkBox = parameterChecks.get(parameter.getId());
            EditText input = parameterInputs.get(parameter.getId());
            if (checkBox == null || input == null || !checkBox.isChecked()) {
                continue;
            }

            String value = valueOf(input);
            if (TextUtils.isEmpty(value)) {
                if (strict) {
                    input.setError("Enter value");
                }
                continue;
            }
            values.add(new CreateReadingRequest.ParameterValueRequest(parameter.getId(), value));
        }
        return values;
    }

    private void clearWizardAfterSubmit() {
        for (CheckBox checkBox : parameterChecks.values()) {
            checkBox.setChecked(false);
        }
        for (EditText input : parameterInputs.values()) {
            input.setText("");
            input.setEnabled(false);
            input.setAlpha(0.5f);
        }
        notesEdit.setText("");
        seedTimestamp();
        buildReview();
        showStep(1);
    }

    private void showStep(int step) {
        currentStep = step;
        stepIndicatorText.setText("Step " + step + " of " + TOTAL_STEPS);
        stepOnePanel.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        stepTwoPanel.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        stepThreePanel.setVisibility(step == 3 ? View.VISIBLE : View.GONE);
        stepFourPanel.setVisibility(step == 4 ? View.VISIBLE : View.GONE);
    }

    private ApiPlant getSelectedPlant() {
        int position = plantsSpinner.getSelectedItemPosition();
        if (position < 0 || position >= plants.size()) {
            return null;
        }
        return plants.get(position);
    }

    private ApiLocation getSelectedLocation() {
        int position = locationsSpinner.getSelectedItemPosition();
        if (position < 0 || position >= locations.size()) {
            return null;
        }
        return locations.get(position);
    }

    private void setLoadingState(boolean loading, String message) {
        reloadButton.setEnabled(!loading);
        stepFourNextButton.setEnabled(!loading);
        statusText.setText(message);
        parameterEmptyText.setText(loading ? "Loading parameters..." : parameterEmptyText.getText());
    }

    private String formatUnit(String unit) {
        if (unit == null || unit.trim().isEmpty()) {
            return "";
        }
        return " (" + unit.trim() + ")";
    }

    private String detectShift(String time) {
        if (time == null || !time.contains(":")) {
            return "N/A";
        }
        try {
            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            int total = (hour * 60) + minute;
            if (total >= 360 && total < 840) {
                return "Morning";
            }
            if (total >= 840 && total < 1200) {
                return "Afternoon";
            }
            if (total >= 1200 && total < 1380) {
                return "Evening";
            }
            return "Night";
        } catch (Exception exception) {
            return "N/A";
        }
    }

    private boolean isValidDate(String date) {
        if (!date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return false;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            format.setLenient(false);
            format.parse(date);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean isValidTime(String time) {
        if (!time.matches("^\\d{2}:\\d{2}$")) {
            return false;
        }
        try {
            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59;
        } catch (Exception exception) {
            return false;
        }
    }

    private String normaliseTime(String time) {
        return isValidTime(time) ? time : time;
    }

    private String buildParameterMeta(ApiParameter parameter) {
        String category = parameter.getCategory();
        String unit = parameter.getUnit();
        boolean hasCategory = category != null && !category.trim().isEmpty();
        boolean hasUnit = unit != null && !unit.trim().isEmpty();

        if (hasCategory && hasUnit) {
            return category.trim() + " (" + unit.trim() + ")";
        }
        if (hasCategory) {
            return category.trim();
        }
        if (hasUnit) {
            return "Unit: " + unit.trim();
        }
        return "Select this parameter to enter a reading.";
    }

    private String valueOf(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private abstract static class SimpleWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
