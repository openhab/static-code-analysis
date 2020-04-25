import java.math.BigDecimal;

import org.apache.commons.lang.ArrayUtils;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveThermostatSetpointCommandClass.SetpointType;

public class InvalidLoggerMultipleParameters {

    public SerialMessage setMessage(int scale, SetpointType setpointType, BigDecimal setpoint) {
        logger.debug("NODE {}: Creating new message for command THERMOSTAT_SETPOINT_SET", this.getNode().getNodeId());

        try {
            // empty for the purpose of the test
        } catch (ArithmeticException e) {
            logger.error(
                    "NODE {}: Got an arithmetic exception converting value {} to a valid Z-Wave value. Ignoring THERMOSTAT_SETPOINT_SET message.",
                    e, e.getMessage(), this.getNode().getNodeId(), setpoint);
            return null;
        }
    }
}
