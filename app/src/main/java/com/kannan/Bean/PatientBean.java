package com.kannan.Bean;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mldof on 3/3/2016.
 */
public class PatientBean implements Serializable {
    private int Id;
    private String name;
    private int age;
    private String sex;
    private List<AccelEntryBean> history;

    public PatientBean(int id, String name, int age, String sex) {
        this.Id = id;
        this.name = name;
        this.age = age;
        this.sex = sex;
    }

    public List<AccelEntryBean> getHistory() {
        return history;
    }

    /** GETTERS AND SETTERS **/

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

}
