package com.match.choms.model;

import com.google.firebase.firestore.DocumentId;

/** A signed-in person. Stored in Firestore at users/{uid} (created by an Admin in the web app). */
public class User {
    @DocumentId public String id;   // = Firebase Auth UID
    public String name;
    public String email;
    public String role;             // admin | coordinator | chw
    public Boolean active;          // false = account deactivated

    /** Required by Firestore for toObject(). */
    public User() { }

    public boolean isAdmin() { return "admin".equals(role); }
    public boolean isCoordinator() { return "coordinator".equals(role); }
    public boolean isChw() { return "chw".equals(role); }
    /** Admins and Coordinators can see all data and reports. */
    public boolean isStaff() { return isAdmin() || isCoordinator(); }
    public boolean isActive() { return active == null || active; }
    public String displayName() { return name == null || name.isEmpty() ? (email == null ? "User" : email) : name; }
}
