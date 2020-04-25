import static org.eclipse.smarthome.binding.hue.HueBindingConstants.USER_NAME;

import org.eclipse.smarthome.config.core.Configuration;

public class CaughtUncheckedExceptionForMultipleLoggers {

    private void updateBridgeThingConfiguration(String userName) {
        Configuration config = editConfiguration();
        config.put(USER_NAME, userName);

        try {
            updateConfiguration(config);
            logger.debug("Updated configuration parameter {} to '{}'", USER_NAME, userName);
        } catch (IllegalStateException e) {
            logger.trace("Configuration update failed.", e);
            logger.warn("Unable to update configuration of Hue bridge.");
            logger.warn("Please configure the following user name manually: {}", userName);
        }
    }
}
