package simple.orm.loader.impl.builder;

import io.vavr.collection.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import simple.orm.loader.ExtractionStrategy;
import simple.orm.loader.InjectionStrategy;
import simple.orm.loader.builder.QueryParameter;

import static org.assertj.core.api.Assertions.assertThatCode;
import static simple.orm.loader.builder.ParameterType.*;

/**
 * Tests on {@link QueryValidator}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QueryValidatorTest {

    private final QueryValidator validator = QueryValidator.getInstance();

    @Test
    public void testDDL() {
        // wrong injection strategy
        assertThatCode(() -> validator.validateDDL(
                InjectionStrategy.indexed(), ExtractionStrategy.noneDdl(), List.empty()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("injection strategy")
                .hasMessageEndingWith("should be NONE");
        assertThatCode(() -> validator.validateDDL(
                InjectionStrategy.named(SomeComplexObject.class), ExtractionStrategy.noneDdl(), List.empty()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("injection strategy")
                .hasMessageEndingWith("should be NONE");
        // wrong extraction strategy
        assertThatCode(() -> validator.validateDDL(
                InjectionStrategy.none(), ExtractionStrategy.indexed(), List.empty()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("extraction strategy")
                .hasMessageEndingWith("should be NONE");
        assertThatCode(() -> validator.validateDDL(
                InjectionStrategy.none(), ExtractionStrategy.named(SomeComplexObject.class), List.empty()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("extraction strategy")
                .hasMessageEndingWith("should be NONE");
        // has params
        assertThatCode(() -> validator.validateDDL(
                InjectionStrategy.none(),
                ExtractionStrategy.noneDdl(),
                List.of(new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageEndingWith("should have no parameters");
        assertThatCode(() -> validator.validateDDL(
                InjectionStrategy.none(),
                ExtractionStrategy.noneDdl(),
                List.of(new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageEndingWith("should have no parameters");
        // valid
        assertThatCode(() -> validator.validateDDL(
                InjectionStrategy.none(), ExtractionStrategy.noneDdl(), List.empty()
        ))
                .doesNotThrowAnyException();
    }

    @Test
    public void testDML_without_injection_params() {
        // extraction strategy
        assertThatCode(() -> validator.validateDML(
                InjectionStrategy.none(), ExtractionStrategy.indexed(), List.empty()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("extraction strategy")
                .hasMessageEndingWith("should be NONE");
        assertThatCode(() -> validator.validateDML(
                InjectionStrategy.none(), ExtractionStrategy.named(SomeComplexObject.class), List.empty()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("extraction strategy")
                .hasMessageEndingWith("should be NONE");
        // extraction params
        assertThatCode(() -> validator.validateDML(
                InjectionStrategy.none(),
                ExtractionStrategy.noneDml(),
                List.of(new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("query has extraction parameters");
        // valid
        assertThatCode(() -> validator.validateDML(
                InjectionStrategy.none(), ExtractionStrategy.noneDml(), List.empty()
        ))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validateDML(
                InjectionStrategy.indexed(),
                ExtractionStrategy.noneDml(),
                List.of(new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null))
        ))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validateDML(
                InjectionStrategy.named(SomeComplexObject.class),
                ExtractionStrategy.noneDml(),
                List.of(new QueryParameter(INJECTION, 1, null, "name", null, null, null, null, null, null, null))
        ))
                .doesNotThrowAnyException();
    }

    @Test
    public void testDML_injection_params_indexed() {
        // injection params
        // - parameters present
        assertThatCode(() -> validator.validateDML(
                InjectionStrategy.indexed(),
                ExtractionStrategy.noneDml(),
                List.empty()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("should have at least one parameter");
        assertThatCode(() -> validator.validateDML(
                InjectionStrategy.indexed(),
                ExtractionStrategy.noneDml(),
                List.of(new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("should have at least one parameter");
        // - properly indexed
        assertThatCode(() -> validator.validateDML(
                InjectionStrategy.indexed(),
                ExtractionStrategy.noneDml(),
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(INJECTION, 3, null, null, null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("broken parameter indexing");
        assertThatCode(() -> validator.validateDML(
                InjectionStrategy.indexed(),
                ExtractionStrategy.noneDml(),
                List.of(new QueryParameter(INJECTION, 2, null, null, null, null, null, null, null, null, null))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("broken parameter indexing");
        assertThatCode(() -> validator.validateDML(
                InjectionStrategy.indexed(),
                ExtractionStrategy.noneDml(),
                List.of(
                        new QueryParameter(INJECTION, 0, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("broken parameter indexing");
        // - has no labels
        assertThatCode(() -> validator.validateDML(
                InjectionStrategy.indexed(),
                ExtractionStrategy.noneDml(),
                List.of(new QueryParameter(INJECTION, 1, "label", null, null, null, null, null, null, null, null))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("should have no label");
    }

    @Test
    public void testDML_injection_params_named() {
        // injection params
        // - parameters present
        assertThatCode(() -> validator.validateDML(
                InjectionStrategy.named(SomeComplexObject.class),
                ExtractionStrategy.noneDml(),
                List.empty()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("should have at least one parameter");
        assertThatCode(() -> validator.validateDML(
                InjectionStrategy.named(SomeComplexObject.class),
                ExtractionStrategy.noneDml(),
                List.of(new QueryParameter(EXTRACTION, 1, null, "prop1", null, null, null, null, null, null, null))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("should have at least one parameter");
        // - properly indexed
        assertThatCode(() -> validator.validateDML(
                InjectionStrategy.named(SomeComplexObject.class),
                ExtractionStrategy.noneDml(),
                List.of(
                        new QueryParameter(INJECTION, 1, null, "prop1", null, null, null, null, null, null, null),
                        new QueryParameter(INJECTION, 3, null, "prop2", null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("broken parameter indexing");
        assertThatCode(() -> validator.validateDML(
                InjectionStrategy.named(SomeComplexObject.class),
                ExtractionStrategy.noneDml(),
                List.of(new QueryParameter(INJECTION, 2, null, "prop1", null, null, null, null, null, null, null))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("broken parameter indexing");
        assertThatCode(() -> validator.validateDML(
                InjectionStrategy.named(SomeComplexObject.class),
                ExtractionStrategy.noneDml(),
                List.of(
                        new QueryParameter(INJECTION, 0, null, "prop1", null, null, null, null, null, null, null),
                        new QueryParameter(INJECTION, 1, null, "prop2", null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("broken parameter indexing");
        // - has defined property name
        assertThatCode(() -> validator.validateDML(
                InjectionStrategy.named(SomeComplexObject.class),
                ExtractionStrategy.noneDml(),
                List.of(new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("should have nonempty property name");
        // - has no labels
        assertThatCode(() -> validator.validateDML(
                InjectionStrategy.named(SomeComplexObject.class),
                ExtractionStrategy.noneDml(),
                List.of(new QueryParameter(INJECTION, 1, "label", "prop1", null, null, null, null, null, null, null))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("should have no label");
    }

    @Test
    public void testSelect_without_params() {
        // extraction strategy
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.none(), ExtractionStrategy.noneDdl(), List.empty()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("extraction strategy is NONE");
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.none(), ExtractionStrategy.noneDml(), List.empty()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("extraction strategy is NONE");
        // valid
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.none(),
                ExtractionStrategy.indexed(),
                List.of(new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null))
        ))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.none(),
                ExtractionStrategy.named(SomeComplexObject.class),
                List.of(new QueryParameter(EXTRACTION, 1, null, "name", null, null, null, null, null, null, null))
        ))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.indexed(),
                ExtractionStrategy.indexed(),
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null)
                )
        ))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.indexed(),
                ExtractionStrategy.named(SomeComplexObject.class),
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, "label", "name", null, null, null, null, null, null, null)
                )
        ))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.named(SomeComplexObject.class),
                ExtractionStrategy.indexed(),
                List.of(
                        new QueryParameter(INJECTION, 1, null, "name", null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null)
                )
        ))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.named(SomeComplexObject.class),
                ExtractionStrategy.named(SomeComplexObject.class),
                List.of(
                        new QueryParameter(INJECTION, 1, null, "name", null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, "label", null, null, null, null, null, null, null, null)
                )
        ))
                .doesNotThrowAnyException();
    }

    @Test
    public void testSelect_injection_params_indexed() {
        // injection params
        // - parameters present
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.indexed(),
                ExtractionStrategy.indexed(),
                List.of(new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("should have at least one parameter");
        // - properly indexed
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.indexed(),
                ExtractionStrategy.indexed(),
                List.of(
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(INJECTION, 3, null, null, null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("broken parameter indexing");
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.indexed(),
                ExtractionStrategy.indexed(),
                List.of(
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(INJECTION, 2, null, null, null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("broken parameter indexing");
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.indexed(),
                ExtractionStrategy.indexed(),
                List.of(
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(INJECTION, 0, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("broken parameter indexing");
        // - has no labels
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.indexed(),
                ExtractionStrategy.indexed(),
                List.of(
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(INJECTION, 1, "label", null, null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("should have no label");
    }

    @Test
    public void testSelect_injection_params_named() {
        // injection params
        // - parameters present
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.named(SomeComplexObject.class),
                ExtractionStrategy.indexed(),
                List.of(new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("should have at least one parameter");
        // - properly indexed
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.named(SomeComplexObject.class),
                ExtractionStrategy.indexed(),
                List.of(
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(INJECTION, 1, null, "prop1", null, null, null, null, null, null, null),
                        new QueryParameter(INJECTION, 3, null, "prop2", null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("broken parameter indexing");
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.named(SomeComplexObject.class),
                ExtractionStrategy.indexed(),
                List.of(
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(INJECTION, 2, null, "prop1", null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("broken parameter indexing");
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.named(SomeComplexObject.class),
                ExtractionStrategy.indexed(),
                List.of(
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(INJECTION, 0, null, "prop1", null, null, null, null, null, null, null),
                        new QueryParameter(INJECTION, 1, null, "prop2", null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("broken parameter indexing");
        // - has defined property name
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.named(SomeComplexObject.class),
                ExtractionStrategy.indexed(),
                List.of(
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("should have nonempty property name");
        // - has no labels
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.named(SomeComplexObject.class),
                ExtractionStrategy.indexed(),
                List.of(
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(INJECTION, 1, "label", "prop1", null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("should have no label");
    }

    @Test
    public void testSelect_extraction_params_indexed() {
        // extraction params
        // - parameters present
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.indexed(),
                ExtractionStrategy.indexed(),
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("should have at least one parameter");
        // - properly indexed without labels
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.indexed(),
                ExtractionStrategy.indexed(),
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, -3, null, null, null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("all indexes should be >0");
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.indexed(),
                ExtractionStrategy.indexed(),
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 0, null, null, null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("all indexes should be >0");
        // - all labelled if label present
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.indexed(),
                ExtractionStrategy.indexed(),
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, "label1", null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 2, null, null, null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("some parameters have labels, some don't");
    }

    @Test
    public void testSelect_extraction_params_named() {
        // extraction params
        // - parameters present
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.indexed(),
                ExtractionStrategy.named(SomeComplexObject.class),
                List.of(new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null))
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("should have at least one parameter");
        // - properly indexed without labels
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.indexed(),
                ExtractionStrategy.named(SomeComplexObject.class),
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, null, "prop1", null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, -3, null, "prop2", null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("all indexes should be >0");
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.indexed(),
                ExtractionStrategy.named(SomeComplexObject.class),
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 0, null, "prop1", null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("all indexes should be >0");
        // - has defined property name
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.indexed(),
                ExtractionStrategy.named(SomeComplexObject.class),
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, null, null, null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("should have nonempty property name");
        // - all labelled
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.indexed(),
                ExtractionStrategy.named(SomeComplexObject.class),
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, "label", "prop1", null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 2, null, "prop2", null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("some parameters have labels, some don't");
        // - has prop names (or label to be used instead)
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.indexed(),
                ExtractionStrategy.named(SomeComplexObject.class),
                List.of(
                        // if label is present then see previous case
                        // then all labels should be present
                        // then all prop names can be empty (label will be used instead)
                        // so case without labels
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, null, "prop1", null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 2, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 3, null, "prop3", null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("each parameter should have nonempty property name or label");
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.indexed(),
                ExtractionStrategy.named(SomeComplexObject.class),
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, null, "prop1", null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 2, null, "prop2", null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 3, null, "prop3", null, null, null, null, null, null, null)
                )
        ))
                .doesNotThrowAnyException();
        // - no duplicate prop names (or label to be used instead)
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.indexed(),
                ExtractionStrategy.named(SomeComplexObject.class),
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, "prop1", null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 2, "prop2", "prop2", null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 3, "prop3", "prop1", null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate property names found");
        assertThatCode(() -> validator.validateSelect(
                InjectionStrategy.indexed(),
                ExtractionStrategy.named(SomeComplexObject.class),
                List.of(
                        new QueryParameter(INJECTION, 1, null, null, null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 1, null, "prop1", null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 2, null, "prop2", null, null, null, null, null, null, null),
                        new QueryParameter(EXTRACTION, 3, null, "prop2", null, null, null, null, null, null, null)
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate property names found");
    }

    private static class SomeComplexObject {
        // just to use something as class in named strategies
        // not some nonsense like Object.class
    }

}
