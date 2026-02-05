open module simple.orm.jdbc.wrap {

    requires io.vavr;
    requires java.sql;
    requires simple.orm.util;

    exports simple.orm.jdbc;
    exports simple.orm.jdbc.param;
    exports simple.orm.jdbc.map;
    exports simple.orm.jdbc.query;

    exports simple.orm.jdbc.common;
    exports simple.orm.jdbc.common.builders;

}
