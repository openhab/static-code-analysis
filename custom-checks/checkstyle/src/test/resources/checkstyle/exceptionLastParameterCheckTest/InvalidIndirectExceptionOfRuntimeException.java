import java.awt.color.CMMException;
import java.awt.color.ICC_ColorSpace;

public class InvalidIndirectExceptionOfRuntimeException {

    public void exampleMethod() {
        try {
            // empty for the purpose of the test
        } catch (ClassCastException e) {
            iccCS = null;
            cbLock.lock();
            logger.trace(e, " exception caught");
            try {
                warningOccurred(WARNING_IGNORE_INVALID_ICC);
            } finally {
                cbLock.unlock();
            }
        }
    }
}
