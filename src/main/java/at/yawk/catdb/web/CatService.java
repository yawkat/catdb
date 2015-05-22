package at.yawk.catdb.web;

import at.yawk.catdb.db.Database;
import at.yawk.catdb.db.Image;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yawkat
 */
@RestController
public class CatService {
    @Autowired Database database;

    @RequestMapping("/images")
    public Collection<Image> query(@RequestParam(value = "tag", required = false) Set<String> tags) {
        return database.listImages(tags == null ? Collections.emptySet() : tags);
    }

    @RequestMapping("/images/{id}")
    public Image query(@PathVariable int id) {
        return database.getImage(id);
    }
}
