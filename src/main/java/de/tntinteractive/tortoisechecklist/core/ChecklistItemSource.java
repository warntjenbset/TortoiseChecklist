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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ChecklistItemSource {

    private ChecklistItemSourceFilter filter;
    private FileFilter withFiles;

    /**
     * Wird von den Unterklassen implementiert, um den konkreten Algorithmus zur Bestimmung
     * der Checklisten-Einträge festzulegen.
     */
    protected abstract List<? extends ChecklistItem> createChecklistItems(
            final String wcRoot, final List<String> relativePaths, String commitComment) throws Exception;

    /**
     * Liefert die Beschreibung, die während des Ladens angezeigt wird.
     */
    protected abstract String getDescription();

    /**
     * Setzt die Bedingung, unter der die Einträge hinzugefügt werden sollen.
     * @pre Es wurde noch kein Filter gesetzt.
     */
    public void when(final ChecklistItemSourceFilter filter) {
        if (this.filter != null) {
            throw new IllegalStateException("filter ('when') has already been set");
        }
        this.filter = filter;
    }

    /**
     * Setzt die Bedingung, unter der die Einträge hinzugefügt werden sollen.
     * @pre Es wurde noch kein Filter gesetzt.
     */
    public void withFilesWhere(final FileFilter filter) {
        this.when(filter);
        this.withFiles = filter;
    }

    public List<? extends ChecklistItem> checkFilterAndCreateChecklistItems(
            final String wcRoot, final List<String> relativePaths, final String commitComment)
        throws Exception {
        List<String> filteredPaths;
        if (this.withFiles == null) {
            filteredPaths = relativePaths;
        } else {
            filteredPaths = this.determineFilteredPaths(wcRoot, relativePaths);
        }
        if (this.filter == null || this.filter.matches(wcRoot, filteredPaths, commitComment)) {
            return this.createChecklistItems(wcRoot, filteredPaths, commitComment);
        } else {
            return Collections.emptyList();
        }
    }

    private List<String> determineFilteredPaths(final String wcRoot, final List<String> relativePaths) {
        final List<String> result = new ArrayList<>();
        for (final String path : relativePaths) {
            if (this.withFiles.matches(wcRoot, path) && new File(wcRoot, path).exists()) {
                result.add(path);
            }
        }
        return result;
    }

}
