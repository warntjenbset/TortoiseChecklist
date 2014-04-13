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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

import de.tntinteractive.tortoisechecklist.core.ChecklistItem;
import de.tntinteractive.tortoisechecklist.core.ChecklistItemSource;

public class CheckstyleSource extends ChecklistItemSource {

    private final String description;
    private final String pathToCheckConfig;

    public CheckstyleSource(final String description, final String pathToCheckConfig) {
        this.description = description;
        this.pathToCheckConfig = pathToCheckConfig;
    }

    @Override
    protected String getDescription() {
        return this.description;
    }

    @Override
    protected List<? extends ChecklistItem> createChecklistItems(
            final String wcRoot, final List<String> relativePaths, final String commitComment)
        throws CheckstyleException, ParserConfigurationException, SAXException, IOException, TransformerException {

        final Checker checker = new Checker();
        checker.setModuleClassLoader(Checker.class.getClassLoader());
        checker.configure(this.loadConfig(wcRoot));
        final AuditListenerAdapter listener = new AuditListenerAdapter(this.description);
        checker.addListener(listener);
        checker.process(this.toFiles(wcRoot, relativePaths));
        checker.destroy();
        return listener.getViolations();
    }

    private Configuration loadConfig(final String wcRoot)
        throws ParserConfigurationException, SAXException, IOException, CheckstyleException, TransformerException {

        final Document config = this.loadOriginalXmlConfig(wcRoot);
        this.removeFilteredChecks(config);
        final byte[] filteredConfig = this.serializeXml(config);

        return ConfigurationLoader.loadConfiguration(new InputSource(new ByteArrayInputStream(filteredConfig)),
                new PropertiesExpander(System.getProperties()), true);
    }

    private Document loadOriginalXmlConfig(final String wcRoot)
        throws ParserConfigurationException, SAXException, IOException {

        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return builder.parse(new File(wcRoot, this.pathToCheckConfig));
    }

    private void removeFilteredChecks(final Node node) {
        final NodeList children = node.getChildNodes();
        final List<Node> toRemove = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (this.isFiltered(child)) {
                toRemove.add(child);
            } else {
                this.removeFilteredChecks(child);
            }
        }
        for (final Node r : toRemove) {
            node.removeChild(r);
        }
    }

    private boolean isFiltered(final Node node) {
        final NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (child.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                final ProcessingInstruction pi = (ProcessingInstruction) child;
                if (pi.getTarget().equals("dontCheckOnCommit")) {
                    return true;
                }
            }
        }
        return false;
    }

    private byte[] serializeXml(final Document config) throws TransformerException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        if (config.getDoctype() != null) {
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, config.getDoctype().getPublicId());
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, config.getDoctype().getSystemId());
        }
        transformer.transform(new DOMSource(config), new StreamResult(out));
        return out.toByteArray();
    }

    private List<File> toFiles(final String wcRoot, final List<String> relativePaths) {
        final List<File> ret = new ArrayList<>();
        for (final String path : relativePaths) {
            ret.add(new File(wcRoot, path));
        }
        return ret;
    }

}
