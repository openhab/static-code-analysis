import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

import org.openhab.binding.zwave.internal.protocol.SecurityEncapsulatedSerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveDoorLockCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSecurityPayloadFrame;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSecureNonceTracker.Nonce;

public class CaughtUncheckedExceptionWithMethodCall {

    protected void sendNextMessageUsingDeviceNonce() {
        if (payloadEncapsulationQueue.isEmpty()) {
            logger.warn("NODE {}: payloadQueue was empty, returning", this.getNode().getNodeId());
            return;
        }

        ZWaveSecurityPayloadFrame securityPayload = payloadEncapsulationQueue.poll();
        if (securityPayload == null) {
            logger.warn("NODE {}: payloadQueue was empty, returning", this.getNode().getNodeId());
            return;
        }

        try {
            // empty for the purpose of the test
        } catch (ArithmeticException e) {
            logger.error("NODE {}: Error in sendNextMessageWithNonce, message not sent", e.toString(), e.getClass());
        } catch (ArrayStoreException e) {
            logger.error("NODE {}: Error in sendNextMessageWithNonce, message not sent", e);
        }
    }
}
