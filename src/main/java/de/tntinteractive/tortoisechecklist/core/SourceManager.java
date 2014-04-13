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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class SourceManager {

    private final List<ChecklistItemSource> sources = new ArrayList<>();

    /**
     * Fügt eine weitere Quelle hinzu.
     * @return Die gerade hinzugefügte Quelle (für's Chaining).
     */
    public ChecklistItemSource add(final ChecklistItemSource source) {
        this.sources.add(source);
        return source;
    }

    public void evaluateSources(final String wcRoot, final List<String> relativePaths, final String commitComment,
            final ExecutorService executor, final QuestionView resultTarget) {
        for (final ChecklistItemSource source : this.sources) {
            final String id = resultTarget.addSource(source.getDescription());
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    final List<ChecklistItem> items = new ArrayList<>();
                    try {
                        items.addAll(source.checkFilterAndCreateChecklistItems(wcRoot, relativePaths, commitComment));
                    } catch (final Throwable t) {
                        t.printStackTrace();
                        items.add(ChecklistItem.createViolation("Exception in Source " + source + ": " + t));
                    }
                    resultTarget.setChecklistItemsForSource(id, items);
                }
            });
        }
        resultTarget.allSourcesAdded();
    }

}
