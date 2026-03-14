open module simple.orm.jdbcwrap {

    requires io.vavr;
    requires java.sql;
    requires simple.orm.util;

    exports simple.orm.jdbc;
    exports simple.orm.jdbc.map;
    exports simple.orm.jdbc.query;

}
