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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.tntinteractive.tortoisechecklist.core.ChecklistItem;
import de.tntinteractive.tortoisechecklist.core.QuestionView;

public class QuestionViewStub implements QuestionView {

    private int nextId;
    private final Set<String> openSources = new HashSet<>();
    private boolean allSourcesKnown;
    private final List<ChecklistItem> results = new ArrayList<>();

    @Override
    public synchronized String addSource(final String description) {
        assert !this.allSourcesKnown;
        final String id = Integer.toString(this.nextId++);
        this.openSources.add(id);
        return id;
    }

    @Override
    public synchronized void allSourcesAdded() {
        this.allSourcesKnown = true;
    }

    @Override
    public synchronized void setChecklistItemsForSource(final String id, final List<? extends ChecklistItem> items) {
        assert this.openSources.contains(id);
        this.openSources.remove(id);
        this.results.addAll(items);
    }

    public List<ChecklistItem> getResults() {
        assert this.allSourcesKnown;
        assert this.openSources.isEmpty();
        return this.results;
    }

}
