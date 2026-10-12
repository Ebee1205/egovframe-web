package com.cmm.storage;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.ObjectProvider;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class R2FileStorage implements FileStorage {
    private final ObjectProvider<S3Client> s3ClientProvider;
    private final String bucket;

    public R2FileStorage(ObjectProvider<S3Client> s3ClientProvider, String bucket) {
        this.s3ClientProvider = s3ClientProvider;
        this.bucket = R2Configuration.required(bucket, "Globals.R2.Bucket");
    }
    private String key(String value) {
        if (value == null || !value.matches("[A-Za-z0-9_-]+(?:[./][A-Za-z0-9_-]+)*") || value.contains("..")) {
            throw new IllegalArgumentException("Invalid storage key.");
        }
        return value;
    }
    @Override public String getLocation() { return "r2:" + bucket; }
    @Override public void checkConnection() {
        s3ClientProvider.getObject().headBucket(HeadBucketRequest.builder().bucket(bucket).build());
    }
    @Override public void put(String key, InputStream input, long size, String contentType) throws IOException {
        s3ClientProvider.getObject().putObject(PutObjectRequest.builder().bucket(bucket).key(key(key))
                .contentType(contentType).contentLength(size).build(), RequestBody.fromInputStream(input, size));
    }
    @Override public InputStream open(String key) throws IOException {
        return s3ClientProvider.getObject().getObject(GetObjectRequest.builder().bucket(bucket).key(key(key)).build());
    }
    @Override public void delete(String key) throws IOException {
        s3ClientProvider.getObject().deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key(key)).build());
    }
}
