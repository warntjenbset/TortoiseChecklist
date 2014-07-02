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

import java.util.regex.Pattern;


/**
 * Muster für einen Pfad, angelehnt an Ant-Syntax.
 */
class PathPattern {

    private static abstract class PathPatternMatcher {

        public abstract boolean matches(final String[] parts, final int index);

    }

    private static final class FinalMatcher extends PathPatternMatcher {

        @Override
        public boolean matches(final String[] parts, final int index) {
            return index == parts.length;
        }

    }

    private static final class MultiDirMatcher extends PathPatternMatcher {

        private final PathPatternMatcher next;

        public MultiDirMatcher(final PathPatternMatcher next) {
            this.next = next;
        }

        @Override
        public boolean matches(final String[] parts, final int index) {
            for (int i = index; i < parts.length; i++) {
                if (this.next.matches(parts, i)) {
                    return true;
                }
            }
            return false;
        }

    }

    private static final class SinglePathItemMatcher extends PathPatternMatcher {

        private final PathPatternMatcher next;
        private final Pattern pattern;

        public SinglePathItemMatcher(final PathPatternMatcher next, final String part) {
            this.next = next;
            final StringBuilder regex = new StringBuilder();
            for (final char ch : part.toCharArray()) {
                if (ch == '*') {
                    regex.append(".*");
                } else {
                    regex.append(Pattern.quote(Character.toString(ch)));
                }
            }
            this.pattern = Pattern.compile(regex.toString());
        }

        @Override
        public boolean matches(final String[] parts, final int index) {
            if (parts.length <= index) {
                return false;
            }
            return this.pattern.matcher(parts[index]).matches()
                && this.next.matches(parts, index + 1);
        }

    }

    private final PathPatternMatcher rootMatcher;

    public PathPattern(final String pattern) {
        PathPatternMatcher cur = new FinalMatcher();
        final String[] patternParts = this.split(stripLeadingSlash(pattern));
        for (int i = patternParts.length - 1; i >= 0; i--) {
            final String part = patternParts[i];
            if (part.equals("**")) {
                cur = new MultiDirMatcher(cur);
            } else if (part.contains("**")) {
                throw new IllegalArgumentException("** not allowed in middle of a word. Invalid pattern: " + pattern);
            } else {
                cur = new SinglePathItemMatcher(cur, part);
            }
        }
        this.rootMatcher = cur;
    }

    private static String stripLeadingSlash(final String pattern) {
        return (pattern.startsWith("/") || pattern.startsWith("\\")) ? pattern.substring(1) : pattern;
    }

    public boolean matches(final String path) {
        final String[] parts = this.split(stripLeadingSlash(path));
        return this.rootMatcher.matches(parts, 0);
    }

    private String[] split(final String path) {
        return path.split("/|\\\\");
    }

}
