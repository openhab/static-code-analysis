import static org.openhab.binding.tesla.TeslaBindingConstants.*;

import javax.swing.undo.CannotRedoException;

import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.tesla.TeslaBindingConstants;
import org.openhab.binding.tesla.internal.protocol.TokenRequestRefreshToken;
import org.openhab.binding.tesla.internal.protocol.TokenResponse;

public class InvalidLoggerWithMultipleExceptionParameters {

    private ThingStatusDetail authenticate() {
        try {
            tokenRequest = new TokenRequestRefreshToken(token.refresh_token);
        } catch (CannotRedoException e) {
            Logger().error("An exception occurred while requesting a new token : '{}'", e, " ... ", e.getMessage());
        }
        return ThingStatusDetail.CONFIGURATION_ERROR;
    }
}
