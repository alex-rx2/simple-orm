package simple.orm.h2;

import io.vavr.Tuple;
import io.vavr.collection.List;
import org.h2.api.H2Type;
import simple.orm.mapping.param.ParameterGetterImpl;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.param.ParameterSetterImpl;
import simple.orm.mapping.param.TypesCollection;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.UUID;

import static simple.orm.mapping.param.ParameterGetterImpl.wrapCheckWasNull;

/**
 * Collection of {@link ParameterJdbcType} types to be used with H2 data types.
 * <br>
 * This collection contains the most direct approach to working with H2 data types.
 */
public final class H2Types {
    private H2Types() {
    }

    // Note: as H2 treats CHAR and NCHAR as the same (both of them Unicode) I will use simpler "CHAR" notation
    // Note: same goes to all string related data types and JDBC API operations

    // CHARACTER
    public static final ParameterJdbcType<String> CHARACTER = ParameterJdbcType.of(
            JDBCType.CHAR,
            String.class,
            new ParameterGetterImpl<>(ResultSet::getString, ResultSet::getString),
            new ParameterSetterImpl<>(PreparedStatement::setString)
    );

    // CHARACTER VARYING
    public static final ParameterJdbcType<String> CHARACTER_VARYING = ParameterJdbcType.of(
            JDBCType.VARCHAR,
            String.class,
            new ParameterGetterImpl<>(ResultSet::getString, ResultSet::getString),
            new ParameterSetterImpl<>(PreparedStatement::setString)
    );

    // CHARACTER LARGE OBJECT
    public static final ParameterJdbcType<Clob> CHARACTER_LARGE_OBJECT = ParameterJdbcType.of(
            JDBCType.CLOB,
            Clob.class,
            new ParameterGetterImpl<>(ResultSet::getClob, ResultSet::getClob),
            new ParameterSetterImpl<>(PreparedStatement::setClob)
    );

    // VARCHAR_IGNORECASE
    public static final ParameterJdbcType<String> VARCHAR_IGNORECASE = ParameterJdbcType.of(
            H2Type.VARCHAR_IGNORECASE,
            String.class,
            new ParameterGetterImpl<>(ResultSet::getString, ResultSet::getString),
            new ParameterSetterImpl<>(PreparedStatement::setString)
    );

    // BINARY
    public static final ParameterJdbcType<byte[]> BINARY = ParameterJdbcType.of(
            JDBCType.BINARY,
            byte[].class,
            new ParameterGetterImpl<>(ResultSet::getBytes, ResultSet::getBytes),
            new ParameterSetterImpl<>(PreparedStatement::setBytes)
    );

    // BINARY VARYING
    public static final ParameterJdbcType<byte[]> BINARY_VARYING = ParameterJdbcType.of(
            JDBCType.VARBINARY,
            byte[].class,
            new ParameterGetterImpl<>(ResultSet::getBytes, ResultSet::getBytes),
            new ParameterSetterImpl<>(PreparedStatement::setBytes)
    );

    // BINARY LARGE OBJECT
    public static final ParameterJdbcType<Blob> BINARY_LARGE_OBJECT = ParameterJdbcType.of(
            JDBCType.BLOB,
            Blob.class,
            new ParameterGetterImpl<>(ResultSet::getBlob, ResultSet::getBlob),
            new ParameterSetterImpl<>(PreparedStatement::setBlob)
    );

    // BOOLEAN
    public static final ParameterJdbcType<Boolean> BOOLEAN = ParameterJdbcType.of(
            JDBCType.BOOLEAN,
            Boolean.class,
            new ParameterGetterImpl<>(
                    (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getBoolean, false),
                    (rs, label) -> wrapCheckWasNull(rs, label, ResultSet::getBoolean, false)
            ),
            new ParameterSetterImpl<>(
                    PreparedStatement::setBoolean,
                    (stmt, index) -> stmt.setNull(index, JDBCType.BOOLEAN.getVendorTypeNumber())
            )
    );

    // TINYINT
    public static final ParameterJdbcType<Byte> TINYINT = ParameterJdbcType.of(
            JDBCType.TINYINT,
            Byte.class,
            new ParameterGetterImpl<>(
                    (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getByte, (byte) 0),
                    (rs, label) -> wrapCheckWasNull(rs, label, ResultSet::getByte, (byte) 0)
            ),
            new ParameterSetterImpl<>(
                    PreparedStatement::setByte,
                    (stmt, index) -> stmt.setNull(index, JDBCType.TINYINT.getVendorTypeNumber())
            )
    );

