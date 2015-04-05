package at.yawk.catdb.irc;

import java.util.regex.MatchResult;

/**
 * @author yawkat
 */
public interface Request {
    MatchResult getResult();

    void sendMessage(String msg);

    ChannelData getChannelData();

    String getSenderNick();
}
