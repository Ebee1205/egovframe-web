package com.cmm.file.storage;

import java.net.URI;
import java.nio.file.Paths;
import java.time.Duration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
public class R2Configuration {

    @Bean
    public FileStorage fileStorage(
            @Value("${Globals.R2.Endpoint:}") String endpoint,
            @Value("${Globals.R2.AccessKeyId:}") String accessKeyId,
            @Value("${Globals.R2.SecretAccessKey:}") String secretAccessKey,
            @Value("${Globals.R2.Bucket:}") String bucket,
            @Value("${Globals.File.LocalPath:D:/document/files}") String localPath,
            ObjectProvider<S3Client> s3ClientProvider) {
        int configuredR2Values = configured(endpoint) + configured(accessKeyId)
                + configured(secretAccessKey) + configured(bucket);
        if (configuredR2Values == 0) {
            return new LocalFileStorage(Paths.get(required(localPath, "Globals.File.LocalPath")));
        }
        if (configuredR2Values != 4) {
            throw new IllegalStateException(
                    "Set Globals.R2.Endpoint, Globals.R2.AccessKeyId, Globals.R2.SecretAccessKey, "
                            + "and Globals.R2.Bucket together, or leave all four empty to use local storage.");
        }
        return new R2FileStorage(s3ClientProvider, bucket);
    }

    // Lazy initialization allows the existing application to start before keys are set.
    @Bean(name = "r2S3Client", destroyMethod = "close")
    @Lazy
    public S3Client r2S3Client(
            @Value("${Globals.R2.Endpoint:}") String endpoint,
            @Value("${Globals.R2.AccessKeyId:}") String accessKeyId,
            @Value("${Globals.R2.SecretAccessKey:}") String secretAccessKey) {
        URI endpointUri = URI.create(required(endpoint, "Globals.R2.Endpoint"));
        if (!"https".equalsIgnoreCase(endpointUri.getScheme())
                || endpointUri.getHost() == null || endpointUri.getUserInfo() != null
                || endpointUri.getQuery() != null || endpointUri.getFragment() != null
                || (endpointUri.getPath() != null && !endpointUri.getPath().isEmpty()
                    && !"/".equals(endpointUri.getPath()))) {
            throw new IllegalArgumentException("Globals.R2.Endpoint must be an HTTPS S3 endpoint without a bucket path.");
        }
        return S3Client.builder()
                .endpointOverride(endpointUri)
                .region(Region.of("auto"))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        required(accessKeyId, "Globals.R2.AccessKeyId"),
                        required(secretAccessKey, "Globals.R2.SecretAccessKey"))))
                .httpClientBuilder(UrlConnectionHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(5))
                        .socketTimeout(Duration.ofSeconds(15)))
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .apiCallAttemptTimeout(Duration.ofSeconds(20))
                        .apiCallTimeout(Duration.ofSeconds(30)).build())
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .chunkedEncodingEnabled(false).build())
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
    }

    static String required(String value, String propertyName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Set " + propertyName + " in config/globals.properties.");
        }
        return value.trim();
    }

    private static int configured(String value) {
        return value == null || value.trim().isEmpty() ? 0 : 1;
    }
}