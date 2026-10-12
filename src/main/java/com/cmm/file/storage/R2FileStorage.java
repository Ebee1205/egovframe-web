package com.cmm.file.storage;

import org.springframework.beans.factory.ObjectProvider;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

public class R2FileStorage implements FileStorage {

    private final ObjectProvider<S3Client> s3ClientProvider;
    private final String bucket;

    public R2FileStorage(ObjectProvider<S3Client> s3ClientProvider, String bucket) {
        this.s3ClientProvider = s3ClientProvider;
        this.bucket = bucket;
    }

    @Override
    public void checkConnection() {
        s3ClientProvider.getObject().headBucket(HeadBucketRequest.builder()
                .bucket(R2Configuration.required(bucket, "Globals.R2.Bucket")).build());
    }
}