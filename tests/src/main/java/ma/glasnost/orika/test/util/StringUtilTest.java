package ma.glasnost.orika.test.util;

import org.junit.Test;

import static ma.glasnost.orika.impl.util.StringUtil.toValidVariableName;
import static org.junit.Assert.assertEquals;

public class StringUtilTest {

    @Test
    public void to_valid_variable_name_replaces_all_invalid_characters_with_underscore() {
        assertEquals("foo_bar_baz", toValidVariableName("foo/bar baz"));
    }

    @Test
    public void to_valid_variable_name_add_underscore__when_string_start_with_number() {
        assertEquals("_42", toValidVariableName("42"));
    }
}
