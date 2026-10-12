package com.cmm.storage;

import java.io.IOException;
import java.io.InputStream;

/** Storage operations use keys and streams, independently of HTTP and database metadata. */
public interface FileStorage {
    void checkConnection();
    String getLocation();
    void put(String key, InputStream input, long size, String contentType) throws IOException;
    InputStream open(String key) throws IOException;
    void delete(String key) throws IOException;
}
