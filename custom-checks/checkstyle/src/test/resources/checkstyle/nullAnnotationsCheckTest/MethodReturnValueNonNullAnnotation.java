package checkstyle.nullAnnotationsCheckTest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.NonNull;

@NonNullByDefault
public class MethodReturnValueNonNullAnnotation {
    
    @Override
    public @NonNull StateDescription getStateDescription(Channel channel) {
        
    }

}
