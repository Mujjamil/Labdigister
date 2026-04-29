package com.labdigitiser;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

public final class FirebaseManager {

    private static final FirebaseAuth AUTH = FirebaseAuth.getInstance();
    private static final FirebaseFirestore FIRESTORE = FirebaseFirestore.getInstance();

    private FirebaseManager() {
    }

    public static boolean isLoggedIn() {
        return AUTH.getCurrentUser() != null;
    }

    public static void signIn(String email, String password, OnCompleteListener<AuthResult> listener) {
        AUTH.signInWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    public static void signUp(String email, String password, OnCompleteListener<AuthResult> listener) {
        AUTH.createUserWithEmailAndPassword(email, password).addOnCompleteListener(listener);
    }

    public static void saveUserProfile(Map<String, Object> userMap, OnCompleteListener<Void> listener) {
        FirebaseUser user = AUTH.getCurrentUser();
        if (user == null) {
            return;
        }

        FIRESTORE.collection("users")
                .document(user.getUid())
                .set(userMap)
                .addOnCompleteListener(listener);
    }

    public static void ensureOrganizationProfile(
            String organizationId,
            String organizationName,
            OnCompleteListener<Void> listener
    ) {
        if (organizationId == null || organizationId.trim().isEmpty()) {
            return;
        }

        Map<String, Object> orgMap = new HashMap<>();
        orgMap.put("name", organizationName);
        orgMap.put("plantLabel", "PLANT: " + organizationName.toUpperCase(Locale.US));
        orgMap.put("adminPanelEnabled", true);
        orgMap.put("primaryModuleName", "Bio WRP");
        orgMap.put("primaryModuleDescription", "Water Recycle Plant");
        orgMap.put("primaryModuleActive", true);
        orgMap.put("secondaryModuleName", "Bio ETP");
        orgMap.put("secondaryModuleDescription", "Effluent Treatment");
        orgMap.put("secondaryModuleActive", true);
        orgMap.put("updatedAt", System.currentTimeMillis());

        FIRESTORE.collection("organizations")
                .document(organizationId)
                .set(orgMap, SetOptions.merge())
                .addOnCompleteListener(listener);
    }

    public static FirebaseUser getCurrentUser() {
        return AUTH.getCurrentUser();
    }

    public static void getCurrentUserProfile(OnCompleteListener<DocumentSnapshot> listener) {
        FirebaseUser user = AUTH.getCurrentUser();
        if (user == null) {
            return;
        }

        FIRESTORE.collection("users")
                .document(user.getUid())
                .get()
                .addOnCompleteListener(listener);
    }

    public static void getOrganizationProfile(String organizationId, OnCompleteListener<DocumentSnapshot> listener) {
        if (organizationId == null || organizationId.trim().isEmpty()) {
            return;
        }

        FIRESTORE.collection("organizations")
                .document(organizationId)
                .get()
                .addOnCompleteListener(listener);
    }

    public static String toOrganizationId(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim().toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "-");
        normalized = normalized.replaceAll("^-+", "").replaceAll("-+$", "");
        return normalized;
    }

    public static void signOut() {
        AUTH.signOut();
    }
}
