package com.hieu.payload.request.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BannerRequest {

    @NotBlank(message = "Đường dẫn ảnh không được để trống")
    @Size(max = 512, message = "Đường dẫn ảnh tối đa 512 ký tự")
    private String imageUrl;


    @Size(max = 512, message = "Liên kết tối đa 512 ký tự")
    private String link;


    private Boolean active;

    public BannerRequest() {
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
