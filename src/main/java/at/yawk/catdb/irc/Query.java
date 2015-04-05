package at.yawk.catdb.irc;

import at.yawk.catdb.db.Database;
import at.yawk.catdb.db.Image;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
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
    Image queryTags(String ignored, String tags) {
        Collection<Image> candidates = database.listImages(Sets.newHashSet(Edit.SPACE_SPLITTER.split(tags)));
        return select(candidates);
    }

    Image select(Collection<Image> candidates) {
        if (candidates.isEmpty()) {
            throw new NoSuchElementException("No results found");
        }

        double[] accumulatedProbabilities = new double[candidates.size()];
        double totalProbability = 0;
        int i = 0;
        for (Image image : candidates) {
            double probability = 0.9 / (1 + Math.exp(-1 * 0.9 * image.getScore()) * (0.9 / 0.2 - 1)) + 0.1;
            totalProbability += probability;
            accumulatedProbabilities[i++] = totalProbability;
        }
        double selection = ThreadLocalRandom.current().nextDouble() * totalProbability;

        i = 0;
        for (Image image : candidates) {
            if (accumulatedProbabilities[i++] > selection) {
                return image;
            }
        }
        throw new AssertionError();
    }
}
