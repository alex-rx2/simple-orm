package simple.orm.jdbc.common;

import simple.orm.jdbc.common.builders.IndexedExtractorBuilder;
import simple.orm.jdbc.common.builders.IndexedInjectorBuilder;
import simple.orm.jdbc.common.builders.NamedExtractorByIndexBuilder;
import simple.orm.jdbc.common.builders.NamedExtractorByLabelBuilder;
import simple.orm.jdbc.common.builders.NamedInjectorBuilder;

import static simple.orm.jdbc.common.BasicGetters.BASIC_GETTERS_MAP;
import static simple.orm.jdbc.common.BasicSetters.DEFAULT_SETTERS_MAP;

/**
 * Utility class providing single point access to builders.
 */
public final class InjectorsExtractors {

    public static IndexedInjectorBuilder indexedInjector() {
        return IndexedInjectorBuilder.builder().withSetters(DEFAULT_SETTERS_MAP);
    }

    public static IndexedExtractorBuilder indexedExtractor() {
        return IndexedExtractorBuilder.builder().withGetters(BASIC_GETTERS_MAP);
    }

    public static <T> NamedInjectorBuilder<T> namedInjector() {
        return NamedInjectorBuilder.<T>builder().withSetters(DEFAULT_SETTERS_MAP);
    }

    public static <T> NamedInjectorBuilder<T> namedInjector(Class<T> sourceClass) {
        return NamedInjectorBuilder.<T>builder().withSetters(DEFAULT_SETTERS_MAP);
    }

    public static <T> NamedExtractorByIndexBuilder<T> namedExtractorByIndex() {
        return NamedExtractorByIndexBuilder.<T>builder().withGetters(BASIC_GETTERS_MAP);
    }

    public static <T> NamedExtractorByIndexBuilder<T> namedExtractorByIndex(Class<T> resultClass) {
        return NamedExtractorByIndexBuilder.<T>builder().withGetters(BASIC_GETTERS_MAP).resultClass(resultClass);
    }

    public static <T> NamedExtractorByLabelBuilder<T> namedExtractorByLabel() {
        return NamedExtractorByLabelBuilder.<T>builder().withGetters(BASIC_GETTERS_MAP);
    }

    public static <T> NamedExtractorByLabelBuilder<T> namedExtractorByLabel(Class<T> resultClass) {
        return NamedExtractorByLabelBuilder.<T>builder().withGetters(BASIC_GETTERS_MAP).resultClass(resultClass);
    }

    private InjectorsExtractors() {
    }

}
