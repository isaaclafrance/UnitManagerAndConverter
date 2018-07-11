package com.isaacapps.unitconverterapp.dao.api;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ApiRequester {
    public static final String DEFAULT_USER_AGENT = "(Android Device)sometestemail@gmail.com";

    public HttpsURLConnection createHttpsURLConnection(String requestURL, String userAgent) throws IOException {
        HttpsURLConnection requestHttpsURLConnection = (HttpsURLConnection) new URL(requestURL).openConnection();
        requestHttpsURLConnection.setRequestProperty("User-Agent", userAgent);

        return requestHttpsURLConnection;
    }

    public HttpsURLConnection createHttpURLConnection(String requestURI) throws IOException {
        return createHttpsURLConnection(requestURI, DEFAULT_USER_AGENT);
    }

    public String retrieveResponseString(HttpsURLConnection httpsURLConnection) {
        StringBuilder responseBuilder = new StringBuilder();

        try {
            InputStream bufferedInputStream = new BufferedInputStream(httpsURLConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(bufferedInputStream));

            String readLine;
            while ((readLine = bufferedReader.readLine()) != null) {
                responseBuilder.append(readLine);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpsURLConnection.disconnect();
        }

        return responseBuilder.toString();
    }
}