    // SMALLINT
    public static final ParameterJdbcType<Short> SMALLINT = ParameterJdbcType.of(
            JDBCType.SMALLINT,
            Short.class,
            new ParameterGetterImpl<>(
                    (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getShort, (short) 0),
                    (rs, label) -> wrapCheckWasNull(rs, label, ResultSet::getShort, (short) 0)
            ),
            new ParameterSetterImpl<>(
                    PreparedStatement::setShort,
                    (stmt, index) -> stmt.setNull(index, JDBCType.SMALLINT.getVendorTypeNumber())
            )
    );

    // INTEGER
    public static final ParameterJdbcType<Integer> INTEGER = ParameterJdbcType.of(
            JDBCType.INTEGER,
            Integer.class,
            new ParameterGetterImpl<>(
                    (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getInt, 0),
                    (rs, label) -> wrapCheckWasNull(rs, label, ResultSet::getInt, 0)
            ),
            new ParameterSetterImpl<>(
                    PreparedStatement::setInt,
                    (stmt, index) -> stmt.setNull(index, JDBCType.INTEGER.getVendorTypeNumber())
            )
    );

    // BIGINT
    public static final ParameterJdbcType<Long> BIGINT = ParameterJdbcType.of(
            JDBCType.BIGINT,
            Long.class,
            new ParameterGetterImpl<>(
                    (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getLong, 0L),
                    (rs, label) -> wrapCheckWasNull(rs, label, ResultSet::getLong, 0L)
            ),
            new ParameterSetterImpl<>(
                    PreparedStatement::setLong,
                    (stmt, index) -> stmt.setNull(index, JDBCType.BIGINT.getVendorTypeNumber())
            )
    );

    // NUMERIC
    public static final ParameterJdbcType<BigDecimal> NUMERIC = ParameterJdbcType.of(
            JDBCType.NUMERIC,
            BigDecimal.class,
            new ParameterGetterImpl<>(ResultSet::getBigDecimal, ResultSet::getBigDecimal),
            new ParameterSetterImpl<>(PreparedStatement::setBigDecimal)
    );

    // REAL
    public static final ParameterJdbcType<Float> REAL = ParameterJdbcType.of(
            JDBCType.REAL,
            Float.class,
            new ParameterGetterImpl<>(
                    (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getFloat, 0.0f),
                    (rs, label) -> wrapCheckWasNull(rs, label, ResultSet::getFloat, 0.0f)
            ),
            new ParameterSetterImpl<>(
                    PreparedStatement::setFloat,
                    (stmt, index) -> stmt.setNull(index, JDBCType.REAL.getVendorTypeNumber())
            )
    );

    // DOUBLE PRECISION
    public static final ParameterJdbcType<Double> DOUBLE_PRECISION = ParameterJdbcType.of(
            JDBCType.DOUBLE,
            Double.class,
            new ParameterGetterImpl<>(
                    (rs, index) -> wrapCheckWasNull(rs, index, ResultSet::getDouble, 0.0),
                    (rs, label) -> wrapCheckWasNull(rs, label, ResultSet::getDouble, 0.0)
            ),
            new ParameterSetterImpl<>(
                    PreparedStatement::setDouble,
                    (stmt, index) -> stmt.setNull(index, JDBCType.DOUBLE.getVendorTypeNumber())
            )
    );

    // DECFLOAT
    public static final ParameterJdbcType<String> DECFLOAT = ParameterJdbcType.of(
            JDBCType.DECIMAL,
            String.class,
            new ParameterGetterImpl<>(ResultSet::getString, ResultSet::getString),
            new ParameterSetterImpl<>(PreparedStatement::setString)
    );

    // DATE
    public static final ParameterJdbcType<LocalDate> DATE = ParameterJdbcType.of(
            JDBCType.DATE,
            LocalDate.class,
            new ParameterGetterImpl<>(
                    (rs, index) -> rs.getObject(index, LocalDate.class),
                    (rs, label) -> rs.getObject(label, LocalDate.class)
            ),
            new ParameterSetterImpl<>(
                    (stmt, index, value) -> stmt.setObject(index, value, JDBCType.DATE.getVendorTypeNumber())
            )
    );

    // TIME
    public static final ParameterJdbcType<LocalTime> TIME = ParameterJdbcType.of(
            JDBCType.TIME,
            LocalTime.class,
            new ParameterGetterImpl<>(
                    (rs, index) -> rs.getObject(index, LocalTime.class),
                    (rs, label) -> rs.getObject(label, LocalTime.class)
            ),
            new ParameterSetterImpl<>(
                    (stmt, index, value) -> stmt.setObject(index, value, JDBCType.TIME.getVendorTypeNumber())
            )
    );

