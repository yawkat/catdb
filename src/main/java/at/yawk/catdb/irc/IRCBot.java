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
    @Autowired CommandManager commandManager;

    private List<ExtBot> bots = new ArrayList<>();
    private Executor executor;

    @PostConstruct
    private void launch() {
        Path configPath = Paths.get("config.yml");
        at.yawk.config.Configuration configuration = at.yawk.config.Configuration.create();
        Config config;
        if (Files.exists(configPath)) {
            config = configuration.load(Config.class, configPath);
        } else {
            config = new Config();
            configuration.save(config, configPath);
        }

        for (int i = 0; i < config.getServers().size(); i++) {
            Config.Server server = config.getServers().get(i);
            Configuration.Builder<PircBotX> configBuilder = new Configuration.Builder<>();
            server.getChannels().forEach(configBuilder::addAutoJoinChannel);
            if (server.isSsl()) {
                configBuilder.setSocketFactory(SSLSocketFactory.getDefault());
            }
            ExtBot bot = new ExtBot(
                    configBuilder
                            .setServer(server.getHost(), server.getPort())
                            .setAutoReconnect(false)
                            .setName(server.getNick())
                            .setRealName(server.getNick())
                            .setLogin(server.getLogin())
                            .setServerPassword(server.getPassword())
                            .setAutoNickChange(true)
                            .buildConfiguration(),
                    server
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
            }, "Bot thread #" + (i + 1)).start();
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
        private final Config.Server serverConfig;

        public ExtBot(Configuration<? extends PircBotX> configuration, Config.Server serverConfig) {
            super(configuration);
            this.serverConfig = serverConfig;
        }

        // make visible
        @Override
        protected void shutdown() {
            super.shutdown();
        }

        @Override
        protected void startLineProcessing() {
            ChannelData cd = new ChannelData();
            Channel channel = new Channel() {
                @Override
                public void send(String message) {
                    log.info("CONSOLE: {}", message);
                }

                @Override
                public ChannelData getData() {
                    return cd;
                }
            };
            for (String command : serverConfig.getRunOnJoin()) {
                Request request = new Request();
                request.setCommand(command);
                request.setSender("*CONSOLE");
                request.setChannel(channel);
                request.setBot(this);
                commandManager.handleCommand(request);
            }
            super.startLineProcessing();
        }
    }
}
