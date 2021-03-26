package com.example.mosqueaishatv;

import java.util.ArrayList;
import java.util.List;

public final class PrayerList {
    public static final String PRAYER_CATEGORY[] = {
            "Fajr",
            "SUNRISE",
            "DHUHR",
            "ASR",
            "MAGHRIB",
            "ISHA",
    };

    private static List<Prayer> list;
    private static long count = 0;

    public static List<Prayer> getList() {
        if (list == null) {
            list = setupPrayers();
        }
        return list;
    }

    public static List<Prayer> setupPrayers() {
        list = new ArrayList<>();
        String title[] = {
                "Zeitgeist 2010_ Year in Review",
                "Google Demo Slam_ 20ft Search",
                "Introducing Gmail Blue",
                "Introducing Google Fiber to the Pole",
                "Introducing Google Nose"
        };

        String time[] = {
                "5:30","7:00","12:30","3:30","6:20","8:00"
        };


        for (int index = 0; index < title.length; ++index) {
            list.add(
                    buildMovieInfo(
                            title[index],
                            time[index]));
        }

        return list;
    }

    private static Prayer buildMovieInfo(
            String title,
            String time) {
        Prayer movie = new Prayer();
        movie.setTitle(title);
        movie.setTime(time);
        return movie;
    }
}