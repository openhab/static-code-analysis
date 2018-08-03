import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import sun.awt.SunHints.Value;

public class LoggerWithMultipleParametersWithoutException {

    void setupNetworkKey(boolean useSchemeZero) {
        try {
          //empty for the purpose of the test
        } catch (IllegalStateException e) {
            logger.error("NODE " + this.getNode().getNodeId() + ": Error building derived keys", Value.get(5, 10));
            keyException = e;
        }
    }
}
