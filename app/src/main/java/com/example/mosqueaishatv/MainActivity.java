package com.example.mosqueaishatv;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

/*
 * Main Activity class that loads {@link MainFragment}.
 */

//Checking Git
public class MainActivity extends Activity {
    TextView prayerBanner;
    TextView newsBanner;
    TextView islamicDate;
    SliderView sliderView;
    TextView[] prayerTimeTableStart = new TextView[5];
    TextView[] prayerTimeTableJamat = new TextView[5];
    TextView[] prayers = new TextView[5];

    String newsList = "";
    String bannerString = "";

    String[] prayerNames = new String[5];
    String[] prayerJamatTimes = new String[5];
    String[] prayerStartTimes = new String[5];
    ArrayList<SliderItem> sliderDataArrayList = new ArrayList<>();

    SimpleDateFormat sdf;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prayerBanner = (TextView) this.findViewById(R.id.prayerBanner);
        newsBanner = (TextView) this.findViewById(R.id.newsBanner);
        islamicDate = (TextView) this.findViewById(R.id.islamic_date);
        sliderView = findViewById(R.id.slider);



        prayerStartTimes[0] = "00:00";
        prayerStartTimes[1] = "00:00";
        prayerStartTimes[2] = "00:00";
        prayerStartTimes[3] = "00:00";
        prayerStartTimes[4] = "00:00";

        prayerJamatTimes[0] = "00:00";
        prayerJamatTimes[1] = "00:00";
        prayerJamatTimes[2] = "00:00";
        prayerJamatTimes[3] = "00:00";
        prayerJamatTimes[4] = "00:00";

        prayerNames[0] = "Fajr";
        prayerNames[1] = "Dhuhr";
        prayerNames[2] = "Asr";
        prayerNames[3] = "Maghrib";
        prayerNames[4] = "Isha";
        for (int i=0; i<prayerTimeTableStart.length; i++) {
            int resourceIdStart = getResources().getIdentifier(
                    prayerNames[i].toLowerCase()+"_start",
                    "id",
                    this.getPackageName());
            int resourceIdJamat = getResources().getIdentifier(
                    prayerNames[i].toLowerCase()+"_jamat",
                    "id",
                    this.getPackageName());
            int resourceIdPrayer = getResources().getIdentifier(
                    prayerNames[i].toLowerCase(),
                    "id",
                    this.getPackageName());
            prayerTimeTableStart[i] = findViewById(resourceIdStart);
            prayerTimeTableJamat[i] = findViewById(resourceIdJamat);
            prayers[i] = findViewById(resourceIdPrayer);

        }


        sdf = new SimpleDateFormat("HH:mm", Locale.CANADA);

        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Eastern"));

        try {
            getPrayerTimes();
            getDate();
            getNewsItems();
            getSlideShowItems();
            String currentTime = sdf.format(new Date());
            Date current = sdf.parse((currentTime));

        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }

