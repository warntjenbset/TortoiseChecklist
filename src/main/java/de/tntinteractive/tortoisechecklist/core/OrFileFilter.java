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

public class OrFileFilter extends FileFilter {

    private final FileFilter f1;
    private final FileFilter f2;

    public OrFileFilter(final FileFilter f1, final FileFilter f2) {
        this.f1 = f1;
        this.f2 = f2;
    }

    @Override
    public boolean matches(final String wcRoot, final String path) {
        return this.f1.matches(wcRoot, path) || this.f2.matches(wcRoot, path);
    }

}
