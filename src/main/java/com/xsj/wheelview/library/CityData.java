package com.xsj.wheelview.library;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/11/22.
 */
public class CityData implements Serializable {

    //"id": 1,"old_id": "CIT_10001","state_name": "北京","name": "东城区","state_code": 11,"city_code": 1
    private int id;
    private String old_id;
    private String state_name;
    private String name;
    private int state_code;
    private int city_code;

    public CityData() {
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOld_id() {
        return old_id;
    }

    public void setOld_id(String old_id) {
        this.old_id = old_id;
    }

    public String getState_name() {
        return state_name;
    }

    public void setState_name(String state_name) {
        this.state_name = state_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getState_code() {
        return state_code;
    }

    public void setState_code(int state_code) {
        this.state_code = state_code;
    }

    public int getCity_code() {
        return city_code;
    }

    public void setCity_code(int city_code) {
        this.city_code = city_code;
    }
}
