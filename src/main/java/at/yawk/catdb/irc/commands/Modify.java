package at.yawk.catdb.irc.commands;

import at.yawk.catdb.db.Database;
import at.yawk.catdb.db.Image;
import at.yawk.catdb.irc.CommandHandler;
import at.yawk.catdb.irc.Permission;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yawkat
 */
@Component
class Modify {
    @Autowired Database database;

    @Permission("modify")
    @CommandHandler("(?:modify) (\\d+)(( [+-]\\w+)*)")
    String store(String id, String tags) throws MalformedURLException {
        Image image = database.getImage(Integer.parseInt(id));

        Set<String> newTags = new HashSet<>(image.getTags());
        for (String op : Add.SPACE_SPLITTER.split(tags)) {
            boolean add = op.charAt(0) == '+';
            String tag = op.substring(1);
            if (add) {
                if (!newTags.add(tag)) {
                    throw new NoSuchElementException("Tag already present found: '" + tag + "'");
                }
            } else {
                if (!newTags.remove(tag)) {
                    throw new NoSuchElementException("Tag not found: '" + tag + "'");
                }
            }
        }
        image.setTags(newTags);
        database.storeImage(image);

        return "Updated " + image;
    }
}
