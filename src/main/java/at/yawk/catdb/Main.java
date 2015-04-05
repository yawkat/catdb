package at.yawk.catdb;

import at.yawk.catdb.db.Database;
import at.yawk.catdb.irc.IRCBot;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author yawkat
 */
@ComponentScan(basePackageClasses = {
        Main.class,
        Database.class,
        IRCBot.class
})
public class Main {
    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(Main.class, args);
    }
}
