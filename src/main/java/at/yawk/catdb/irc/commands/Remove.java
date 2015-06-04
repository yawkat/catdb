package at.yawk.catdb.irc.commands;

import at.yawk.catdb.db.Database;
import at.yawk.catdb.db.Image;
import at.yawk.catdb.irc.CommandHandler;
import at.yawk.catdb.irc.Permission;
import java.net.MalformedURLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yawkat
 */
@Component
class Remove {
    @Autowired Database database;

    @Permission("remove")
    @CommandHandler("(?:remove|delete) (\\d+)")
    String delete(String id) throws MalformedURLException {
        Image image = database.getImage(Integer.parseInt(id));
        database.deleteImage(image);
        return "Deleted " + image;
    }
}
