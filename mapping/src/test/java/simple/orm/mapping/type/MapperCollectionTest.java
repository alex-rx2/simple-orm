package simple.orm.mapping.type;

import io.vavr.Function1;
import io.vavr.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import simple.orm.mapping.param.ParameterJdbcType;

import java.sql.JDBCType;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * {@link MappersCollection} tests.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MapperCollectionTest {

    private final ParameterJdbcType<Integer> typeInt = ParameterJdbcType.of(JDBCType.INTEGER, Integer.class, mock(), mock());
    private final ParameterJdbcType<String> typeString = ParameterJdbcType.of(JDBCType.VARCHAR, String.class, mock(), mock());

    private final TypeMapper<Integer, Integer> mapperIntInt =
            typeInt.mappedTo(Integer.class, Function1.identity(), Function1.identity());
    private final TypeMapper<Integer, String> mapperIntStr =
            typeInt.mappedTo(String.class, Integer::valueOf, String::valueOf);
    private final TypeMapper<String, String> mapperStrStr =
            typeString.mappedTo(String.class, Function1.identity(), Function1.identity());

    @Test
    public void testEmptyAddReplaceAll_NoTags() {
        // empty
        MappersCollection empty = MappersCollection.empty();
        assertThat(empty.allMappers()).isEmpty();
        // add first mapper
        MappersCollection maps1 = empty.addMapper("intMapper", mapperIntInt);
        assertThat(empty.allMappers()).isEmpty();
        assertThat(maps1.allMappers())
                .containsExactlyInAnyOrder(
                        Tuple.of("intMapper", mapperIntInt, null)
                );
        // add second mapper
        MappersCollection maps2 = maps1.addMapper("strMapper", mapperStrStr);
        assertThat(empty.allMappers()).isEmpty();
        assertThat(maps1.allMappers())
                .containsExactlyInAnyOrder(
                        Tuple.of("intMapper", mapperIntInt, null)
                );
        assertThat(maps2.allMappers())
                .containsExactlyInAnyOrder(
                        Tuple.of("intMapper", mapperIntInt, null),
                        Tuple.of("strMapper", mapperStrStr, null)
                );
        // replace first mapper
        TypeMapper<Integer, Integer> anotherIntInt =
                typeInt.mappedTo(Integer.class, Function1.identity(), Function1.identity());
        MappersCollection maps3 = maps2.replaceMapper("intMapper", anotherIntInt);
        assertThat(empty.allMappers()).isEmpty();
        assertThat(maps1.allMappers())
                .containsExactlyInAnyOrder(
                        Tuple.of("intMapper", mapperIntInt, null)
                );
        assertThat(maps2.allMappers())
                .containsExactlyInAnyOrder(
                        Tuple.of("intMapper", mapperIntInt, null),
                        Tuple.of("strMapper", mapperStrStr, null)
                );
        assertThat(maps3.allMappers())
                .containsExactlyInAnyOrder(
                        Tuple.of("intMapper", anotherIntInt, null),
                        Tuple.of("strMapper", mapperStrStr, null)
                );
    }

    @Test
    public void testAddReplaceAllFind_WithTags() {
        // some mappers with different tags
        TypeMapper<Integer, Integer> mapperIntInt2 =
                typeInt.mappedTo(Integer.class, Function1.identity(), Function1.identity());
        TypeMapper<Integer, String> mapperIntStr2 =
                typeInt.mappedTo(String.class, Integer::valueOf, String::valueOf);
        TypeMapper<String, String> mapperStrStr2 =
                typeString.mappedTo(String.class, Function1.identity(), Function1.identity());
        MappersCollection mappers = MappersCollection.empty()
                .addMapper("intMapper", mapperIntInt, null)
                .addMapper("intMapper", mapperIntStr, null)
                .addMapper("strMapper", mapperStrStr, null)
                .addMapper("intMapper", mapperIntInt, "null")
                .addMapper("intMapper", mapperIntStr, "null")
                .addMapper("strMapper", mapperStrStr, "null")
                .addMapper("intMapper", mapperIntInt, "int")
                .addMapper("intMapper", mapperIntStr, "str")
                .addMapper("strMapper", mapperStrStr, "str");
        assertThat(mappers.allMappers())
                .containsExactlyInAnyOrder(
                        Tuple.of("intMapper", mapperIntInt, null),
                        Tuple.of("intMapper", mapperIntStr, null),
                        Tuple.of("strMapper", mapperStrStr, null),
                        Tuple.of("intMapper", mapperIntInt, "null"),
                        Tuple.of("intMapper", mapperIntStr, "null"),
                        Tuple.of("strMapper", mapperStrStr, "null"),
                        Tuple.of("intMapper", mapperIntInt, "int"),
                        Tuple.of("intMapper", mapperIntStr, "str"),
                        Tuple.of("strMapper", mapperStrStr, "str")
                );
        // replace
        MappersCollection mappersReplaced = mappers
                .replaceMapper("intMapper", mapperIntInt2, "int")
                .replaceMapper("intMapper", mapperIntStr2, "str")
                .replaceMapper("strMapper", mapperStrStr2, "str");
        assertThat(mappersReplaced.allMappers())
                .containsExactlyInAnyOrder(
                        Tuple.of("intMapper", mapperIntInt, null),
                        Tuple.of("intMapper", mapperIntStr, null),
                        Tuple.of("strMapper", mapperStrStr, null),
                        Tuple.of("intMapper", mapperIntInt, "null"),
                        Tuple.of("intMapper", mapperIntStr, "null"),
                        Tuple.of("strMapper", mapperStrStr, "null"),
                        Tuple.of("intMapper", mapperIntInt2, "int"),
                        Tuple.of("intMapper", mapperIntStr2, "str"),
                        Tuple.of("strMapper", mapperStrStr2, "str")
                );
        // add more with different types/tags
        MappersCollection mappersAdded = mappersReplaced
                .addMapper("intMapper", mapperIntInt, "INT")
                .addMapper("intMapper", mapperIntStr, "STR")
                .addMapper("strMapper", mapperStrStr, "STR")
                .addMapper("intMapper", mapperIntInt, "str") // combination of name,types,tag is still unique
                .addMapper("intMapper", mapperIntStr, "int") // combination of name,types,tag is still unique
                ;
        assertThat(mappersAdded.allMappers())
                .containsExactlyInAnyOrder(
                        Tuple.of("intMapper", mapperIntInt, null),
                        Tuple.of("intMapper", mapperIntStr, null),
                        Tuple.of("strMapper", mapperStrStr, null),
                        Tuple.of("intMapper", mapperIntInt, "null"),
                        Tuple.of("intMapper", mapperIntStr, "null"),
                        Tuple.of("strMapper", mapperStrStr, "null"),
                        Tuple.of("intMapper", mapperIntInt2, "int"),
                        Tuple.of("intMapper", mapperIntStr2, "str"),
                        Tuple.of("strMapper", mapperStrStr2, "str"),
                        Tuple.of("intMapper", mapperIntInt, "INT"),
                        Tuple.of("intMapper", mapperIntStr, "STR"),
                        Tuple.of("strMapper", mapperStrStr, "STR"),
                        Tuple.of("intMapper", mapperIntInt, "str"),
                        Tuple.of("intMapper", mapperIntStr, "int")
                );
    }

    @Test
    public void testFinds() {
        // some mappers with different tags
        TypeMapper<Integer, Integer> mapperIntInt2 =
                typeInt.mappedTo(Integer.class, Function1.identity(), Function1.identity());
        TypeMapper<Integer, String> mapperIntStr2 =
                typeInt.mappedTo(String.class, Integer::valueOf, String::valueOf);
        TypeMapper<String, String> mapperStrStr2 =
                typeString.mappedTo(String.class, Function1.identity(), Function1.identity());
        TypeMapper<Integer, Integer> mapperIntInt3 =
                typeInt.mappedTo(Integer.class, Function1.identity(), Function1.identity());
        TypeMapper<Integer, String> mapperIntStr3 =
                typeInt.mappedTo(String.class, Integer::valueOf, String::valueOf);
        TypeMapper<String, String> mapperStrStr3 =
                typeString.mappedTo(String.class, Function1.identity(), Function1.identity());
        MappersCollection mappers = MappersCollection.empty()
                .addMapper("intMapper", mapperIntInt, null)
                .addMapper("intMapper", mapperIntStr, null)
                .addMapper("strMapper", mapperStrStr, null)
                .addMapper("intMapper", mapperIntInt2, "null")
                .addMapper("intMapper", mapperIntStr2, "null")
                .addMapper("strMapper", mapperStrStr2, "null")
                .addMapper("intMapper", mapperIntInt3, "int")
                .addMapper("intMapper", mapperIntStr3, "str")
                .addMapper("strMapper", mapperStrStr3, "str");
        assertThat(mappers.allMappers())
                .containsExactlyInAnyOrder(
                        Tuple.of("intMapper", mapperIntInt, null),
                        Tuple.of("intMapper", mapperIntStr, null),
                        Tuple.of("strMapper", mapperStrStr, null),
                        Tuple.of("intMapper", mapperIntInt2, "null"),
                        Tuple.of("intMapper", mapperIntStr2, "null"),
                        Tuple.of("strMapper", mapperStrStr2, "null"),
                        Tuple.of("intMapper", mapperIntInt3, "int"),
                        Tuple.of("intMapper", mapperIntStr3, "str"),
                        Tuple.of("strMapper", mapperStrStr3, "str")
                );
        // find with only name provided
        assertThat(mappers.findMappers("intMapper", null, null, null))
                .containsExactlyInAnyOrder(mapperIntInt, mapperIntStr);
        assertThat(mappers.findMappers("intMapper", "null", null, null))
                .containsExactlyInAnyOrder(mapperIntInt2, mapperIntStr2);
        assertThat(mappers.findMappers("intMapper", "int", null, null))
                .containsExactlyInAnyOrder(mapperIntInt3);
        assertThat(mappers.findMappers("intMapper", "str", null, null))
                .containsExactlyInAnyOrder(mapperIntStr3);
        assertThat(mappers.findMappers("xxxMapper", null, null, null)).isEmpty();
        // find with only jdbcType provided
        assertThat(mappers.findMappers(null, null, typeInt, null))
                .containsExactlyInAnyOrder(mapperIntInt, mapperIntStr);
        assertThat(mappers.findMappers(null, "str", typeString, null))
                .containsExactlyInAnyOrder(mapperStrStr3);
        // name+javaType
        assertThat(mappers.findMappers("intMapper", null, null, Integer.class))
                .containsExactlyInAnyOrder(mapperIntInt);
        assertThat(mappers.findMappers("intMapper", "null", null, String.class))
                .containsExactlyInAnyOrder(mapperIntStr2);
        assertThat(mappers.findMappers("intMapper", null, null, Float.class)).isEmpty();
        assertThat(mappers.findMappers("xxxMapper", null, null, String.class)).isEmpty();
        // jdbcType+javaType
        assertThat(mappers.findMappers(null, "int", typeInt, Integer.class))
                .containsExactlyInAnyOrder(mapperIntInt3);
        assertThat(mappers.findMappers(null, "null", typeString, String.class))
                .containsExactlyInAnyOrder(mapperStrStr2);
        assertThat(mappers.findMappers(null, null, typeInt, String.class))
                .containsExactlyInAnyOrder(mapperIntStr);
        assertThat(mappers.findMappers(null, null, Mockito.<ParameterJdbcType<String>>mock(), String.class)).isEmpty();
        // no name or type
        assertThatCode(() -> mappers.findMappers(null, "str", null, String.class))
                .isInstanceOf(IllegalArgumentException.class);
        // JDBCType
        assertThat(mappers.findMappers(JDBCType.INTEGER.getVendorTypeNumber(), null, null, null))
                .containsExactlyInAnyOrder(mapperIntInt, mapperIntStr);
        assertThat(mappers.findMappers(JDBCType.INTEGER.getVendorTypeNumber(), null, "null", null))
                .containsExactlyInAnyOrder(mapperIntInt2, mapperIntStr2);
        assertThat(mappers.findMappers(JDBCType.INTEGER.getVendorTypeNumber(), null, "xxx", null)).isEmpty();
        assertThat(mappers.findMappers(JDBCType.INTEGER.getVendorTypeNumber(), "intMapper", null, null))
                .containsExactlyInAnyOrder(mapperIntInt, mapperIntStr);
        assertThat(mappers.findMappers(JDBCType.INTEGER.getVendorTypeNumber(), "strMapper", null, null)).isEmpty();
        assertThat(mappers.findMappers(JDBCType.VARCHAR.getVendorTypeNumber(), null, "str", String.class))
                .containsExactlyInAnyOrder(mapperStrStr3);
        assertThat(mappers.findMappers(JDBCType.VARCHAR.getVendorTypeNumber(), null, null, String.class))
                .containsExactlyInAnyOrder(mapperStrStr);
    }

    @Test
    public void testNullTag() {
        // mapper registered without a tag is considered to have tag==null
        // that means if we have a mapper with tag "tag" find methods without tag should not find it (as no tag means tag==null)
        MappersCollection mappers = MappersCollection.empty()
                .addMapper("intMapper", mapperIntInt, "int");
        assertThat(mappers.findMappers("intMapper", null, null, null)).isEmpty();
        assertThat(mappers.findMappers("intMapper", null, null, Integer.class)).isEmpty();
        assertThat(mappers.findMappers(null, null, typeInt, Integer.class)).isEmpty();
        assertThat(mappers.findMappers("intMapper", null, typeInt, Integer.class)).isEmpty();
        assertThat(mappers.findMappers(JDBCType.INTEGER.getVendorTypeNumber(), null, null, null)).isEmpty();
        assertThat(mappers.findMappers(JDBCType.INTEGER.getVendorTypeNumber(), "intMapper", null, null)).isEmpty();
        assertThat(mappers.findMappers(JDBCType.INTEGER.getVendorTypeNumber(), null, null, Integer.class)).isEmpty();
        assertThat(mappers.findMappers(JDBCType.INTEGER.getVendorTypeNumber(), "intMapper", null, Integer.class)).isEmpty();
        // test after adding a mapper
        TypeMapper<Integer, Integer> mapperIntInt2 =
                typeInt.mappedTo(Integer.class, Function1.identity(), Function1.identity());
        MappersCollection mappers2 = mappers.addMapper("intMapper", mapperIntInt2);
        assertThat(mappers2.findMappers("intMapper", null, null, null))
                .containsExactly(mapperIntInt2);
        assertThat(mappers2.findMappers("intMapper", null, null, Integer.class))
                .containsExactly(mapperIntInt2);
        assertThat(mappers2.findMappers(null, null, typeInt, Integer.class))
                .containsExactly(mapperIntInt2);
        assertThat(mappers2.findMappers("intMapper", null, typeInt, Integer.class))
                .containsExactly(mapperIntInt2);
        assertThat(mappers2.findMappers(JDBCType.INTEGER.getVendorTypeNumber(), null, null, null))
                .containsExactly(mapperIntInt2);
        assertThat(mappers2.findMappers(JDBCType.INTEGER.getVendorTypeNumber(), "intMapper", null, null))
                .containsExactly(mapperIntInt2);
        assertThat(mappers2.findMappers(JDBCType.INTEGER.getVendorTypeNumber(), null, null, Integer.class))
                .containsExactly(mapperIntInt2);
        assertThat(mappers2.findMappers(JDBCType.INTEGER.getVendorTypeNumber(), "intMapper", null, Integer.class))
                .containsExactly(mapperIntInt2);
    }

    @Test
    public void testAddReplaceExceptions() {
        MappersCollection mappers = MappersCollection.empty()
                .addMapper("intMapper", mapperIntInt)
                .addMapper("intMapper", mapperIntStr, "str")
                .addMapper("strMapper", mapperStrStr);
        // add
        assertThatCode(() -> mappers.addMapper("intMapper", mapperIntInt))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> mappers.addMapper("intMapper", mapperIntStr, "str"))
                .isInstanceOf(IllegalArgumentException.class);
        // replace
        assertThatCode(() -> mappers.replaceMapper("xxxMapper", mapperIntStr))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> mappers.replaceMapper("strMapper", mapperIntStr, "str"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testCaseInsensitive() {
        TypeMapper<Integer, Integer> mapperIntInt2 =
                typeInt.mappedTo(Integer.class, Function1.identity(), Function1.identity());
        TypeMapper<Integer, String> mapperIntStr2 =
                typeInt.mappedTo(String.class, Integer::valueOf, String::valueOf);
        TypeMapper<String, String> mapperStrStr2 =
                typeString.mappedTo(String.class, Function1.identity(), Function1.identity());
        MappersCollection mappers = MappersCollection.empty()
                .addMapper("intMapper", mapperIntInt, null)
                .addMapper("intMapper", mapperIntStr, null)
                .addMapper("strMapper", mapperStrStr, null)
                .caseInsensitive();
        assertThat(mappers.allMappers())
                .containsExactlyInAnyOrder(
                        Tuple.of("intMapper", mapperIntInt, null),
                        Tuple.of("intMapper", mapperIntStr, null),
                        Tuple.of("strMapper", mapperStrStr, null)
                );
        // add
        assertThatCode(() -> mappers.addMapper("INTmapper", mapperIntInt2))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> mappers.addMapper("intMAPPER", mapperIntInt2))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> mappers.addMapper("STRMAPPER", mapperStrStr2))
                .isInstanceOf(IllegalArgumentException.class);
        // replace
        MappersCollection mappers2 = mappers
                .replaceMapper("INTmapper", mapperIntInt2, null)
                .replaceMapper("intMAPPER", mapperIntStr2, null)
                .replaceMapper("STRMAPPER", mapperStrStr2, null);
        assertThat(mappers2.allMappers())
                .containsExactlyInAnyOrder(
                        Tuple.of("INTmapper", mapperIntInt2, null),
                        Tuple.of("intMAPPER", mapperIntStr2, null),
                        Tuple.of("STRMAPPER", mapperStrStr2, null)
                );
        // find
        assertThat(mappers2.findMappers("intmapper", null, null, null))
                .containsExactlyInAnyOrder(mapperIntInt2, mapperIntStr2);
        assertThat(mappers2.findMappers("INTMAPPER", null, null, null))
                .containsExactlyInAnyOrder(mapperIntInt2, mapperIntStr2);
        assertThat(mappers2.findMappers("STRmapper", null, null, null))
                .containsExactlyInAnyOrder(mapperStrStr2);
        assertThat(mappers2.findMappers("strMAPPER", null, null, null))
                .containsExactlyInAnyOrder(mapperStrStr2);
        // check tag is still case-sensitive
        MappersCollection mappers3 = mappers
                .addMapper("intmapper", mapperIntInt, "tag")
                .addMapper("INTMAPPER", mapperIntStr, "TAG")
                .addMapper("strmapper", mapperStrStr, "tAg");
        // find with tag
        assertThat(mappers3.findMappers("intMAPPER", "tag", null, null))
                .containsExactlyInAnyOrder(mapperIntInt);
        assertThat(mappers3.findMappers("intMAPPER", "tAg", null, null))
                .isEmpty();
        assertThat(mappers3.findMappers("INTmapper", "TAG", null, null))
                .containsExactlyInAnyOrder(mapperIntStr);
        assertThat(mappers3.findMappers("INTmapper", "tAg", null, null))
                .isEmpty();
        assertThat(mappers3.findMappers("StrMapper", "tAg", null, null))
                .containsExactlyInAnyOrder(mapperStrStr);
        assertThat(mappers3.findMappers("StrMapper", "TaG", null, null))
                .isEmpty();
    }

    @Test
    public void testCaseSwitching() {
        // case-sensitive by default
        TypeMapper<Integer, Integer> mapperIntInt2 =
                typeInt.mappedTo(Integer.class, Function1.identity(), Function1.identity());
        TypeMapper<String, String> mapperStrStr2 =
                typeString.mappedTo(String.class, Function1.identity(), Function1.identity());
        MappersCollection mappers = MappersCollection.empty()
                .addMapper("intMapper", mapperIntInt, null)
                .addMapper("strMapper", mapperStrStr, null)
                .addMapper("INTmapper", mapperIntInt2, null)
                .addMapper("STRmapper", mapperStrStr2, null);
        assertThat(mappers.isCaseSensitive()).isTrue();
        assertThat(mappers.findMappers("intMapper", null, null, null))
                .containsExactlyInAnyOrder(mapperIntInt);
        assertThat(mappers.findMappers("INTmapper", null, null, null))
                .containsExactlyInAnyOrder(mapperIntInt2);
        assertThat(mappers.findMappers("intmapper", null, null, null))
                .isEmpty();
        assertThat(mappers.findMappers("INTMAPPER", null, null, null))
                .isEmpty();
        assertThat(mappers.findMappers("strMapper", null, null, null))
                .containsExactlyInAnyOrder(mapperStrStr);
        assertThat(mappers.findMappers("STRmapper", null, null, null))
                .containsExactlyInAnyOrder(mapperStrStr2);
        assertThat(mappers.findMappers("StrMapper", null, null, null))
                .isEmpty();
        assertThat(mappers.findMappers("sTrMaPpEr", null, null, null))
                .isEmpty();
        assertThat(mappers.caseSensitive()).isSameAs(mappers);
        // make case-insensitive
        MappersCollection mappers2 = mappers.caseInsensitive();
        assertThat(mappers2.isCaseSensitive()).isFalse();
        assertThat(mappers2.findMappers("intMapper", null, null, null))
                .containsExactlyInAnyOrder(mapperIntInt, mapperIntInt2);
        assertThat(mappers2.findMappers("INTmapper", null, null, null))
                .containsExactlyInAnyOrder(mapperIntInt, mapperIntInt2);
        assertThat(mappers2.findMappers("intmapper", null, null, null))
                .containsExactlyInAnyOrder(mapperIntInt, mapperIntInt2);
        assertThat(mappers2.findMappers("INTMAPPER", null, null, null))
                .containsExactlyInAnyOrder(mapperIntInt, mapperIntInt2);
        assertThat(mappers2.findMappers("strMapper", null, null, null))
                .containsExactlyInAnyOrder(mapperStrStr, mapperStrStr2);
        assertThat(mappers2.findMappers("STRmapper", null, null, null))
                .containsExactlyInAnyOrder(mapperStrStr, mapperStrStr2);
        assertThat(mappers2.findMappers("StrMapper", null, null, null))
                .containsExactlyInAnyOrder(mapperStrStr, mapperStrStr2);
        assertThat(mappers2.findMappers("sTrMaPpEr", null, null, null))
                .containsExactlyInAnyOrder(mapperStrStr, mapperStrStr2);
        assertThat(mappers2.caseInsensitive()).isSameAs(mappers2);
        // make case-sensitive again
        MappersCollection mappers3 = mappers2.caseSensitive();
        assertThat(mappers3.isCaseSensitive()).isTrue();
        assertThat(mappers3.findMappers("intMapper", null, null, null))
                .containsExactlyInAnyOrder(mapperIntInt);
        assertThat(mappers3.findMappers("INTmapper", null, null, null))
                .containsExactlyInAnyOrder(mapperIntInt2);
        assertThat(mappers3.findMappers("intmapper", null, null, null))
                .isEmpty();
        assertThat(mappers3.findMappers("INTMAPPER", null, null, null))
                .isEmpty();
        assertThat(mappers3.findMappers("strMapper", null, null, null))
                .containsExactlyInAnyOrder(mapperStrStr);
        assertThat(mappers3.findMappers("STRmapper", null, null, null))
                .containsExactlyInAnyOrder(mapperStrStr2);
        assertThat(mappers3.findMappers("StrMapper", null, null, null))
                .isEmpty();
        assertThat(mappers3.findMappers("sTrMaPpEr", null, null, null))
                .isEmpty();
        assertThat(mappers3.caseSensitive()).isSameAs(mappers3);
        // make case-insensitive again
        MappersCollection mappers4 = mappers3.caseInsensitive();
        assertThat(mappers4.isCaseSensitive()).isFalse();
        assertThat(mappers4.findMappers("intMapper", null, null, null))
                .containsExactlyInAnyOrder(mapperIntInt, mapperIntInt2);
        assertThat(mappers4.findMappers("INTmapper", null, null, null))
                .containsExactlyInAnyOrder(mapperIntInt, mapperIntInt2);
        assertThat(mappers4.findMappers("intmapper", null, null, null))
                .containsExactlyInAnyOrder(mapperIntInt, mapperIntInt2);
        assertThat(mappers4.findMappers("INTMAPPER", null, null, null))
                .containsExactlyInAnyOrder(mapperIntInt, mapperIntInt2);
        assertThat(mappers4.findMappers("strMapper", null, null, null))
                .containsExactlyInAnyOrder(mapperStrStr, mapperStrStr2);
        assertThat(mappers4.findMappers("STRmapper", null, null, null))
                .containsExactlyInAnyOrder(mapperStrStr, mapperStrStr2);
        assertThat(mappers4.findMappers("StrMapper", null, null, null))
                .containsExactlyInAnyOrder(mapperStrStr, mapperStrStr2);
        assertThat(mappers4.findMappers("sTrMaPpEr", null, null, null))
                .containsExactlyInAnyOrder(mapperStrStr, mapperStrStr2);
        assertThat(mappers4.caseInsensitive()).isSameAs(mappers4);
    }

}
