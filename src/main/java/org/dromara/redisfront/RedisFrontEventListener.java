package org.dromara.redisfront;

import org.dromara.quickswing.events.QSEvent;
import org.dromara.quickswing.events.QSEventListener;
import org.dromara.redisfront.model.event.RedisFrontEvent;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class RedisFrontEventListener extends QSEventListener<RedisFrontWidget> {
    private static final Map<Integer, EventConsumer> listener = new ConcurrentHashMap<>();

    public RedisFrontEventListener(RedisFrontWidget source) {
        super(source);
        RedisFrontContext context = (RedisFrontContext) source.getContext();
        context.getEventBus().subscribe(this);
    }

    @Override
    protected void onEvent(QSEvent qsEvent) {
        if (qsEvent instanceof RedisFrontEvent redisFrontEvent) {
            if (redisFrontEvent.getRedisConnectContext() != null) {
                EventConsumer eventConsumer = listener.get(redisFrontEvent.getRedisConnectContext().getId());
                if (eventConsumer != null) {
                    eventConsumer.forEach((name, consumer) -> {
                        if (name.equals(redisFrontEvent.getClass().getName())) {
                            consumer.accept(redisFrontEvent);
                        }
                    });
                }
            }
        } else {
            listener.forEach((_, eventConsumer) -> eventConsumer.forEach((_, consumer) -> consumer.accept(qsEvent)));
        }
    }

    public void bind(int id, Class<? extends QSEvent> clazz, Consumer<QSEvent> consumer) {
        String name = clazz.getName();
        if (listener.containsKey(id)) {
            EventConsumer eventConsumer = listener.get(id);
            if (!eventConsumer.containsKey(name)) {
                eventConsumer.put(name, consumer);
            }
        } else {
            listener.put(id, new EventConsumer(name, consumer));
        }
    }

    public void unbind(int id) {
        listener.remove(id);
    }

    private static class EventConsumer extends HashMap<String, Consumer<QSEvent>> {
        public EventConsumer(String name, Consumer<QSEvent> consumer) {
            this.put(name, consumer);
        }
    }

}
