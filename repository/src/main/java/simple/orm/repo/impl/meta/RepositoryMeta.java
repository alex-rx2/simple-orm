package simple.orm.repo.impl.meta;

import io.vavr.collection.Traversable;
import simple.orm.repo.anno.RepoType;

public record RepositoryMeta(
        RepoType type,
        Traversable<QueryMethodMeta> queryMethods,
        int timeout
) {
}
