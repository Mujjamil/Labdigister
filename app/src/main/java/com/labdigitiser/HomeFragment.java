package com.labdigitiser;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.labdigitiser.network.WebsiteRepository;
import com.labdigitiser.network.model.ApiDashboardData;
import com.labdigitiser.network.model.ApiDashboardEntry;
import com.labdigitiser.network.model.ApiPlant;
import com.labdigitiser.network.model.ApiResponse;
import com.labdigitiser.network.model.ApiSessionData;
import com.labdigitiser.network.model.ApiStats;
import com.labdigitiser.network.model.ApiUser;

import java.util.List;

public class HomeFragment extends Fragment {

    private WebsiteRepository websiteRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        websiteRepository = new WebsiteRepository(requireContext());
        bindUserHeader(view);
        return view;
    }

    private void bindUserHeader(View view) {
        TextView userNameText = view.findViewById(R.id.tv_user_name);
        TextView avatarInitialText = view.findViewById(R.id.tv_avatar_initial);
        TextView plantNameText = view.findViewById(R.id.tv_plant_name);
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
                    primaryModuleNameText.setText(selectedPlant.getPlantName());
                    primaryModuleDescriptionText.setText(selectedPlant.getDescription());
                    primaryModuleStatusText.setText(selectedPlant.isActive() ? "Active" : "Inactive");
                    secondaryModuleNameText.setText(selectedPlant.getPlantType() + " Configuration");
                    secondaryModuleDescriptionText.setText("Company code: " + safeText(selectedPlant.getShortCode(), "-"));
                    secondaryModuleStatusText.setText(safeText(user != null ? user.getRole() : null, "member"));
                }

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
                ApiStats stats = dashboardData.getStats();
                if (stats != null) {
                    metricOneLabel.setText("TOTAL RECORDS");
                    metricOneValue.setText(String.valueOf(stats.getTotalRecords()));
                    metricOneUnit.setText("");
                    metricTwoLabel.setText("TODAY'S ENTRIES");
                    metricTwoValue.setText(String.valueOf(stats.getTodayEntries()));
                    metricTwoUnit.setText("");
                    metricThreeLabel.setText("THIS WEEK");
                    metricThreeValue.setText(String.valueOf(stats.getWeekEntries()));
                    metricThreeUnit.setText("");
                    metricFourLabel.setText("LOCATIONS");
                    metricFourValue.setText(String.valueOf(stats.getActiveLocations()));
                    metricFourUnit.setText("");
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
