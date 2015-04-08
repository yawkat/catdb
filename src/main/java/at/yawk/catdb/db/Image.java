package at.yawk.catdb.db;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    private Map<String, Integer> ratings = new HashMap<>();
    private int score;

    public void recalculateScore() {
        score = ratings.values().stream().mapToInt(i -> i).sum();
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Image clone() {
        Image image = new Image();
        image.setId(getId());
        image.setUrl(getUrl());
        image.setTags(new HashSet<>(getTags()));
        image.setRatings(new HashMap<>(getRatings()));
        image.setScore(getScore());
        return image;
    }

    @Override
    public String toString() {
        return "#" + getId() + ": " + getUrl() +
               " | tags: " + String.join(", ", getTags()) +
               " | score: " + getScore();
    }
}
