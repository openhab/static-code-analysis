import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;

public class ThingHandlerWithoutThingFieldWithSetLabel extends BaseThingHandler {

    private String label;

    public ThingHandlerWithoutThingFieldWithSetLabel(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, Command command) {
        setLabel("Label");
    }

    private void setLabel(String label) {
        this.label = label;
    }
}
