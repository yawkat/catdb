package at.yawk.catdb.irc;

import com.google.common.eventbus.EventBus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLSocketFactory;
import lombok.extern.slf4j.Slf4j;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.Listener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yawkat
 */
@Component
@Slf4j
public class IRCBot implements Listener {
    @Autowired EventBus bus;

    private List<ExtBot> bots = new ArrayList<>();
    private Executor executor;
    private Config config;

    @PostConstruct
    private void loadConfig() {
        Path configPath = Paths.get("config.yml");
        at.yawk.config.Configuration configuration = at.yawk.config.Configuration.create();
        if (Files.exists(configPath)) {
            config = configuration.load(Config.class, configPath);
        } else {
            config = new Config();
            configuration.save(config, configPath);
        }
    }

    @PostConstruct
    private void launch() {
        for (int i = 0; i < config.getServers().size(); i++) {
            Config.Server server = config.getServers().get(i);
            Configuration.Builder<PircBotX> configBuilder = new Configuration.Builder<>();
            server.getChannels().forEach(configBuilder::addAutoJoinChannel);
            ExtBot bot = new ExtBot(
                    configBuilder
                            .setServer(server.getHost(), server.getPort())
                            .setAutoReconnect(false)
                            .setName(server.getNick())
                            .setRealName(server.getNick())
                            .setLogin(server.getLogin())
                            .setAutoNickChange(true)
                            .setSocketFactory(SSLSocketFactory.getDefault())
                            .buildConfiguration()
            );
            bot.getConfiguration().getListenerManager().addListener(this);
            log.info("Launching bot thread {}", bot);
            new Thread(() -> {
                try {
                    log.info("Starting bot {}.", bot);
                    bot.startBot();
                    log.info("Bot {} shut down.", bot);
                } catch (IOException | IrcException e) {
                    log.error("Error during bot processing", e);
                }
            }, "IRC bot thread #" + (i + 1)).start();
            bots.add(bot);
        }
        executor = new ThreadPoolExecutor(
                0, Runtime.getRuntime().availableProcessors() * 2,
                10, TimeUnit.SECONDS,
                new SynchronousQueue<>()
        );
    }

    @PreDestroy
    private void stop() {
        for (ExtBot bot : bots) {
            log.info("Interrupting bot {}", bot);
            bot.stopBotReconnect();
            bot.shutdown();
        }
    }

    @Override
    public void onEvent(Event event) throws Exception {
        executor.execute(() -> bus.post(event));
    }

    private class ExtBot extends PircBotX {
        public ExtBot(Configuration<? extends PircBotX> configuration) {
            super(configuration);
        }

        // make visible
        @Override
        protected void shutdown() {
            super.shutdown();
        }
    }
}
