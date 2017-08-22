package io.heltech.design;

import android.graphics.drawable.Drawable;
import android.media.Image;

/**
 * Created by shadow on 03/08/17.
 */

public class Channel {



    private int id;
    private String name;
    private String desc;
    private String logo;

    public Channel(int id, String name, String desc, String logo) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.logo = logo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

}
