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


public class ChecklistItem {

    private final String text;
    private final boolean isQuestion;

    ChecklistItem(final String text, final boolean isQuestion) {
        this.text = text;
        this.isQuestion = isQuestion;
    }

    public static ChecklistItem createQuestion(final String text) {
        return new ChecklistItem(text, true);
    }

    public static ChecklistItem createViolation(final String text) {
        return new ChecklistItem(text, false);
    }

    public final boolean isQuestion() {
        return this.isQuestion;
    }

    public final String getText() {
        return this.text;
    }

}
