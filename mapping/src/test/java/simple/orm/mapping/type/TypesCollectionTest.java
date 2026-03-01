package simple.orm.mapping.type;

import io.vavr.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.param.TypesCollection;

import java.sql.JDBCType;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * {@link TypesCollection} tests.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TypesCollectionTest {

    private final ParameterJdbcType<Integer> typeInt = ParameterJdbcType.of(JDBCType.INTEGER, Integer.class, mock(), mock());
    private final ParameterJdbcType<String> typeInt2 = ParameterJdbcType.of(JDBCType.INTEGER, String.class, mock(), mock());
    private final ParameterJdbcType<String> typeString = ParameterJdbcType.of(JDBCType.VARCHAR, String.class, mock(), mock());

    @Test
    public void testEmptyAddReplaceFindAll() {
        // empty
        TypesCollection empty = TypesCollection.empty();
        assertThat(empty.allTypes()).isEmpty();
        // add one type
        TypesCollection types1 = empty.addType("int", typeInt);
        assertThat(empty.allTypes()).isEmpty();
        assertThat(types1.allTypes())
                .containsExactlyInAnyOrder(
                        Tuple.of("int", typeInt)
                );
        assertThat(types1.findType("int")).isSameAs(typeInt);
        assertThat(types1.findType("INT")).isNull();
        assertThat(types1.findType("xxx")).isNull();
        assertThat(types1.findTypes(JDBCType.INTEGER))
                .containsExactlyInAnyOrder(
                        Tuple.of("int", typeInt)
                );
        assertThat(types1.findTypes(JDBCType.VARCHAR)).isEmpty();
        // add another type
        TypesCollection types2 = types1.addType("str", typeString);
        assertThat(empty.allTypes()).isEmpty();
        assertThat(types1.allTypes())
                .containsExactlyInAnyOrder(
                        Tuple.of("int", typeInt)
                );
        assertThat(types2.allTypes())
                .containsExactlyInAnyOrder(
                        Tuple.of("int", typeInt),
                        Tuple.of("str", typeString)
                );
        assertThat(types1.findType("int")).isSameAs(typeInt);
        assertThat(types1.findType("str")).isNull();
        assertThat(types2.findType("int")).isSameAs(typeInt);
        assertThat(types2.findType("str")).isSameAs(typeString);
        assertThat(types2.findType("STR")).isNull();
        assertThat(types2.findType("xxx")).isNull();
        assertThat(types2.findTypes(JDBCType.INTEGER))
                .containsExactlyInAnyOrder(
                        Tuple.of("int", typeInt)
                );
        assertThat(types2.findTypes(JDBCType.VARCHAR))
                .containsExactlyInAnyOrder(
                        Tuple.of("str", typeString)
                );
        // replace first type
        TypesCollection types3 = types2.replaceType("int", typeInt2);
        assertThat(empty.allTypes()).isEmpty();
        assertThat(types1.allTypes())
                .containsExactlyInAnyOrder(
                        Tuple.of("int", typeInt)
                );
        assertThat(types2.allTypes())
                .containsExactlyInAnyOrder(
                        Tuple.of("int", typeInt),
                        Tuple.of("str", typeString)
                );
        assertThat(types3.allTypes())
                .containsExactlyInAnyOrder(
                        Tuple.of("int", typeInt2),
                        Tuple.of("str", typeString)
                );
        assertThat(types1.findType("int")).isSameAs(typeInt);
        assertThat(types1.findType("str")).isNull();
        assertThat(types2.findType("int")).isSameAs(typeInt);
        assertThat(types2.findType("str")).isSameAs(typeString);
        assertThat(types3.findType("int")).isSameAs(typeInt2);
        assertThat(types3.findType("str")).isSameAs(typeString);
        assertThat(types3.findType("INT")).isNull();
        assertThat(types3.findType("STR")).isNull();
        assertThat(types3.findType("xxx")).isNull();
        assertThat(types3.findTypes(JDBCType.INTEGER))
                .containsExactlyInAnyOrder(
                        Tuple.of("int", typeInt2)
                );
        assertThat(types3.findTypes(JDBCType.VARCHAR))
                .containsExactlyInAnyOrder(
                        Tuple.of("str", typeString)
                );
        // add one more to test find
        TypesCollection types4 = types3.addType("intOld", typeInt);
        assertThat(types4.findTypes(JDBCType.INTEGER))
                .containsExactlyInAnyOrder(
                        Tuple.of("intOld", typeInt),
                        Tuple.of("int", typeInt2)
                );
        assertThat(types4.findTypes(JDBCType.VARCHAR))
                .containsExactlyInAnyOrder(
                        Tuple.of("str", typeString)
                );
    }

    @Test
    public void testAddReplaceExceptions() {
        TypesCollection types = TypesCollection.empty()
                .addType("int",typeInt)
                .addType("str",typeString);
        // add
        assertThatCode(()->types.addType("int", typeInt))
                .isInstanceOf(IllegalArgumentException.class);
        // replace
        assertThatCode(()->types.replaceType("INT", typeInt))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
