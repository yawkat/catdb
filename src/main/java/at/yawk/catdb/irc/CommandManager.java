package at.yawk.catdb.irc;

import com.google.common.eventbus.Subscribe;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
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

    private ChannelData getChannelData(PircBotX bot, String channel) {
        return channelDataMap.computeIfAbsent(bot.getBotId() + "|" + channel, n -> new ChannelData());
    }

    @Subscribe
    public void message(MessageEvent<?> event) {
        if (isFromSelf(event)) { return; }

        Channel channel = new ChannelChannel(
                getChannelData(event.getBot(), "#" + event.getChannel().getName()),
                event.getChannel(),
                event.getUser()
        );

        // handle
        handleChannelMessage(event, channel);
    }

    @Subscribe
    public void message(PrivateMessageEvent<?> event) {
        if (isFromSelf(event)) { return; }

        Channel channel = new UserChannel(
                getChannelData(event.getBot(), event.getUser().getNick()),
                event.getUser()
        );

        // "katbot, ....."
        if (handleChannelMessage(event, channel)) { return; }

        // ".....", only allowed in pm

        Request request = new Request();
        request.setSender(event.getUser().getNick());
        request.setChannel(channel);
        request.setCommand(event.getMessage());
        handleCommand(request);
    }

    private boolean handleChannelMessage(GenericMessageEvent<?> event, Channel channel) {

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

        Request request = new Request();
        request.setSender(event.getUser().getNick());
        request.setChannel(channel);
        request.setCommand(command);
        handleCommand(request);
        return true;
    }

    public void handleCommand(Request request) {
        log.info("Received command {}", request);

        for (Map.Entry<Pattern, Handler> entry : requestHandlers.entrySet()) {
            log.info("Attempting match with {}", entry.getKey());
            Matcher matcher = entry.getKey().matcher(request.getCommand());
            if (matcher.matches()) {
                Request subRequest = request.withMatchResult(matcher);
                if (entry.getValue().handle(subRequest)) {
                    break;
                }
            }
        }
    }

    static interface Handler {
        boolean handle(Request request);
    }

    @RequiredArgsConstructor
    private static class UserChannel implements Channel {
        final ChannelData data;
        final User user;

        @Override
        public void send(String message) {
            user.send().message(message);
        }

        @Override
        public ChannelData getData() {
            return data;
        }
    }

    @RequiredArgsConstructor
    private static class ChannelChannel implements Channel {
        final ChannelData data;
        final org.pircbotx.Channel channel;
        final User subject;

        @Override
        public void send(String message) {
            if (subject == null) {
                channel.send().message(message);
            } else {
                channel.send().message(subject, message);
            }
        }

        @Override
        public ChannelData getData() {
            return null;
        }
    }
}
