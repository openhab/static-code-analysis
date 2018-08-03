import java.text.ParseException;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.module.timer.factory.TimerModuleHandlerFactory;
import org.eclipse.smarthome.core.scheduler.ExpressionThreadPoolManager;
import org.eclipse.smarthome.core.scheduler.RecurrenceExpression;
import java.nio.file.DirectoryIteratorException;

public class ValidIndirectExceptionOfRuntimeException {

    public TimeOfDayTriggerHandler(Trigger module) {
        try {
          //empty for the purpose of the test
        } catch (ArrayIndexOutOfBoundsException | DirectoryIteratorException e) {
            logger.trace(e);
        }
        scheduler = ExpressionThreadPoolManager.getExpressionScheduledPool(TimerModuleHandlerFactory.THREADPOOLNAME);
    }
}
