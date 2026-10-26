package com.match.choms.repository;

import com.match.choms.model.User;
import com.match.choms.util.Constants;
import com.match.choms.util.ErrorMapper;

import java.util.ArrayList;
import java.util.List;

/** Reads the list of Community Health Workers (Admin/Coordinator only - used to assign clients and visits). */
public class UserRepository extends BaseRepository {

    public void listChws(RepoCallback<List<User>> cb) {
        db.collection(Constants.COL_USERS).whereEqualTo("role", Constants.ROLE_CHW).get()
                .addOnSuccessListener(snap -> {
                    List<User> all = toList(snap, User.class);
                    List<User> active = new ArrayList<>();
                    for (User u : all) if (u.isActive()) active.add(u);
                    cb.onSuccess(active);
                })
                .addOnFailureListener(e -> cb.onError(ErrorMapper.friendly(e)));
    }
}
