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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tntinteractive.tortoisechecklist.core.ChecklistItem;
import de.tntinteractive.tortoisechecklist.core.ChecklistItemSource;
import edu.umd.cs.findbugs.ClassScreener;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.config.UserPreferences;

public class FindbugsSource extends ChecklistItemSource {

    private static final class BinaryInfo {
        private final Pattern pathPattern;
        private final String binDir;

        public BinaryInfo(final Pattern pathPattern, final String binDir) {
            this.pathPattern = pathPattern;
            this.binDir = binDir;
        }

        public void addIfMatches(final String wcRoot, final String path,
                final Set<File> relevantBinDirBuffer, final Set<String> classesBuffer) {
            final Matcher m = this.pathPattern.matcher(path);
            if (m.matches()) {
                final File project = new File(wcRoot, m.group(1));
                final File bin = new File(project, this.binDir);
                relevantBinDirBuffer.add(bin);

                final String className = m.group(2).replace(".java", "").replace('/', '.').replace('\\', '.');
                classesBuffer.add(className);
            }
        }
    }

    private final List<BinaryInfo> binaryInfos;
    private final String filterFile;

    public FindbugsSource(final String filterFile,
            final String pathPattern1, final String binDir1,
            final String pathPattern2, final String binDir2) {
        this.binaryInfos = new ArrayList<>();
        this.handleParam(pathPattern1, binDir1);
        this.handleParam(pathPattern2, binDir2);
        this.filterFile = filterFile;
    }

    private void handleParam(final String pathPattern, final String binDir) {
        if (pathPattern != null && binDir != null) {
            this.binaryInfos.add(new BinaryInfo(Pattern.compile(pathPattern), binDir));
        }
    }

    @Override
    protected String getDescription() {
        return "FindBugs";
    }

    @Override
    protected List<? extends ChecklistItem> createChecklistItems(final String wcRoot, final List<String> relativePaths,
            final String commitComment) throws Exception {

        final Set<File> relevantBinDirs = new LinkedHashSet<>();
        final Set<String> classes = new LinkedHashSet<>();
        this.determineBinDirsAndClasses(wcRoot, relativePaths, relevantBinDirs, classes);

        final ChecklistBugReporter bugs = new ChecklistBugReporter(this.getDescription());
        bugs.setPriorityThreshold(Priorities.LOW_PRIORITY);

        final FindBugs2 fb = new FindBugs2();
        fb.setDetectorFactoryCollection(DetectorFactoryCollection.instance());
        fb.setBugReporter(bugs);
        fb.setProject(this.createProject(relevantBinDirs));
        fb.setUserPreferences(UserPreferences.createDefaultUserPreferences());
        fb.setClassScreener(this.createClassScreener(classes));
        if (this.filterFile != null) {
            fb.addFilter(new File(wcRoot, this.filterFile).toString(), false);
        }
        fb.finishSettings();
        fb.execute();
        return bugs.toChecklistItems();
    }

    private ClassScreener createClassScreener(final Set<String> classes) {
        final ClassScreener classScreener = new ClassScreener();
        for (final String c : classes) {
            classScreener.addAllowedClass(c);
        }
        return classScreener;
    }

    private Project createProject(final Set<File> relevantBinDirs) {
        final Project project = new Project();
        for (final File f : relevantBinDirs) {
            if (f.exists()) {
                project.addFile(f.toString());
            }
        }
        this.addClasspathToProject(project);
        return project;
    }

    private void determineBinDirsAndClasses(
            final String wcRoot, final List<String> relativePaths,
            final Set<File> relevantBinDirs, final Set<String> classes) {

        for (final String path : relativePaths) {
            for (final BinaryInfo binInfo : this.binaryInfos) {
                binInfo.addIfMatches(wcRoot, path, relevantBinDirs, classes);
            }
        }
    }

    private void addClasspathToProject(final Project project) {
        for (final URL url : ((URLClassLoader) this.getClass().getClassLoader()).getURLs()) {
            project.addAuxClasspathEntry(url.getPath());
        }
    }

}
