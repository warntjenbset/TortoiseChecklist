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
package de.tntinteractive.tortoisechecklist.plugins.findbugs;

import java.util.ArrayList;
import java.util.List;

import de.tntinteractive.tortoisechecklist.core.ChecklistItem;
import edu.umd.cs.findbugs.AbstractBugReporter;
import edu.umd.cs.findbugs.AnalysisError;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;

class ChecklistBugReporter extends AbstractBugReporter {

    private final String description;
    private final SortedBugCollection bugs = new SortedBugCollection();

    public ChecklistBugReporter(final String description) {
        this.description = description;
    }

    @Override
    public void observeClass(final ClassDescriptor arg0) {
    }

    @Override
    protected void doReportBug(final BugInstance bugInstance) {
        this.bugs.add(bugInstance);
    }

    @Override
    public void reportAnalysisError(final AnalysisError arg0) {
        System.err.println("analysis error " + arg0.getMessage());
        if (arg0.getException() != null) {
            arg0.getException().printStackTrace(System.err);
        }
    }

    @Override
    public void reportMissingClass(final String arg0) {
    }

    @Override
    public void finish() {
    }

    @Override
    public BugCollection getBugCollection() {
        return this.bugs;
    }

    public List<? extends ChecklistItem> toChecklistItems() {
        final List<ChecklistItem> items = new ArrayList<>();
        for (final BugInstance bug : this.bugs) {
            final SourceLineAnnotation line = bug.getPrimarySourceLineAnnotation();
            final String text = this.description + " in "
                    + line.getSourceFile() +  ":" + line.getStartLine() + " (" + line.getSourcePath() + ")" + ":\n"
                    + bug.getMessage();
                items.add(ChecklistItem.createViolation(text));
        }
        return items;
    }

}
