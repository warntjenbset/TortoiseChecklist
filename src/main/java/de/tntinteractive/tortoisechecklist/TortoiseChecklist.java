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
package de.tntinteractive.tortoisechecklist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import de.tntinteractive.tortoisechecklist.core.ChecklistPlugin;
import de.tntinteractive.tortoisechecklist.core.ChecklistScript;
import de.tntinteractive.tortoisechecklist.core.SourceManager;
import de.tntinteractive.tortoisechecklist.statistic.FileStatisticLogger;
import de.tntinteractive.tortoisechecklist.statistic.NullLogger;
import de.tntinteractive.tortoisechecklist.statistic.StatisticLogger;
import de.tntinteractive.tortoisechecklist.view.ChecklistGui;

public class TortoiseChecklist {

    private static final String CONFIG_PROPERTY = "tortoisechecklist.config.directory";
    private static final String LOGDIR_PROPERTY = "tortoisechecklist.statistic.directory";

    public static void main(final String[] args) {
        if (args.length != 4) {
            JOptionPane.showMessageDialog(null, "Muss als TortoiseSVN client side hook aufgerufen werden");
            System.exit(2);
        }

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread t, final Throwable e) {
                handleException(e);
            }
        });

        try {
            final StatisticLogger logger = createLogger();

            final List<String> rawPaths = readPathList(args[0]);
            final String wcRoot;
            if (rawPaths.isEmpty()) {
                wcRoot = args[3];
            } else {
                wcRoot = determineWcRoot(rawPaths.get(0));
            }
            final List<String> paths = makeRelativeToWc(wcRoot, rawPaths);

            final String message = readUtfFile(args[2]);
            logger.log("message", message);
            logger.log("fileCount", Integer.toString(paths.size()));

            final SourceManager sources = new SourceManager();
            final Iterable<? extends ChecklistPlugin> plugins = loadPlugins();
            for (final ChecklistPlugin plugin : plugins) {
                logger.log("activePlugin", plugin.getClass().getName());
            }
            ChecklistScript.evaluate(loadConfigScript(wcRoot), plugins, sources);

            final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
                @Override
                public Thread newThread(final Runnable r) {
                    final Thread t = new Thread(r);
                    t.setDaemon(true);
                    return t;
                }
            });

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    final ChecklistGui gui = new ChecklistGui(logger);
                    sources.evaluateSources(wcRoot, paths, message, executor, gui);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (gui.isCommitConfirmed()) {
                                //wenn sich alles schon automatisch erledigt hat, gar nicht mehr öffnen
                                logger.log("exitAutomatic", "");
                                logger.close();
                                System.exit(0);
                            }
                            gui.setVisible(true);
                            if (gui.isCommitConfirmed()) {
                                logger.log("exitOK", "");
                                logger.close();
                                System.exit(0);
                            } else {
                                logger.log("exitCancel", "");
                                logger.close();
                                System.err.println("Checkliste nicht erfüllt");
                                System.exit(1);
                            }
                        }
                    });
                }
            });
        } catch (final Throwable e) {
            handleException(e);
        }
    }

    private static Iterable<? extends ChecklistPlugin> loadPlugins() {
        return ServiceLoader.load(ChecklistPlugin.class);
    }

    private static void handleException(final Throwable e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, e.getMessage());
        System.exit(2);
    }

    private static StatisticLogger createLogger() throws IOException {
        final String logDir = System.getProperty(LOGDIR_PROPERTY);
        if (logDir == null) {
            return new NullLogger();
        } else {
            final Date d = new Date();
            final SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd.");
            final String user = getUserName();
            return new FileStatisticLogger(d.getTime(),
                    user,
                    new File(logDir, format.format(d) + user + ".log"));
        }
    }

    private static String getUserName() {
        final String trUser = System.getenv("TESTRUNNER_USER");
        if (trUser != null) {
            return trUser;
        }
        return System.getProperty("user.name");
    }

    private static String loadConfigScript(final String wcRoot) throws IOException {
        final File checklistFile = new File(getConfigDirectory(wcRoot), "checklist.global.js");
        return readUtfFile(checklistFile);
    }

    private static File getConfigDirectory(final String wcRoot) {
        final String configDir = System.getProperty(CONFIG_PROPERTY);
        if (configDir == null) {
            throw new IllegalArgumentException(String.format(
                    "Java-Property %s mit Pfad zum Konfigurationsverzeichnis nicht gesetzt.", CONFIG_PROPERTY));
        }

        File f = new File(configDir);
        if (!f.isAbsolute()) {
            f = new File(wcRoot, configDir);
        }
        return f;
    }

    private static List<String> readPathList(final String file) throws IOException {
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            final List<String> ret = new ArrayList<String>();
            String line;
            while ((line = r.readLine()) != null) {
                ret.add(line);
            }
            return ret;
        }
    }

    private static List<String> makeRelativeToWc(final String wcRoot, final List<String> pathList) {
        final List<String> ret = new ArrayList<>();
        for (final String oldPath : pathList) {
            assert oldPath.startsWith(wcRoot);
            ret.add(oldPath.substring(wcRoot.length()));
        }
        return ret;
    }

    private static String determineWcRoot(final String pathInWc) {
        File cur = new File(pathInWc);
        File potentialWc = null;
        do {
            if (containsSvnDir(cur)) {
                potentialWc = cur;
            } else {
                if (potentialWc != null) {
                    //das hier ist das erste Verzeichnis ohne .svn, also war der Vorgänger
                    //  die Working Copy Wurzel
                    break;
                }
            }
            cur = cur.getParentFile();
        } while (cur != null);
        return potentialWc.toString();
    }

    private static boolean containsSvnDir(final File cur) {
        final String[] content = cur.list();
        if (content == null) {
            return false;
        }
        for (final String filename : content) {
            if (filename.equals(".svn")) {
                return true;
            }
        }
        return false;
    }

    private static String readUtfFile(final File file) throws IOException {
        return readUtfFile(file.getPath());
    }

    private static String readUtfFile(final String file) throws IOException {
        try (Reader r = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
            final StringBuilder ret = new StringBuilder();
            int ch;
            while ((ch = r.read()) >= 0) {
                ret.append((char) ch);
            }
            return ret.toString();
        }
    }

}
