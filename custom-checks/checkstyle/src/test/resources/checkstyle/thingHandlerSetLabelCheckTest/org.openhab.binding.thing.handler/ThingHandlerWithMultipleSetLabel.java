import java.net.Socket;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;

public class ThingHandlerWithMultipleSetLabel extends BaseThingHandler {

    public ThingHandlerWithMultipleSetLabel(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, Command command) {
        thing.setLabel("Label");
    }

    public void socketDidChangeLabel(Socket socket, String label) {
        thing.setLabel(label);
    }
}
