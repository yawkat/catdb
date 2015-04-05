package at.yawk.catdb.irc;

import com.google.common.eventbus.EventBus;
import java.io.IOException;
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

    private ExtBot bot;
    private Executor executor;

    @PostConstruct
    private void launch() {
        bot = new ExtBot(
                new Configuration.Builder<>()
                        .setServer("irc.spi.gt", 6697)
                        .setAutoReconnect(false)
                        .setName("katdb")
                        .setRealName("katdb")
                        .setLogin("kitty")
                        .setAutoNickChange(true)
                        .addAutoJoinChannel("#cricket")
                        .addAutoJoinChannel("#thinkofcat")
                        .setSocketFactory(SSLSocketFactory.getDefault())
                        .buildConfiguration()
        );
        bot.getConfiguration().getListenerManager().addListener(this);
        log.info("Launching bot thread");
        new Thread(() -> {
            try {
                log.info("Starting bot.");
                bot.startBot();
                log.info("Bot shut down.");
            } catch (IOException | IrcException e) {
                log.error("Error during bot processing", e);
            }
        }, "IRC bot thread").start();
        executor = new ThreadPoolExecutor(
                0, Runtime.getRuntime().availableProcessors() * 2,
                10, TimeUnit.SECONDS,
                new SynchronousQueue<>()
        );
    }

    @PreDestroy
    private void stop() {
        log.info("Interrupting bot");
        bot.stopBotReconnect();
        bot.shutdown();
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
