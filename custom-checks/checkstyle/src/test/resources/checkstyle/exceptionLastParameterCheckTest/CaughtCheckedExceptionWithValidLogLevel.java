import java.lang.reflect.InvocationTargetException;
import java.rmi.activation.ActivationException;
import java.security.acl.AclNotFoundException;

public class CaughtCheckedExceptionWithValidLogLevel {

    private <T extends CULConfig> CULHandlerInternal<T> createNewHandler(T config) throws CULDeviceException {
        try {
            // empty for the purpose of the test
        } catch (SecurityException e1) {
            logger.error(" ... ", e1);
            // checked exception
        } catch (ActivationException e1) {
            logger.error(" ... ", e1, e1.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error(" ... ", e);
            // checked exception
        } catch (InstantiationException e) {
            logger.error(" ... ", e);
        } catch (IllegalAccessException e) {
            logger.error(" ... ", e);
            // checked exception
        } catch (InvocationTargetException e) {
            logger.error(" ... ", e);
            // checked exception
        } catch (AclNotFoundException e) {
            logger.error(e.getMethod(), " ... ");
        }
    }
}
