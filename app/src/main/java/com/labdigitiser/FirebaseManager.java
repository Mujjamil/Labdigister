package com.labdigitiser;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

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

    public static void signOut() {
        AUTH.signOut();
    }
}
