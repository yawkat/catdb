package at.yawk.catdb;

import at.yawk.catdb.db.Database;
import at.yawk.catdb.irc.IRCBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author yawkat
 */
@Configuration
@ComponentScan(basePackageClasses = {
        Main.class,
        Database.class,
        IRCBot.class
})
@SpringBootApplication
@EnableAutoConfiguration
public class Main {
    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(Main.class, args);
    }
}
