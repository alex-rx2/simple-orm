package simple.orm.repo.impl.meta;

import io.vavr.Function1;
import io.vavr.collection.Traversable;
import simple.orm.repo.anno.RepoType;

public record RepositoryMeta(
        RepoType type,
        Traversable<QueryMethodMeta> queryMethods,
        int timeout
) {

    public RepositoryMeta replaceMethods(
            Function1<Traversable<QueryMethodMeta>, Traversable<QueryMethodMeta>> mapper
    ) {
        return new RepositoryMeta(type, mapper.apply(queryMethods), timeout);
    }

}
