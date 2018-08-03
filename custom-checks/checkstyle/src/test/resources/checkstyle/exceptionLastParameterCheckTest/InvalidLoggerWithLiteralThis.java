import java.io.IOException;

public class InvalidLoggerWithLiteralThis {

    private synchronized void disconnect() {
        this.logger.debug("Disconnecting from bridge");

        if (this.keepAlive != null) {
            this.keepAlive.cancel(true);
        }

        if (this.keepAliveReconnect != null) {
            // This method can be called from the keepAliveReconnect thread. Make sure
            // we don't interrupt ourselves, as that may prevent the reconnection attempt.
            this.keepAliveReconnect.cancel(false);
        }

        if (this.messageSender != null) {
            this.messageSender.cancel(true);
        }

        try {
            this.session.close();
        } catch (IOException e) {
            this.logger.error("Error disconnecting", e.getClass());
        }
    }
}
