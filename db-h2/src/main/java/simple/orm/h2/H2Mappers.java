package simple.orm.h2;

import io.vavr.Tuple;
import io.vavr.collection.List;
import simple.orm.h2.param.DecFloat;
import simple.orm.h2.type.ByteIntMapper;
import simple.orm.h2.type.DateStringMapper;
import simple.orm.h2.type.DecFloatMapper;
import simple.orm.h2.type.H2DateStringMapper;
import simple.orm.h2.type.ShortIntMapper;
import simple.orm.h2.type.StringBooleanMapper;
import simple.orm.mapping.type.AsIsTypeMapper;
import simple.orm.mapping.type.MappersCollection;
import simple.orm.mapping.type.SimpleTypeMapper;
import simple.orm.mapping.type.TypeMapper;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Collection of {@link TypeMapper}s for common use-cases.
 */
public final class H2Mappers {
    private H2Mappers() {
    }

    // Strings
    public static final TypeMapper<String, String> CHAR = new AsIsTypeMapper<>(H2Types.CHARACTER);
    public static final TypeMapper<String, String> VARCHAR = new AsIsTypeMapper<>(H2Types.CHARACTER_VARYING);
    public static final TypeMapper<String, String> VARCHAR_I = new AsIsTypeMapper<>(H2Types.VARCHAR_IGNORECASE);
    public static final TypeMapper<Clob, Clob> CLOB = new AsIsTypeMapper<>(H2Types.CHARACTER_LARGE_OBJECT);

    // Binary
    public static final TypeMapper<byte[], byte[]> BINARY = new AsIsTypeMapper<>(H2Types.BINARY);
    public static final TypeMapper<byte[], byte[]> VARBINARY = new AsIsTypeMapper<>(H2Types.BINARY_VARYING);
    public static final TypeMapper<Blob, Blob> BLOB = new AsIsTypeMapper<>(H2Types.BINARY_LARGE_OBJECT);

    // Primitives (boolean, numeric)
    public static final TypeMapper<Boolean, Boolean> BOOL = new AsIsTypeMapper<>(H2Types.BOOLEAN);
    public static final TypeMapper<String, Boolean> BOOL_STR_YN = new StringBooleanMapper(H2Types.CHARACTER, "Y", "N", false);
    public static final TypeMapper<Byte, Byte> TINYINT = new AsIsTypeMapper<>(H2Types.TINYINT);
    public static final TypeMapper<Byte, Integer> TINYINT_I = new ByteIntMapper(H2Types.TINYINT, false);
    public static final TypeMapper<Short, Short> SMALLINT = new AsIsTypeMapper<>(H2Types.SMALLINT);
    public static final TypeMapper<Short, Integer> SMALLINT_I = new ShortIntMapper(H2Types.SMALLINT, false);
    public static final TypeMapper<Integer, Integer> INT = new AsIsTypeMapper<>(H2Types.INTEGER);
    public static final TypeMapper<Long, Long> BIGINT = new AsIsTypeMapper<>(H2Types.BIGINT);
    public static final TypeMapper<BigDecimal, BigDecimal> NUMERIC = new AsIsTypeMapper<>(H2Types.NUMERIC);
    public static final TypeMapper<Float, Float> REAL = new AsIsTypeMapper<>(H2Types.REAL);
    public static final TypeMapper<Double, Double> DOUBLE = new AsIsTypeMapper<>(H2Types.DOUBLE_PRECISION);
    public static final TypeMapper<String, DecFloat> DECFLOAT = new DecFloatMapper();

