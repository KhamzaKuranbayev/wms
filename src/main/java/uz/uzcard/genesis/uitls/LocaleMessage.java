package uz.uzcard.genesis.uitls;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * @author Javohir Elmurodov
 * @created 30/11/2020 - 4:10 PM
 * @project to-do
 */

@Service
public class LocaleMessage {
    private final MessageSource messageSource;
    private final Locale locale;

    public LocaleMessage(@Value("${locale}") String locale, MessageSource messageSource) {
        this.locale = Locale.forLanguageTag(locale);
        this.messageSource = messageSource;
    }


    public String getMessage(String message, String localeTag, Object... args) {
        if (localeTag != null && !localeTag.isEmpty()) {
            return messageSource.getMessage(message, args, Locale.forLanguageTag(localeTag));
        }
        return messageSource.getMessage(message, args, locale);
    }

}
