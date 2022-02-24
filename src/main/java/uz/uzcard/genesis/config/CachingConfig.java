package uz.uzcard.genesis.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@EnableCaching
@Configuration
//@EnableAutoConfiguration(exclude = ElastiCacheAutoConfiguration.class)
public class CachingConfig {

    //    @Lazy
    @Bean("customKeyGenerator")
    public KeyGenerator keyGenerator() {
        return new CustomKeyGenerator();
    }

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<Cache> caches = new ArrayList<Cache>();
        caches.add(new ConcurrentMapCache("image"));
        cacheManager.setCaches(caches);
        return cacheManager;
    }

    public class CustomKeyGenerator implements KeyGenerator {

        public Object generate(Object target, Method method, Object... params) {
            return target.getClass().getSimpleName() + "_" + method.getName() + "_" +
                    Arrays.stream(params).filter(o -> !(o instanceof HttpServletRequest || o instanceof HttpServletResponse))
                            .map(o -> "" + o)
                            .collect(Collectors.joining("_"));
        }
    }
}