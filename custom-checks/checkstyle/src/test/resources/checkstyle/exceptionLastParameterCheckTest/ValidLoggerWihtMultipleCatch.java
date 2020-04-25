import java.util.UUID;

import org.openhab.action.ciscospark.internal.CiscoSparkActionService;
import org.openhab.core.scriptengine.action.ActionDoc;
import org.openhab.core.scriptengine.action.ParamDoc;

import com.ciscospark.Message;
import com.ciscospark.SparkException;

public class ValidLoggerWihtMultipleCatch {

    public static boolean sparkMessage(@ParamDoc(name = "msgTxt", text = "the Message to send") String msgTxt,
            @ParamDoc(name = "roomId", text = "the Room to which to send") String roomId) {
        try {
            UUID.fromString(roomId);
        } catch (IllegalArgumentException e) {
            logger.warn("Room id is not a UUID");
            return false;
        }

        try {
            logger.debug("Creating message");
            return true;
        } catch (IllegalStateException se) {
            logger.warn("Failed to send message.", se);
            return false;
        } catch (Exception e) {
            logger.warn("Failed to send message!", e);
            return false;
        }
    }
}
