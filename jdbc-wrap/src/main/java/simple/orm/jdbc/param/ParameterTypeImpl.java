package simple.orm.jdbc.param;

import io.vavr.Function1;

import java.sql.JDBCType;
import java.util.Objects;

/**
 * {@link ParameterType} implementation.
 */
public class ParameterTypeImpl<Jdbc, Java> extends AbstractParameterImplType<Jdbc, Java> {

    protected Function1<Jdbc, Java> jdbcToJava;
    protected Function1<Java, Jdbc> javaToJdbc;

    public ParameterTypeImpl(JDBCType jdbcType,
                             Class<Jdbc> jdbcClass,
                             Class<Java> javaClass,
                             Function1<Java, Jdbc> javaToJdbc,
                             Function1<Jdbc, Java> jdbcToJava
    ) {
        super(jdbcType, jdbcClass, javaClass);
        if (javaToJdbc == null) {
            throw new NullPointerException("javaToJdbc is null");
        }
        if (jdbcToJava == null) {
            throw new NullPointerException("jdbcToJava is null");
        }
        this.javaToJdbc = javaToJdbc;
        this.jdbcToJava = jdbcToJava;
    }

    @Override
    public Jdbc fromJava(Java value) {
        return javaToJdbc.apply(value);
    }

    @Override
    public Java fromJDBC(Jdbc value) {
        return jdbcToJava.apply(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ParameterTypeImpl))
            return false;
        ParameterTypeImpl<?, ?> that = (ParameterTypeImpl<?, ?>) o;
        return this.jdbcType == that.jdbcType
                && this.jdbcClass == that.jdbcClass
                && this.javaClass == that.javaClass
                && this.javaToJdbc == that.javaToJdbc
                && this.jdbcToJava == that.jdbcToJava
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jdbcType, jdbcClass, javaClass, jdbcToJava, javaToJdbc);
    }

    @Override
    public String toString() {
        return "ParameterImpl(" +
                "jdbcType=" + jdbcType + "," +
                "jdbcClass=" + jdbcClass.getName() + "," +
                "javaClass=" + javaClass.getName() + "," +
                "jdbcToJava=" + jdbcToJava + "," +
                "javaToJdbc=" + javaToJdbc +
                ")";
    }
}
