package at.yawk.catdb.irc;

import at.yawk.config.document.DescribedAs;
import java.util.*;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class Config {
    List<Server> servers = Collections.singletonList(new Server());

    @Data
    public static class Server {
        @DescribedAs("Server hostname")
        String host = "irc.spi.gt";
        @DescribedAs("Server port")
        int port = 6697;
        @DescribedAs("Server password")
        String password = null;
        @DescribedAs("Username (nick)")
        String nick = "katdb";
        @DescribedAs("Display full name")
        String realName = "katdb";
        @DescribedAs("Hostname login (xyz@example.com, xyz is the login)")
        String login = "kitty";
        @DescribedAs("Channels to join")
        Set<String> channels = new HashSet<>(Arrays.asList("#thinkofcat", "#cricket"));
        @DescribedAs("Commands to run on join")
        List<String> runOnJoin = new ArrayList<>();
    }
}
