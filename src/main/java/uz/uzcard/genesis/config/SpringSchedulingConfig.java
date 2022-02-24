package uz.uzcard.genesis.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import uz.uzcard.genesis.uitls.ServerUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
public class SpringSchedulingConfig implements SchedulingConfigurer {
    private final Logger logger = LogManager.getLogger(SpringSchedulingConfig.class);

    @Bean(name = "taskScheduler", destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler taskScheduler() {
        // set properties if required
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(500);
        scheduler.setThreadNamePrefix("scheduler");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.initialize();
        scheduler.setErrorHandler(throwable -> ServerUtils.error(logger, new Exception(throwable)));
        return scheduler;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.setScheduler(taskScheduler());
    }

    @Bean(name = "telegramThreadPool", destroyMethod = "shutdownNow")
    public ExecutorService telegramThreadPool() {
        return Executors.newFixedThreadPool(20);
    }
}