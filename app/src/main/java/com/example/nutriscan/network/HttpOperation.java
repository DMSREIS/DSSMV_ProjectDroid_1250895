package com.example.nutriscan.network;

import com.example.nutriscan.helper.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpOperation {

    private static String readBody(InputStream in) {
        if (in == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        try {
            String read = br.readLine();
            while (read != null) {
                sb.append(read);
                read = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    // GET simples (Open Food Facts): devolve o corpo, ou null se falhar
    public static String get(String urlStr) {
        try {
            HttpResponse response = send("GET", urlStr, null);
            return response.isSuccess() ? response.getBody() : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // GET, POST, PUT ou DELETE. Lança IOException se não houver ligação.
    public static HttpResponse send(String method, String urlStr, String jsonBody)
            throws IOException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("User-Agent", Utils.USER_AGENT);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            if (jsonBody != null) {
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                try (OutputStream out = conn.getOutputStream()) {
                    out.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                }
            }

            int code = conn.getResponseCode();
            InputStream in = (code >= 200 && code < 400)
                    ? conn.getInputStream() : conn.getErrorStream();
            return new HttpResponse(code, readBody(in));
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}