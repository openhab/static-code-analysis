import static org.apache.commons.lang.StringUtils.capitalize;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class MultipleCatchBlocksWithInvalidLogger {

    private Object invokeGetter(Field field, Object object) {
        try {
            // empty for the purpose of the test
        } catch (ArithmeticException e) {
            logger.trace(" ... ", e.getMessage(), " ... ", e.getClass(), " .... ", test());
        } catch (NumberFormatException e) {
            logger.debug("Error getting property value", e);
        } catch (SecurityException e) {
            logger.debug("Error getting property value", e);
        } catch (InvocationTargetException e) {
            logger.debug("Error getting property value", e);
        }
        return null;
    }
}
