import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;

public class ThingHandlerExtendingIndirectlyBTH extends ThingHandlerWithSetLabel{

    public ThingHandlerExtendingIndirectlyBTH(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, Command command) {
        thing.setLabel("Label");
    }
}
