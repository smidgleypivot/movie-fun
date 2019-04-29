package org.superbiz.moviefun.albums;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;


@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private final BlobStore blobStore;

    private final String defaultCover = "default-cover.jpg";

    public AlbumsController(AlbumsBean albumsBean, BlobStore blobstore) {
        this.albumsBean = albumsBean;
        this.blobStore = blobstore;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        blobStore.put(new Blob(format("%d", albumId), uploadedFile.getInputStream(), uploadedFile.getContentType()));
        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        Blob blob = getImageBlobOrDefault(albumId);

        byte[] bytes = IOUtils.toByteArray(blob.inputStream);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(blob.contentType));
        headers.setContentLength(bytes.length);
        return new HttpEntity<>(bytes,headers);
    }

    private Blob getImageBlobOrDefault(@PathVariable long albumId) throws IOException, URISyntaxException {
        Optional<Blob> result = blobStore.get(format("%d",albumId));
        if (!result.isPresent()) {
            URL defaultCover = this.getClass().getClassLoader().getResource("default-cover.jpg");
            Path path = Paths.get(defaultCover.toURI());
            byte[] imageBytes = readAllBytes(path);
            String contentType = new Tika().detect(path);
            InputStream inputStream = new ByteArrayInputStream(imageBytes);
            Blob defaultBlob = new Blob(format("%d",albumId), inputStream, contentType);
            return defaultBlob;
        }
        return result.get();
    }

}
