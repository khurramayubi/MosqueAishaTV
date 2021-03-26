package com.example.mosqueaishatv;

public class SliderItem {

    // image url is used to
    // store the url of image
    private String imgUrl;

    // Constructor method.
    public SliderItem(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    // Getter method
    public String getImgUrl() {
        return imgUrl;
    }

    // Setter method
    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getDescription() {
        return 0;
    }

    public byte[] getImageUrl() {
        return null;
    }
}