package checkstyle.nullAnnotationsCheckTest;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class NotAnnotatedInnerClasses {

    private class InnerClass {
        
    }
    
    private class AnotherInnerClass {
        
    }
    
}
