package com.itant.dotonmap.bean;

import java.io.Serializable;

/**
 * Created by Jason on 2018/11/18.
 */

public class LocationBean implements Serializable {
    private String name;
    private String city;
    private String address;
    private double lat;
    private double lng;
    private double late6;
    private double lnge6;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLate6() {
        return late6;
    }

    public void setLate6(double late6) {
        this.late6 = late6;
    }

    public double getLnge6() {
        return lnge6;
    }

    public void setLnge6(double lnge6) {
        this.lnge6 = lnge6;
    }
}
