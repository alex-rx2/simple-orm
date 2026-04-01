package simple.orm.mapping.builder;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.mapping.impl.cache.FoundMappersCache;
import simple.orm.mapping.indexed.IndexedExtractorImpl;
import simple.orm.mapping.indexed.IndexedParameter;
import simple.orm.mapping.type.MappersCollection;
import simple.orm.util.Mutable;

/**
 * Abstract base class for builder of {@link IndexedExtractor}.
 *
 * @param <SELF> actual builder, extending this abstract one.
 */
public class AbstractIndexedExtractorBuilder<SELF extends AbstractIndexedExtractorBuilder<SELF>> {

    protected MappersFinder mappersFinder;
    protected final Mutable<Seq<IndexedParameter>> params;

    AbstractIndexedExtractorBuilder() {
        this.params = Mutable.of(List.empty());
    }

    @SuppressWarnings("unchecked")
    SELF param(IndexedParameter param) {
        if (param == null) {
            throw new NullPointerException("param is null");
        }
        params.apply(p -> p.append(param));
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public SELF withCustomMappersFinder(MappersFinder mappersFinder) {
        if (mappersFinder == null) {
            throw new NullPointerException("mappersFinder is null");
        }
        this.mappersFinder = mappersFinder;
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    public SELF withDefaultMappersFinder(MappersCollection mappers) {
        if (mappers == null) {
            throw new NullPointerException("mappers is null");
        }
        this.mappersFinder = new FoundMappersCache(mappers);
        return (SELF) this;
    }

    public IndexedExtractor build() {
        if (mappersFinder == null) {
            throw new NullPointerException("mappersFinder is null");
        }
        return new IndexedExtractorImpl(mappersFinder, params.get());
    }

}
