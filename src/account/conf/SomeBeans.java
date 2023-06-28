package account.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Configuration
public class SomeBeans {
    @Bean
    public ConcurrentMap<String, Integer> failures() {
        return new ConcurrentHashMap<>();
    }
}