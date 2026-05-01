package com.labdigitiser;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.labdigitiser.network.WebsiteRepository;
import com.labdigitiser.network.model.ApiDashboardData;
import com.labdigitiser.network.model.ApiDashboardEntry;
import com.labdigitiser.network.model.ApiPlant;
import com.labdigitiser.network.model.ApiReadingDetail;
import com.labdigitiser.network.model.ApiReadingValue;
import com.labdigitiser.network.model.ApiResponse;
import com.labdigitiser.network.model.ApiSessionData;
import com.labdigitiser.network.model.ApiUser;

import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private WebsiteRepository websiteRepository;
    private View rootView;
    private ApiPlant primaryAssignedPlant;
    private ApiPlant secondaryAssignedPlant;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        rootView = view;
        websiteRepository = new WebsiteRepository(requireContext());
        bindUserHeader(view);
        bindAssignedPlantActions(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (rootView != null) {
            bindUserHeader(rootView);
        }
    }

    private void bindUserHeader(View view) {
        TextView userNameText = view.findViewById(R.id.tv_user_name);
        TextView avatarInitialText = view.findViewById(R.id.tv_avatar_initial);
        TextView plantNameText = view.findViewById(R.id.tv_plant_name);
        TextView assignedPlantsTitleText = view.findViewById(R.id.tv_section_assigned_plants);
        TextView assignedPlantsActionText = view.findViewById(R.id.tv_assigned_plants_action);
        TextView primaryModuleNameText = view.findViewById(R.id.tv_primary_module_name);
        TextView primaryModuleDescriptionText = view.findViewById(R.id.tv_primary_module_description);
        TextView primaryModuleStatusText = view.findViewById(R.id.tv_primary_module_status);
        TextView secondaryModuleNameText = view.findViewById(R.id.tv_secondary_module_name);
        TextView secondaryModuleDescriptionText = view.findViewById(R.id.tv_secondary_module_description);
        TextView secondaryModuleStatusText = view.findViewById(R.id.tv_secondary_module_status);
        TextView metricOneLabel = view.findViewById(R.id.metric_one_label);
        TextView metricOneValue = view.findViewById(R.id.metric_one_value);
        TextView metricOneUnit = view.findViewById(R.id.metric_one_unit);
        TextView metricTwoLabel = view.findViewById(R.id.metric_two_label);
        TextView metricTwoValue = view.findViewById(R.id.metric_two_value);
        TextView metricTwoUnit = view.findViewById(R.id.metric_two_unit);
        TextView metricThreeLabel = view.findViewById(R.id.metric_three_label);
        TextView metricThreeValue = view.findViewById(R.id.metric_three_value);
        TextView metricThreeUnit = view.findViewById(R.id.metric_three_unit);
        TextView metricFourLabel = view.findViewById(R.id.metric_four_label);
        TextView metricFourValue = view.findViewById(R.id.metric_four_value);
        TextView metricFourUnit = view.findViewById(R.id.metric_four_unit);
        TextView recentEntryOneTitle = view.findViewById(R.id.recent_entry_one_title);
        TextView recentEntryOneSubtitle = view.findViewById(R.id.recent_entry_one_subtitle);
        TextView recentEntryOneStatus = view.findViewById(R.id.recent_entry_one_status);
        TextView recentEntryTwoTitle = view.findViewById(R.id.recent_entry_two_title);
        TextView recentEntryTwoSubtitle = view.findViewById(R.id.recent_entry_two_subtitle);
        TextView recentEntryTwoStatus = view.findViewById(R.id.recent_entry_two_status);
        TextView recentEntryThreeTitle = view.findViewById(R.id.recent_entry_three_title);
        TextView recentEntryThreeSubtitle = view.findViewById(R.id.recent_entry_three_subtitle);
        TextView recentEntryThreeStatus = view.findViewById(R.id.recent_entry_three_status);

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
                    return;
                }

                ApiSessionData data = body.getData();
                ApiUser user = data.getUser();
                List<ApiPlant> plants = data.getPlants();
                ApiPlant selectedPlant = getSelectedPlant(plants);

                String displayName = user != null ? safeText(user.getName(), "User") : "User";
                userNameText.setText(displayName);
                avatarInitialText.setText(getInitial(displayName));

                if (selectedPlant != null) {
                    plantNameText.setText(selectedPlant.getCompanyName() + " / " + selectedPlant.getPlantName());
                }

                assignedPlantsTitleText.setText("ASSIGNED PLANTS");
                bindAssignedPlantCards(
                        plants,
                        selectedPlant,
                        assignedPlantsActionText,
                        primaryModuleNameText,
                        primaryModuleDescriptionText,
                        primaryModuleStatusText,
                        secondaryModuleNameText,
                        secondaryModuleDescriptionText,
                        secondaryModuleStatusText
                );

                bindDashboardData(
                        metricOneLabel,
                        metricOneValue,
                        metricOneUnit,
                        metricTwoLabel,
                        metricTwoValue,
                        metricTwoUnit,
                        metricThreeLabel,
                        metricThreeValue,
                        metricThreeUnit,
                        metricFourLabel,
                        metricFourValue,
                        metricFourUnit,
                        recentEntryOneTitle,
                        recentEntryOneSubtitle,
                        recentEntryOneStatus,
                        recentEntryTwoTitle,
                        recentEntryTwoSubtitle,
                        recentEntryTwoStatus,
                        recentEntryThreeTitle,
                        recentEntryThreeSubtitle,
                        recentEntryThreeStatus
                );
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<ApiSessionData>> call, Throwable t) {
            }
        });
    }

    private void bindAssignedPlantActions(View view) {
        View primaryCard = view.findViewById(R.id.card_primary_assigned_plant);
        View secondaryCard = view.findViewById(R.id.card_secondary_assigned_plant);

        primaryCard.setOnClickListener(v -> switchToAssignedPlant(primaryAssignedPlant));
        secondaryCard.setOnClickListener(v -> switchToAssignedPlant(secondaryAssignedPlant));
    }

    private void bindDashboardData(
            TextView metricOneLabel,
            TextView metricOneValue,
            TextView metricOneUnit,
            TextView metricTwoLabel,
            TextView metricTwoValue,
            TextView metricTwoUnit,
            TextView metricThreeLabel,
            TextView metricThreeValue,
            TextView metricThreeUnit,
            TextView metricFourLabel,
            TextView metricFourValue,
            TextView metricFourUnit,
            TextView recentEntryOneTitle,
            TextView recentEntryOneSubtitle,
            TextView recentEntryOneStatus,
            TextView recentEntryTwoTitle,
            TextView recentEntryTwoSubtitle,
            TextView recentEntryTwoStatus,
            TextView recentEntryThreeTitle,
            TextView recentEntryThreeSubtitle,
            TextView recentEntryThreeStatus
    ) {
        String selectedPlantId = websiteRepository.getSelectedPlantId();
        if (selectedPlantId == null || selectedPlantId.trim().isEmpty()) {
            return;
        }

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
                    return;
                }

                ApiDashboardData dashboardData = body.getData();
                applyReadingCardDefaults(
                        metricOneLabel,
                        metricOneValue,
                        metricOneUnit,
                        metricTwoLabel,
                        metricTwoValue,
                        metricTwoUnit,
                        metricThreeLabel,
                        metricThreeValue,
                        metricThreeUnit,
                        metricFourLabel,
                        metricFourValue,
                        metricFourUnit
                );

                ApiDashboardEntry latestEntry = getEntryAt(dashboardData.getRecentEntries(), 0);
                if (latestEntry != null) {
                    bindLatestReadingValues(
                            latestEntry,
                            metricOneLabel,
                            metricOneValue,
                            metricOneUnit,
                            metricTwoLabel,
                            metricTwoValue,
                            metricTwoUnit,
                            metricThreeLabel,
                            metricThreeValue,
                            metricThreeUnit,
                            metricFourLabel,
                            metricFourValue,
                            metricFourUnit
                    );
                }

                bindRecentEntry(recentEntryOneTitle, recentEntryOneSubtitle, recentEntryOneStatus, getEntryAt(dashboardData.getRecentEntries(), 0));
                bindRecentEntry(recentEntryTwoTitle, recentEntryTwoSubtitle, recentEntryTwoStatus, getEntryAt(dashboardData.getRecentEntries(), 1));
                bindRecentEntry(recentEntryThreeTitle, recentEntryThreeSubtitle, recentEntryThreeStatus, getEntryAt(dashboardData.getRecentEntries(), 2));
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<ApiDashboardData>> call, Throwable t) {
            }
        });
    }

    private void bindLatestReadingValues(
            ApiDashboardEntry latestEntry,
            TextView metricOneLabel,
            TextView metricOneValue,
            TextView metricOneUnit,
            TextView metricTwoLabel,
            TextView metricTwoValue,
            TextView metricTwoUnit,
            TextView metricThreeLabel,
            TextView metricThreeValue,
            TextView metricThreeUnit,
            TextView metricFourLabel,
            TextView metricFourValue,
            TextView metricFourUnit
    ) {
        websiteRepository.getReading(String.valueOf(latestEntry.getId())).enqueue(new retrofit2.Callback<ApiResponse<ApiReadingDetail>>() {
            @Override
            public void onResponse(
                    retrofit2.Call<ApiResponse<ApiReadingDetail>> call,
                    retrofit2.Response<ApiResponse<ApiReadingDetail>> response
            ) {
                if (!isAdded()) {
                    return;
                }

                ApiResponse<ApiReadingDetail> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                    return;
                }

                ApiReadingDetail detail = body.getData();
                bindMetricCard(metricOneLabel, metricOneValue, metricOneUnit, "PH LEVEL", findReadingValue(detail.getValues(), "PH"));
                bindMetricCard(metricTwoLabel, metricTwoValue, metricTwoUnit, "TDS", findReadingValue(detail.getValues(), "TDS"));
                bindMetricCard(metricThreeLabel, metricThreeValue, metricThreeUnit, "BOD", findReadingValue(detail.getValues(), "BOD"));
                bindMetricCard(metricFourLabel, metricFourValue, metricFourUnit, "COD", findReadingValue(detail.getValues(), "COD"));
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<ApiReadingDetail>> call, Throwable t) {
            }
        });
    }

    private void applyReadingCardDefaults(
            TextView metricOneLabel,
            TextView metricOneValue,
            TextView metricOneUnit,
            TextView metricTwoLabel,
            TextView metricTwoValue,
            TextView metricTwoUnit,
            TextView metricThreeLabel,
            TextView metricThreeValue,
            TextView metricThreeUnit,
            TextView metricFourLabel,
            TextView metricFourValue,
            TextView metricFourUnit
    ) {
        metricOneLabel.setText("PH LEVEL");
        metricOneValue.setText("--");
        metricOneUnit.setText("pH");

        metricTwoLabel.setText("TDS");
        metricTwoValue.setText("--");
        metricTwoUnit.setText("ppm");

        metricThreeLabel.setText("BOD");
        metricThreeValue.setText("--");
        metricThreeUnit.setText("mg/L");

        metricFourLabel.setText("COD");
        metricFourValue.setText("--");
        metricFourUnit.setText("mg/L");
    }

    private void bindMetricCard(
            TextView label,
            TextView value,
            TextView unit,
            String fallbackLabel,
            ApiReadingValue readingValue
    ) {
        label.setText(fallbackLabel);
        if (readingValue == null) {
            value.setText("--");
            return;
        }

        value.setText(formatReadingValue(readingValue.getValue()));
        unit.setText(safeText(readingValue.getUnit(), unit.getText().toString()));
    }

    private ApiReadingValue findReadingValue(List<ApiReadingValue> values, String parameterName) {
        if (values == null || parameterName == null) {
            return null;
        }

        for (ApiReadingValue value : values) {
            if (value.getParameterName() != null
                    && parameterName.equalsIgnoreCase(value.getParameterName().trim())) {
                return value;
            }
        }
        return null;
    }

    private String formatReadingValue(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001d) {
            return String.format(Locale.US, "%.0f", value);
        }
        return String.format(Locale.US, "%.1f", value);
    }

    private void bindRecentEntry(
            TextView title,
            TextView subtitle,
            TextView status,
            ApiDashboardEntry entry
    ) {
        if (entry == null) {
            return;
        }

        title.setText(entry.getLocation());
        subtitle.setText(entry.getReadingDate() + ", " + trimSeconds(entry.getReadingTime()) + " / " + entry.getSubmittedBy());
        status.setText(entry.getShift());
    }

    private ApiDashboardEntry getEntryAt(List<ApiDashboardEntry> entries, int index) {
        if (entries == null || index < 0 || index >= entries.size()) {
            return null;
        }
        return entries.get(index);
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

    private String trimSeconds(String value) {
        if (value == null) {
            return "";
        }
        return value.length() >= 5 ? value.substring(0, 5) : value;
    }

    private void bindAssignedPlantCards(
            List<ApiPlant> plants,
            ApiPlant selectedPlant,
            TextView actionText,
            TextView primaryName,
            TextView primaryDescription,
            TextView primaryStatus,
            TextView secondaryName,
            TextView secondaryDescription,
            TextView secondaryStatus
    ) {
        List<ApiPlant> orderedPlants = new java.util.ArrayList<>();
        if (selectedPlant != null) {
            orderedPlants.add(selectedPlant);
        }
        if (plants != null) {
            for (ApiPlant plant : plants) {
                if (selectedPlant != null && plant.getId() == selectedPlant.getId()) {
                    continue;
                }
                orderedPlants.add(plant);
            }
        }

        primaryAssignedPlant = getPlantAt(orderedPlants, 0);
        secondaryAssignedPlant = getPlantAt(orderedPlants, 1);

        bindAssignedPlantCard(primaryName, primaryDescription, primaryStatus, primaryAssignedPlant);
        bindAssignedPlantCard(secondaryName, secondaryDescription, secondaryStatus, secondaryAssignedPlant);

        if (actionText != null) {
            actionText.setVisibility(orderedPlants.size() > 2 ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void bindAssignedPlantCard(
            TextView nameView,
            TextView descriptionView,
            TextView statusView,
            ApiPlant plant
    ) {
        if (plant == null) {
            nameView.setText("No plant assigned");
            descriptionView.setText("Assign another plant from the website admin panel.");
            bindAssignmentStatus(statusView, false, false);
            return;
        }

        nameView.setText(plant.getPlantName());
        descriptionView.setText(safeText(plant.getCompanyName(), safeText(plant.getDescription(), "-")));
        boolean isCurrentPlant = websiteRepository.getSelectedPlantId() != null
                && websiteRepository.getSelectedPlantId().equals(String.valueOf(plant.getId()));
        bindAssignmentStatus(statusView, true, isCurrentPlant);
    }

    private ApiPlant getPlantAt(List<ApiPlant> plants, int index) {
        if (plants == null || index < 0 || index >= plants.size()) {
            return null;
        }
        return plants.get(index);
    }

    private void switchToAssignedPlant(ApiPlant plant) {
        if (!isAdded() || plant == null) {
            return;
        }

        String currentPlantId = websiteRepository.getSelectedPlantId();
        String targetPlantId = String.valueOf(plant.getId());
        if (targetPlantId.equals(currentPlantId)) {
            return;
        }

        websiteRepository.selectPlant(targetPlantId, plant.getPlantName());
        Toast.makeText(requireContext(), "Switched to " + plant.getPlantName(), Toast.LENGTH_SHORT).show();
        if (rootView != null) {
            bindUserHeader(rootView);
        }
    }

    private void bindAssignmentStatus(TextView statusView, boolean isAssigned, boolean isCurrentPlant) {
        if (!isAssigned) {
            statusView.setText("Not Assigned");
            statusView.setBackgroundResource(R.drawable.bg_table_status_pending);
            statusView.setTextColor(getResources().getColor(R.color.status_pending, null));
            return;
        }

        if (isCurrentPlant) {
            statusView.setText("Current");
            statusView.setBackgroundResource(R.drawable.bg_table_status_synced);
            statusView.setTextColor(getResources().getColor(R.color.brand_green, null));
            return;
        }

        statusView.setText("Assigned");
        statusView.setBackgroundResource(R.drawable.bg_chip_active_soft);
        statusView.setTextColor(getResources().getColor(R.color.brand_green, null));
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String getInitial(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "U";
        }
        return String.valueOf(Character.toUpperCase(name.trim().charAt(0)));
    }
}
