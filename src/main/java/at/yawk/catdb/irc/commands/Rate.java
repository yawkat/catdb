package at.yawk.catdb.irc.commands;

import at.yawk.catdb.db.Database;
import at.yawk.catdb.db.Image;
import at.yawk.catdb.irc.CommandHandler;
import at.yawk.catdb.irc.Permission;
import at.yawk.catdb.irc.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yawkat
 */
@Component
class Rate {
    @Autowired Database database;

    @Permission("rate")
    @CommandHandler("\\^\\+\\+")
    public void incrementLast(Request request) {
        rate(request, request.getChannel().getData().getLastShownImage(), +1);
    }

    @Permission("rate")
    @CommandHandler("\\^--")
    public void decrementLast(Request request) {
        rate(request, request.getChannel().getData().getLastShownImage(), -1);
    }

    @Permission("rate")
    @CommandHandler("(\\d+)\\+\\+")
    public void increment(Request request, String id) {
        rate(request, database.getImage(Integer.parseInt(id)), +1);
    }

    @Permission("rate")
    @CommandHandler("(\\d+)--")
    public void decrement(Request request, String id) {
        rate(request, database.getImage(Integer.parseInt(id)), -1);
    }

    private void rate(Request request, Image image, int delta) {
        image.getRatings().put(request.getSender(), delta);
        image.recalculateScore();
        database.storeImage(image);
    }
}
