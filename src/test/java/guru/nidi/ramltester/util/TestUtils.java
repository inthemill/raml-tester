/*
 * Copyright (C) 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.util;

import guru.nidi.ramltester.model.Values;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;

/**
 *
 */
public class TestUtils {
    private TestUtils() {
    }

    public static String getEnv(String name) {
        final String env = System.getenv(name);
        assumeThat("Environment variable " + name + " is not set, skipping test", env, notNullValue());
        return env;
    }

    public static void assertStringArrayMapEquals(Object[] expected, Values actual) {
        Values v = stringArrayMapOf(expected);
        assertEquals(v, actual);
    }

    public static Values stringArrayMapOf(Object... keysAndValues) {
        Values v = new Values();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            List<String> value = keysAndValues[i + 1] instanceof String
                    ? Arrays.asList((String) keysAndValues[i + 1])
                    : Arrays.asList((String[]) keysAndValues[i + 1]);
            v.addValues((String) keysAndValues[i], value);
        }
        return v;
    }

    public static Matcher<Number> biggerThan(final Number value) {
        return new TypeSafeMatcher<Number>() {
            @Override
            protected boolean matchesSafely(Number item) {
                return item.doubleValue() > value.doubleValue();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("A number bigger than ").appendValue(value);
            }
        };
    }

}
