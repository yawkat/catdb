package at.yawk.catdb;

import at.yawk.catdb.db.Database;
import at.yawk.catdb.db.Image;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Watchcat that periodically polls image URLs to check if they were removed.
 *
 * @author yawkat
 */
@Component
@Slf4j
class ImageDeletionWatchcat {
    private static final int MAX_MISS_COUNT = 2;
    /**
     * Interval to ping images in.
     */
    private static final long PING_INTERVAL = 1000 * 60 * 10; /* ms = 10 minutes */

    @Autowired Database database;

    /**
     * Image ID -> how many times it wasn't found
     */
    private Map<Integer, Integer> missCounts = Collections.emptyMap();

    // fixedRate will not concurrently execute so we're fine on that front
    @Scheduled(fixedRate = PING_INTERVAL)
    void poll() throws InterruptedException {
        log.info("Pinging images...");
        Map<Integer, Integer> newMissCounts = new HashMap<>();
        for (Image image : database.listImages()) {
            TimeUnit.SECONDS.sleep(10);
            try {
                if (!isFound(image.getUrl())) {
                    int k = image.getId();
                    int newMissCount = missCounts.getOrDefault(k, 0) + 1;
                    if (newMissCount > MAX_MISS_COUNT) {
                        log.info("Deleting '{}' because it exceeded {} misses", image, MAX_MISS_COUNT);
                        database.deleteImage(image);
                    } else {
                        log.info("Missed image '{}', miss count is now {}/{}", image, newMissCount, MAX_MISS_COUNT);
                        newMissCounts.put(k, newMissCount);
                    }
                }
            } catch (IOException e) {
                log.warn("Failed to ping " + image, e);
            }
        }
        this.missCounts = newMissCounts;
    }

    private static boolean isFound(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.connect();
        try {
            // standard 404
            if (connection.getResponseCode() == 404) {
                return false;
            }
            // forward to http://i.imgur.com/removed.png
            if (connection.getResponseCode() == 302 &&
                connection.getHeaderField("Location").equals("http://i.imgur.com/removed.png")) {
                return false;
            }
        } finally {
            connection.disconnect();
        }
        // no or unknown error
        return true;
    }
}
