package com.match.choms.util;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.firestore.FirebaseFirestoreException;

/** Turns technical Firebase exceptions into messages a Community Health Worker can understand. */
public final class ErrorMapper {
    private ErrorMapper() { }

    public static String friendly(Exception e) {
        if (e == null) return "Something went wrong. Please try again.";
        if (e instanceof FirebaseAuthInvalidCredentialsException || e instanceof FirebaseAuthInvalidUserException) {
            return "Incorrect email or password."; // same message for both: do not reveal which one is wrong
        }
        if (e instanceof FirebaseTooManyRequestsException) {
            return "Too many attempts. Please wait a few minutes and try again.";
        }
        if (e instanceof FirebaseNetworkException) {
            return "No internet connection. Please check your data or Wi-Fi and try again.";
        }
        if (e instanceof FirebaseAuthException) {
            String code = ((FirebaseAuthException) e).getErrorCode();
            if ("ERROR_INVALID_CREDENTIAL".equals(code) || "ERROR_WRONG_PASSWORD".equals(code)
                    || "ERROR_USER_NOT_FOUND".equals(code)) return "Incorrect email or password.";
            if ("ERROR_USER_DISABLED".equals(code)) return "This account has been disabled.";
            if ("ERROR_INVALID_EMAIL".equals(code)) return "Enter a valid email address.";
        }
        if (e instanceof FirebaseFirestoreException) {
            switch (((FirebaseFirestoreException) e).getCode()) {
                case PERMISSION_DENIED: return "You do not have permission to do that.";
                case UNAVAILABLE: return "The server is unreachable. Please check your connection.";
                case UNAUTHENTICATED: return "Your session has expired - please sign in again.";
                case DEADLINE_EXCEEDED: return "The request took too long. Please try again.";
                case NOT_FOUND: return "That record no longer exists.";
                default: break;
            }
        }
        String msg = e.getMessage();
        if (msg != null && msg.toLowerCase().contains("network")) {
            return "No internet connection. Please check your data or Wi-Fi and try again.";
        }
        return "Something went wrong. Please try again.";
    }
}
