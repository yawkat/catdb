package at.yawk.catdb.db;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * @author yawkat
 */
@Service
public interface Database {
    Image getImage(int id) throws NoSuchElementException;

    Collection<Image> listImages(Set<String> tags);

    Collection<Image> listImages();

    void storeImage(Image image);

    void deleteImage(Image image);
}
