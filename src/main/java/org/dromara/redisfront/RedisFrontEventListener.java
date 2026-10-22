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
            listener.forEach((ignore,eventConsumer) -> eventConsumer.forEach((key,consumer) -> consumer.accept(qsEvent)));
        }
    }

    public void bind(int id, Class<? extends QSEvent> clazz, Consumer<QSEvent> consumer) {
        String name = clazz.getName();
        listener.computeIfAbsent(id, ignored -> new EventConsumer()).putIfAbsent(name, consumer);
    }

    public void unbind(int id) {
        listener.remove(id);
    }

    private static class EventConsumer extends ConcurrentHashMap<String, Consumer<QSEvent>> { }

}
