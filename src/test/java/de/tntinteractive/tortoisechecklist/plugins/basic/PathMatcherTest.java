/*
    Copyright (C) 2014  Tobias Baum <tbaum at tntinteractive.de>

    This file is a part of TortoiseChecklist.

    TortoiseChecklist is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TortoiseChecklist is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TortoiseChecklist.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tntinteractive.tortoisechecklist.plugins.basic;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

public class PathMatcherTest {

    private static Matcher<String> matchesPathPattern(final String pattern) {
        return new TypeSafeMatcher<String>() {
            @Override
            public void describeTo(final Description description) {
                description.appendText("matches path pattern ");
                description.appendValue(pattern);
            }
            @Override
            protected boolean matchesSafely(final String item) {
                return new PathPattern(pattern).matches(item);
            }
            @Override
            final protected void describeMismatchSafely(final String item, final Description description) {
                description.appendValue(item);
                description.appendText(" did not match");
            }
        };
    }

    @Test
    public void testFullMatch() {
        assertThat("test.txt", matchesPathPattern("test.txt"));
    }

    @Test
    public void testNoFullMatch() {
        assertThat("test2.txt", not(matchesPathPattern("test.txt")));
    }

    @Test
    public void testNoFullMatchWithSubdirectory() {
        assertThat("asdf/test.txt", not(matchesPathPattern("test.txt")));
    }

    @Test
    public void testWildcardMatch() {
        assertThat("test.txt", matchesPathPattern("*.txt"));
    }

    @Test
    public void testWildcardNoMatchWhenDifferentDirectory() {
        assertThat("dir/test.txt", not(matchesPathPattern("*.txt")));
    }

    @Test
    public void testWildcardNoMatchWhenDifferentExtension() {
        assertThat("testtxt", not(matchesPathPattern("*.txt")));
    }

    @Test
    public void testDoubleWildcardMatchWhenRoot() {
        assertThat("test.txt", matchesPathPattern("**/test.txt"));
    }

    @Test
    public void testDoubleWildcardMatchWhenSubDirectories() {
        assertThat("dir/test.txt", matchesPathPattern("**/test.txt"));
    }

    @Test
    public void testDoubleWildcardMatchWhenTwoSubDirectories() {
        assertThat("dir/sub/test.txt", matchesPathPattern("**/test.txt"));
    }

    @Test
    public void testDoubleWildcardNoMatchWhenDifferentExtension() {
        assertThat("testtxt", not(matchesPathPattern("**/test.txt")));
    }

    @Test
    public void testMatchesRegardlessOfSlashOrientation() {
        assertThat("dir/test.txt", matchesPathPattern("dir\\test.txt"));
        assertThat("dir\\test.txt", matchesPathPattern("dir/test.txt"));
        assertThat("dir\\test.txt", matchesPathPattern("dir\\test.txt"));
    }

}
