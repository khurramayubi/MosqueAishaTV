package com.example.mosqueaishatv;

import java.io.Serializable;

/*
 * Prayer class representing Prayer Object with title and time.
 */
public class Prayer implements Serializable {
    static final long serialVersionUID = 727566175075960653L;
    private String title;
    private String time;

    public Prayer() {
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Prayer{" +
                "title=" + title +
                ", time='" + time + '\'' +
                '}';
    }
}