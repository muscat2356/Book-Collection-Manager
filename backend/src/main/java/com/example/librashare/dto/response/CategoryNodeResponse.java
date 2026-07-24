package com.example.librashare.dto.response;

import java.util.ArrayList;
import java.util.List;

public class CategoryNodeResponse {

    private Long id;
    private String name;
    private List<CategoryNodeResponse> children = new ArrayList<>();

    public CategoryNodeResponse() {
    }
    
    public CategoryNodeResponse(Long id, String name, List<CategoryNodeResponse> children) {
        this.id = id;
        this.name = name;
        this.children = children;
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

    public List<CategoryNodeResponse> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryNodeResponse> children) {
        this.children = children;
    }

    

}
