import java.io.File;
import java.io.IOException;
import java.util.concurrent.RejectedExecutionException;
import java.lang.annotation.AnnotationTypeMismatchException;

import org.rrd4j.core.RrdDb;

public class InvalidLoggedUncheckedException {

    protected synchronized RrdDb getDB(String alias) {
        try {
            // empty for the purpose of the test
        } catch (RejectedExecutionException e) {
            logger.error("Could not create rrd4j database file '{}': {}", file.getAbsolutePath(), e.getMessage());
        } catch (AnnotationTypeMismatchException ae) {
            logger.debug("Could not create rrd4j database file '{}': {}", file.getAbsolutePath(), ae.getMessage());
        }
        return db;
    }
}
