package com.cmm.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class LocalFileStorage implements FileStorage {
    private final Path directory;

    public LocalFileStorage(Path directory) { this.directory = directory.toAbsolutePath().normalize(); }

    @Override
    public String getLocation() { return "local:" + directory; }

    @Override
    public void checkConnection() {
        try {
            Files.createDirectories(directory);
            if (!Files.isDirectory(directory) || !Files.isWritable(directory)) {
                throw new IOException("Storage directory is not writable.");
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to access local file storage.", exception);
        }
    }

    private Path resolve(String key, boolean createParents) throws IOException {
        if (key == null || !key.matches("[A-Za-z0-9_-]+(?:/[A-Za-z0-9_.-]+)*")
                || key.contains("..")) { throw new IllegalArgumentException("Invalid storage key."); }
        Files.createDirectories(directory);
        Path root = directory.toRealPath();
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root) || target.equals(root)) { throw new IllegalArgumentException("Invalid storage key."); }
        Path parent = root;
        Path relative = root.relativize(target.getParent());
        for (Path segment : relative) {
            parent = parent.resolve(segment);
            if (Files.isSymbolicLink(parent)) { throw new IOException("Symbolic links are not allowed in storage paths."); }
            if (createParents && !Files.exists(parent, LinkOption.NOFOLLOW_LINKS)) {
                try { Files.createDirectory(parent); }
                catch (java.nio.file.FileAlreadyExistsException concurrentCreation) {
                    if (!Files.isDirectory(parent, LinkOption.NOFOLLOW_LINKS)) { throw concurrentCreation; }
                }
            }
            if (Files.exists(parent, LinkOption.NOFOLLOW_LINKS) && !Files.isDirectory(parent, LinkOption.NOFOLLOW_LINKS)) {
                throw new IOException("Storage parent is not a directory.");
            }
        }
        if (Files.isSymbolicLink(target)) { throw new IOException("Symbolic links are not allowed for stored files."); }
        return target;
    }

    @Override
    public void put(String key, InputStream input, long size, String contentType) throws IOException {
        Path target = resolve(key, true);
        boolean created = false;
        try {
            try (OutputStream output = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                created = true;
                byte[] buffer = new byte[8192];
                long count = 0;
                int read;
                while ((read = input.read(buffer)) != -1) {
                    count += read;
                    if (count > size) { throw new IOException("Uploaded size differs from declared size."); }
                    output.write(buffer, 0, read);
                }
                if (count != size) { throw new IOException("Uploaded size differs from declared size."); }
            }
        } catch (IOException | RuntimeException exception) {
            if (created) {
                try { Files.deleteIfExists(target); } catch (IOException cleanup) { exception.addSuppressed(cleanup); }
            }
            throw exception;
        }
    }

    @Override
    public InputStream open(String key) throws IOException {
        return Files.newInputStream(resolve(key, false), StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
    }

    @Override
    public void delete(String key) throws IOException { Files.deleteIfExists(resolve(key, false)); }
}
