package com.match.choms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.match.choms.databinding.ActivityLoginBinding;
import com.match.choms.viewmodel.AuthViewModel;

/** Login screen (Firebase Authentication). Starting point of the app. */
public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AuthViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        vm = new ViewModelProvider(this).get(AuthViewModel.class);

        // Field-level validation messages
        vm.getFieldErrors().observe(this, errors -> {
            binding.tilEmail.setError(errors.get("email"));
            binding.tilPassword.setError(errors.get("password"));
        });

        // Overall login state: loading / success / error
        vm.getLoginState().observe(this, state -> {
            boolean loading = state.status == com.match.choms.util.Resource.Status.LOADING;
            binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!loading);
            switch (state.status) {
                case SUCCESS:
                    startActivity(new Intent(this, DashboardActivity.class));
                    finish();
                    break;
                case ERROR:
                    showError(state.message);
                    break;
                default:
                    binding.tvError.setVisibility(View.GONE);
            }
        });

        binding.btnLogin.setOnClickListener(v -> submit());
        binding.etPassword.setOnEditorActionListener((tv, actionId, ev) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) { submit(); return true; }
            return false;
        });

        // Already signed in before? Skip the password screen.
        if (savedInstanceState == null && vm.hasSession()) vm.restoreSession();
    }

    private void submit() {
        binding.tvError.setVisibility(View.GONE);
        vm.login(String.valueOf(binding.etEmail.getText()), String.valueOf(binding.etPassword.getText()));
    }

    private void showError(String message) {
        if (message == null || message.isEmpty()) return;  // silent: e.g. "no saved session"
        binding.tvError.setText(message);
        binding.tvError.setVisibility(View.VISIBLE);
    }
}
