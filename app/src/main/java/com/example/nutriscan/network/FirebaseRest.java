package com.example.nutriscan.network;

import android.util.Log;

import com.example.nutriscan.helper.Utils;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.io.IOException;

public class FirebaseRest {

    public static HttpResponse get(String path) throws Exception {
        return call("GET", path, null, "");
    }

    // GET com parâmetros extra já codificados, começados por "&"
    public static HttpResponse get(String path, String query) throws Exception {
        return call("GET", path, null, query);
    }

    public static HttpResponse put(String path, String json) throws Exception {
        return call("PUT", path, json, "");
    }

    public static HttpResponse post(String path, String json) throws Exception {
        return call("POST", path, json, "");
    }

    public static HttpResponse delete(String path) throws Exception {
        return call("DELETE", path, null, "");
    }

    private static HttpResponse call(String method, String path, String body, String query)
            throws Exception {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("Sem sessão iniciada");
        }

        GetTokenResult tokenResult = Tasks.await(user.getIdToken(false));
        String token = tokenResult.getToken();

        String url = Utils.DATABASE_URL + "/users/" + user.getUid() + "/" + path
                + ".json?auth=" + token + query;

        HttpResponse response = HttpOperation.send(method, url, body);
        Log.d("FirebaseRest", method + " " + path + " -> " + response.getCode());

        if (!response.isSuccess()) {
            throw new IOException("HTTP " + response.getCode() + ": " + response.getBody());
        }
        return response;
    }
}