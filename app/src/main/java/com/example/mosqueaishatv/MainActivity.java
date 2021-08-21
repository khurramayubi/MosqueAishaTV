package com.example.mosqueaishatv;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    TextView newsBanner;
    TextView islamicDate;
    SliderView sliderView;
    TextView[] prayerTimeTableStart = new TextView[5];
    TextView[] prayerTimeTableJamat = new TextView[5];
    TextView[] prayerTimeTableName = new TextView[5];
    TextView[] jummahKhutba = new TextView[2];
    TextView[] jummahJammat = new TextView[2];
    TextView errorBox;
    String newsList = "";
    String[] prayerNames = new String[5];
    ArrayList<SliderItem> sliderDataArrayList = new ArrayList<>();
    //MediaPlayer mp = null;

    SimpleDateFormat sdf;
    SimpleDateFormat _24HourSDF;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mp =  MediaPlayer.create(this, R.raw.test);
        newsBanner = (TextView) this.findViewById(R.id.newsBanner);
        islamicDate = (TextView) this.findViewById(R.id.islamic_date);
        sliderView = findViewById(R.id.slider);
        errorBox = this.findViewById(R.id.error_box);
        prayerNames[0] = "Fajr";
        prayerNames[1] = "Dhuhr";
        prayerNames[2] = "Asr";
        prayerNames[3] = "Maghrib";
        prayerNames[4] = "Isha";

        //Loop through to find all the textViews for each of the 5 prayers, adding them to the respective arrays
        for (int i=0; i<prayerTimeTableStart.length; i++) {
            String prayerName = prayerNames[i].toLowerCase();
            int nameId = findResourceId(prayerName);
            int startId = findResourceId(prayerName +"_start");
            int jammatId = findResourceId(prayerName+"_jammat");
            prayerTimeTableStart[i] = findViewById(startId);
            prayerTimeTableJamat[i] = findViewById(jammatId);
            prayerTimeTableName[i] = findViewById(nameId);
        }

        //Find and add the jummah textviews
        int jummahKhutba1Id = findResourceId("jummah_1_khutba");
        int jummahKhutba2Id = findResourceId("jummah_2_khutba");
        int jummahJammat1Id = findResourceId("jummah_1_jammat");
        int jummahJammat2Id = findResourceId("jummah_2_jammat");

        jummahKhutba[0] = findViewById(jummahKhutba1Id);
        jummahKhutba[1] = findViewById(jummahKhutba2Id);
        jummahJammat[0] = findViewById(jummahJammat1Id);
        jummahJammat[1] = findViewById(jummahJammat2Id);

        sdf = new SimpleDateFormat("hh:mm", Locale.CANADA);
        _24HourSDF = new SimpleDateFormat("HH:mm");

        //sdf.setTimeZone(TimeZone.getTimeZone("Canada/Eastern"));

        try {
            String currentTime = sdf.format(new Date());
            Date current = null;
            current = sdf.parse((currentTime));
            getPrayerTimes();
            getJummahTimes();
            getDate();
            getNewsItems();
            getSlideShowItems();

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
                            try {
                                String currentTime = sdf.format(new Date());
                                Date current = null;
                                current = sdf.parse((currentTime));
                                updateColors(current);
                                updateColors(current);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }
            }, sdf.parse((String) prayerTimeTableStart[0].getText()), 60000);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    /** Helper method to find the id of a resource.
     * @param name Name of the resource (i.e. the ID used in .xml Files etc.)
     * @return int Resource id. */
    public int findResourceId(String name) {
        return getResources().getIdentifier(name, "id", this.getPackageName());
    }


    /** Helper method to set the error box to visible and set the text message.
     * @param msg The message to show in the error box. */
    public void setErrorBox(String msg) {
        errorBox.setText(msg);
        errorBox.setVisibility(View.VISIBLE);
    }

    /** Gets the Islamic date from the API endpoint. */
    private void getDate() {
        MosqueAishaRestClient.get("/api/prayertimes/date", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    islamicDate.setText(response.getString("date"));
                } catch (JSONException e) {
                    setErrorBox("App failed to load date, restart app. \n Error: " + e);
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });

    }


    /** Method to update the colors of the prayers in the UI depending on the current time.
     * @param currentTime The current time. */
    public void updateColors(Date currentTime) {
        for (int i = 0; i< prayerTimeTableStart.length; i++) {
            try {
                Date prayerTime = sdf.parse((String) prayerTimeTableStart[i].getText());
                TextView prayer = prayerTimeTableName[i];
                //Check if not last prayer, so we can compare with next prayer
               /* if (currentTime.equals(prayerTime)) {
                    mp.start();
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                        }
                    });
                }*/
                if (i != prayerTimeTableStart.length - 1) {
                    //Get the next prayer time
                    Date next = sdf.parse((String) prayerTimeTableStart[i + 1].getText());
                    //If current time between two prayers, current time for that prayer, so we render it green
                    if ((currentTime.after(prayerTime) && currentTime.before(next)) || currentTime.equals(prayerTime)) {
                        prayer.setTextColor(ContextCompat.getColor(this, R.color.green));
                    }
                    //Else the time has passed so we render it red
                    else {
                        prayer.setTextColor(ContextCompat.getColor(this, R.color.crimson));
                    }
                }
                //Else Isha prayer, so just check if current time is after Isha time
                else {
                    //If current time after Isha time, we render it green
                    if (currentTime.after(prayerTime) || currentTime.equals(prayerTime)) {
                        prayer.setTextColor(ContextCompat.getColor(this, R.color.green));
                    }
                    //Else render it red
                    else {
                        prayer.setTextColor(ContextCompat.getColor(this, R.color.crimson));
                    }
                }
            } catch (ParseException e) {
                setErrorBox("App failed to change colors, restart app. \n Error: " + e);
                e.printStackTrace();
            }
        }
    }

    public void getPrayerTimes() throws JSONException {
        String location = getSharedPreferences("SharedPreferences", MODE_PRIVATE).getString("location", "niagara");
        MosqueAishaRestClient.get("/api/prayerTimes/times/regular/"+location, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    for (int i=0; i<prayerNames.length; i++) {
                        prayerTimeTableStart[i].setText((sdf.format(_24HourSDF.parse((String) response.getJSONObject(i).get("start")))));
                        if (i==3) { //If maghrib, set to the Jammat time to same as start time for Maghrib
                            prayerTimeTableJamat[i].setText((sdf.format(_24HourSDF.parse((String) response.getJSONObject(i).get("start")))));

                        }
                        else {
                            prayerTimeTableJamat[i].setText((sdf.format(_24HourSDF.parse((String) response.getJSONObject(i).get("jamat")))));
                        }
                    }
                    String currentTime = sdf.format(new Date());
                    Date current = sdf.parse((currentTime));
                    updateColors(current);

                } catch (JSONException | ParseException e) {
                    setErrorBox("App failed to load prayer times, restart app. \n Error: " + e +"\n");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    public void getJummahTimes() throws JSONException {
        String location = getSharedPreferences("SharedPreferences", MODE_PRIVATE).getString("location", "niagara");
        MosqueAishaRestClient.get("/api/prayerTimes/times/jummah/"+location, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    JSONObject respOne = response.getJSONObject(0);
                    JSONObject respTwo = response.getJSONObject(1);
                    if (respOne != null && respTwo != null) {
                        jummahJammat[0].setText( (String) (respOne.get("jammat")));
                        jummahJammat[1].setText( (String) (respTwo.get("jammat")));
                        jummahKhutba[0].setText( (String) (respOne.get("khutba")));
                        jummahKhutba[1].setText( (String) (respTwo.get("khutba")));
                    }

                } catch (JSONException e) {
                    setErrorBox("App failed to load Jummah times, restart app. \n Error: " + e+"\n");
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    /**
     * Gets the news items from the news API endpoint, adding them to between <h4> HTML tags for rendering in the marquee.
     * @throws JSONException
     */
    public void getNewsItems() throws JSONException {
        MosqueAishaRestClient.get("/api/news", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    for (int i=0; i<response.length(); i++) {
                        if (response.getString(i) != null) {
                            newsList += ("<h4>"+response.get(i).toString()+"</h4>&emsp");
                        }
                    }
                    //Set news banner properties; These should be set after the String has been generated and set, to give the marquee effect
                    newsBanner.setText(Html.fromHtml(newsList));
                    newsBanner.setSelected(true);
                    newsBanner.setSingleLine(true);
                } catch (JSONException e) {
                    setErrorBox("App failed to load news items, restart app. \n Error: " + e);
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    /**
     * Gets the items from the slideshow api endpoint.
     * @throws JSONException
     */
    public void getSlideShowItems() throws JSONException {
        MosqueAishaRestClient.get("/api/slideshow", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    for (int i=0; i<response.length(); i++) {
                        sliderDataArrayList.add(new SliderItem("https://www.mosqueaisha.ca/api/slideshow/" + response.getString(i)));
                    }
                    // Set properties for Slider
                    SliderAdapter adapter = new SliderAdapter(this, sliderDataArrayList);
                    sliderView.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);
                    sliderView.setSliderTransformAnimation(SliderAnimations.FADETRANSFORMATION);
                    sliderView.setSliderAnimationDuration(100);
                    sliderView.setSliderAdapter(adapter);
                    sliderView.setScrollTimeInSec(5);
                    sliderView.setAutoCycle(true);
                    sliderView.startAutoCycle();
                } catch (JSONException e) {
                    setErrorBox("App failed to load slideshow, restart app. \n Error: " + e);
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

}