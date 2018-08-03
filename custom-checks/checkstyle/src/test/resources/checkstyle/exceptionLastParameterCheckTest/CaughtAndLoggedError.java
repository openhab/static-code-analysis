import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.ThingStatus;

public class CaughtAndLoggedError {

    @Override
    public void initialize() {
        logger.debug("Initializing handler for Pioneer AVR @{}", connection.getConnectionName());
        updateStatus(ThingStatus.ONLINE);

        // Start the status checker
        Runnable statusChecker = new Runnable() {
            @Override
            public void run() {
                try {
                    logger.debug("Checking status of AVR @{}", connection.getConnectionName());
                    checkStatus();
                } catch (LinkageError e) {
                    logger.warn(
                            "Failed to check the status for AVR @{}. If a Serial link is used to connect to the AVR, please check that the Bundle org.openhab.io.transport.serial is available. Cause: {}",
                            connection.getConnectionName(), e, e.getMessage());
                    // Stop to check the status of this AVR.
                    if (statusCheckerFuture != null) {
                        statusCheckerFuture.cancel(false);
                    }
                }
            }
        };
        statusCheckerFuture = scheduler.scheduleWithFixedDelay(statusChecker, 1, 10, TimeUnit.SECONDS);
    }
}
