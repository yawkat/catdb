package at.yawk.catdb.db;

import java.net.URL;
import java.util.Set;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class Image {
    private int id;
    private URL url;
    private Set<String> tags;
    private int score;

    @Override
    public String toString() {
        return "#" + getId() + ": " + getUrl() +
               " | tags: " + String.join(", ", getTags()) +
               " | score: " + getScore();
    }
}
