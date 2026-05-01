package com.labdigitiser;

import android.os.Bundle;
import android.app.DatePickerDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.labdigitiser.network.WebsiteRepository;
import com.labdigitiser.network.model.ApiDashboardData;
import com.labdigitiser.network.model.ApiLocation;
import com.labdigitiser.network.model.ApiPlant;
import com.labdigitiser.network.model.ApiResponse;
import com.labdigitiser.network.model.ApiSessionData;
import com.labdigitiser.network.model.ApiStats;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportsFragment extends Fragment {

    private WebsiteRepository websiteRepository;
    private TextView plantText;
    private TextView hintText;
    private TextView previewText;
    private TextView previewCountText;
    private TextView statusText;
    private Spinner locationSpinner;
    private EditText startDateEdit;
    private EditText endDateEdit;
    private MaterialButton downloadButton;
    private final List<ApiLocation> locations = new ArrayList<>();
    private int availableRecordCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);
        websiteRepository = new WebsiteRepository(requireContext());
        bindViews(view);
        seedDates();
        bindActions(view);
        loadExportContext();
        return view;
    }

    private void bindViews(View view) {
        plantText = view.findViewById(R.id.tv_export_plant_name);
        hintText = view.findViewById(R.id.tv_export_hint);
        previewText = view.findViewById(R.id.tv_export_preview);
        previewCountText = view.findViewById(R.id.tv_export_preview_count);
        statusText = view.findViewById(R.id.tv_export_status);
        locationSpinner = view.findViewById(R.id.spinner_export_location);
        startDateEdit = view.findViewById(R.id.edit_export_start_date);
        endDateEdit = view.findViewById(R.id.edit_export_end_date);
        downloadButton = view.findViewById(R.id.button_download_export);
    }

    private void seedDates() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        startDateEdit.setText(today);
        endDateEdit.setText(today);
    }

    private void bindActions(View view) {
        MaterialButton previewButton = view.findViewById(R.id.button_prepare_export);
        MaterialButton resetButton = view.findViewById(R.id.button_reset_export);

        previewButton.setOnClickListener(v -> buildPreviewSummary());
        resetButton.setOnClickListener(v -> resetFilters());
        downloadButton.setOnClickListener(v -> onDownloadClicked());
        startDateEdit.setOnClickListener(v -> showDatePicker(startDateEdit));
        endDateEdit.setOnClickListener(v -> showDatePicker(endDateEdit));
    }

    private void loadExportContext() {
        statusText.setText("Loading export workflow for the selected member plant...");
        previewCountText.setText("0");
        previewText.setText("records match your filters");
        downloadButton.setEnabled(false);
        websiteRepository.getCurrentUser().enqueue(new retrofit2.Callback<ApiResponse<ApiSessionData>>() {
            @Override
            public void onResponse(
                    retrofit2.Call<ApiResponse<ApiSessionData>> call,
                    retrofit2.Response<ApiResponse<ApiSessionData>> response
            ) {
                if (!isAdded()) {
                    return;
                }

                ApiResponse<ApiSessionData> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                    statusText.setText(body != null ? body.getReadableMessage() : "Unable to load export workflow.");
                    return;
                }

                ApiPlant plant = getSelectedPlant(body.getData().getPlants());
                if (plant == null) {
                    plantText.setText("No plant selected");
                    hintText.setText("Assign or select a plant before preparing exports.");
                    statusText.setText("Export workflow unavailable.");
                    previewCountText.setText("0");
                    previewText.setText("records match your filters");
                    bindLocations(new ArrayList<>());
                    return;
                }

                websiteRepository.selectPlant(String.valueOf(plant.getId()), plant.getPlantName());
                plantText.setText(plant.getCompanyName() + " / " + plant.getPlantName());
                hintText.setText("Match the website export flow: choose dates, location, preview the record scope, then download CSV.");
                loadLocations();
                loadDashboardPreview();
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<ApiSessionData>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                statusText.setText(t.getMessage());
                previewCountText.setText("0");
            }
        });
    }

    private void loadLocations() {
        websiteRepository.getPlantLocations().enqueue(new retrofit2.Callback<ApiResponse<List<ApiLocation>>>() {
            @Override
            public void onResponse(
                    retrofit2.Call<ApiResponse<List<ApiLocation>>> call,
                    retrofit2.Response<ApiResponse<List<ApiLocation>>> response
            ) {
                if (!isAdded()) {
                    return;
                }

                ApiResponse<List<ApiLocation>> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                    bindLocations(new ArrayList<>());
                    return;
                }

                bindLocations(body.getData());
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<List<ApiLocation>>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                bindLocations(new ArrayList<>());
            }
        });
    }

    private void bindLocations(List<ApiLocation> data) {
        locations.clear();
        locations.addAll(data);

        List<String> labels = new ArrayList<>();
        labels.add("All locations");
        for (ApiLocation location : locations) {
            labels.add(location.getName() + " (" + location.getZoneCode() + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                labels
        );
        locationSpinner.setAdapter(adapter);
    }

    private void loadDashboardPreview() {
        websiteRepository.getDashboard().enqueue(new retrofit2.Callback<ApiResponse<ApiDashboardData>>() {
            @Override
            public void onResponse(
                    retrofit2.Call<ApiResponse<ApiDashboardData>> call,
                    retrofit2.Response<ApiResponse<ApiDashboardData>> response
            ) {
                if (!isAdded()) {
                    return;
                }

                ApiResponse<ApiDashboardData> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                    previewCountText.setText("0");
                    previewText.setText("records match your filters");
                    statusText.setText("Export filters are ready, but live record counts could not be loaded.");
                    return;
                }

                ApiStats stats = body.getData().getStats();
                availableRecordCount = stats != null ? stats.getTotalRecords() : 0;
                previewCountText.setText(String.valueOf(availableRecordCount));
                previewText.setText("records currently available for this plant");
                downloadButton.setText(buildDownloadLabel(availableRecordCount));
                downloadButton.setEnabled(availableRecordCount > 0);
                statusText.setText("Filters loaded. Tap Preview to confirm scope before download.");
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<ApiDashboardData>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                previewCountText.setText("0");
                previewText.setText("records match your filters");
                downloadButton.setEnabled(false);
                statusText.setText(t.getMessage());
            }
        });
    }

    private void buildPreviewSummary() {
        String startDate = valueOf(startDateEdit);
        String endDate = valueOf(endDateEdit);
        if (!isValidDate(startDate) || !isValidDate(endDate)) {
            statusText.setText("Use YYYY-MM-DD dates before previewing.");
            Toast.makeText(requireContext(), "Use valid dates first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String locationLabel = locationSpinner.getSelectedItem() == null
                ? "All locations"
                : locationSpinner.getSelectedItem().toString();

        previewCountText.setText(String.valueOf(availableRecordCount));
        previewText.setText(availableRecordCount == 1
                ? "record currently matches the selected plant scope"
                : "records currently match the selected plant scope");
        downloadButton.setText(buildDownloadLabel(availableRecordCount));
        downloadButton.setEnabled(availableRecordCount > 0);
        statusText.setText(
                "Preview ready for "
                        + startDate
                        + " to "
                        + endDate
                        + " • "
                        + locationLabel
        );
    }

    private void resetFilters() {
        seedDates();
        if (locationSpinner.getAdapter() != null && locationSpinner.getAdapter().getCount() > 0) {
            locationSpinner.setSelection(0);
        }

        previewCountText.setText(String.valueOf(availableRecordCount));
        previewText.setText(availableRecordCount == 1
                ? "record currently available for this plant"
                : "records currently available for this plant");
        downloadButton.setText(buildDownloadLabel(availableRecordCount));
        downloadButton.setEnabled(availableRecordCount > 0);
        statusText.setText("Filters reset to the website-style default range.");
    }

    private void onDownloadClicked() {
        if (availableRecordCount <= 0) {
            Toast.makeText(requireContext(), "No records available to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(
                requireContext(),
                "CSV download needs a dedicated API endpoint. The app flow is ready.",
                Toast.LENGTH_LONG
        ).show();
        statusText.setText("CSV button is ready in the UI, but the backend still needs a JWT export endpoint.");
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        String currentValue = valueOf(target);
        if (isValidDate(currentValue)) {
            String[] parts = currentValue.split("-");
            calendar.set(Calendar.YEAR, Integer.parseInt(parts[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
        }

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> target.setText(String.format(
                        Locale.US,
                        "%04d-%02d-%02d",
                        year,
                        month + 1,
                        dayOfMonth
                )),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private ApiPlant getSelectedPlant(List<ApiPlant> plants) {
        if (plants == null || plants.isEmpty()) {
            return null;
        }

        String selectedPlantId = websiteRepository.getSelectedPlantId();
        if (selectedPlantId != null) {
            for (ApiPlant plant : plants) {
                if (selectedPlantId.equals(String.valueOf(plant.getId()))) {
                    return plant;
                }
            }
        }

        return plants.get(0);
    }

    private String valueOf(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private String buildDownloadLabel(int count) {
        return count == 1 ? "Download CSV (1 record)" : "Download CSV (" + count + " records)";
    }

    private boolean isValidDate(String value) {
        if (TextUtils.isEmpty(value) || !value.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return false;
        }

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            format.setLenient(false);
            format.parse(value);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
