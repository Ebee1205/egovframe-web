package com.cmm.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalFileStorage implements FileStorage {

    private final Path directory;

    public LocalFileStorage(Path directory) {
        this.directory = directory;
    }

    @Override
    public void checkConnection() {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to create local file storage directory: " + directory, e);
        }
        if (!Files.isDirectory(directory) || !Files.isWritable(directory)) {
            throw new IllegalStateException("Local file storage directory is not writable: " + directory);
        }
    }
}
