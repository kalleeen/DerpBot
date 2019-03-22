package fi.derpnet.derpbot.handler;

import fi.derpnet.derpbot.handler.impl.*;
import java.util.LinkedList;
import java.util.List;

public class HandlerRegistry {

    public static final List<Class<? extends GenericHandler>> handlers = new LinkedList<>();

    static {
        handlers.add(Echo.class);
        handlers.add(LinkTitle.class);
        handlers.add(MacFinder.class);
        handlers.add(Help.class);
        handlers.add(GeoIp.class);
        handlers.add(CommonCtcpResponder.class);
        handlers.add(Poikkeusinfo.class);
        handlers.add(SslLabs.class);
        handlers.add(Randomizer.class);
        handlers.add(Reittiopas.class);
        handlers.add(Pvm.class);
        handlers.add(MegaHal.class);
        handlers.add(Maps.class);
        handlers.add(Sed.class);
        handlers.add(Stats.class);
    }
}
