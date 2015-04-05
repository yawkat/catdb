package at.yawk.catdb.irc;

import at.yawk.catdb.db.Image;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
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
                    CommandManager.Handler handler = req -> {
                        for (Permission permission : permissions) {
                            if (!permissionManager.hasPermission(req.getSenderNick(), permission.value())) {
                                return false;
                            }
                        }
                        List<Object> ivkArgs = new ArrayList<>();
                        int group = 1;
                        for (Class<?> parameterType : parameterTypes) {
                            if (parameterType == Request.class) {
                                ivkArgs.add(req);
                            } else if (parameterType == String.class) {
                                ivkArgs.add(req.getResult().group(group++));
                            }
                        }
                        try {
                            Object returnValue = handle.invokeWithArguments(ivkArgs);
                            if (returnValue != null) {
                                req.sendMessage(String.valueOf(returnValue));
                                if (returnValue instanceof Image) {
                                    req.getChannelData().setLastShownImage((Image) returnValue);
                                }
                            }
                        } catch (Throwable e) {
                            log.warn("Failed to execute handler", e);
                            req.sendMessage(e.getClass().getName() + ": " + e.getMessage());
                        }
                        return true;
                    };
                    for (CommandHandler commandHandler : commandHandlers) {
                        log.info("Registering {} on {}", method, commandHandler.value());
                        commandManager.requestHandlers.put(Pattern.compile(commandHandler.value()), handler);
                    }
                }
            }
        }
    }
}
