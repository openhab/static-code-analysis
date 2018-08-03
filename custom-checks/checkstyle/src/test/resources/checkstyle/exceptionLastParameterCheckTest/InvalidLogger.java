import java.math.BigDecimal;
import java.math.RoundingMode;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import java.lang.annotation.AnnotationTypeMismatchException;

class InvalidLogger extends AbstractNormalizer {

    @Override
    public Object doNormalize(Object value) {
        try {
            // empty for the purpose of the test
        } catch (ArithmeticException | NumberFormatException e) {
            logger.trace("\"{}\" is not a valid integer number.", e, value);
            return value;
        }
        Logger().trace("Class \"{}\" cannot be converted to an integer number.", value.getClass().getName());
        return value;
    }
}
