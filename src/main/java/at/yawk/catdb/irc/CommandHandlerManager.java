package at.yawk.catdb.irc;

import at.yawk.catdb.db.Image;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yawkat
 */
@Component
@Slf4j
class CommandHandlerManager {
    @Autowired List<Object> beans = Collections.emptyList();
    @Autowired CommandManager commandManager;
    @Autowired PermissionManager permissionManager;

    @PostConstruct
    private void register() {
        for (Object bean : beans) {
            for (Method method : bean.getClass().getDeclaredMethods()) {
                CommandHandler[] commandHandlers = method.getAnnotationsByType(CommandHandler.class);
                if (commandHandlers.length > 0) {
                    Permission[] permissions = method.getAnnotationsByType(Permission.class);
                    method.setAccessible(true);
                    MethodHandle handle;
                    try {
                        handle = MethodHandles.lookup()
                                .unreflect(method)
                                .bindTo(bean);
                    } catch (IllegalAccessException e) {
                        log.error("Failed to bind command handler", e);
                        continue;
                    }
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    List<Pattern> patterns = Arrays.stream(commandHandlers)
                            .map(h -> Pattern.compile(h.value()))
                            .collect(Collectors.toList());
                    CommandManager.Handler handler = req -> {
                        Matcher matcher = null;
                        for (Pattern pattern : patterns) {
                            matcher = pattern.matcher(req.getCommand());
                            if (matcher.matches()) {
                                break; // found match
                            }
                            matcher = null;
                        }
                        if (matcher == null) {
                            return false;
                        }
                        req = req.withMatchResult(matcher);
                        for (Permission permission : permissions) {
                            if (!permissionManager.hasPermission(req.getSender(), permission.value())) {
                                return false;
                            }
                        }
                        List<Object> ivkArgs = new ArrayList<>();
                        int group = 1;
                        for (Class<?> parameterType : parameterTypes) {
                            if (parameterType == Request.class) {
                                ivkArgs.add(req);
                            } else if (parameterType == String.class) {
                                ivkArgs.add(req.getMatchResult().group(group++));
                            }
                        }
                        try {
                            Object returnValue = handle.invokeWithArguments(ivkArgs);
                            if (returnValue != null) {
                                req.getChannel().send(String.valueOf(returnValue));
                                if (returnValue instanceof Image) {
                                    req.getChannel().getData().setLastShownImage((Image) returnValue);
                                }
                            }
                        } catch (Throwable e) {
                            log.warn("Failed to execute handler", e);
                            req.getChannel().send(e.getClass().getName() + ": " + e.getMessage());
                        }
                        return true;
                    };
                    commandManager.requestHandlers.add(handler);
                }
            }
        }
    }
}
