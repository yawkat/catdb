package at.yawk.catdb;

import com.google.common.eventbus.EventBus;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author yawkat
 */
@Component
class EventManager {
    private final EventBus bus = new EventBus();
    @Autowired private List<Object> beans = Collections.emptyList();

    @PostConstruct
    private void register() {
        beans.forEach(bus::register);
    }

    @Bean
    EventBus eventBus() {
        return bus;
    }
}
