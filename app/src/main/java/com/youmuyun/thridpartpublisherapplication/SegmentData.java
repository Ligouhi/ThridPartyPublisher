package com.youmuyun.thridpartpublisherapplication;

public class SegmentData {
    String name;
    int img_src;

    public SegmentData(String name, int img_src) {
        this.name = name;
        this.img_src = img_src;
    }

    public String getName() {
        return name;
    }

    public int getImg_src() {
        return img_src;
    }
}
