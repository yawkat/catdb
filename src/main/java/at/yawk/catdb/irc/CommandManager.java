package at.yawk.catdb.irc;

import com.google.common.eventbus.Subscribe;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.stereotype.Component;

/**
 * @author yawkat
 */
@Component
@Slf4j
class CommandManager {
    final Map<Pattern, Handler> requestHandlers = new HashMap<>();

    private final Map<String, ChannelData> channelDataMap = new HashMap<>();

    private static boolean isFromSelf(GenericMessageEvent<?> event) {
        // don't reply to self - just to be safe
        return event.getUser().equals(event.getBot().getUserBot());
    }

    private ChannelData getChannelData(String channel) {
        return channelDataMap.computeIfAbsent(channel, n -> new ChannelData());
    }

    @Subscribe
    public void message(MessageEvent<?> event) {
        if (isFromSelf(event)) { return; }

        // handle
        handleChannelMessage(
                event,
                s -> event.getChannel().send().message(event.getUser(), s),
                getChannelData('#' + event.getChannel().getName())
        );
    }

    @Subscribe
    public void message(PrivateMessageEvent<?> event) {
        if (isFromSelf(event)) { return; }

        Consumer<String> channel = s -> event.getUser().send().message(s);
        ChannelData channelData = getChannelData(event.getUser().getNick());

        // "katbot, ....."
        if (handleChannelMessage(event, channel, channelData)) { return; }

        // ".....", only allowed in pm

        handleCommand(event.getUser().getNick(), channel, channelData, event.getMessage());
    }

    private boolean handleChannelMessage(
            GenericMessageEvent<?> event,
            Consumer<String> channel,
            ChannelData channelData
    ) {

        // "katbot, ....."
        String ownNick = event.getBot().getUserBot().getNick();
        if (!event.getMessage().startsWith(ownNick)) {
            return false;
        }

        // "katbot, tell me xyz" -> "tell me xyz"
        int commandStart = event.getMessage().indexOf(' ', ownNick.length());
        if (commandStart == -1) {
            return false;
        }
        String command = event.getMessage().substring(commandStart + 1);

        handleCommand(event.getUser().getNick(), channel, channelData, command);
        return true;
    }

    private void handleCommand(
            String sender,
            Consumer<String> channel,
            ChannelData channelData,
            String command
    ) {
        log.info("Received command {}", command);

        for (Map.Entry<Pattern, Handler> entry : requestHandlers.entrySet()) {
            log.info("Attempting match with {}", entry.getKey());
            Matcher matcher = entry.getKey().matcher(command);
            if (matcher.matches()) {
                Request request = new Request() {
                    @Override
                    public MatchResult getResult() {
                        return matcher;
                    }

                    @Override
                    public void sendMessage(String msg) {
                        channel.accept(msg);
                    }

                    @Override
                    public ChannelData getChannelData() {
                        return channelData;
                    }

                    @Override
                    public String getSenderNick() {
                        return sender;
                    }
                };
                if (entry.getValue().handle(request)) {
                    break;
                }
            }
        }
    }

    static interface Handler {
        boolean handle(Request request);
    }
}
