import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.example.test.core.thing.binding.ExampleAbstractClass;
import org.eclipse.smarthome.core.types.Command;

public class ThingHandlerExtendingIncorrectClass extends ExampleAbstractClass{

    public ThingHandlerExtendingIncorrectClass(Thing thing) {
        super(thing);
    }

    @Override
    public void exampleMethod() {
        // Empty - for the purpose of the test
    }
}
