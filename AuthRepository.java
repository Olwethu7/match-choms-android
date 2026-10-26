package com.match.choms.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.match.choms.model.User;
import com.match.choms.util.Constants;
import com.match.choms.util.ErrorMapper;

import java.util.Arrays;
import java.util.List;

/**
 * Authentication + profile loading.
 * Step 1: Firebase Auth checks the email/password.
 * Step 2: the role is read from Firestore users/{uid} (same document the web app uses).
 * The signed-in profile is kept in memory so every screen can apply role-based access control.
 */
public class AuthRepository {
    private static AuthRepository instance;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private User currentUser;

    private AuthRepository() { }

    public static synchronized AuthRepository getInstance() {
        if (instance == null) instance = new AuthRepository();
        return instance;
    }

    /** The profile of the signed-in user, or null when signed out. */
    public User getCurrentUser() { return currentUser; }

    public boolean hasFirebaseSession() { return auth.getCurrentUser() != null; }

    public void login(String email, String password, RepoCallback<User> cb) {
        auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(result -> {
                    FirebaseUser fu = result.getUser();
                    if (fu == null) { cb.onError("Sign-in failed. Please try again."); return; }
                    loadProfile(fu.getUid(), fu.getEmail(), cb);
                })
                .addOnFailureListener(e -> cb.onError(ErrorMapper.friendly(e)));
    }

    /** Called on app start: if Firebase still has a session, reload the profile instead of asking for a password. */
    public void restoreSession(RepoCallback<User> cb) {
        FirebaseUser fu = auth.getCurrentUser();
        if (fu == null) { cb.onError(""); return; }
        loadProfile(fu.getUid(), fu.getEmail(), cb);
    }

    private void loadProfile(String uid, String email, RepoCallback<User> cb) {
        db.collection(Constants.COL_USERS).document(uid).get()
                .addOnSuccessListener(doc -> {
                    User u = null;
                    try {
                        u = doc.exists() ? doc.toObject(User.class) : null;
                    } catch (RuntimeException ignored) { /* treated as missing profile below */ }
                    List<String> roles = Arrays.asList(Constants.ROLE_ADMIN, Constants.ROLE_COORDINATOR, Constants.ROLE_CHW);
                    if (u == null || u.role == null || !roles.contains(u.role)) {
                        logout();
                        cb.onError("Your account has no CHOMS profile. Please contact your administrator.");
                        return;
                    }
                    if (!u.isActive()) {
                        logout();
                        cb.onError("Your account has been deactivated. Please contact your administrator.");
                        return;
                    }
                    u.id = uid;
                    if (u.email == null || u.email.isEmpty()) u.email = email;
                    currentUser = u;
                    cb.onSuccess(u);
                })
                .addOnFailureListener(e -> {
                    logout();
                    cb.onError(ErrorMapper.friendly(e));
                });
    }

    public void logout() {
        currentUser = null;
        auth.signOut();
    }
}
