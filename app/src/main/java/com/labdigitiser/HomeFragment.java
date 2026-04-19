package com.labdigitiser;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        bindUserHeader(view);
        return view;
    }

    private void bindUserHeader(View view) {
        TextView userNameText = view.findViewById(R.id.tv_user_name);
        TextView avatarInitialText = view.findViewById(R.id.tv_avatar_initial);

        FirebaseManager.getCurrentUserProfile(task -> {
            if (!isAdded()) {
                return;
            }

            String displayName = null;

            if (task.isSuccessful()) {
                DocumentSnapshot snapshot = task.getResult();
                if (snapshot != null) {
                    displayName = snapshot.getString("fullName");
                }
            }

            if (displayName == null || displayName.trim().isEmpty()) {
                FirebaseUser user = FirebaseManager.getCurrentUser();
                if (user != null && user.getEmail() != null) {
                    displayName = user.getEmail();
                } else {
                    displayName = "User";
                }
            }

            String safeName = displayName.trim();
            userNameText.setText(safeName);
            avatarInitialText.setText(getInitial(safeName));
        });
    }

    private String getInitial(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "U";
        }
        return String.valueOf(Character.toUpperCase(name.trim().charAt(0)));
    }
}
