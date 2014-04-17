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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public final class ChecklistScript {

    private ChecklistScript() {
    }

    public static void evaluate(
            final String script, final Iterable<? extends ChecklistPlugin> plugins, final SourceManager sourceManager)
        throws ScriptException {

        if (script.isEmpty()) {
            return;
        }

        final StringBuilder fullScript = new StringBuilder();
        for (final ChecklistPlugin plugin : plugins) {
            fullScript.append(plugin.getJS());
        }
        fullScript.append(script);

        final ScriptEngineManager factory = new ScriptEngineManager();
        final ScriptEngine engine = factory.getEngineByName("JavaScript");
        engine.put("sourceManager", sourceManager);
        engine.eval(fullScript.toString());
    }

}
