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

import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import de.tntinteractive.tortoisechecklist.core.ChecklistItem;
import de.tntinteractive.tortoisechecklist.core.ChecklistScript;
import de.tntinteractive.tortoisechecklist.core.SourceManager;

public class ChecklistScriptTest {

    private static List<ChecklistItem> evaluate(final String script, final String... files)
        throws Exception {
        final SourceManager sourceManager = new SourceManager();
        ChecklistScript.evaluate(script, Collections.singletonList(new BasicChecklistPlugin()), sourceManager);
        final ExecutorService e = new SynchronousExecutorService();
        final QuestionViewStub results = new QuestionViewStub();
        sourceManager.evaluateSources("C:\\wcRoot", Arrays.asList(files), "Kommentar", e, results);
        return results.getResults();
    }

    private static Matcher<List<ChecklistItem>> hasQuestions(final String... expected) {
        return new TypeSafeMatcher<List<ChecklistItem>>(List.class) {

            @Override
            public void describeTo(final Description description) {
                description.appendText("question texts ");
                description.appendValue(Arrays.asList(expected));
            }

            @Override
            protected boolean matchesSafely(final List<ChecklistItem> item) {
                return this.toStrings(item).equals(Arrays.asList(expected));
            }

            @Override
            protected void describeMismatchSafely(final List<ChecklistItem> item, final Description mismatchDescription) {
                mismatchDescription.appendValue(this.toStrings(item));
            }

            private List<String> toStrings(final List<ChecklistItem> item) {
                final List<String> actual = new ArrayList<>();
                for (final ChecklistItem q : item) {
                    if (q.isQuestion()) {
                        actual.add(q.getText());
                    }
                }
                return actual;
            }

        };
    }


    @Test
    public void testUnconditionalQuestion() throws Exception {
        final String script = "question('Sind alle Unit-Tests grün?')";
        final List<ChecklistItem> result = evaluate(script);
        assertThat(result, hasQuestions("Sind alle Unit-Tests grün?"));
    }

    @Test
    public void testTwoUnconditionalQuestions() throws Exception {
        final String script =
                "question('Sind alle Unit-Tests grün?')\n"
                + "question('Keine Systemtest-Warnungen?')";
        final List<ChecklistItem> result = evaluate(script);
        assertThat(result, hasQuestions("Sind alle Unit-Tests grün?", "Keine Systemtest-Warnungen?"));
    }

    @Test
    public void testConditionalQuestionIsUsed() throws Exception {
        final String script =
                "question('Schema-Checks ausgeführt?').when(pathMatches('**/*.xsd'))\n";
        final List<ChecklistItem> result = evaluate(script, "C:\\einTestpfad\\xml\\testschema.xsd");
        assertThat(result, hasQuestions("Schema-Checks ausgeführt?"));
    }

    @Test
    public void testConditionalQuestionIsNotUsed() throws Exception {
        final String script =
                "question('Schema-Checks ausgeführt?').when(pathMatches('**/*.xsd'))\n";
        final List<ChecklistItem> result = evaluate(script, "C:\\einTestpfad\\xml\\readme.txt");
        assertThat(result, hasQuestions());
    }

    @Test
    public void testConditionalQuestionWithTwoFiles() throws Exception {
        final String script =
                "question('Schema-Checks ausgeführt?').when(pathMatches('**/*.xsd'))\n";
        final List<ChecklistItem> result = evaluate(script, "C:\\einTestpfad\\xml\\readme.txt", "C:\\einTestpfad\\xml\\testschema.xsd");
        assertThat(result, hasQuestions("Schema-Checks ausgeführt?"));
    }

//    @Test
//    public void testUnconditionalAutomatic() throws Exception {
//        final String script = "automaticCheck('Automatische Prüfung', 'de.tntinteractive.tortoisechecklist.TestFactory', new Array('param1'))";
//        final List<ChecklistItem> result = evaluate(script);
//        assertThat(result, hasSize(1));
//        final AutomaticCheck check = (AutomaticCheck) result.get(0);
//        assertThat(check.getText(), is("Automatische Prüfung"));
//        assertThat(evaluate(check, "a", "b", "c"), contains("a", "b", "c", "param1"));
//    }
//
//    @Test
//    public void testConditionalAutomaticIsUsed() throws Exception {
//        final String script =
//                "automaticCheck('Automatische Prüfung', 'de.tntinteractive.tortoisechecklist.TestFactory', new Array('p')).whenPathMatches('**/*.xsd')\n";
//        final List<ChecklistItem> result = evaluate(script, "C:\\einTestpfad\\xml\\testschema.xsd");
//        assertThat(result, hasSize(1));
//        final AutomaticCheck check = (AutomaticCheck) result.get(0);
//        assertThat(check.getText(), is("Automatische Prüfung"));
//        assertThat(evaluate(check, "a", "b", "c"), contains("a", "b", "c", "p"));
//    }
//
//    @Test
//    public void testConditionalAutomaticIsNotUsed() throws Exception {
//        final String script =
//                "automaticCheck('Automatische Prüfung', 'de.tntinteractive.tortoisechecklist.TestFactory', new Array('p')).whenPathMatches('**/*.xsd')\n";
//        final List<ChecklistItem> result = evaluate(script, "C:\\einTestpfad\\xml\\readme.txt");
//        assertThat(result, hasSize(0));
//    }

}
