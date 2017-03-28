package com.renderedtext.pizzachallenge;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.android.volley.request.MultiPartRequest;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.Group;
import statics.Constants;
import utility.Enums;
import models.Pizza;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Generate result JSON data based on fetched JSON data
    private Button generateFileButton;
    private Button submitFileButton;

    // Request for fetching JSON
    private RequestQueue requestQueue;

    // Calculate the pizzas and set categories
    private List<Pizza> pizzas;
    private Group pizzaGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        generateFileButton = (Button) findViewById(R.id.generate_file_button);
        submitFileButton = (Button) findViewById(R.id.submit_file_button);

        requestQueue = Volley.newRequestQueue(this);
        pizzas = new ArrayList<>();
        getJsonObject(generateURL());

        submitFileButton.setEnabled(false);
        generateFileButton.setOnClickListener(this);
        submitFileButton.setOnClickListener(this);
    }

    // Return the target link
    private String generateURL() {
        return Constants.URL;
    }

    private void getJsonObject(String url) {
        // JSON object listener
        // This will fetch the corresponding JSON values
        JsonObjectRequest objectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // The root array from the link
                            JSONArray rootArray = response.getJSONArray("pizzas");

                            // This will loop through every item in the array
                            for (int i = 0; i < rootArray.length(); i++) {

                                // Gets the entire block (row) of items per loop
                                JSONObject elementObject = rootArray.getJSONObject(i);

                                // Gets an array of names from the first entry,
                                // e.g. ["margherita","price"]...
                                JSONArray entryNames = elementObject.names();

                                // Gets exactly the first item from the entry,
                                // e.g. margherita, funghi, capricciosa...
                                String pizzaName = entryNames.getString(0);

                                // Initialize the price
                                int price = 0;

                                // Add the ingredients into this array
                                List<String> ingredients = new ArrayList<>();

                                // "nil" doesn't have a "price" entry. So to avoid a crash - avoid nils!
                                if (!pizzaName.equals("nil")) {

                                    // Get ingredients and fetch the price Value
                                    price = elementObject.getInt("price");

                                    // Fetches all ingredients within entry based on pizza name
                                    JSONObject ingredientsJsonObject = elementObject.getJSONObject(pizzaName);

                                    // Holds the array of ingredients in JSON format
                                    JSONArray ingredientsJsonArray = ingredientsJsonObject.getJSONArray("ingredients");

                                    // Loop through every ingredient and add it to the String array
                                    for (int j = 0; j < ingredientsJsonArray.length(); j++) {
                                        ingredients.add(ingredientsJsonArray.getString(j));
                                    }
                                }

                                // Add to pizza class so we can separate them by category
                                Pizza pizza = new Pizza(pizzaName, price, ingredients);
                                pizzas.add(pizza);
                            }

                            // Generate pizzas by groups in JSON format
                            GenerateJSON();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("VOLLEY_FAIL", e.getMessage());
                            Toast.makeText(MainActivity.this, "Oops! Something went wrong while fetching the results.", Toast.LENGTH_SHORT).show();
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(com.android.volley.error.VolleyError error) {
                        try {
                            Log.e("VOLLEY_FAIL", error.getMessage());
                            Toast.makeText(MainActivity.this, "Whoops! Something went wrong...", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        // Add the request to the queue
        requestQueue.add(objectRequest);
    }

    // Save the file to device
    private void writeToFile(String data) {

        // This part is based on Google docs
        // Check if the permission is granted
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        Constants.REQUEST_WRITE_EXTERNAL_STORAGE);

            }
        }

        try {
            File file = new File(Environment.getExternalStorageDirectory(), "result.json");
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter printWriter = new PrintWriter(f);
            printWriter.print(data);
            printWriter.flush();
            printWriter.close();
        } catch (Exception e) {
            Toast.makeText(this, "There was an error while generating the file.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!

                } else {

                    // permission denied! Disable the functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    // Generate the file in JSON format by groups
    private void GenerateJSON() {
        Group   grp1 = new Group("Pizzas with meat"),
                grp2 = new Group("Pizzas with more than one type of cheese"),
                grp3 = new Group("Pizzas with meat and olives"),
                grp4 = new Group("Pizzas with mozzarela and mushrooms");

        for (Pizza pizza : pizzas) {
            for (Enums.Categories cat : pizza.getCategory()) {
                if (cat == Enums.Categories.WITH_MEAT)                  grp1.add(pizza);
                if (cat == Enums.Categories.WITH_MORE_CHEESE)           grp2.add(pizza);
                if (cat == Enums.Categories.WITH_MOZZARELA_MUSHROOM)    grp4.add(pizza);
                if (cat == Enums.Categories.WITH_MEAT_OLIVE)            grp3.add(pizza);
            }
        }

        JSONObject rootObject = new JSONObject();
        JSONObject personalInfo = new JSONObject();
        JSONArray answersJsonArray = new JSONArray();
        JSONObject temp;

        try {
            // Set info
            personalInfo.put("full_name", "Milan Obrenovic");
            personalInfo.put("email", "mmobrenovic@gmail.com");
            personalInfo.put("code_link", "link do tvog koda");

            // Add info to the root object
            rootObject.put("personal_info", personalInfo);

            temp = new JSONObject();
            temp.put("group_1", fetchGroupAsJson(grp1));
            answersJsonArray.put(temp);

            temp = new JSONObject();
            temp.put("group_2", fetchGroupAsJson(grp2));
            answersJsonArray.put(temp);

            temp = new JSONObject();
            temp.put("group_3",fetchGroupAsJson(grp3));
            answersJsonArray.put(temp);

            temp = new JSONObject();
            temp.put("group_4",fetchGroupAsJson(grp4));
            answersJsonArray.put(temp);

            rootObject.putOpt("answer", answersJsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.v("result", rootObject.toString());

//        resultTextView.setText(rootObject.toString());
        writeToFile(rootObject.toString());
    }

    public JSONObject fetchGroupAsJson(Group grp) {
        int counter = 0;
        JSONObject rootObject = new JSONObject();
        Pizza pizza = grp.getCheapest();
        counter++;

        try {
            rootObject.put("percentage", grp.getPercentage(pizzas) + "%");
            String pizzaAndPrice = pizza.getPizzaName() + " " + pizza.getPrice() + " ";

            for(String ingredient : pizza.getIngredients()) {
                pizzaAndPrice = pizzaAndPrice + ingredient + ", ";
            }

            rootObject.put("cheapest", pizzaAndPrice);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rootObject;
    }

    // Multipart request for sending the file via HTTP POST request
    private void MakeMultiPartReq(String url) {
        MultiPartRequest request = new MultiPartRequest(
                Request.Method.POST,
                url,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        Log.e("response", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }) {

            @Override
            public int compareTo(@NonNull Object o) {
                return 0;
            }

            @Override
            protected Response parseNetworkResponse(NetworkResponse response) {
                try{
                    String json = new String(
                            response.data,
                            HttpHeaderParser.parseCharset(response.headers)
                    );

                    return Response.success(json,HttpHeaderParser.parseCacheHeaders(response));
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return super.getParams();
            }
        };

        request.addFile("result", Environment.getExternalStorageDirectory().getPath() + "/result.json");
        requestQueue.add(request);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.generate_file_button:
                Toast.makeText(MainActivity.this, "File generated successfully", Toast.LENGTH_SHORT).show();
                submitFileButton.setEnabled(true);
                break;
            case R.id.submit_file_button:
                MakeMultiPartReq(Constants.SUBMIT_LINK);
                Toast.makeText(MainActivity.this, "File sent successfully", Toast.LENGTH_SHORT).show();
                generateFileButton.setEnabled(false);
                submitFileButton.setEnabled(false);
                break;
        }
    }
}
