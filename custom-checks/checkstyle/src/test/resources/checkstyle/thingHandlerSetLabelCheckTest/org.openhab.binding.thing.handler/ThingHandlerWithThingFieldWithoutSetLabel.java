import java.net.Socket;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;

public class ThingHandlerWithThingFieldWithoutSetLabel extends BaseThingHandler {

    public ThingHandlerWithThingFieldWithoutSetLabel(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, Command command) {
        // Empty - for the purpose of the test
    }

    public void socketDidInitialisation(Socket socket) {
        thing.getStatus();
    }
}
