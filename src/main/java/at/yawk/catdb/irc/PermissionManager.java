package at.yawk.catdb.irc;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.annotation.PostConstruct;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author yawkat
 */
@Slf4j
@Component
public class PermissionManager {
    private ScriptEngine engine;

    @PostConstruct
    private void load() throws IOException, ScriptException {
        engine = new ScriptEngineManager()
                .getEngineFactories()
                .get(0)
                .getScriptEngine();
        try (Reader reader = Files.newBufferedReader(Paths.get("permissions.js"))) {
            engine.eval(reader);
        }
    }

    public boolean hasPermission(String user, String permission, Object context) {
        boolean accept;
        try {
            accept = (boolean) ((Invocable) engine).invokeFunction("hasPermission", user, permission, context);
        } catch (ScriptException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        if (!accept) {
            log.debug("{} was declined permission {}", user, permission);
        }
        return accept;
    }

}
