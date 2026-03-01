package simple.orm.util;

/**
 * Some utility string methods.
 */
public final class StringUtils {
    private StringUtils() {
    }

    /**
     * A helper method to make strings <code>s="null"</code> and <code>s=null</code> distinguishable in output.
     * <br>
     * The method returns <code>'null'</code> if str is null, <code>"str"</code> otherwise.
     *
     * @param str a string.
     * @return <code>'null'</code> if str is null, <code>"str"</code> otherwise.
     */
    public static String quoteNonNull(String str) {
        return str == null ? "'null'" : ('"' + str + '"');
    }

    /**
     * Short alias method for {@link #quoteNonNull(String)}.
     *
     * @param str a string.
     * @return <code>'null'</code> if str is null, <code>"str"</code> otherwise.
     */
    public static String qnn(String str) {
        return quoteNonNull(str);
    }

}
