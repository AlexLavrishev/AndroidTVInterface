package io.heltech.design;

/**
 * Created by shadow on 03/08/17.
 */

public class Channel {



    private int id;
    private String name;
    private String url;
    private String logo;

    public Channel(int id, String name, String url, String logo) {
        this.id = id;
        this.name = name;
        this.url = url;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

}
