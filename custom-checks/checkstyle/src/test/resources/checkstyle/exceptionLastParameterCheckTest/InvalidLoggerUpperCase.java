import java.util.BitSet;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.rpircswitch.internal.RPiRcSwitchBindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.model.item.binding.BindingConfigParseException;

import de.pi3g.pi.rcswitch.RCSwitch;
import javax.lang.model.type.MirroredTypesException;

public class InvalidLoggerUpperCase {

    @Override
    public void processBindingConfiguration(String context, Item item, String bindingConfig)
            throws BindingConfigParseException {
        try {
            // empty for the purpose of the test
        } catch (IndexOutOfBoundsException e) {
            LOGGER.error("The group address '" + groupAddressString
                    + "' is invalid. The group address must have 5 bits, e.g. 10101.");
        } catch (MirroredTypesException e) {
            LOGGER.error("The device address '" + deviceAddressString
                    + "' is invalid. The device address must be an Integer value, e.g. 4.", e.getClass());
        }
    }
}
