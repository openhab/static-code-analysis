package checkstyle.nullAnnotationsCheckTest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class GenericsNonNullAnnotation<@Nullable U, @NonNull V> {

    public @Nullable List<@NonNull Class<? extends State>> getNullableAcceptedDataTypes() {
        return Collections.emptyList();
    }

    public @NonNull List<@NonNull Class<? extends State>> getNonNullAcceptedDataTypes() {
        return Collections.emptyList();
    }

    public <@NonNull T> T getAsType(Class<T> type, Object object) {
        return (T) object;
    }

    public U getAsTypeU(@Nullable Object object) {
        return (U) object;
    }

    public V getAsTypeV(Object object) {
        return (V) object;
    }
}
