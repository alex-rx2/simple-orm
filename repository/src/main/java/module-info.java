open module simple.orm.repository {

    requires io.vavr;
    requires simple.orm.jdbcwrap;
    requires simple.orm.mapping;
    requires simple.orm.queryloader;
    requires simple.orm.util;

    exports simple.orm.repo.anno;
    exports simple.orm.repo;

}
