package com.myapp.project;

import java.io.Serializable;

public class Document implements Serializable {

    private long id;
    private String name;
    private String description;
    private String filePath;
    private String fileType; // PDF, TXT, IMAGE
    private String tags;

    // THÊM FIELD NÀY
    private String extractedText;

    private long createdDate;
    private long lastModified;

    public Document() {
        this.createdDate = System.currentTimeMillis();
        this.lastModified = System.currentTimeMillis();
    }

    // Constructor chuẩn 9 tham số để DatabaseHelper gọi
    public Document(long id, String name, String description, String filePath,
                    String fileType, String tags, String extractedText,
                    long createdDate, long lastModified) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.filePath = filePath;
        this.fileType = fileType;
        this.tags = tags;
        this.extractedText = extractedText;
        this.createdDate = createdDate;
        this.lastModified = lastModified;
    }

    // ===== GETTERS / SETTERS =====

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }

    public long getCreatedDate() { return createdDate; }
    public void setCreatedDate(long createdDate) { this.createdDate = createdDate; }

    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }
}
