public class CaughtUncheckedExceptionWithValidLogger {

    private <T extends CULConfig> CULHandlerInternal<T> createNewHandler(T config) throws CULDeviceException {
        try {
            // empty for the purpose of the test
        } catch (SecurityException e1) {
            logger.error(" ... ", e1);
        } catch (IllegalArgumentException e) {
            logger.error(" ... ", e);
        } catch (IllegalAccessException e) {
            logger.error(" ... ", e);
        }
    }
}
