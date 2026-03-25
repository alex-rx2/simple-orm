package simple.orm.repo.anno;

import simple.orm.jdbc.query.Query;

/**
 * Types of repositories.
 */
public enum RepoType {

    /**
     * Repository of {@link Query}s.
     * <br>
     * Each repo method return a proper {@link Query} to be used elsewhere.
     */
    QUERY,

}
