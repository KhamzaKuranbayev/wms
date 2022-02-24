package uz.uzcard.genesis.uitls;

import org.springframework.core.env.Environment;
import uz.uzcard.genesis.config.ApplicationContextProvider;

import java.util.UUID;

public class AttachmentUtils {
    private static Environment environment;

    public static String generateName(String name) {
        return UUID.randomUUID().toString() + "." +
                name.split("\\.")
                        [name.split("\\.").length - 1];
    }

    public static String getLink(String name) {
//        return String.format("%s%s", getEnvironment().getProperty("fileServer.host"), String.format("/api/attachment/file/%s", name));
//        return String.format("/attachment/file/%s", name);
        return name;
    }

    private static Environment getEnvironment() {
        if (environment == null) {
            environment = ApplicationContextProvider.applicationContext.getBean(Environment.class);
        }
        return environment;
    }
}
