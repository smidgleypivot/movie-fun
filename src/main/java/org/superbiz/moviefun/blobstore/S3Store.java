package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.springframework.stereotype.Component;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.IOException;
import java.util.Optional;

public class S3Store implements BlobStore {

    private final AmazonS3Client amazonS3Client;
    private final String photoStorageBucket;


    public S3Store(AmazonS3Client amazonS3Client, String photoStorageBucket) {
        this.amazonS3Client = amazonS3Client;
        this.photoStorageBucket = photoStorageBucket;
    }

    @Override
    public void put(Blob blob) throws IOException {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(blob.contentType);
        this.amazonS3Client.putObject(photoStorageBucket,blob.name,blob.inputStream,objectMetadata);

    }

    @Override
    public Optional<Blob> get(String name) throws IOException {

        try {
            S3Object imageObject = this.amazonS3Client.getObject(photoStorageBucket, name);
            S3ObjectInputStream objectContent = imageObject.getObjectContent();
            String contentType = imageObject.getObjectMetadata().getContentType();
            Blob blob = new Blob(name, objectContent, contentType);
            return Optional.of(blob);
        }catch  ( Exception nameE) {
            return Optional.empty();
        }

    }

    @Override
    public void deleteAll() {

    }
}