package at.yawk.catdb.irc;

import java.util.regex.MatchResult;
import lombok.Data;
import lombok.experimental.Wither;
import org.pircbotx.PircBotX;

/**
 * @author yawkat
 */
@Data
@Wither
public class Request {
    private String sender;
    private String command;
    private Channel channel;
    private MatchResult matchResult;
    private PircBotX bot;
}
