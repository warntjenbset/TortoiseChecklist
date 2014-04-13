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

public interface QuestionView {

    /**
     * Registriert eine weitere Quelle.
     * @return Eine ID, mit der später die {@link ChecklistItem}s zur Quelle hinzugefügt werden können.
     */
    public abstract String addSource(String description);

    /**
     * Wird aufgerufen, wenn alle Quellen registriert wurden.
     * Danach darf {@link #addSource} nicht mehr aufgerufen werden.
     */
    public abstract void allSourcesAdded();

    /**
     * Teilt der Ansicht mit, dass für die Quelle mit der übergebenen ID die übergebenen Einträge bestimmt
     * wurden.
     */
    public abstract void setChecklistItemsForSource(String id, List<? extends ChecklistItem> items);

}
