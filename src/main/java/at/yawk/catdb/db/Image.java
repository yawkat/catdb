package at.yawk.catdb.db;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class Image {
    private int id;
    private URL url;
    private Set<String> tags = new HashSet<>();

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Image clone() {
        Image image = new Image();
        image.setId(getId());
        image.setUrl(getUrl());
        image.setTags(new HashSet<>(getTags()));
        return image;
    }

    @Override
    public String toString() {
        return "#" + getId() + ": " + getUrl() +
               " | tags: " + String.join(", ", getTags());
    }
}
