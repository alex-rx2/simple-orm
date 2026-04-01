package simple.orm.mapping.builder;

import simple.orm.mapping.type.MappersCollection;

/**
 * Utility class providing single point access to builders.
 */
public final class InjectorsExtractors {
    private InjectorsExtractors() {
    }

    public static IndexedInjectorBuilder indexedInjector(MappersCollection mappers) {
        return IndexedInjectorBuilder.builder(mappers);
    }

    public static SimpleIndexedExtractorBuilder indexedExtractorSimple(MappersCollection mappers) {
        return SimpleIndexedExtractorBuilder.builder(mappers);
    }

    public static IndexIndexedExtractorBuilder indexedExtractorByIndex(MappersCollection mappers) {
        return IndexIndexedExtractorBuilder.builder(mappers);
    }

    public static LabelIndexedExtractorBuilder indexedExtractorByLabel(MappersCollection mappers) {
        return LabelIndexedExtractorBuilder.builder(mappers);
    }

    public static <T> NamedInjectorBuilder<T> namedInjector(MappersCollection mappers, Class<T> sourceClass) {
        return NamedInjectorBuilder.builder(mappers, sourceClass);
    }

    public static <T> SimpleNamedExtractorBuilder<T> namedExtractorSimple(MappersCollection mappers, Class<T> targetClass) {
        return SimpleNamedExtractorBuilder.builder(mappers, targetClass);
    }

    public static <T> IndexNamedExtractorBuilder<T> namedExtractorByIndex(MappersCollection mappers, Class<T> targetClass) {
        return IndexNamedExtractorBuilder.builder(mappers, targetClass);
    }

    public static <T> LabelNamedExtractorBuilder<T> namedExtractorByLabel(MappersCollection mappers, Class<T> targetClass) {
        return LabelNamedExtractorBuilder.builder(mappers, targetClass);
    }

}
