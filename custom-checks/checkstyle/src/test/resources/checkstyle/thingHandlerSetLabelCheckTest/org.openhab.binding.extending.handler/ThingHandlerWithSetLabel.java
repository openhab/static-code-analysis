import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.test.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;

// class with duplicate name extending incorrect BaseThingHandler
public class ThingHandlerWithSetLabel extends BaseThingHandler {

    public ThingHandlerWithSetLabel(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, Command command) {
        thing.setLabel("Label");
    }
}
