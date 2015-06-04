package at.yawk.catdb.db;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author yawkat
 */
@Component
@Slf4j
class FileDatabase implements Database {
    private static final Gson GSON = new Gson();

    private Path location = Paths.get("images.json").toAbsolutePath();
    private List<Image> images;

    @PostConstruct
    private void load() throws IOException {
        if (!Files.exists(location)) {
            images = new ArrayList<>();
            return;
        }

        try (Reader in = Files.newBufferedReader(location)) {
            images = new ArrayList<>(Arrays.asList(GSON.fromJson(in, Image[].class)));
        }
    }

    private void save() {
        Path tempLoc = location.getParent().resolve(
                "." + location.getFileName() + "." + Long.toHexString(System.nanoTime())
        );
        try {
            try (Writer out = Files.newBufferedWriter(tempLoc)) {
                GSON.toJson(images, out);
            }
            Files.move(tempLoc, location, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to save database", e);
        } finally {
            try {
                Files.deleteIfExists(tempLoc);
            } catch (IOException ignored) {} // swallow
        }
    }

    private static Image copy(Image image) {
        return image.clone();
    }

    @Override
    public synchronized Image getImage(int id) {
        for (Image image : images) {
            if (image.getId() == id) {
                return copy(image);
            }
        }
        throw new NoSuchElementException(String.valueOf(id));
    }

    @Override
    public synchronized Collection<Image> listImages(Set<String> tags) {
        if (tags.isEmpty()) {
            return listImages();
        }
        return images.stream()
                .filter(image -> image.getTags().containsAll(tags))
                .map(FileDatabase::copy)
                .collect(Collectors.toSet());
    }

    @Override
    public synchronized Collection<Image> listImages() {
        return images.stream()
                .map(FileDatabase::copy)
                .collect(Collectors.toSet());
    }

    @Override
    public synchronized void storeImage(Image image) {
        boolean modified = false;
        int maxId = 0;
        for (int i = 0; i < images.size(); i++) {
            Image other = images.get(i);
            maxId = Math.max(maxId, other.getId());
            if (other.getId() == image.getId()) {
                if (other.equals(image)) {
                    // no modification
                    return;
                }
                images.set(i, image);
                modified = true;
                break;
            }
        }
        if (!modified) {
            image.setId(maxId + 1);
            images.add(image);
        }
        save();
    }

    @Override
    public synchronized void deleteImage(Image image) {
        images.remove(image);
    }
}
