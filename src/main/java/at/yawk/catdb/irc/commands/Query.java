package at.yawk.catdb.irc.commands;

import at.yawk.catdb.db.Database;
import at.yawk.catdb.db.Image;
import at.yawk.catdb.irc.CommandHandler;
import at.yawk.catdb.irc.Permission;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author yawkat
 */
@Component
class Query {
    @Autowired Database database;

    @Permission("query.id")
    @CommandHandler("(show )?(\\d+)")
    Image getId(String ignored, String id) {
        return database.getImage(Integer.parseInt(id));
    }

    @Permission("query.any")
    @CommandHandler("show")
    Image queryAll() {
        return select(database.listImages());
    }

    @Permission("query.tag")
    @CommandHandler("(show )?(( ?\\w)+)")
    @Order(Ordered.LOWEST_PRECEDENCE)
    Image queryTags(String ignored, String tags) {
        Collection<Image> candidates = database.listImages(Sets.newHashSet(Add.SPACE_SPLITTER.split(tags)));
        return select(candidates);
    }

    Image select(Collection<Image> candidates) {
        if (candidates.isEmpty()) {
            throw new NoSuchElementException("No results found");
        }

        int i = ThreadLocalRandom.current().nextInt(candidates.size());
        for (Image candidate : candidates) {
            if (i-- == 0) {
                return candidate;
            }
        }
        throw new AssertionError();
    }
}
