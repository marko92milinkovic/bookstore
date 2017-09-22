package rs.bookstore.book;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

@Configuration
@ComponentScan
public class SpringConfiguration {

    @Bean("config")
    JsonObject jsonConfig() {
        File conf = new File("src/conf/config.json");
        return getConfiguration(conf);
    }


    private JsonObject getConfiguration(File config) {
        JsonObject conf = new JsonObject();
        if (config.isFile()) {
            System.out.println("Reading config file: " + config.getAbsolutePath());
            try (Scanner scanner = new Scanner(config).useDelimiter("\\A")) {
                String sconf = scanner.next();
                try {
                    conf = new JsonObject(sconf);
                } catch (DecodeException e) {
                    System.err.println("Configuration file " + sconf + " does not contain a valid JSON object");
                }
            } catch (FileNotFoundException e) {
                // Ignore it.
            }
        } else {
            System.out.println("Config file not found " + config.getAbsolutePath());
        }
        System.out.println("conf: " + conf.encodePrettily());
        return conf;
    }
}
