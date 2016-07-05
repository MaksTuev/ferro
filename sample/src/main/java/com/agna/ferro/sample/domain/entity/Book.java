package com.agna.ferro.sample.domain.entity;

/**
 * Book entity
 */
public class Book {
    private String id;
    private String name;
    private int downloadProgress;
    private String imageUrl;

    public Book(String id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.downloadProgress = -1;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isDownloaded() {
        return downloadProgress == 100;
    }

    public boolean isDownloading() {
        return downloadProgress >= 0;
    }

    public int getDownloadProgress() {
        return downloadProgress;
    }

    public void setDownloadProgress(int downloadProgress) {
        this.downloadProgress = downloadProgress;
    }
}
