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
package de.tntinteractive.tortoisechecklist.core;

import java.util.List;

public abstract class FileFilter extends ChecklistItemSourceFilter {

    public FileFilter without(final FileFilter f) {
        return new WithoutFileFilter(this, f);
    }

    @Override
    public final boolean matches(final String wcRoot, final List<String> relativePaths, final String commitComment) {
        for (final String path : relativePaths) {
            if (this.matches(wcRoot, path)) {
                return true;
            }
        }
        return false;
    }

    public abstract boolean matches(String wcRoot, String path);

}
