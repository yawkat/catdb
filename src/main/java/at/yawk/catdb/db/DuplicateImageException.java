package at.yawk.catdb.db;

/**
 * @author yawkat
 */
public class DuplicateImageException extends RuntimeException {
    public DuplicateImageException(int id) {
        super("Image with same url already exists (" + id + ")");
    }
}
