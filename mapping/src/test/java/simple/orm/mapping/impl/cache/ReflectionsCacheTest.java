package simple.orm.mapping.impl.cache;

import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.mapping.NoAccessorFoundException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link ReflectionsCache} tests.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReflectionsCacheTest {

    @Test
    public void testGetters_ClazzA() {
        final ReflectionsCache refCache = new ReflectionsCache();
        final Class<ClazzA> source = ClazzA.class;
        // fieldAint
        {
            Either<Method, Field> getter = refCache.findGetter("fieldAint", source);
            assertThat(getter.isRight()).isTrue();
            Field field = getter.get();
            assertThat(field.getName()).isEqualTo("fieldAint");
            assertThat(field.getType()).isEqualTo(int.class);
            assertThat(field.getDeclaringClass()).isEqualTo(ClazzA.class);
        }
        // fieldADouble
        {
            Either<Method, Field> getter = refCache.findGetter("fieldADouble", source);
            assertThat(getter.isRight()).isTrue();
            Field field = getter.get();
            assertThat(field.getName()).isEqualTo("fieldADouble");
            assertThat(field.getType()).isEqualTo(Double.class);
            assertThat(field.getDeclaringClass()).isEqualTo(ClazzA.class);
        }
        // ADouble
        {
            Either<Method, Field> getter = refCache.findGetter("ADouble", source);
            assertThat(getter.isLeft()).isTrue();
            Method method = getter.getLeft();
            assertThat(method.getName()).isEqualTo("ADouble");
            assertThat(method.getParameterCount()).isEqualTo(0);
            assertThat(method.getReturnType()).isEqualTo(Double.class);
            assertThat(method.getDeclaringClass()).isEqualTo(ClazzA.class);
        }
        // fieldANumber
        {
            Either<Method, Field> getter = refCache.findGetter("fieldANumber", source);
            assertThat(getter.isLeft()).isTrue();
            Method method = getter.getLeft();
            assertThat(method.getName()).isEqualTo("getFieldANumber");
            assertThat(method.getParameterCount()).isEqualTo(0);
            assertThat(method.getReturnType()).isEqualTo(Number.class);
            assertThat(method.getDeclaringClass()).isEqualTo(ClazzA.class);
        }
        // fieldNotFound
        {
            assertThatCode(() -> refCache.findGetter("fieldNotFound", source))
                    .isInstanceOf(NoAccessorFoundException.class);
        }
    }

    @Test
    public void testGetters_ClazzB() {
        final ReflectionsCache refCache = new ReflectionsCache();
        final Class<ClazzB> source = ClazzB.class;
        // fieldAint
        {
            Either<Method, Field> getter = refCache.findGetter("fieldAint", source);
            assertThat(getter.isRight()).isTrue();
            Field field = getter.get();
            assertThat(field.getName()).isEqualTo("fieldAint");
            assertThat(field.getType()).isEqualTo(int.class);
            assertThat(field.getDeclaringClass()).isEqualTo(ClazzB.class); // field overridden
        }
        // fieldADouble
        {
            Either<Method, Field> getter = refCache.findGetter("fieldADouble", source);
            assertThat(getter.isRight()).isTrue();
            Field field = getter.get();
            assertThat(field.getName()).isEqualTo("fieldADouble");
            assertThat(field.getType()).isEqualTo(Double.class);
            assertThat(field.getDeclaringClass()).isEqualTo(ClazzA.class);
        }
        // ADouble
        {
            Either<Method, Field> getter = refCache.findGetter("ADouble", source);
            assertThat(getter.isLeft()).isTrue();
            Method method = getter.getLeft();
            assertThat(method.getName()).isEqualTo("ADouble");
            assertThat(method.getParameterCount()).isEqualTo(0);
            assertThat(method.getReturnType()).isEqualTo(Double.class);
            assertThat(method.getDeclaringClass()).isEqualTo(ClazzA.class);
        }
        // fieldANumber
        {
            Either<Method, Field> getter = refCache.findGetter("fieldANumber", source);
            assertThat(getter.isLeft()).isTrue();
            Method method = getter.getLeft();
            assertThat(method.getName()).isEqualTo("getFieldANumber");
            assertThat(method.getParameterCount()).isEqualTo(0);
            assertThat(method.getReturnType()).isEqualTo(Number.class);
            assertThat(method.getDeclaringClass()).isEqualTo(ClazzB.class); // getter overridden
        }
        // fieldBString
        {
            Either<Method, Field> getter = refCache.findGetter("fieldBString", source);
            assertThat(getter.isLeft()).isTrue();
            Method method = getter.getLeft();
            assertThat(method.getName()).isEqualTo("getFieldBString");
            assertThat(method.getParameterCount()).isEqualTo(0);
            assertThat(method.getReturnType()).isEqualTo(String.class);
            assertThat(method.getDeclaringClass()).isEqualTo(ClazzB.class);
        }
        // fieldNotFound
        {
            assertThatCode(() -> refCache.findGetter("fieldNotFound", source))
                    .isInstanceOf(NoAccessorFoundException.class);
        }
        // fieldVoid (void return type)
        {
            assertThatCode(() -> refCache.findGetter("fieldVoid", source))
                    .isInstanceOf(NoAccessorFoundException.class);
        }
        // fieldParams (has parameters)
        {
            assertThatCode(() -> refCache.findGetter("fieldParams", source))
                    .isInstanceOf(NoAccessorFoundException.class);
        }
    }

    @Test
    public void testConstructors() {
        final ReflectionsCache refCache = new ReflectionsCache();
        {
            assertThatCode(() -> refCache.findDefaultConstructor(ClazzA.class))
                    .isInstanceOf(NoAccessorFoundException.class);
        }
        {
            Constructor<?> constructor = refCache.findDefaultConstructor(ClazzB.class);
            assertThat(constructor).isNotNull();
            assertThat(constructor.getDeclaringClass()).isEqualTo(ClazzB.class);
            assertThat(constructor.getParameterCount()).isEqualTo(0);
        }
    }

    @Test
    public void testSetters_ClazzA() {
        final ReflectionsCache refCache = new ReflectionsCache();
        final Class<ClazzA> source = ClazzA.class;
        // fieldAint
        {
            Either<Method, Field> setter = refCache.findSetter("fieldAint", source);
            assertThat(setter.isRight()).isTrue();
            Field field = setter.get();
            assertThat(field.getName()).isEqualTo("fieldAint");
            assertThat(field.getType()).isEqualTo(int.class);
            assertThat(field.getDeclaringClass()).isEqualTo(ClazzA.class);
        }
        // fieldADouble
        {
            Either<Method, Field> setter = refCache.findSetter("fieldADouble", source);
            assertThat(setter.isRight()).isTrue();
            Field field = setter.get();
            assertThat(field.getName()).isEqualTo("fieldADouble");
            assertThat(field.getType()).isEqualTo(Double.class);
            assertThat(field.getDeclaringClass()).isEqualTo(ClazzA.class);
        }
        // ADouble
        {
            Either<Method, Field> setter = refCache.findSetter("ADouble", source);
            assertThat(setter.isLeft()).isTrue();
            Method method = setter.getLeft();
            assertThat(method.getName()).isEqualTo("ADouble");
            assertThat(method.getParameterCount()).isEqualTo(1);
            assertThat(method.getParameterTypes()[0]).isEqualTo(Double.class);
            assertThat(method.getReturnType()).isEqualTo(void.class);
            assertThat(method.getDeclaringClass()).isEqualTo(ClazzA.class);
        }
        // fieldANumber
        {
            Either<Method, Field> setter = refCache.findSetter("fieldANumber", source);
            assertThat(setter.isLeft()).isTrue();
            Method method = setter.getLeft();
            assertThat(method.getName()).isEqualTo("setFieldANumber");
            assertThat(method.getParameterCount()).isEqualTo(1);
            assertThat(method.getParameterTypes()[0]).isEqualTo(Number.class);
            assertThat(method.getReturnType()).isEqualTo(void.class);
            assertThat(method.getDeclaringClass()).isEqualTo(ClazzA.class);
        }
        // fieldNotFound
        {
            assertThatCode(() -> refCache.findSetter("fieldNotFound", source))
                    .isInstanceOf(NoAccessorFoundException.class);
        }
    }

    @Test
    public void testSetters_ClazzB() {
        final ReflectionsCache refCache = new ReflectionsCache();
        final Class<ClazzB> source = ClazzB.class;
        // fieldAint
        {
            Either<Method, Field> setter = refCache.findSetter("fieldAint", source);
            assertThat(setter.isRight()).isTrue();
            Field field = setter.get();
            assertThat(field.getName()).isEqualTo("fieldAint");
            assertThat(field.getType()).isEqualTo(int.class);
            assertThat(field.getDeclaringClass()).isEqualTo(ClazzB.class); // field overridden
        }
        // fieldADouble
        {
            Either<Method, Field> setter = refCache.findSetter("fieldADouble", source);
            assertThat(setter.isRight()).isTrue();
            Field field = setter.get();
            assertThat(field.getName()).isEqualTo("fieldADouble");
            assertThat(field.getType()).isEqualTo(Double.class);
            assertThat(field.getDeclaringClass()).isEqualTo(ClazzA.class);
        }
        // ADouble
        {
            Either<Method, Field> setter = refCache.findSetter("ADouble", source);
            assertThat(setter.isLeft()).isTrue();
            Method method = setter.getLeft();
            assertThat(method.getName()).isEqualTo("ADouble");
            assertThat(method.getParameterCount()).isEqualTo(1);
            assertThat(method.getParameterTypes()[0]).isEqualTo(Double.class);
            assertThat(method.getReturnType()).isEqualTo(void.class);
            assertThat(method.getDeclaringClass()).isEqualTo(ClazzA.class);
        }
        // fieldANumber
        {
            Either<Method, Field> setter = refCache.findSetter("fieldANumber", source);
            assertThat(setter.isLeft()).isTrue();
            Method method = setter.getLeft();
            assertThat(method.getName()).isEqualTo("setFieldANumber");
            assertThat(method.getParameterCount()).isEqualTo(1);
            assertThat(method.getParameterTypes()[0]).isEqualTo(Number.class);
            assertThat(method.getReturnType()).isEqualTo(void.class);
            assertThat(method.getDeclaringClass()).isEqualTo(ClazzB.class); // setter overridden
        }
        // fieldBString
        {
            Either<Method, Field> setter = refCache.findSetter("fieldBString", source);
            assertThat(setter.isLeft()).isTrue();
            Method method = setter.getLeft();
            assertThat(method.getName()).isEqualTo("setFieldBString");
            assertThat(method.getParameterCount()).isEqualTo(1);
            assertThat(method.getParameterTypes()[0]).isEqualTo(String.class);
            assertThat(method.getReturnType()).isEqualTo(void.class);
            assertThat(method.getDeclaringClass()).isEqualTo(ClazzB.class);
        }
        // fieldNotFound
        {
            assertThatCode(() -> refCache.findSetter("fieldNotFound", source))
                    .isInstanceOf(NoAccessorFoundException.class);
        }
        // fieldVoid (void return type)
        {
            assertThatCode(() -> refCache.findSetter("fieldVoid", source))
                    .isInstanceOf(NoAccessorFoundException.class);
        }
        // fieldParams (has parameters)
        {
            assertThatCode(() -> refCache.findSetter("fieldParams", source))
                    .isInstanceOf(NoAccessorFoundException.class);
        }
    }

    private static class ClazzA {
        public int fieldAint;
        public Double fieldADouble;
        protected Number fieldANumber;

        public ClazzA(int fieldAint, Double fieldADouble, Number fieldANumber) {
            this.fieldAint = fieldAint;
            this.fieldADouble = fieldADouble;
            this.fieldANumber = fieldANumber;
        }

        public Double ADouble() {
            return fieldADouble;
        }

        public void ADouble(Double aDouble) {
            this.fieldADouble = aDouble;
        }

        public Number getFieldANumber() {
            return fieldANumber;
        }

        public void setFieldANumber(Number fieldANumber) {
            this.fieldANumber = fieldANumber;
        }
    }

    private static class ClazzB extends ClazzA {
        public int fieldAint;
        private String fieldBString;

        public ClazzB() {
            super(0, null, null);
        }

        public ClazzB(int fieldAint, Double fieldADouble, Number fieldANumber,
                      int fieldAint1, String fieldBString) {
            super(fieldAint, fieldADouble, fieldANumber);
            this.fieldAint = fieldAint1;
            this.fieldBString = fieldBString;
        }

        public String getFieldBString() {
            return fieldBString;
        }

        public void setFieldBString(String fieldBString) {
            this.fieldBString = fieldBString;
        }

        public void getFieldVoid() {
        }

        public String getFieldParams(String param) {
            return param;
        }

        public void setFieldVoid() {
        }

        public String setFieldParams(String param) {
            return param;
        }

        @Override
        public Number getFieldANumber() {
            return super.getFieldANumber();
        }

        @Override
        public void setFieldANumber(Number fieldANumber) {
            super.setFieldANumber(fieldANumber);
        }
    }

}