        Timer timer = new Timer();
        try {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            prayerBanner.setText(Html.fromHtml(getPrayerBanner() ));
                            prayerBanner.setSelected(true);
                            prayerBanner.setSingleLine(true);
                        }
                    });

                }
            }, sdf.parse(prayerStartTimes[0]), 60000);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void getDate() {
        MosqueAishaRestClient.get("/api/prayertimes/date", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    islamicDate.setText(response.getString("date"));
                } catch (JSONException e) {
                    TextView errorBox = findViewById(R.id.error_box);
                    errorBox.setText("Api Failed to load date - Restart App");
                    errorBox.setVisibility(View.VISIBLE);
                    e.printStackTrace();                }
            }
        });

    }

    //Method to generate the complete prayer time string
    public String  getPrayerBanner() {
        String prayerTimesBanner = "";
        String currentTime = sdf.format(new Date());
        Date current = null;

        try {
            current = sdf.parse((currentTime));
            Log.d("time", currentTime);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        prayerTimesBanner+= "<h3>START TIMES:</h3>";
        prayerTimesBanner+= renderPrayerBanner(prayerStartTimes, prayerStartTimes, current);
        prayerTimesBanner += "<h3>JAMAT TIMES:</h3>";
        prayerTimesBanner+= renderPrayerBanner(prayerJamatTimes, prayerStartTimes, current);
        //renderPrayers(prayerJamatTimes, prayerStartTimes, current );

        return prayerTimesBanner;
    }

    //Helper method to render the HTML prayer Strings (name and time ) with the appropriate color
    public String renderIndividualPrayerTime(String color, String prayerName, String prayerTime) {
        return "<h4>" + prayerName + ": <font color = " + color + ">" + prayerTime + " &nbsp </font> </h4>";
    }

    //Helper method to loop over an array of prayer times and generate the Strings in a combined String
    //Use this method to loop over the Start time and Jammah time Arrays
    public String renderPrayerBanner(String[] times, String[] startTimes, Date currentTime) {
        String prayerString = "";
        for (int i = 0; i< startTimes.length; i++) {
            try {
                Date prayerTime = sdf.parse(startTimes[i]);

                //Check if not last prayer, so we can compare with next prayer
                if (i != times.length - 1) {
                    //Get the next prayer time
                    Date next = sdf.parse(startTimes[i + 1]);
                    //If current time between two prayers, current time for that prayer, so we render it green
                    if ((currentTime.after(prayerTime) && currentTime.before(next)) || currentTime.equals(prayerTime)) {
                        prayerString += renderIndividualPrayerTime("green", prayerNames[i].toUpperCase(), times[i]);
                    }
                    //Else the time has passed so we render it red
                    else {
                        prayerString += renderIndividualPrayerTime("red", prayerNames[i].toUpperCase(), times[i]);
                    }
                }

                //Else Isha prayer, so just check if current time is after Isha time
                else {
                    //If current time after Isha time, we render it green
                    if (currentTime.after(prayerTime) || currentTime.equals(prayerTime)) {
                        prayerString += renderIndividualPrayerTime("green", prayerNames[i].toUpperCase(), times[i]);
                    }
                    //Else render it red
                    else {
                        prayerString += renderIndividualPrayerTime("red", prayerNames[i].toUpperCase(), times[i]);

                    }
                }


            } catch (ParseException e) {
                TextView errorBox = findViewById(R.id.error_box);
                errorBox.setText("Api Failed to load prayer times - Restart App");
                errorBox.setVisibility(View.VISIBLE);
                e.printStackTrace();            }
        }
        return prayerString;
    }

    //Helper method to loop over an array of prayer times and generate the Strings in a combined String
    //Use this method to loop over the Start time and Jammah time Arrays
    public void renderPrayers(String[] times, String[] startTimes, Date currentTime) {
        for (int i = 0; i< startTimes.length; i++) {
            try {
                Date prayerTime = sdf.parse(startTimes[i]);

                //Check if not last prayer, so we can compare with next prayer
                Log.d("time", i+" i value");
                if (i != times.length - 1) {
                    //Get the next prayer time
                    Date next = sdf.parse(startTimes[i + 1]);
                    //If current time between two prayers, current time for that prayer, so we render it green
                    if ((currentTime.after(prayerTime) && currentTime.before(next)) || currentTime.equals(prayerTime)) {
                        prayers[i].setTextColor(ContextCompat.getColor(this, R.color.green));
                    }
                    //Else the time has passed so we render it red
                    else {
                        prayers[i].setTextColor(ContextCompat.getColor(this, R.color.crimson));
                    }
                }

                //Else Isha prayer, so just check if current time is after Isha time
                else {
                    //If current time after Isha time, we render it green
                    if (currentTime.after(prayerTime) || currentTime.equals(prayerTime)) {
                        prayers[i].setTextColor(ContextCompat.getColor(this, R.color.green));
                    }
                    //Else render it red
                    else {
                        prayers[i].setTextColor(ContextCompat.getColor(this, R.color.crimson));
                    }
                }


            } catch (ParseException e) {
                TextView errorBox = findViewById(R.id.error_box);
                errorBox.setText("Api Failed to load prayer times - Restart App");
                errorBox.setVisibility(View.VISIBLE);
                e.printStackTrace();            }
        }
    }

    public void getPrayerTimes() throws JSONException {
        String location = getSharedPreferences("SharedPreferences", MODE_PRIVATE).getString("location", "niagara");
        MosqueAishaRestClient.get("/api/prayerTimes/times/"+location, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Log.d("time", String.valueOf(response.getJSONObject(0).get("name")));
                    for (int i=0; i<prayerNames.length; i++) {
                        prayerTimeTableStart[i].setText( (String) response.getJSONObject(i).get("start"));
                        prayerTimeTableJamat[i].setText( (String) response.getJSONObject(i).get("jamat"));
                        prayerStartTimes[i] = (String) response.getJSONObject(i).get("start");
                        prayerJamatTimes[i] = (String) response.getJSONObject(i).get("jamat");
                    }
                    bannerString = getPrayerBanner();
                    //Set prayer banner properties; These should be set after the String has been generated and set, to give the marquee effect
                    prayerBanner.setText(Html.fromHtml(bannerString ));
                    prayerBanner.setSelected(true);
                    prayerBanner.setSingleLine(true);
                    String currentTime = sdf.format(new Date());
                    Date current = sdf.parse((currentTime));

                    renderPrayers(prayerJamatTimes, prayerStartTimes, current );

                } catch (JSONException | ParseException e) {
                    TextView errorBox = findViewById(R.id.error_box);
                    errorBox.setText("Api Failed to load prayer times - Restart App");
                    errorBox.setVisibility(View.VISIBLE);
                    e.printStackTrace();                }

            }
        });
    }

    public void getNewsItems() throws JSONException {
        MosqueAishaRestClient.get("/api/news", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    for (int i=0; i<response.length(); i++) {
                        newsList += ("<h4>"+response.get(i).toString()+"</h4>&emsp");
                    }

                    //Set ews banner properties; These should be set after the String has been generated and set, to give the marquee effect
                    newsBanner.setText(Html.fromHtml(newsList));
                    newsBanner.setSelected(true);
                    newsBanner.setSingleLine(true);


                } catch (JSONException e) {
                    TextView errorBox = findViewById(R.id.error_box);
                    errorBox.setText("Api Failed to load News - Restart App");
                    errorBox.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                }

            }
        });
    }

    public void getSlideShowItems() throws JSONException {
        MosqueAishaRestClient.get("/api/slideshow", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Log.d("time", String.valueOf(response));
                    for (int i=0; i<response.length(); i++) {
                        sliderDataArrayList.add(new SliderItem("https://www.mosqueaisha.ca/api/slideshow/" + response.getString(i)));
                    }
                    // Set properties for Slider
                    SliderAdapter adapter = new SliderAdapter(this, sliderDataArrayList);
                    sliderView.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);
                    sliderView.setSliderTransformAnimation(SliderAnimations.FADETRANSFORMATION);
                    sliderView.setSliderAnimationDuration(3000);
                    sliderView.setSliderAdapter(adapter);
                    sliderView.setScrollTimeInSec(6);
                    sliderView.setAutoCycle(true);
                    sliderView.startAutoCycle();


                } catch (JSONException e) {
                    TextView errorBox = findViewById(R.id.error_box);
                    errorBox.setText("Api Failed to load pictures - Restart App");
                    errorBox.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                }

            }
        });
    }

}