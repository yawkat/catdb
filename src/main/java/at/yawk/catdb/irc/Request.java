package at.yawk.catdb.irc;

import java.util.regex.MatchResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import org.pircbotx.PircBotX;

/**
 * @author yawkat
 */
@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Request {
    private String sender;
    private String command;
    private Channel channel;
    private MatchResult matchResult;
    private PircBotX bot;
}
