package org.superbiz.moviefun.FileStore;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.stereotype.Component;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Optional;

import static java.lang.String.format;

@Component
public class FileStore implements BlobStore {
    private final String fileStoreName;

    public FileStore(String fileStoreName) {
        this.fileStoreName = fileStoreName;
    }

    @Override
    public void put(Blob blob) throws IOException {
        saveUploadToFile(blob, getFileForName(blob.name));
    }


    @Override
    public Optional<Blob> get(String name) throws IOException {
        Optional<Path> pathResult = getExistingPath(name);
        System.out.println(pathResult.toString());
        if (pathResult.isPresent()) {
            FileInputStream fileInputStream = new FileInputStream(pathResult.get().toFile());
            String contentType = new Tika().detect(pathResult.get());
            Blob blob = new Blob(name, fileInputStream, contentType);
            return Optional.of(blob);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void deleteAll() {
        // ...
    }

    private Optional<Path> getExistingPath(String name) {
        File file = getFileForName(name);
        Path path;

        if (file.exists()) {
            path = file.toPath();
            return Optional.of(path);
        } else {
            return Optional.empty();
        }
    }

    private void saveUploadToFile(Blob uploadedBlob, File targetFile) throws IOException {
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        byte[] bytes = IOUtils.toByteArray(uploadedBlob.inputStream);
        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(bytes);
        }
    }

    private File getFileForName(String name) {
        String fullName = format("%s/%s", this.fileStoreName, name);
        return new File(fullName);
    }
}