    // Date/Time
    // - as-is
    public static final TypeMapper<LocalDate, LocalDate> DATE = new AsIsTypeMapper<>(H2Types.DATE);
    public static final TypeMapper<LocalTime, LocalTime> TIME = new AsIsTypeMapper<>(H2Types.TIME);
    public static final TypeMapper<OffsetTime, OffsetTime> TIME_TZ = new AsIsTypeMapper<>(H2Types.TIME_WITH_TIMEZONE);
    public static final TypeMapper<LocalDateTime, LocalDateTime> TIMESTAMP = new AsIsTypeMapper<>(H2Types.TIMESTAMP);
    public static final TypeMapper<OffsetDateTime, OffsetDateTime> TIMESTAMP_TZ = new AsIsTypeMapper<>(H2Types.TIMESTAMP_WITH_TIMEZONE);
    // - string, uses internal parsing/formatting classes of H2 database
    public static final TypeMapper<LocalDate, String> DATE_STR = H2DateStringMapper.date();
    public static final TypeMapper<LocalTime, String> TIME_STR = H2DateStringMapper.time();
    public static final TypeMapper<OffsetTime, String> TIME_STR_TZ = H2DateStringMapper.timeWithTZ();
    public static final TypeMapper<LocalDateTime, String> TIMESTAMP_STR = H2DateStringMapper.timestamp();
    public static final TypeMapper<OffsetDateTime, String> TIMESTAMP_STR_TZ = H2DateStringMapper.timestampWithTZ();
    // - string, ISO format as DateTimeFormatter understand it
    public static final TypeMapper<LocalDate, String> DATE_STR_ISO = DateStringMapper.date(DateTimeFormatter.ISO_LOCAL_DATE);
    public static final TypeMapper<LocalTime, String> TIME_STR_ISO = DateStringMapper.time(DateTimeFormatter.ISO_LOCAL_TIME);
    public static final TypeMapper<OffsetTime, String> TIME_STR_TZ_ISO = DateStringMapper.timeWithTZ(DateTimeFormatter.ISO_OFFSET_TIME);
    public static final TypeMapper<LocalDateTime, String> TIMESTAMP_STR_ISO = DateStringMapper.timestamp(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    public static final TypeMapper<OffsetDateTime, String> TIMESTAMP_STR_TZ_ISO = DateStringMapper.timestampWithTZ(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

    // UUID
    public static final TypeMapper<UUID, UUID> UUID = new AsIsTypeMapper<>(H2Types.UUID);
    // - UUID stored as UUID in database, but represented as String in java application
    public static final TypeMapper<UUID, String> UUID_JAVA_STR =
            new SimpleTypeMapper<>(
                    H2Types.UUID,
                    String.class,
                    java.util.UUID::fromString,
                    java.util.UUID::toString
            );
    // - UUID stored as CHAR/VARCHAR in database, but represented as UUID in java application
    public static final TypeMapper<String, UUID> UUID_DB_STR =
            new SimpleTypeMapper<>(
                    H2Types.CHARACTER,
                    UUID.class,
                    java.util.UUID::toString,
                    java.util.UUID::fromString
            );

    // ENUM
    // - ENUM data type represented as String in java application
    public static final TypeMapper<String, String> ENUM_STR = new AsIsTypeMapper<>(H2Types.ENUM);

    // some mappers are registered twice with tag and without tag as default type mapping for jdbcType<->javaType
    //@formatter:off
    private static final MappersCollection MAPPERS_COLLECTION = MappersCollection.of(List.of(
            Tuple.of(   "char",             CHAR,                   null        ),
            Tuple.of(   "varchar",          VARCHAR,                null        ),
            Tuple.of(   "varcharic",        VARCHAR_I,              null        ),
            Tuple.of(   "varchar",          VARCHAR_I,              "ic"        ),
            Tuple.of(   "clob",             CLOB,                   null        ),
            Tuple.of(   "binary",           BINARY,                 null        ),
            Tuple.of(   "varbinary",        VARBINARY,              null        ),
            Tuple.of(   "blob",             BLOB,                   null        ),
            Tuple.of(   "bool",             BOOL,                   null        ),
            Tuple.of(   "boolyn",           BOOL_STR_YN,            null        ),
            Tuple.of(   "bool",             BOOL_STR_YN,            "yn"        ),
            Tuple.of(   "tinyint",          TINYINT,                null        ),
            Tuple.of(   "tinyintint",       TINYINT_I,              null        ),
            Tuple.of(   "tinyint",          TINYINT_I,              "int"       ),
            Tuple.of(   "smallint",         SMALLINT,               null        ),
            Tuple.of(   "smallintint",      SMALLINT_I,             null        ),
            Tuple.of(   "smallint",         SMALLINT_I,             "int"       ),
            Tuple.of(   "int",              INT,                    null        ),
            Tuple.of(   "bigint",           BIGINT,                 null        ),
            Tuple.of(   "numeric",          NUMERIC,                null        ),
            Tuple.of(   "real",             REAL,                   null        ),
            Tuple.of(   "double",           DOUBLE,                 null        ),
            Tuple.of(   "decfloat",         DECFLOAT,               null        ),
            Tuple.of(   "date",             DATE,                   null        ),
            Tuple.of(   "time",             TIME,                   null        ),
            Tuple.of(   "timetz",           TIME_TZ,                null        ),
            Tuple.of(   "timestamp",        TIMESTAMP,              null        ),
            Tuple.of(   "timestamptz",      TIMESTAMP_TZ,           null        ),
            Tuple.of(   "datestr",          DATE_STR,               null        ),
            Tuple.of(   "timestr",          TIME_STR,               null        ),
            Tuple.of(   "timetzstr",        TIME_STR_TZ,            null        ),
            Tuple.of(   "timestampstr",     TIMESTAMP_STR,          null        ),
            Tuple.of(   "timestamptzstr",   TIMESTAMP_STR_TZ,       null        ),
            Tuple.of(   "date",             DATE_STR,               "h2str"     ),
            Tuple.of(   "time",             TIME_STR,               "h2str"     ),
            Tuple.of(   "timetz",           TIME_STR_TZ,            "h2str"     ),
            Tuple.of(   "timestamp",        TIMESTAMP_STR,          "h2str"     ),
            Tuple.of(   "timestamptz",      TIMESTAMP_STR_TZ,       "h2str"     ),
            Tuple.of(   "date",             DATE_STR_ISO,           "isostr"    ),
            Tuple.of(   "time",             TIME_STR_ISO,           "isostr"    ),
            Tuple.of(   "timetz",           TIME_STR_TZ_ISO,        "isostr"    ),
            Tuple.of(   "timestamp",        TIMESTAMP_STR_ISO,      "isostr"    ),
            Tuple.of(   "timestamptz",      TIMESTAMP_STR_TZ_ISO,   "isostr"    ),
            Tuple.of(   "uuid",             UUID,                   null        ),
            Tuple.of(   "uuiddbstr",        UUID_DB_STR,            null        ),
            Tuple.of(   "uuidjavastr",      UUID_JAVA_STR,          null        ),
            Tuple.of(   "uuid",             UUID_DB_STR,            "dbstr"     ),
            Tuple.of(   "uuid",             UUID_JAVA_STR,          "javastr"   ),
            Tuple.of(   "enumstr",          ENUM_STR,               null        ),
            Tuple.of(   "enum",             ENUM_STR,               "str"       )
    ));
    //@formatter:on

    public static MappersCollection collection() {
        return MAPPERS_COLLECTION;
    }


}
