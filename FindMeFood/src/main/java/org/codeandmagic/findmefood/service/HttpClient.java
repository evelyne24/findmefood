package org.codeandmagic.findmefood.service;

import android.content.Context;
import org.apache.http.HttpStatus;
import org.codeandmagic.findmefood.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.codeandmagic.findmefood.Consts.Http.*;

/**
 * Created by evelyne24.
 */
public class HttpClient {

    private String apiKey;

    public HttpClient(Context context) {
        apiKey = context.getString(R.string.google_places_api_key);
    }

    public HttpResponse executeRequest(HttpRequest request) {
        request.addParam(HttpRequest.PARAM_API_KEY, apiKey);
        InputStream inputStream = null;

        try {
            URL url = new URL(request.buildUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setRequestMethod(GET);
            connection.setDoInput(true);
            connection.connect();

            int statusCode = connection.getResponseCode();
            if (statusCode != HttpStatus.SC_OK) {
                return new HttpResponse(statusCode, "Unexpected status code " + statusCode);
            } else {
                return new HttpResponse(statusCode, readFromInputStream(connection.getInputStream()));
            }

        } catch (IOException e) {
            return new HttpResponse(STATUS_CODE_EXCEPTION, e.getMessage(), e);
        } finally {
            closeInputStream(inputStream);
        }
    }

    private String readFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder responseBuilder = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            responseBuilder.append(line);
        }
        return responseBuilder.toString();
    }

    private void closeInputStream(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                // Safe to ignore this exception.
            }
        }
    }
}
