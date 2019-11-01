package com.example.scmxpert.model;

import org.json.JSONArray;

import java.util.List;

public class WayPoint {
  private String lat;
  private String longt;
  private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLongt() {
        return longt;
    }

    public void setLongt(String longt) {
        this.longt = longt;
    }
}
