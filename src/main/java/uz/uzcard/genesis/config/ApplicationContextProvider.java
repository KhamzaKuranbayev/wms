package uz.uzcard.genesis.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by Virus on 19-Sep-16.
 */
public class ApplicationContextProvider implements ApplicationContextAware {
    public static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        uz.uzcard.genesis.config.ApplicationContextProvider.applicationContext = applicationContext;
    }
}
