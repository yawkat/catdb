package at.yawk.catdb.irc;

import at.yawk.catdb.db.Image;
import java.net.URL;
import java.util.NoSuchElementException;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class ChannelData {
    private Image lastShownImage;
    private URL lastSeenUrl;

    public Image getLastShownImage() {
        if (lastShownImage == null) {
            throw new NoSuchElementException("No image shown recently");
        }
        return lastShownImage;
    }

    public URL getLastSeenUrl() {
        if (lastSeenUrl == null) {
            throw new NoSuchElementException("No URL seen recently");
        }
        return lastSeenUrl;
    }
}
