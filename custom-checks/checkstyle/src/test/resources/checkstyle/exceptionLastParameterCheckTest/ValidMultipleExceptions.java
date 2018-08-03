import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.concurrent.Executors;

import org.openhab.binding.ebus.internal.EBusTelegram;
import org.openhab.binding.ebus.internal.connection.WorkerThreadFactory;
import javax.lang.model.UnknownEntityException;
import java.lang.invoke.WrongMethodTypeException;
import java.util.ConcurrentModificationException;
import java.awt.geom.IllegalPathStateException;

public class ValidMultipleExceptions {

    @Override
    public void run() {
        try {
            // empty for the purpose of the test
        } catch (ArithmeticException e) {
            logger.error("An IO exception has occured! Try to reconnect eBus connector ...", e);

            try {
                reconnect();
            } catch (NumberFormatException e1) {
                logger.error(e.toString(), e1);
            } catch (UnknownEntityException e1) {
                logger.error(e.toString(), e1);
            }

        } catch (WrongMethodTypeException e) {
            logger.error("eBUS telegram buffer overflow - not enough sync bytes received! Try to adjust eBus adapter.");
            inputBuffer.clear();

        } catch (ConcurrentModificationException e) {
            logger.error(e.toString(), e);
            Thread.currentThread().interrupt();
            inputBuffer.clear();

        } catch (IllegalPathStateException e) {
            logger.error(e.toString(), e);
            inputBuffer.clear();
        }
    }
}
