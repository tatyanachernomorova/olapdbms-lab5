package org.dbmslabs.olap4;


import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RestUtils {

    public static final PathTrie.Decoder REST_DECODER = new PathTrie.Decoder() {
        @Override
        public String decode(String value) {
            return RestUtils.decodeComponent(value);
        }
    };

    public static String decodeComponent(final String s) {
        return decodeComponent(s, StandardCharsets.UTF_8, false);
    }

    private static String decodeComponent(final String s, final Charset charset, boolean plusAsSpace) {
        if (s == null) {
            return "";
        }
        final int size = s.length();
        if (!decodingNeeded(s, size, plusAsSpace)) {
            return s;
        }
        final byte[] buf = new byte[size];
        int pos = decode(s, size, buf, plusAsSpace);
        return new String(buf, 0, pos, charset);
    }

    private static int decode(String s, int size, byte[] buf, boolean plusAsSpace) {
        int pos = 0;  // position in `buf'.
        for (int i = 0; i < size; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '+':
                    buf[pos++] = (byte) (plusAsSpace ? ' ' : '+');  // "+" -> " "
                    break;
                case '%':
                    if (i == size - 1) {
                        throw new IllegalArgumentException("unterminated escape sequence at end of string: " + s);
                    }
                    c = s.charAt(++i);
                    if (c == '%') {
                        buf[pos++] = '%';  // "%%" -> "%"
                        break;
                    } else if (i == size - 1) {
                        throw new IllegalArgumentException("partial escape sequence at end of string: " + s);
                    }
                    c = decodeHexNibble(c);
                    final char c2 = decodeHexNibble(s.charAt(++i));
                    if (c == Character.MAX_VALUE || c2 == Character.MAX_VALUE) {
                        throw new IllegalArgumentException(
                                "invalid escape sequence `%" + s.charAt(i - 1)
                                        + s.charAt(i) + "' at index " + (i - 2)
                                        + " of: " + s);
                    }
                    c = (char) (c * 16 + c2);
                    // Fall through.
                default:
                    buf[pos++] = (byte) c;
                    break;
            }
        }
        return pos;
    }

    private static boolean decodingNeeded(String s, int size, boolean plusAsSpace) {
        boolean decodingNeeded = false;
        for (int i = 0; i < size; i++) {
            final char c = s.charAt(i);
            if (c == '%') {
                i++;  // We can skip at least one char, e.g. `%%'.
                decodingNeeded = true;
            } else if (plusAsSpace && c == '+') {
                decodingNeeded = true;
            }
        }
        return decodingNeeded;
    }


    private static char decodeHexNibble(final char c) {
        if ('0' <= c && c <= '9') {
            return (char) (c - '0');
        } else if ('a' <= c && c <= 'f') {
            return (char) (c - 'a' + 10);
        } else if ('A' <= c && c <= 'F') {
            return (char) (c - 'A' + 10);
        } else {
            return Character.MAX_VALUE;
        }
    }

    public static void decodeQueryString(String s, int fromIndex, Map<String, String> params) {
        if (fromIndex < 0) {
            return;
        }
        if (fromIndex >= s.length()) {
            return;
        }

        int queryStringLength = s.contains("#") ? s.indexOf("#") : s.length();

        String name = null;
        int pos = fromIndex; // Beginning of the unprocessed region
        int i;       // End of the unprocessed region
        char c = 0;  // Current character
        for (i = fromIndex; i < queryStringLength; i++) {
            c = s.charAt(i);
            if (c == '=' && name == null) {
                if (pos != i) {
                    name = decodeQueryStringParam(s.substring(pos, i));
                }
                pos = i + 1;
            } else if (c == '&' || c == ';') {
                if (name == null && pos != i) {
                    // We haven't seen an `=' so far but moved forward.
                    // Must be a param of the form '&a&' so add it with
                    // an empty value.
                    addParam(params, decodeQueryStringParam(s.substring(pos, i)), "");
                } else if (name != null) {
                    addParam(params, name, decodeQueryStringParam(s.substring(pos, i)));
                    name = null;
                }
                pos = i + 1;
            }
        }

        if (pos != i) {  // Are there characters we haven't dealt with?
            if (name == null) {     // Yes and we haven't seen any `='.
                addParam(params, decodeQueryStringParam(s.substring(pos, i)), "");
            } else {                // Yes and this must be the last value.
                addParam(params, name, decodeQueryStringParam(s.substring(pos, i)));
            }
        } else if (name != null) {  // Have we seen a name without value?
            addParam(params, name, "");
        }
    }

    private static String decodeQueryStringParam(final String s) {
        return decodeComponent(s, StandardCharsets.UTF_8, true);
    }

    private static void addParam(Map<String, String> params, String name, String value) {
        params.put(name, value);
    }

}
