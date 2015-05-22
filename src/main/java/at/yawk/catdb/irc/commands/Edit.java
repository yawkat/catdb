package at.yawk.catdb.irc.commands;

import at.yawk.catdb.db.Database;
import at.yawk.catdb.db.Image;
import at.yawk.catdb.irc.CommandHandler;
import at.yawk.catdb.irc.Permission;
import at.yawk.catdb.irc.Request;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import java.net.MalformedURLException;
import java.net.URL;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yawkat
 */
@Component
class Edit {
    static final Splitter SPACE_SPLITTER = Splitter.on(' ').omitEmptyStrings();

    @Autowired Database database;

    @Permission("add")
    @CommandHandler("(save|store|add) (\\S+)(( \\w+)*)")
    String store(String ignored, String url, String tags) throws MalformedURLException {
        URL parsedUrl = URLParser.parse(url);

        return store(tags, parsedUrl);
    }

    @Permission("add")
    @CommandHandler("(save|store|add) \\^(( \\w+)*)")
    String storeLastUrl(Request request, String ignored, String tags) throws MalformedURLException {
        return store(tags, request.getChannel().getData().getLastSeenUrl());
    }

    @NotNull
    private String store(String tags, URL parsedUrl) {
        Image image = new Image();
        image.setUrl(parsedUrl);
        image.setTags(Sets.newHashSet(SPACE_SPLITTER.split(tags)));
        database.storeImage(image);

        // id populated by database
        return "Stored item #" + image.getId();
    }
}
