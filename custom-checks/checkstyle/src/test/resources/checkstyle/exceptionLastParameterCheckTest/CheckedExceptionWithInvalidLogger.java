import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.AlreadyBoundException;
import java.rmi.activation.ActivationException;
import java.io.IOException;
import java.awt.AWTException;
import java.net.MalformedURLException;
import java.util.prefs.BackingStoreException;
import java.security.acl.AclNotFoundException;
import javax.management.BadAttributeValueExpException;

import org.omg.CORBA.portable.ApplicationException;

public class CheckedExceptionWithInvalidLogger {

    @Override
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
        logger.info("Update eBus Binding configuration ...");

        try {
            // empty for the purpose of the test
            try {
                // empty for the purpose of the test
            } catch (AclNotFoundException e) {
                logger.error(e.toString(), e);
            } catch (ActivationException e) {
                logger.error(e.toString(), e);
            } catch (AlreadyBoundException e) {
                logger.error(e.toString(), e);
            } catch (ApplicationException e) {
                logger.error(e.toString(), e);
            } catch (AWTException e) {
                logger.error(e.toString(), e);
            } catch (BackingStoreException e) {
                logger.error(e.toString(), e);
            } catch (BadAttributeValueExpException e) {
                logger.error(e.toString(), e);
            }
        } catch (MalformedURLException e) {
            logger.error(e, e.toString());
        } catch (IOException e) {
            throw new ConfigurationException("general", e.toString(), e);
        }
    }
}
