package simple.orm.jdbc.query;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Seq;

/**
 * Class holding parameter names of SQL query parameters with their index for insertion into PreparedStatement.
 */
@Deprecated
public class NamedParametersMap {

    private final Seq<Tuple2<Integer, String>> parameters;

    public NamedParametersMap(Seq<String> names) {
        parameters = names.zipWithIndex((name, idx) -> Tuple.of(idx + 1, name));
    }

    public Seq<Tuple2<Integer, String>> getParameters() {
        return parameters;
    }

}
