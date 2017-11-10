package hk.idv.wongkalun.networktest;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static String LOG_TAG = "MainActivity";
    TextView TxtResult;
    Button PostBtn, GetBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GetBtn = findViewById(R.id.get_btn);
        PostBtn = findViewById(R.id.post_btn);

        GetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MakeNetworkCall().execute("http://192.168.8.8/Mvc5/Home/Test?text=test", "Get");
            }

        });

        PostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MakeNetworkCall().execute("http://192.168.8.8/Mvc5/Home/Test?text=test", "Post");
            }
        });
    }

    InputStream ByGetMethod(String ServerURL) {

        InputStream DataInputStream = null;
        try {

            URL url = new URL(ServerURL);
            HttpURLConnection cc = (HttpURLConnection)
                    url.openConnection();
            cc.setRequestProperty("Accept", "application/json");
            //set timeout for reading InputStream
            cc.setReadTimeout(5000);
            // set timeout for connection
            cc.setConnectTimeout(5000);
            //set HTTP method to GET
            cc.setRequestMethod("GET");
            //set it to true as we are connecting for input
            cc.setDoInput(true);

            //reading HTTP response code
            int response = cc.getResponseCode();

            //if response code is 200 / OK then read Inputstream
            if (response == HttpURLConnection.HTTP_OK) {
                DataInputStream = cc.getInputStream();
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in GetData", e);
        }
        return DataInputStream;
    }

    InputStream ByPostMethod(String ServerURL) {
        Connection.Response res = null;
        Document doc = null;
        String hidden_token = "";

        try {
            res = Jsoup.connect(ServerURL).method(Connection.Method.GET).execute();
            doc = res.parse();
            org.jsoup.nodes.Element el = doc.select("input[name=__RequestVerificationToken]").first();
            hidden_token = el.attr("value");
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream DataInputStream = null;
        try {

            res = Jsoup.connect(ServerURL)
                    .header("Accept", "application/json")
                    .cookies(res.cookies())
                    .data("__RequestVerificationToken", hidden_token)
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .execute();

            //Getting HTTP response code
            int response = res.statusCode();

            //if response code is 200 / OK then read Inputstream
            //HttpURLConnection.HTTP_OK is equal to 200
            if (response == HttpURLConnection.HTTP_OK) {
                DataInputStream = res.bodyStream();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in GetData", e);
        }
        return DataInputStream;
    }

    String ConvertStreamToString(InputStream stream) {

        InputStreamReader isr = new InputStreamReader(stream);
        BufferedReader reader = new BufferedReader(isr);
        StringBuilder response = new StringBuilder();

        String line = null;
        try {

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error in ConvertStreamToString", e);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in ConvertStreamToString", e);
        } finally {

            try {
                stream.close();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error in ConvertStreamToString", e);

            } catch (Exception e) {
                Log.e(LOG_TAG, "Error in ConvertStreamToString", e);
            }
        }
        return response.toString();


    }

    public void DisplayMessage(String a) {

        TxtResult = (TextView) findViewById(R.id.response);
        TxtResult.setText(a);
    }

    private class MakeNetworkCall extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DisplayMessage("Please Wait ...");
        }

        @Override
        protected String doInBackground(String... arg) {

            InputStream is = null;
            String URL = arg[0];
            Log.d(LOG_TAG, "URL: " + URL);
            String res = "";


            if (arg[1].equals("Post")) {

                is = ByPostMethod(URL);

            } else {

                is = ByGetMethod(URL);
            }
            if (is != null) {
                res = ConvertStreamToString(is);
            } else {
                res = "Something went wrong";
            }
            return res;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            DisplayMessage(result);
            Log.d(LOG_TAG, "Result: " + result);
        }
    }
}
