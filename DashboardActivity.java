package com.match.choms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.match.choms.databinding.ActivityDashboardBinding;
import com.match.choms.model.DashboardStats;
import com.match.choms.model.User;
import com.match.choms.util.Resource;
import com.match.choms.viewmodel.DashboardViewModel;

/**
 * Dashboard. Role-based access control in the UI:
 *  - CHW: sees only their own clients/visits/referrals (the queries are filtered by their UID).
 *  - Admin / Coordinator: sees everything and gets the Reports button.
 */
public class DashboardActivity extends AppCompatActivity {
    private ActivityDashboardBinding binding;
    private DashboardViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        User user = SessionGuard.require(this);
        if (user == null) return;

        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        vm = new ViewModelProvider(this).get(DashboardViewModel.class);

        binding.tvGreeting.setText("Hello, " + user.displayName());
        binding.tvRole.setText(roleLabel(user));
        binding.tvStatBenLabel.setText(user.isStaff() ? "Active beneficiaries" : "My active clients");
        binding.btnReports.setVisibility(user.isStaff() ? View.VISIBLE : View.GONE);

        binding.btnBeneficiaries.setOnClickListener(v -> startActivity(new Intent(this, BeneficiaryListActivity.class)));
        binding.btnVisits.setOnClickListener(v -> startActivity(new Intent(this, VisitListActivity.class)));
        binding.btnReferrals.setOnClickListener(v -> startActivity(new Intent(this, ReferralListActivity.class)));
        binding.btnReports.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        binding.btnLogout.setOnClickListener(v -> confirmLogout());
        binding.swipe.setOnRefreshListener(() -> vm.load());

        vm.getStats().observe(this, state -> {
            binding.swipe.setRefreshing(state.status == Resource.Status.LOADING);
            if (state.status == Resource.Status.SUCCESS && state.data != null) {
                show(state.data);
                binding.tvError.setVisibility(View.GONE);
            } else if (state.status == Resource.Status.ERROR) {
                binding.tvError.setText(state.message);
                binding.tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (vm != null) vm.load();   // refresh the numbers whenever we come back to this screen
    }

    private void show(DashboardStats s) {
        binding.statBeneficiaries.setText(String.valueOf(s.activeBeneficiaries));
        binding.statVisits.setText(String.valueOf(s.visitsThisMonth));
        binding.statPending.setText(String.valueOf(s.referralsPending));
        binding.statUrgent.setText(String.valueOf(s.referralsUrgent));
    }

    private String roleLabel(User u) {
        if (u.isAdmin()) return "Administrator";
        if (u.isCoordinator()) return "Coordinator";
        return "Community Health Worker";
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Sign out")
                .setMessage("Do you want to sign out?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Sign out", (d, w) -> {
                    vm.logout();
                    Intent i = new Intent(this, LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                })
                .show();
    }
}
