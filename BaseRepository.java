package com.match.choms.repository;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.match.choms.model.User;
import com.match.choms.util.Constants;
import com.match.choms.util.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Shared Firestore helpers for all repositories (Repository pattern: the only layer that talks to Firebase). */
abstract class BaseRepository {
    private static final String TAG = "CHOMS";
    protected final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Role-scoped query. Admin/Coordinator read the whole collection; a CHW only reads documents
     * whose owner field equals their UID. The Firestore rules enforce the same restriction on the
     * server, so a query WITHOUT this filter would be rejected for a CHW.
     */
    protected Query scoped(String collection, String ownerField, User user) {
        Query q = db.collection(collection);
        if (user == null || !user.isStaff()) {
            q = q.whereEqualTo(ownerField, user == null ? "" : user.id);
        }
        return q;
    }

    /** Convert documents to model objects, skipping any malformed document instead of crashing. */
    protected <T> List<T> toList(QuerySnapshot snap, Class<T> type) {
        List<T> out = new ArrayList<>();
        for (DocumentSnapshot d : snap.getDocuments()) {
            try {
                T obj = d.toObject(type);
                if (obj != null) out.add(obj);
            } catch (RuntimeException ex) {
                Log.w(TAG, "Skipping unreadable document " + d.getId(), ex);
            }
        }
        return out;
    }

    /** Add audit fields (same names as the web app) to a map that is about to be saved. */
    protected void stamp(Map<String, Object> data, User actor, boolean isNew) {
        String now = DateUtils.nowISO();
        data.put("updatedAt", now);
        data.put("updatedBy", actor.id);
        if (isNew) {
            data.put("createdAt", now);
            data.put("createdBy", actor.id);
        }
    }

    /** Append-only audit trail entry (best effort: a failure here never blocks the user's save). */
    protected void audit(String action, String collection, String docId, String summary, User actor) {
        Map<String, Object> log = new HashMap<>();
        log.put("action", action);                 // create | update | delete  (required by the rules)
        log.put("collection", collection);
        log.put("docId", docId);
        log.put("summary", summary == null ? "" : (summary.length() > 200 ? summary.substring(0, 200) : summary));
        log.put("userId", actor.id);               // must equal the signed-in UID (rules)
        log.put("userName", actor.displayName());
        log.put("role", actor.role);
        log.put("at", DateUtils.nowISO());
        db.collection(Constants.COL_AUDIT).add(log)
                .addOnFailureListener(e -> Log.w(TAG, "Audit log failed", e));
    }
}
