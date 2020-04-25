import java.util.Map;

import java.util.NoSuchElementException;
import javax.xml.bind.TypeConstraintException;

import org.openhab.binding.mcp23017.internal.MCP23017GenericBindingProvider.MCP23017BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.model.item.binding.BindingConfigParseException;

import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;

public class InvalidLoggerWithMultipleExceptions {

    @Override
    public void processBindingConfiguration(String context, Item item, String bindingConfig) {
        try {
            // empty for the purpose of the test
        } catch (NoSuchElementException exception) {
            final String message = "Illegal argument exception in configuration string ";
            logger.error("{} '{}': {}", message, bindingConfig, exception.getMessage());
            throw new BindingConfigParseException(message + "'" + bindingConfig + "'");
        } catch (ArithmeticException exception) {
            final String message = "Illegal access exception in configuration string ";
            logger.error("{} '{}': {}", message, bindingConfig, exception.getMessage());
            throw new BindingConfigParseException(message + "'" + bindingConfig + "'");
        } catch (ArrayStoreException exception) {
            final String message = "No such field exception in configuration string ";
            logger.error("{} '{}': {}", message, bindingConfig, exception.getMessage());
            throw new BindingConfigParseException(message + "'" + bindingConfig + "'");
        } catch (TypeConstraintException exception) {
            final String message = "Security exception in configuration string ";
            logger.error("{} '{}': {}", message, bindingConfig, exception.getMessage());
            throw new BindingConfigParseException(message + "'" + bindingConfig + "'");
        }
        addBindingConfig(item, config);
    }
}
