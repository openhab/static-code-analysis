import java.lang.reflect.Constructor;
import java.rmi.activation.ActivationException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class ExceptionsWithLogger {

    private <T extends CULConfig> CULHandlerInternal<T> createNewHandler(T config) throws CULDeviceException {
        try {
          //empty for the purpose of the test
        } catch (SecurityException e1) {
            logger.error(" ... ", e1.getMessage());
        } catch (NoSuchMethodException e1) {
            logger.error(" ... ", e1.getMessage());
        } catch (IllegalArgumentException e1) {
            logger.error(" ... ", e1);
        } catch (InstantiationException e1) {
            logger.error(" ... ", e1);
        } catch (IllegalAccessException e1) {
            logger.error(" ... ", e1);
        } catch (InvocationTargetException e1) {
            logger.error(" ... ", e1);
        } catch (ActivationException e1) {
            logger.error(" ... ", e1.getMethod());
        }
    }
}
