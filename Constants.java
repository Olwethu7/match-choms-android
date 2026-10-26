package com.match.choms.util;

import java.util.Arrays;
import java.util.List;

/** Names shared with the web app so both apps read/write the SAME Firestore data. */
public final class Constants {
    private Constants() { }

    // Firestore collections
    public static final String COL_USERS = "users";
    public static final String COL_BENEFICIARIES = "beneficiaries";
    public static final String COL_VISITS = "visits";
    public static final String COL_REFERRALS = "referrals";
    public static final String COL_AUDIT = "auditLogs";

    // Roles (stored lowercase in users/{uid}.role)
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_COORDINATOR = "coordinator";
    public static final String ROLE_CHW = "chw";

    // Drop-down values (identical to js/utils.js)
    public static final List<String> GENDERS = Arrays.asList("female", "male", "other");
    public static final List<String> PROGRAMMES = Arrays.asList("HIV/TB", "Maternal Health", "Child Health", "Adolescent Health");
    public static final List<String> BENEFICIARY_STATUSES = Arrays.asList("active", "closed");
    public static final List<String> VISIT_STATUSES = Arrays.asList("scheduled", "completed", "cancelled");
    public static final List<String> VISIT_OUTCOMES = Arrays.asList(
            "Stable - no action needed", "Follow-up visit needed", "Referred to facility",
            "Client not found", "Client refused service");
    public static final List<String> REFERRAL_URGENCY = Arrays.asList("routine", "urgent", "emergency");
    public static final List<String> REFERRAL_STATUSES = Arrays.asList("pending", "attended", "completed", "missed", "cancelled");

    // Intent extra keys
    public static final String EXTRA_ID = "extra_id";
}
