package com.vibeviroma.esign.objects;

import java.io.Serializable;

public class Signature implements Serializable {
    private String key, lien_image, local_image, type, fromUserId;
    private long time;

    public Signature(String key, String lien_image, String local_image, String type, String fromUserId, long time) {
        this.key = key;
        this.lien_image = lien_image;
        this.local_image = local_image;
        this.type = type;
        this.fromUserId = fromUserId;
        this.time = time;
    }

    public Signature() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLien_image() {
        return lien_image;
    }

    public void setLien_image(String lien_image) {
        this.lien_image = lien_image;
    }

    public String getLocal_image() {
        return local_image;
    }

    public void setLocal_image(String local_image) {
        this.local_image = local_image;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
