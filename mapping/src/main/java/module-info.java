open module simple.orm.mapping {

    requires io.vavr;
    requires java.sql;
    requires simple.orm.util;
    requires simple.orm.jdbc.wrap;

    exports simple.orm.mapping.param;
    exports simple.orm.mapping.type;
    exports simple.orm.mapping.indexed;
    exports simple.orm.mapping.named;
    exports simple.orm.mapping.builder;

}
