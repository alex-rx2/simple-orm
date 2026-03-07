package simple.orm.mapping.param;

import io.vavr.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

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

    @Test
    public void testCaseInsensitive() {
        TypesCollection types = TypesCollection.empty()
                .addType("int",typeInt)
                .addType("str",typeString)
                .caseInsensitive();
        // add
        assertThatCode(()->types.addType("INT", typeInt))
                .isInstanceOf(IllegalArgumentException.class);
        // replace
        assertThatCode(()->types.replaceType("INT", typeInt))
                .doesNotThrowAnyException();
        // find
        assertThat(types.findType("int")).isSameAs(typeInt);
        assertThat(types.findType("INT")).isSameAs(typeInt);
        assertThat(types.findType("InT")).isSameAs(typeInt);
        assertThat(types.findType("str")).isSameAs(typeString);
        assertThat(types.findType("STR")).isSameAs(typeString);
        assertThat(types.findType("StR")).isSameAs(typeString);
    }

    @Test
    public void testCaseSwitching() {
        // case-sensitive by default
        final TypesCollection types = TypesCollection.empty()
                .addType("int",typeInt)
                .addType("STR",typeString);
        assertThat(types.isCaseSensitive()).isTrue();
        assertThat(types.findType("int")).isSameAs(typeInt);
        assertThat(types.findType("INT")).isNull();
        assertThat(types.findType("InT")).isNull();
        assertThat(types.findType("str")).isNull();
        assertThat(types.findType("STR")).isSameAs(typeString);
        assertThat(types.findType("StR")).isNull();
        assertThat(types.caseSensitive()).isSameAs(types);
        // make case-insensitive
        final TypesCollection types2 = types.caseInsensitive();
        assertThat(types2.isCaseSensitive()).isFalse();
        assertThat(types2.findType("int")).isSameAs(typeInt);
        assertThat(types2.findType("INT")).isSameAs(typeInt);
        assertThat(types2.findType("InT")).isSameAs(typeInt);
        assertThat(types2.findType("STR")).isSameAs(typeString);
        assertThat(types2.findType("str")).isSameAs(typeString);
        assertThat(types2.findType("StR")).isSameAs(typeString);
        assertThat(types2.caseInsensitive()).isSameAs(types2);
        // make case-sensitive again
        final TypesCollection types3 = types.caseSensitive();
        assertThat(types3.isCaseSensitive()).isTrue();
        assertThat(types3.findType("int")).isSameAs(typeInt);
        assertThat(types3.findType("INT")).isNull();
        assertThat(types3.findType("InT")).isNull();
        assertThat(types3.findType("str")).isNull();
        assertThat(types3.findType("STR")).isSameAs(typeString);
        assertThat(types3.findType("StR")).isNull();
        assertThat(types3.caseSensitive()).isSameAs(types3);
        // make case-insensitive again
        final TypesCollection types4 = types.caseInsensitive();
        assertThat(types4.isCaseSensitive()).isFalse();
        assertThat(types4.findType("int")).isSameAs(typeInt);
        assertThat(types4.findType("INT")).isSameAs(typeInt);
        assertThat(types4.findType("InT")).isSameAs(typeInt);
        assertThat(types4.findType("STR")).isSameAs(typeString);
        assertThat(types4.findType("str")).isSameAs(typeString);
        assertThat(types4.findType("StR")).isSameAs(typeString);
        assertThat(types4.caseInsensitive()).isSameAs(types4);
    }

}
