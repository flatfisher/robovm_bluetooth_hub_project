package com.liferay.bluetooth;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkManager {

    private NetworkManager() {
    }

    public static void getConfiguration(Context context,RequestListener requestListener) {

        if (isAvailable(context)) {

            new DownloadConfigTask(requestListener).execute(Constants.REQUEST_URL + Constants.CONFIG_URL);

        }else {

            requestListener.onError("No network connection available.");

        }

    }

    public static boolean isAvailable(Context context) {

        ConnectivityManager connectivityManager =

                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            return true;

        } else {

            return false;

        }

    }

    private static class DownloadConfigTask extends AsyncTask<String, Void, Response> {

        RequestListener requestListener;

        public DownloadConfigTask(RequestListener requestListener){

            this.requestListener = requestListener;

        }

        @Override
        protected Response doInBackground(String... urls) {

            try {

                return requestUrl(urls[0]);

            } catch (IOException e) {

                return new Response(Constants.UNABLE_RETRIEVE);

            }

        }

        @Override
        protected void onPostExecute(Response response) {

            super.onPostExecute(response);

            if(!response.responseData.equals(Constants.UNABLE_RETRIEVE)){

                requestListener.onResponse(response.responseData,response.code);

            }else{

                requestListener.onError(Constants.UNABLE_RETRIEVE);

            }

        }

    }

    static class Response{

        String responseData;

        int code;

        Response(){

        }

        Response(String unableRetrieveError){

            responseData = unableRetrieveError;

        }

    }

    private static Response requestUrl(String requestUrl) throws IOException {

        Response response = new Response();

        InputStream inputStream = null;

        try {

            URL url = new URL(requestUrl);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setReadTimeout(10000);

            httpURLConnection.setConnectTimeout(15000);

            httpURLConnection.setRequestMethod("GET");

            httpURLConnection.setDoInput(true);

            httpURLConnection.connect();

            response.code = httpURLConnection.getResponseCode();

            inputStream = httpURLConnection.getInputStream();

            response.responseData = convertInputStreamToJsonString(inputStream);

            return response;

        } finally {

            if (inputStream != null) {

                inputStream.close();

            }

        }
    }

    public static String convertInputStreamToJsonString(InputStream inputStream)
            throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder stringBuilder = new StringBuilder();

        String resultData;

        while ((resultData = bufferedReader.readLine()) != null) {

            stringBuilder.append(resultData + "\n");

        }

        bufferedReader.close();

        return stringBuilder.toString();

    }

}
