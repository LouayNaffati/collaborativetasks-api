package com.example.backend.dto;

import java.util.HashSet;
import java.util.Set;

public class ProjectDto {
    private Long id;
    private String imgUrl;
    private String name;
    private String description;
    private Set<Long> collaborators = new HashSet<>();

    public ProjectDto() {
    }

    public ProjectDto(Long id, String imgUrl, String name, String description, Set<Long> collaborators) {
        this.id = id;
        this.imgUrl = imgUrl;
        this.name = name;
        this.description = description;
        this.collaborators = collaborators;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Long> getCollaborators() {
        return collaborators;
    }

    public void setCollaborators(Set<Long> collaborators) {
        this.collaborators = collaborators;
    }
}
