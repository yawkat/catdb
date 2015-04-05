package at.yawk.catdb.irc;

import at.yawk.catdb.db.Database;
import at.yawk.catdb.db.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yawkat
 */
@Component
class Rate {
    @Autowired Database database;

    @Permission("rate")
    @CommandHandler("\\^++")
    public void incrementLast(Request request) {
        modify(request.getChannelData().getLastShownImage(), +1);
    }

    @Permission("rate")
    @CommandHandler("\\^--")
    public void decrementLast(Request request) {
        modify(request.getChannelData().getLastShownImage(), -1);
    }

    @Permission("rate")
    @CommandHandler("(\\d+)++")
    public void increment(Request request, String id) {
        modify(database.getImage(Integer.parseInt(id)), +1);
    }

    @Permission("rate")
    @CommandHandler("(\\d+)--")
    public void decrement(Request request, String id) {
        modify(database.getImage(Integer.parseInt(id)), -1);
    }

    private void modify(Image image, int delta) {
        // todo: store who rated?
        image.setScore(image.getScore() + delta);
        database.storeImage(image);
    }
}
