import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;

public class ThingHandlerExtendingIndirectChildOfBTH extends ThingHandlerExtendingIndirectlyBTH{

    public ThingHandlerExtendingIndirectChildOfBTH(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, Command command) {
        thing.setLabel("Label");
    }
}
