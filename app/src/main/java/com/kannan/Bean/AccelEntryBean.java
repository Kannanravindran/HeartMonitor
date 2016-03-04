package com.kannan.Bean;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by mldof on 3/3/2016.
 */
public class AccelEntryBean {
    private float x;
    private float y;
    private float z;
    private Timestamp timestamp;

    public AccelEntryBean( ) {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public AccelEntryBean(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public AccelEntryBean(Timestamp timestamp, float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
    }

    public static AccelEntryBean getNewAccelEntry(float x, float y, float z) {
        AccelEntryBean newEntry = new AccelEntryBean(x, y, z);
        Date date = new Date();
        newEntry.timestamp = new Timestamp(date.getTime());
        return newEntry;
    }

    /** GETTERS AND SETTERS **/

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
