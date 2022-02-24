package uz.uzcard.genesis.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import uz.uzcard.genesis.config.hibernate.PhysicalNamingStrategyImpl;
import uz.uzcard.genesis.uitls.ServerUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

@Configuration
@PropertySource(value = {"classpath:application.properties", "classpath:hibernate.properties"})
@PropertySource(name = "hibernate", value = "classpath:hibernate.properties")
@EnableTransactionManagement
public class HibernateConfig implements TransactionManagementConfigurer {
    private static final Logger log = LogManager.getLogger(HibernateConfig.class);

    @Autowired
    private Environment environment;
    @Autowired
    private DataSource dataSource;

    @Bean
    public DataSource dataSource() {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        try {
            dataSource.setDriverClass(environment.getProperty("db.driver"));
            dataSource.setJdbcUrl(environment.getProperty("db.url"));
            dataSource.setUser(environment.getProperty("db.username"));
            dataSource.setPassword(environment.getProperty("db.password"));
            dataSource.setMinPoolSize(environment.getProperty("hibernate.c3p0.min_size", Integer.class));
            dataSource.setAcquireIncrement(environment.getProperty("hibernate.c3p0.acquire_increment", Integer.class));
            dataSource.setMaxPoolSize(environment.getProperty("hibernate.c3p0.max_size", Integer.class));
            dataSource.setMaxStatements(environment.getProperty("hibernate.c3p0.max_statements", Integer.class));
        } catch (Exception e) {
        }
        return dataSource;
    }

    @Bean
    public SessionFactory sessionFactory() throws IOException {
        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource());
        sessionFactoryBean.setPackagesToScan("uz.uzcard.genesis.hibernate.entity");
        sessionFactoryBean.setPhysicalNamingStrategy(new PhysicalNamingStrategyImpl());

        Properties properties = (Properties) ((StandardEnvironment) environment).getPropertySources().get("hibernate").getSource();
        sessionFactoryBean.setHibernateProperties(properties);
        sessionFactoryBean.afterPropertiesSet();
        return sessionFactoryBean.getObject();
    }

    private PlatformTransactionManager transactionManager() {
        HibernateTransactionManager transactionManager = null;
        try {
            transactionManager = new HibernateTransactionManager(sessionFactory());
        } catch (IOException e) {
            //e.printStackTrace();
            ServerUtils.error(log, e);
        }
        return transactionManager;
    }

    // todo Liquibase ishlashi uchun
    /*@Bean
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog("classpath:/db/liquibase/changelog-master.xml");
        liquibase.setDataSource(dataSource);
        return liquibase;
    }*/

    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return transactionManager();
    }
}