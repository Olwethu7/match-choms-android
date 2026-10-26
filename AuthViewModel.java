package com.match.choms.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.match.choms.model.User;
import com.match.choms.repository.AuthRepository;
import com.match.choms.repository.RepoCallback;
import com.match.choms.util.Resource;
import com.match.choms.util.Validators;

import java.util.Map;

/** Login screen logic: validate the form, then ask the repository to sign in. */
public class AuthViewModel extends ViewModel {
    private final AuthRepository repo = AuthRepository.getInstance();

    private final MutableLiveData<Resource<User>> loginState = new MutableLiveData<>();
    private final MutableLiveData<Map<String, String>> fieldErrors = new MutableLiveData<>();

    // simple lock-out after repeated failures (same idea as the web app: 5 tries, then 60 s)
    private int failedAttempts = 0;
    private long lockedUntil = 0;

    public LiveData<Resource<User>> getLoginState() { return loginState; }
    public LiveData<Map<String, String>> getFieldErrors() { return fieldErrors; }
    public boolean hasSession() { return repo.hasFirebaseSession(); }

    public void login(String email, String password) {
        long now = System.currentTimeMillis();
        if (now < lockedUntil) {
            long secs = (lockedUntil - now) / 1000 + 1;
            loginState.setValue(Resource.error("Too many failed attempts. Try again in " + secs + " seconds."));
            return;
        }
        Map<String, String> errors = Validators.validateLogin(email, password);
        fieldErrors.setValue(errors);
        if (!errors.isEmpty()) return;

        loginState.setValue(Resource.loading());
        repo.login(email, password, new RepoCallback<User>() {
            @Override public void onSuccess(User user) {
                failedAttempts = 0;
                loginState.setValue(Resource.success(user));
            }
            @Override public void onError(String message) {
                if (++failedAttempts >= 5) {
                    failedAttempts = 0;
                    lockedUntil = System.currentTimeMillis() + 60_000;
                    message = "Too many failed attempts. Please wait 60 seconds.";
                }
                loginState.setValue(Resource.error(message));
            }
        });
    }

    /** Skip the login screen when Firebase still has a valid session. */
    public void restoreSession() {
        loginState.setValue(Resource.loading());
        repo.restoreSession(new RepoCallback<User>() {
            @Override public void onSuccess(User user) { loginState.setValue(Resource.success(user)); }
            @Override public void onError(String message) { loginState.setValue(Resource.error(message)); }
        });
    }
}
