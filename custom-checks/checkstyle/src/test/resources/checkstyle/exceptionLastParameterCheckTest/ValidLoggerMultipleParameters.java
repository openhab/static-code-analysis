import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.automation.Rule;

public class ValidLoggerMultipleParameters {

    @Override
    protected void addNewProvidedObjects(List<String> newPortfolio, List<String> previousPortfolio,
            Set<Rule> parsedObjects) {
        try {
            // empty for the purpose of the test
        } catch (IllegalArgumentException e) {
            logger.debug("Not importing rule '{}' because: {}", rule.getUID(), e.getMessage(), e);
        } catch (IllegalStateException e) {
            logger.debug("Not importing rule '{}' since the rule registry is in an invalid state: {}", rule.getUID(), e.getMessage());
        }
    }
}