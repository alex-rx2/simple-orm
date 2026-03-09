open module simple.orm.db.h2database {

    requires io.vavr;
    requires java.sql;
    requires simple.orm.mapping;
    requires simple.orm.util;
    requires com.h2database;

    exports simple.orm.h2;
    exports simple.orm.h2.param;
    exports simple.orm.h2.type;

}
