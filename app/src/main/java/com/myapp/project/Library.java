package com.myapp.project;

import java.io.Serializable;

public class Library implements Serializable {
    private long id;
    private String name;
    private String description;
    private String tags;
    private long createdDate;

    public Library() {
        this.createdDate = System.currentTimeMillis();
    }

    public Library(long id, String name, String description, String tags, long createdDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.createdDate = createdDate;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public long getCreatedDate() { return createdDate; }
    public void setCreatedDate(long createdDate) { this.createdDate = createdDate; }
}
