package com.labdigitiser;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        TextView nameText = view.findViewById(R.id.tv_profile_name);
        TextView emailText = view.findViewById(R.id.tv_profile_email);
        TextView industryText = view.findViewById(R.id.tv_profile_industry);
        TextView roleText = view.findViewById(R.id.tv_profile_role);
        TextView organizationIdText = view.findViewById(R.id.tv_profile_organization_id);
        TextView panelStatusText = view.findViewById(R.id.tv_profile_panel_status);
        TextView panelMessageText = view.findViewById(R.id.tv_profile_panel_message);
        TextView websiteUrlText = view.findViewById(R.id.tv_profile_website_url);

        websiteUrlText.setText("Website: " + BuildConfig.LABDIGITISER_BASE_URL);
        panelStatusText.setText("Panel status: Checking live API session...");

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
                    panelStatusText.setText("Panel status: API session unavailable");
                    panelMessageText.setText(body != null ? body.getReadableMessage() : getString(R.string.api_ready_waiting_message));
                    return;
                }

                ApiSessionData data = body.getData();
                ApiUser user = data.getUser();
                List<ApiPlant> plants = data.getPlants();

                if (user != null) {
                    nameText.setText(user.getName());
                    emailText.setText(user.getEmail());
                    roleText.setText("Role: " + safeText(user.getRole(), "member"));
                    avatarText.setText(getInitial(user.getName()));
                }

                ApiPlant selectedPlant = getSelectedPlant(plants);
                if (selectedPlant != null) {
                    industryText.setText("Company: " + safeText(selectedPlant.getCompanyName(), "Not assigned"));
                    organizationIdText.setText("Plant ID: " + selectedPlant.getId());
                    panelMessageText.setText(
                            "Connected to live API for "
                                    + selectedPlant.getPlantName()
                                    + " ("
                                    + selectedPlant.getPlantType()
                                    + ") at "
                                    + selectedPlant.getCompanyName()
                    );
                } else {
                    industryText.setText("Company: Not assigned");
                    organizationIdText.setText("Plant ID: -");
                    panelMessageText.setText("Logged in through the live API, but no plant is currently selected.");
                }

                panelStatusText.setText("Panel status: Connected to live API");
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<ApiSessionData>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }

                panelStatusText.setText("Panel status: API request failed");
                panelMessageText.setText(t.getMessage());
            }
        });
    }

    private void bindLogout(View view) {
        Button logoutButton = view.findViewById(R.id.button_logout);
        logoutButton.setOnClickListener(v -> {
            FirebaseManager.signOut();
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
}
