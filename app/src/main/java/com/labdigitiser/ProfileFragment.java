package com.labdigitiser;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.labdigitiser.network.WebsiteRepository;
import com.labdigitiser.network.model.ApiPlant;
import com.labdigitiser.network.model.ApiResponse;
import com.labdigitiser.network.model.ApiSessionData;
import com.labdigitiser.network.model.ApiUser;

import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private WebsiteRepository websiteRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        websiteRepository = new WebsiteRepository(requireContext());
        bindProfile(view);
        bindLogout(view);
        return view;
    }

    private void bindProfile(View view) {
        TextView avatarText = view.findViewById(R.id.tv_profile_avatar);
        TextView verifiedText = view.findViewById(R.id.tv_profile_verified);
        TextView nameText = view.findViewById(R.id.tv_profile_name);
        TextView roleText = view.findViewById(R.id.tv_profile_role);
        TextView plantBadgeText = view.findViewById(R.id.tv_profile_plant_badge);
        TextView plantDetailsText = view.findViewById(R.id.tv_profile_plant_details);
        TextView locationBadgeText = view.findViewById(R.id.tv_profile_location_badge);
        TextView versionText = view.findViewById(R.id.tv_profile_version);
        LinearLayout modulesLayout = view.findViewById(R.id.layout_profile_modules);

        versionText.setText("LIVE MEMBER PROFILE");
        verifiedText.setText("✓");
        roleText.setText("Member");
        plantBadgeText.setText("NO PLANT ASSIGNED");
        plantDetailsText.setText("Waiting for live assignment data.");
        locationBadgeText.setText("API");

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
                    plantDetailsText.setText(body != null ? body.getReadableMessage() : "Unable to load live profile data.");
                    verifiedText.setText("!");
                    return;
                }

                ApiSessionData data = body.getData();
                ApiUser user = data.getUser();
                List<ApiPlant> plants = data.getPlants();

                if (user != null) {
                    nameText.setText(safeText(user.getName(), "Member"));
                    roleText.setText(formatRole(user.getRole()));
                    avatarText.setText(getInitial(user.getName()));
                    verifiedText.setText(user.isActive() ? "✓" : "!");
                }

                ApiPlant selectedPlant = getSelectedPlant(plants);
                if (selectedPlant != null) {
                    plantBadgeText.setText(buildPlantBadge(selectedPlant));
                    plantDetailsText.setText(buildPlantDetails(selectedPlant));
                    locationBadgeText.setText(buildLocationBadge(selectedPlant));
                } else {
                    plantBadgeText.setText("NO PLANT ASSIGNED");
                    plantDetailsText.setText("Logged in successfully, but no plant is currently assigned.");
                    locationBadgeText.setText("N/A");
                }

                bindAssignedModules(modulesLayout, plants);
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<ApiSessionData>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }

                verifiedText.setText("!");
                plantDetailsText.setText(t.getMessage());
            }
        });
    }

    private void bindAssignedModules(LinearLayout container, List<ApiPlant> plants) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        if (plants == null || plants.isEmpty()) {
            TextView chip = (TextView) inflater.inflate(R.layout.include_profile_module_chip, container, false);
            chip.setText("NO MODULES");
            container.addView(chip);
            return;
        }

        for (ApiPlant plant : plants) {
            TextView chip = (TextView) inflater.inflate(R.layout.include_profile_module_chip, container, false);
            chip.setText(buildModuleLabel(plant));
            container.addView(chip);
        }
    }

    private void bindLogout(View view) {
        Button logoutButton = view.findViewById(R.id.button_logout);
        logoutButton.setOnClickListener(v -> {
            if (websiteRepository != null) {
                websiteRepository.clearSession();
            }
            if (getContext() == null) {
                return;
            }

            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private String getInitial(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "U";
        }
        return String.valueOf(Character.toUpperCase(name.trim().charAt(0)));
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

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String formatRole(String role) {
        if (role == null) {
            return "Scientific Member";
        }

        String lower = role.trim().toLowerCase(Locale.US);
        switch (lower) {
            case "member":
                return "Industry Operator";
            case "admin":
                return "Admin";
            default:
                return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
        }
    }

    private String buildPlantBadge(ApiPlant plant) {
        return (safeText(plant.getCompanyName(), "UNIT") + " . " + safeText(plant.getPlantName(), "PLANT")).toUpperCase(Locale.US);
    }

    private String buildPlantDetails(ApiPlant plant) {
        return safeText(plant.getPlantName(), "Plant")
                + " . "
                + safeText(plant.getPlantType(), "Unit")
                + " . "
                + safeText(plant.getCompanyName(), "Assigned company");
    }

    private String buildLocationBadge(ApiPlant plant) {
        String shortCode = safeText(plant.getShortCode(), "");
        if (!shortCode.isEmpty()) {
            return shortCode.toUpperCase(Locale.US);
        }
        return safeText(plant.getPlantType(), "PLANT").toUpperCase(Locale.US);
    }

    private String buildModuleLabel(ApiPlant plant) {
        String type = safeText(plant.getPlantType(), "PLANT").toUpperCase(Locale.US);
        String name = safeText(plant.getPlantName(), "UNIT").toUpperCase(Locale.US);
        if (name.contains(type)) {
            return name;
        }
        return type + " " + name;
    }
}
