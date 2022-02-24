package uz.uzcard.genesis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import uz.uzcard.genesis.uitls.Message;

import java.util.Collections;
import java.util.Locale;
import java.util.Properties;

@Configuration
@ComponentScan(value = {"uz.uzcard.genesis.controller", "uz.uzcard.genesis.service",
        "uz.uzcard.genesis.jwt", "uz.uzcard.genesis.filter"})
@NoRepositoryBean
public class SpringConfig {

    private static final String dateFormat = "yyyy-MM-dd";
    private static final String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
    private final long MAX_AGE_SECS = 3600;
    @Autowired
    private Environment environment;

    @Lazy(value = false)
    @Bean
    public ApplicationContextProvider applicationContextProvider() {
        return new ApplicationContextProvider();
    }

    @Bean(name = "velocityEngine")
    public VelocityEngineFactoryBean velocityEngineFactoryBean() {
        VelocityEngineFactoryBean engineFactory = new VelocityEngineFactoryBean();
        Properties properties = new Properties();
        properties.put("velocimacro.library.autoreload", "false");
        properties.put("file.resource.loader.cache", "true");
        properties.put("file.resource.loader.modificationCheckInterval", "-1");
        properties.put("parser.pool.size", "1000");
        properties.put("resource.loader", "class");
        properties.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        engineFactory.setVelocityProperties(properties);
        return engineFactory;
    }

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        multipartResolver.setMaxUploadSize(200 * 1024 * 1024);
        multipartResolver.setResolveLazily(true);
        multipartResolver.setDefaultEncoding("UTF-8");
        return multipartResolver;
    }

    @Bean
    public View jsonTemplate() {
        MappingJackson2JsonView view = new MappingJackson2JsonView();
        view.setPrettyPrint(true);
        return view;
    }

    @Bean
    public HttpMessageConverters customConverters() {
        return new HttpMessageConverters(false, Collections.singleton(new MappingJackson2HttpMessageConverter()));
    }

    @Bean
    public Message message() {
        Message message = new Message();
        return message;
    }

    @Lazy
    @Bean
    public SessionLocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.forLanguageTag("uz"));
        return localeResolver;
    }
}