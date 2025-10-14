package simple.orm.jdbc.query;

import simple.orm.jdbc.map.in.IndexedExtractor;
import simple.orm.jdbc.map.in.IndexedInjector;
import simple.orm.jdbc.map.in.NamedExtractor;
import simple.orm.jdbc.map.in.NamedInjector;

/**
 * Factory to create properly configured queries.
 */
// TODO stub interface, make a proper class
// TODO provide as builder?
public interface QueryFactory {

    Query createDDLQuery(
            String sql,
            int queryTimeoutSeconds
    );

    IndexedQuery createSelectQuery(
            String sql,
            int queryTimeoutSeconds,
            IndexedInjector injector,
            IndexedExtractor extractor
    );

    <I, O> NamedQuery<I, O> createSelectQuery(
            String sql,
            int queryTimeoutSeconds,
            NamedInjector<I> injector,
            NamedExtractor<O> extractor
    );

    IndexedQuery createSelectQueryWithoutParameters(
            String sql,
            int queryTimeoutSeconds,
            IndexedExtractor extractor
    );

    <O> NamedQuery<Void, O> createSelectQueryWithoutParameters(
            String sql,
            int queryTimeoutSeconds,
            NamedExtractor<O> extractor
    );

    IndexedQuery createIUDQuery(
            String sql,
            int queryTimeoutSeconds,
            IndexedInjector injector
    );

    <I> NamedQuery<I, Void> createIUDQuery(
            String sql,
            int queryTimeoutSeconds,
            NamedInjector<I> injector
    );

    Query createIUDQueryWithoutParameters(
            String sql,
            int queryTimeoutSeconds
    );
}
