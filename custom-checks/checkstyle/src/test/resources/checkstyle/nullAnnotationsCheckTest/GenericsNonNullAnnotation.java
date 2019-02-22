package checkstyle.nullAnnotationsCheckTest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.NonNull;

@NonNullByDefault
public class GenericsNonNullAnnotation {
    
    public @NonNull List<@NonNull Class<? extends State>> getAcceptedDataTypes() {
        return Collections.emptyList();
    }

}
