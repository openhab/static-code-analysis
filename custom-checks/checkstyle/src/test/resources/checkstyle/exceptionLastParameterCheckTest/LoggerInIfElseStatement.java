import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.openhab.binding.dsmr.internal.DSMRPort.PortState;
import org.openhab.binding.dsmr.internal.messages.OBISMessage;

public class LoggerInIfElseStatement {

    public List<OBISMessage> read() {
        try {
            // empty for the purpose of the test
        } catch (ArithmeticException ioe) {
            if (portState == PortState.CLOSED) {
                logger.info("Read aborted: DSMRPort is closed");
            } else {
                logger.warn("DSMRPort is not available anymore, closing port");
                logger.debug("Caused by:", ioe, " exception");
                close();
            }
        } catch (ArrayStoreException npe) {
            if (portState == PortState.CLOSED) {
                logger.info("Read aborted: DSMRPort is closed");
            } else {
                logger.error("Unexpected problem occured", npe.getClass(), " exception");
                close();
            }
        }
        return receivedMessages;
    }
}
