package simple.orm.jdbc.map;

/**
 * Utility class providing single point access to builders.
 */
public final class InjectorsExtractors {

    public static IndexedInjectorBuilder indexedInjector() {
        return IndexedInjectorBuilder.builder();
    }

    public static IndexedExtractorBuilder indexedExtractor() {
        return IndexedExtractorBuilder.builder();
    }

    public static <T> NamedInjectorBuilder<T> namedInjector() {
        return NamedInjectorBuilder.builder();
    }

    public static <T> NamedInjectorBuilder<T> namedInjector(Class<T> sourceClass) {
        return NamedInjectorBuilder.builder();
    }

    public static <T> NamedExtractorByIndexBuilder<T> namedExtractorByIndex() {
        return NamedExtractorByIndexBuilder.builder();
    }

    public static <T> NamedExtractorByIndexBuilder<T> namedExtractorByIndex(Class<T> resultClass) {
        return NamedExtractorByIndexBuilder.<T>builder().resultClass(resultClass);
    }

    public static <T> NamedExtractorByLabelBuilder<T> namedExtractorByLabel() {
        return NamedExtractorByLabelBuilder.builder();
    }

    public static <T> NamedExtractorByLabelBuilder<T> namedExtractorByLabel(Class<T> resultClass) {
        return NamedExtractorByLabelBuilder.<T>builder().resultClass(resultClass);
    }

    private InjectorsExtractors() {
    }

}
