package at.yawk.catdb.irc;

/**
 * @author yawkat
 */
public interface Channel {
    void send(String message);

    ChannelData getData();
}
