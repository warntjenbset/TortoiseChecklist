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
package de.tntinteractive.tortoisechecklist.statistic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class FileStatisticLogger extends StatisticLogger {

    private final long sessionId;
    private final String user;
    private final Writer logWriter;

    public FileStatisticLogger(final long sessionId, final String user, final File logFile) throws IOException {
        this.sessionId = sessionId;
        this.user = user;
        this.logWriter = new OutputStreamWriter(new FileOutputStream(logFile, true), "UTF-8");
    }

    @Override
    public synchronized void log(final String category, final String message) {
        try {
            this.logWriter.write(Long.toString(this.sessionId));
            this.logWriter.write(';');
            this.logWriter.write(escape(this.user));
            this.logWriter.write(';');
            this.logWriter.write(Long.toString(System.currentTimeMillis()));
            this.logWriter.write(';');
            this.logWriter.write(escape(category));
            this.logWriter.write(';');
            this.logWriter.write(escape(message));
            this.logWriter.write('\n');
            this.logWriter.flush();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String escape(final String message) {
        if (message == null) {
            return "null";
        }
        return message.replace("\\", "\\\\").replace(";", "\\s").replace("\n", "\\n").replace("\r", "\\r");
    }

    @Override
    public void close() {
        try {
            this.logWriter.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
