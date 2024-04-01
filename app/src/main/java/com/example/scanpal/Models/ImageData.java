package com.example.scanpal.Models;

public class ImageData {
    private String imageURL;
    private String title;
    private String description;
    private String folderPath;
    private String fileName;

    public ImageData(String image, String title, String description, String folderPath, String fileName) {
        this.imageURL = image;
        this.title = title;
        this.description = description;
        this.folderPath = folderPath;
        this.fileName = fileName;
    }

    public String getImage() {
        return imageURL;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public String getFileName() {
        return fileName;
    }
}
