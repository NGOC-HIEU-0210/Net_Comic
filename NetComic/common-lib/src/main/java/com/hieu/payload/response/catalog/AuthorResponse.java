package com.hieu.payload.response.catalog;

import java.util.ArrayList;
import java.util.List;

public class AuthorResponse {

    private Long id;
    private String name;
    private Boolean active;
    private String bio;


    public AuthorResponse() {
    }



    public AuthorResponse(Long id, String name, Boolean active, String bio) {
        this.id = id;
        this.name = name;
        this.active = active;
        this.bio = bio;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}