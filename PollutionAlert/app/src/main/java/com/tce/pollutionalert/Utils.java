package com.tce.pollutionalert;



import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.DoubleBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;


public final class Utils {


    public static final String LOG_TAG = Utils.class.getSimpleName();


    public static ArrayList<Pollution> fetchFeedbackData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = "";
        try {
            jsonResponse = makeHttpRequest(url);
            Log.e(LOG_TAG,"json response" + jsonResponse);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        // Extract relevant fields from the JSON response and create an {@link Event} object
        ArrayList<Pollution> pollution = extractFeatureFromJson(jsonResponse);

        // Return the {@link Event}
        return pollution;
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }


    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }


    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }


    private static ArrayList<Pollution> extractFeatureFromJson(String POlJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(POlJSON)) {
            return null;
        }
        Log.e(LOG_TAG,"json json json " + POlJSON );
        ArrayList<Pollution> pollution = new ArrayList<Pollution>();

        try {
            JSONArray basearray = new JSONArray(POlJSON);
            JSONObject current = null;
            String location;
            double lati,longi;
            int aqi;
            // If there are results in the features array
            for(int i=0;i<basearray.length();i++){
                current = basearray.getJSONObject(i);
                location = current.getString("stationcode");
                lati = Double.parseDouble(current.getString("lat"));
                longi = Double.parseDouble(current.getString("lon"));
                aqi = Integer.parseInt(current.getString("aqi"));

                Pollution pol = new Pollution(location,aqi,lati,longi);
                pollution.add(pol);

            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the earthquake JSON results", e);
        }
        return pollution;
    }
}

