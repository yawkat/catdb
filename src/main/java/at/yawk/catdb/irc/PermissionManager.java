package at.yawk.catdb.irc;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * @author yawkat
 */
@Component
public class PermissionManager {
    private static final String DEFAULT_USER = "";

    private Map<String, List<Pattern>> permissions;

    @PostConstruct
    private void load() throws IOException {
        Path permissionFile = Paths.get("permissions.json");
        try (JsonReader reader = new JsonReader(Files.newBufferedReader(permissionFile))) {
            reader.beginObject();
            while (reader.peek() != JsonToken.END_OBJECT) {
                String name = reader.nextName();
                permissions.put(name, new ArrayList<>());
                if (reader.peek() == JsonToken.BEGIN_ARRAY) {
                    reader.beginArray();
                    while (reader.peek() != JsonToken.END_ARRAY) {
                        permissions.get(name).add(Pattern.compile(reader.nextString()));
                    }
                    reader.endArray();
                } else {
                    permissions.get(name).add(Pattern.compile(reader.nextString()));
                }
            }
            reader.endObject();
        }
    }

    public boolean hasPermission(String user, String permission) {
        for (Pattern pattern : permissions.get(user)) {
            if (pattern.matcher(permission).matches()) {
                return true;
            }
        }
        if (!user.equals(DEFAULT_USER)) {
            return hasPermission(DEFAULT_USER, permission);
        } else {
            return false;
        }
    }
}
