package simple.orm.jdbc.map.param;

import io.vavr.collection.Array;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Date;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Tests of {@link MappersCollection}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MappersCollectionTest {

    @Test
    public void testCreation() {
        ParameterType<Integer, Integer> pt1 = DefaultTypes.INTEGER_AS_INTEGER;
        ParameterType<Double, Double> pt2 = DefaultTypes.DOUBLE_AS_DOUBLE;
        ParameterType<String, String> pt3 = DefaultTypes.VARCHAR_AS_STRING;

        ParameterMapper<Integer, Integer> pm1 = DefaultMappers.INTEGER_AS_INTEGER;
        ParameterMapper<Double, Double> pm2 = DefaultMappers.DOUBLE_AS_DOUBLE;
        ParameterMapper<String, String> pm3 = DefaultMappers.VARCHAR_AS_STRING;

        MappersCollection mc1 = new MappersCollection();
        // test adding null mapper
        assertThatCode(() -> mc1.addMapper(null))
                .isInstanceOf(NullPointerException.class);
        assertThatCode(() -> mc1.addMappers(pm1, pm2, null, pm3))
                .isInstanceOf(NullPointerException.class);
        assertThatCode(() -> mc1.addMappers(Array.of(pm1, pm2, null, pm3)))
                .isInstanceOf(NullPointerException.class);
        assertThatCode(() -> mc1.addMappers(List.of(pm1, pm2, null, pm3)))
                .isInstanceOf(NullPointerException.class);
        // test mapper already added
        MappersCollection mc2 = mc1.addMappers(pm1, pm2, pm3);
        assertThatCode(() -> mc2.addMapper(pm1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> mc2.addMapper(new SimpleMapper<>(pt1, Function.identity(), Function.identity())))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> mc2.addMappers(DefaultMappers.BOOLEAN_AS_BOOLEAN, DefaultMappers.SQLTIME_AS_SQLTIME, pm3))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> mc2.addMappers(DefaultMappers.BOOLEAN_AS_BOOLEAN, DefaultMappers.SQLTIME_AS_SQLTIME, new SimpleMapper<>(pt3, Function.identity(), Function.identity())))
                .isInstanceOf(IllegalArgumentException.class);
        // test other mappers can be added
        assertThatCode(() -> mc2.addMapper(DefaultMappers.BOOLEAN_AS_BOOLEAN))
                .doesNotThrowAnyException();
        assertThatCode(() -> mc2.addMapper(DefaultMappers.SQLTIME_AS_SQLTIME))
                .doesNotThrowAnyException();
        assertThatCode(() -> mc2.addMappers(DefaultMappers.BOOLEAN_AS_BOOLEAN, DefaultMappers.SQLTIME_AS_SQLTIME))
                .doesNotThrowAnyException();
        assertThatCode(() -> mc2.addMappers(List.of(DefaultMappers.BOOLEAN_AS_BOOLEAN, DefaultMappers.SQLTIME_AS_SQLTIME)))
                .doesNotThrowAnyException();
        // test mappers can be overridden in new collection
        MappersCollection mc3 = new MappersCollection(mc2);
        assertThatCode(() -> mc3.addMapper(pm1))
                .doesNotThrowAnyException();
        assertThatCode(() -> mc3.addMapper(new SimpleMapper<>(pt1, Function.identity(), Function.identity())))
                .doesNotThrowAnyException();
        assertThatCode(() -> mc3.addMappers(Array.of(pm1, pm2, pm3)))
                .doesNotThrowAnyException();
    }

    @Test
    public void testFindMapper() {
        ParameterType<Integer, Integer> pt1 = DefaultTypes.INTEGER_AS_INTEGER;
        ParameterType<Double, Double> pt2 = DefaultTypes.DOUBLE_AS_DOUBLE;
        ParameterType<String, String> pt3 = DefaultTypes.VARCHAR_AS_STRING;
        ParameterType<Date, Date> pt4 = DefaultTypes.SQLDATE_AS_SQLDATE;

        ParameterMapper<Integer, Integer> pm1 = DefaultMappers.INTEGER_AS_INTEGER;
        ParameterMapper<Double, Double> pm2 = DefaultMappers.DOUBLE_AS_DOUBLE;
        ParameterMapper<String, String> pm3 = DefaultMappers.VARCHAR_AS_STRING;

        // first empty mapper
        {
            MappersCollection mc1 = new MappersCollection();
            {
                Option<ParameterMapper<Integer, Integer>> mapper = mc1.findMapperFor(pt1);
                assertThat(mapper).isNotNull();
                assertThat(mapper.isEmpty()).isTrue();
            }
            {
                Option<ParameterMapper<Double, Double>> mapper = mc1.findMapperFor(pt2);
                assertThat(mapper).isNotNull();
                assertThat(mapper.isEmpty()).isTrue();
            }
            {
                Option<ParameterMapper<String, String>> mapper = mc1.findMapperFor(pt3);
                assertThat(mapper).isNotNull();
                assertThat(mapper.isEmpty()).isTrue();
            }
            {
                Option<ParameterMapper<Date, Date>> mapper = mc1.findMapperFor(pt4);
                assertThat(mapper).isNotNull();
                assertThat(mapper.isEmpty()).isTrue();
            }
        }
        // just mappers 1 and 2
        {
            MappersCollection mc2 = new MappersCollection().addMappers(pm1, pm2);
            {
                Option<ParameterMapper<Integer, Integer>> mapper = mc2.findMapperFor(pt1);
                assertThat(mapper).isNotNull();
                assertThat(mapper.isEmpty()).isFalse();
                assertThat(mapper.get()).isSameAs(pm1);
            }
            {
                Option<ParameterMapper<Double, Double>> mapper = mc2.findMapperFor(pt2);
                assertThat(mapper).isNotNull();
                assertThat(mapper.isEmpty()).isFalse();
                assertThat(mapper.get()).isSameAs(pm2);
            }
            {
                Option<ParameterMapper<String, String>> mapper = mc2.findMapperFor(pt3);
                assertThat(mapper).isNotNull();
                assertThat(mapper.isEmpty()).isTrue();
            }
            {
                Option<ParameterMapper<Date, Date>> mapper = mc2.findMapperFor(pt4);
                assertThat(mapper).isNotNull();
                assertThat(mapper.isEmpty()).isTrue();
            }
        }
        // override mappers 2, add mapper 3
        {
            MappersCollection mc2 = new MappersCollection().addMappers(pm1, pm2);
            ParameterMapper<Double, Double> pm2o = new SimpleMapper<>(pt2, Function.identity(), Function.identity());
            MappersCollection mc3 = new MappersCollection(mc2).addMappers(pm2o, pm3);
            {
                Option<ParameterMapper<Integer, Integer>> mapper = mc3.findMapperFor(pt1);
                assertThat(mapper).isNotNull();
                assertThat(mapper.isEmpty()).isFalse();
                assertThat(mapper.get()).isSameAs(pm1);
            }
            {
                Option<ParameterMapper<Double, Double>> mapper = mc3.findMapperFor(pt2);
                assertThat(mapper).isNotNull();
                assertThat(mapper.isEmpty()).isFalse();
                assertThat(mapper.get()).isSameAs(pm2o);
            }
            {
                Option<ParameterMapper<String, String>> mapper = mc3.findMapperFor(pt3);
                assertThat(mapper).isNotNull();
                assertThat(mapper.isEmpty()).isFalse();
                assertThat(mapper.get()).isSameAs(pm3);
            }
            {
                Option<ParameterMapper<Date, Date>> mapper = mc3.findMapperFor(pt4);
                assertThat(mapper).isNotNull();
                assertThat(mapper.isEmpty()).isTrue();
            }
        }
    }
}
