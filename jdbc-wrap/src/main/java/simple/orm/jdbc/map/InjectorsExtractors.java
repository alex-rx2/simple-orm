package simple.orm.jdbc.map;

import simple.orm.jdbc.map.builders.IndexedExtractorBuilder;
import simple.orm.jdbc.map.builders.IndexedInjectorBuilder;
import simple.orm.jdbc.map.builders.NamedExtractorByIndexBuilder;
import simple.orm.jdbc.map.builders.NamedExtractorByLabelBuilder;
import simple.orm.jdbc.map.builders.NamedInjectorBuilder;

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
