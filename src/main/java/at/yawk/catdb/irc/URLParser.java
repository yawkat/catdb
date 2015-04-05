package at.yawk.catdb.irc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yawkat
 */
class URLParser {
    private static final Pattern PATTERN = Pattern.compile(
            "(https?://)?(([0-9]{1,3}\\.){3}[0-9]{1,3}|\\w+\\.\\w{2,8})(\\S+[^\\s\\.\"'])?");

    private URLParser() {}

    public static URL parse(String url) throws MalformedURLException {
        Matcher matcher = PATTERN.matcher(url);
        if (matcher.matches()) {
            return parse0(matcher);
        } else {
            throw new MalformedURLException();
        }
    }

    public static List<URL> find(String message) {
        List<URL> urls = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(message);
        while (matcher.find()) {
            try {
                urls.add(parse0(matcher));
            } catch (MalformedURLException ignored) {}
        }
        return urls;
    }

    private static URL parse0(Matcher matcher) throws MalformedURLException {
        String url = matcher.group();
        if (matcher.group(1) == null) {
            url = "http://" + url;
        }
        return new URL(url);
    }
}
