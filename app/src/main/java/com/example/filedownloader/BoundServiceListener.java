package com.example.filedownloader;

public interface BoundServiceListener {
    void sendProgress(String fileName,int fileId);
    void finishedDownloading();
}