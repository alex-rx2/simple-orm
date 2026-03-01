package simple.orm.mapping.type;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import simple.orm.mapping.param.ParameterGetter;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.param.ParameterSetter;

import java.sql.JDBCType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Some simple tests on ParameterJdbcType.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ParameterJdbcTypeTest {

    @Test
    public void testOf() {
        ParameterJdbcType<Integer> type11 = ParameterJdbcType.of(JDBCType.INTEGER, Integer.class, mock(), mock());
        assertThat(type11.getJDBCType()).isEqualTo(JDBCType.INTEGER);
        assertThat(type11.getJDBCTypeClass()).isEqualTo(Integer.class);

        ParameterJdbcType<Double> type21 = ParameterJdbcType.of(JDBCType.DOUBLE, Double.class, mock(), mock());
        assertThat(type21.getJDBCType()).isEqualTo(JDBCType.DOUBLE);
        assertThat(type21.getJDBCTypeClass()).isEqualTo(Double.class);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testEqualsHashCode() {
        ParameterJdbcType type11 = ParameterJdbcType.of(JDBCType.INTEGER, Integer.class, mock(), mock());
        ParameterJdbcType type12 = ParameterJdbcType.of(JDBCType.INTEGER, String.class, mock(), mock());
        ParameterJdbcType type21 = ParameterJdbcType.of(JDBCType.DOUBLE, Double.class, mock(), mock());
        ParameterJdbcType type111 = ParameterJdbcType.of(JDBCType.INTEGER, Integer.class, mock(), mock());

        assertThat(Mockito.<ParameterGetter>mock()).isNotEqualTo(Mockito.<ParameterGetter>mock());
        assertThat(Mockito.<ParameterSetter>mock()).isNotEqualTo(Mockito.<ParameterSetter>mock());

        assertThat(type11.equals(ParameterJdbcType.of(JDBCType.INTEGER, Integer.class, mock(), mock()))).isTrue();
        assertThat(type11.equals(type11)).isTrue();
        assertThat(type11.equals(type12)).isFalse();
        assertThat(type11.equals(type21)).isFalse();
        assertThat(type11.equals(type111)).isTrue();

        assertThat(type11.hashCode()).isEqualTo(ParameterJdbcType.of(JDBCType.INTEGER, Integer.class, mock(), mock()).hashCode());
        assertThat(type11.hashCode()).isNotEqualTo(type12.hashCode());
        assertThat(type11.hashCode()).isNotEqualTo(type21.hashCode());
    }

    @Test
    public void testMappers() {
        ParameterJdbcType<Integer> type11 = ParameterJdbcType.of(JDBCType.INTEGER, Integer.class, mock(), mock());
        {
            TypeMapper<Integer, String> mapper =
                    type11.mappedTo(String.class, s -> Integer.valueOf(s.substring(1)), integer -> "s" + integer);

            assertThat(mapper.getJdbcType()).isEqualTo(type11);
            assertThat(mapper.getJavaType()).isEqualTo(String.class);
            assertThat(mapper.javaToJdbc("s233")).isEqualTo(233);
            assertThat(mapper.jdbcToJava(-122)).isEqualTo("s-122");
        }
        {
            TypeMapper<Integer, Integer> mapper = type11.trivialMapper();

            assertThat(mapper.getJdbcType()).isEqualTo(type11);
            assertThat(mapper.getJavaType()).isEqualTo(Integer.class);
            assertThat(mapper.javaToJdbc(233)).isEqualTo(233);
            assertThat(mapper.jdbcToJava(-122)).isEqualTo(-122);
        }
    }

}
