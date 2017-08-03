package io.heltech.design;

/**
 * Created by shadow on 03/08/17.
 */

public class Channel {

    private int id;
    private String name;
    private String stream;
    private byte[] logo;

    public Channel(int id, String name, String stream, byte[] logo) {
        this.id = id;
        this.name = name;
        this.stream = stream;
        this.logo = logo;
    }

    public byte[] getLogo() {
        return logo;
    }

    public void setLogo(byte[] logo) {
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

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }
}
