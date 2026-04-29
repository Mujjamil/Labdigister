package com.labdigitiser;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportsFragment extends Fragment {

    private WebsiteRepository websiteRepository;
    private TextView plantText;
    private TextView hintText;
    private TextView previewText;
    private TextView statusText;
    private Spinner locationSpinner;
    private EditText startDateEdit;
    private EditText endDateEdit;
    private final List<ApiLocation> locations = new ArrayList<>();

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
        statusText = view.findViewById(R.id.tv_export_status);
        locationSpinner = view.findViewById(R.id.spinner_export_location);
        startDateEdit = view.findViewById(R.id.edit_export_start_date);
        endDateEdit = view.findViewById(R.id.edit_export_end_date);
    }

    private void seedDates() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        startDateEdit.setText(today);
        endDateEdit.setText(today);
    }

    private void bindActions(View view) {
        MaterialButton refreshButton = view.findViewById(R.id.button_refresh_export);
        MaterialButton previewButton = view.findViewById(R.id.button_prepare_export);

        refreshButton.setOnClickListener(v -> loadExportContext());
        previewButton.setOnClickListener(v -> buildPreviewSummary());
    }

    private void loadExportContext() {
        statusText.setText("Loading export workflow for the selected member plant...");
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
                    bindLocations(new ArrayList<>());
                    return;
                }

                websiteRepository.selectPlant(String.valueOf(plant.getId()), plant.getPlantName());
                plantText.setText(plant.getCompanyName() + " / " + plant.getPlantName());
                hintText.setText("Match the website export flow: choose dates, location, preview scope, then export.");
                loadLocations();
                loadDashboardPreview();
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<ApiSessionData>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                statusText.setText(t.getMessage());
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
                    previewText.setText("Preview count unavailable.");
                    statusText.setText("Export filters are ready, but live record counts could not be loaded.");
                    return;
                }

                ApiStats stats = body.getData().getStats();
                int totalRecords = stats != null ? stats.getTotalRecords() : 0;
                previewText.setText("Live preview: " + totalRecords + " records currently available for this plant.");
                statusText.setText("Filters loaded. CSV download can be wired as soon as the export API is exposed.");
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<ApiDashboardData>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                previewText.setText("Preview count unavailable.");
                statusText.setText(t.getMessage());
            }
        });
    }

    private void buildPreviewSummary() {
        String startDate = safeText(valueOf(startDateEdit), "Not set");
        String endDate = safeText(valueOf(endDateEdit), "Not set");
        String locationLabel = locationSpinner.getSelectedItem() == null
                ? "All locations"
                : locationSpinner.getSelectedItem().toString();

        previewText.setText(
                "Preview filters\n"
                        + "From: " + startDate + "\n"
                        + "To: " + endDate + "\n"
                        + "Location: " + locationLabel
        );
        statusText.setText("This matches the website's member export filter flow. Download wiring still needs an export endpoint.");
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

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
