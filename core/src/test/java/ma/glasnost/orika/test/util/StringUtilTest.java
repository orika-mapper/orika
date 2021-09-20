package ma.glasnost.orika.test.util;

import org.junit.Test;

import ma.glasnost.orika.impl.util.StringUtil;

import static ma.glasnost.orika.impl.util.StringUtil.toValidVariableName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StringUtilTest {
    private static final String FOO_UNCAP = "foo";
    private static final String FOO_CAP = "Foo";

    @Test
    public void to_valid_variable_name_replaces_all_invalid_characters_with_underscore() {
        assertEquals("foo_bar_baz", toValidVariableName("foo/bar baz"));
    }

    @Test
    public void to_valid_variable_name_add_underscore__when_string_start_with_number() {
        assertEquals("_42", toValidVariableName("42"));
    }

    /**
     * Remark: Copied from commons-lang3 v3.5
     */
    @Test
    public void testUnCapitalize() {
        assertNull(StringUtil.uncapitalize(null));

        assertEquals("uncapitalize(String) failed",
                FOO_UNCAP, StringUtil.uncapitalize(FOO_CAP));
        assertEquals("uncapitalize(string) failed",
                FOO_UNCAP, StringUtil.uncapitalize(FOO_UNCAP));
        assertEquals("uncapitalize(empty-string) failed",
                "", StringUtil.uncapitalize(""));
        assertEquals("uncapitalize(single-char-string) failed",
                "x", StringUtil.uncapitalize("X"));

        // Examples from uncapitalize Javadoc
        assertEquals("cat", StringUtil.uncapitalize("cat"));
        assertEquals("cat", StringUtil.uncapitalize("Cat"));
        assertEquals("cAT", StringUtil.uncapitalize("CAT"));
    }
}
