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
package de.tntinteractive.tortoisechecklist.plugins.checkstyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;

import de.tntinteractive.tortoisechecklist.core.ChecklistItem;

class AuditListenerAdapter implements AuditListener {

    private final String description;
    private final List<ChecklistItem> violations = new ArrayList<>();

    public AuditListenerAdapter(final String description) {
        this.description = description;
    }

    @Override
    public void auditStarted(final AuditEvent aEvt) {
    }

    @Override
    public void auditFinished(final AuditEvent aEvt) {
    }

    @Override
    public void fileStarted(final AuditEvent aEvt) {
    }

    @Override
    public void fileFinished(final AuditEvent aEvt) {
    }

    @Override
    public void addError(final AuditEvent aEvt) {
        this.addViolation(aEvt, aEvt.getMessage());
    }

    @Override
    public void addException(final AuditEvent aEvt, final Throwable aThrowable) {
        this.addViolation(aEvt, aEvt.getLocalizedMessage() + ", " + aThrowable);
    }

    private void addViolation(final AuditEvent aEvt, final String message) {
        final File f = new File(aEvt.getFileName());
        this.violations.add(ChecklistItem.createViolation(this.description + " in "
            + f.getName() +  ":" + aEvt.getLine() + " (" + f.getParent() + ")" + ":\n" + aEvt.getSourceName() + "\n" + message));
    }

    public List<? extends ChecklistItem> getViolations() {
        return this.violations;
    }

}