    // TIME WITH TIME ZONE
    public static final ParameterJdbcType<OffsetTime> TIME_WITH_TIMEZONE = ParameterJdbcType.of(
            JDBCType.TIME_WITH_TIMEZONE,
            OffsetTime.class,
            new ParameterGetterImpl<>(
                    (rs, index) -> rs.getObject(index, OffsetTime.class),
                    (rs, label) -> rs.getObject(label, OffsetTime.class)
            ),
            new ParameterSetterImpl<>(
                    (stmt, index, value) -> stmt.setObject(index, value, JDBCType.TIME_WITH_TIMEZONE.getVendorTypeNumber())
            )
    );

    // TIMESTAMP
    public static final ParameterJdbcType<LocalDateTime> TIMESTAMP = ParameterJdbcType.of(
            JDBCType.TIMESTAMP,
            LocalDateTime.class,
            new ParameterGetterImpl<>(
                    (rs, index) -> rs.getObject(index, LocalDateTime.class),
                    (rs, label) -> rs.getObject(label, LocalDateTime.class)
            ),
            new ParameterSetterImpl<>(
                    (stmt, index, value) -> stmt.setObject(index, value, JDBCType.TIMESTAMP.getVendorTypeNumber())
            )
    );

    // TIMESTAMP WITH TIME ZONE
    public static final ParameterJdbcType<OffsetDateTime> TIMESTAMP_WITH_TIMEZONE = ParameterJdbcType.of(
            JDBCType.TIMESTAMP_WITH_TIMEZONE,
            OffsetDateTime.class,
            new ParameterGetterImpl<>(
                    (rs, index) -> rs.getObject(index, OffsetDateTime.class),
                    (rs, label) -> rs.getObject(label, OffsetDateTime.class)
            ),
            new ParameterSetterImpl<>(
                    (stmt, index, value) -> stmt.setObject(index, value, JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber())
            )
    );

    // UUID
    public static final ParameterJdbcType<UUID> UUID = ParameterJdbcType.of(
            H2Type.UUID,
            UUID.class,
            new ParameterGetterImpl<>(
                    (rs, index) -> rs.getObject(index, UUID.class),
                    (rs, label) -> rs.getObject(label, UUID.class)
            ),
            new ParameterSetterImpl<>(
                    (stmt, index, value) -> stmt.setObject(index, value, H2Type.UUID)
            )
    );

    // ENUM
    public static final ParameterJdbcType<String> ENUM = ParameterJdbcType.of(
            H2Type.ENUM,
            String.class,
            new ParameterGetterImpl<>(ResultSet::getString, ResultSet::getString),
            new ParameterSetterImpl<>(PreparedStatement::setString)
    );

    //@formatter:off
    private static final TypesCollection TYPES_COLLECTION = TypesCollection.of(List.of(
            Tuple.of(   "CHAR",         CHARACTER                   ),
            Tuple.of(   "VARCHAR",      CHARACTER_VARYING           ),
            Tuple.of(   "CLOB",         CHARACTER_LARGE_OBJECT      ),
            Tuple.of(   "VARCHAR_I",    VARCHAR_IGNORECASE          ),
            Tuple.of(   "BINARY",       BINARY                      ),
            Tuple.of(   "VARBINARY",    BINARY_VARYING              ),
            Tuple.of(   "BLOB",         BINARY_LARGE_OBJECT         ),
            Tuple.of(   "BOOLEAN",      BOOLEAN                     ),
            Tuple.of(   "TINYINT",      TINYINT                     ),
            Tuple.of(   "SMALLINT",     SMALLINT                    ),
            Tuple.of(   "INT",          INTEGER                     ),
            Tuple.of(   "BIGINT",       BIGINT                      ),
            Tuple.of(   "NUMERIC",      NUMERIC                     ),
            Tuple.of(   "REAL",         REAL                        ),
            Tuple.of(   "DOUBLE",       DOUBLE_PRECISION            ),
            Tuple.of(   "DECFLOAT",     DECFLOAT                    ),
            Tuple.of(   "DATE",         DATE                        ),
            Tuple.of(   "TIME",         TIME                        ),
            Tuple.of(   "TIME_TZ",      TIME_WITH_TIMEZONE          ),
            Tuple.of(   "TIMESTAMP",    TIMESTAMP                   ),
            Tuple.of(   "TIMESTAMP_TZ", TIMESTAMP_WITH_TIMEZONE     ),
            Tuple.of(   "UUID",         UUID                        ),
            Tuple.of(   "ENUM",         ENUM                        )
    ));
    //@formatter:on

    public static TypesCollection collection() {
        return TYPES_COLLECTION;
    }

}
