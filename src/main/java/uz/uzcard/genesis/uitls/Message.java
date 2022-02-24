package uz.uzcard.genesis.uitls;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import uz.uzcard.genesis.config.ApplicationContextProvider;

import java.util.Locale;

public class Message extends ReloadableResourceBundleMessageSource implements MessageSource {
    private static Message target;

    public Message() {
        setBasename("classpath:Message");
        setCacheSeconds(5);
        setDefaultEncoding("UTF-8");
    }

    public static Message get() {
        if (target == null)
            target = ApplicationContextProvider.applicationContext.getBean(Message.class);
        return target;
    }

    public String localize(String message, Object... args) {
        return getMessage(message, args, Locale.forLanguageTag(SessionUtils.getInstance().getLanguage()));
    }

    public String localize2(String codeMessage, String defaultText) {
        Locale userLocale = Locale.forLanguageTag(SessionUtils.getInstance().getLanguage());
        if (userLocale != null && userLocale.getLanguage() != null &&
                !userLocale.getLanguage().equalsIgnoreCase(SessionUtils.getInstance().getLanguage()) &&
                defaultText != null) {
            return defaultText;
        }
        return getMessage(codeMessage, null, defaultText, userLocale);
    }
}