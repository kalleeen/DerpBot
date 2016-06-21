package fi.derpnet.derpbot.handler;

import fi.derpnet.derpbot.handler.impl.Echo;
import fi.derpnet.derpbot.handler.impl.LinkTitle;
import java.util.LinkedList;
import java.util.List;

public class HandlerRegistry {
    public static final List<Class> handlers = new LinkedList<>();
    static {
        handlers.add(Echo.class);
        handlers.add(LinkTitle.class);
    }
}
