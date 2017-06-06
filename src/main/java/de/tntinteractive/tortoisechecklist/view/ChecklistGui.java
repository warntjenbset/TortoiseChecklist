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
package de.tntinteractive.tortoisechecklist.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import de.tntinteractive.tortoisechecklist.core.ChecklistItem;
import de.tntinteractive.tortoisechecklist.core.QuestionView;
import de.tntinteractive.tortoisechecklist.statistic.StatisticLogger;

public class ChecklistGui extends JDialog implements QuestionView {

    private static final int SCREEN_BORDER = 50;

    private static final ImageIcon OK_ICON = new ImageIcon(ChecklistGui.class.getResource("ok-icon.png"));
    private static final ImageIcon CANCEL_ICON = new ImageIcon(ChecklistGui.class.getResource("cancel-icon.png"));
    private static final ImageIcon WAIT_ICON = new ImageIcon(ChecklistGui.class.getResource("wait-icon.png"));
    private static final URL VIOLATION_ICON_URL = ChecklistGui.class.getResource("violation-icon.png");
    private static final ImageIcon VIOLATION_ICON = new ImageIcon(VIOLATION_ICON_URL);

    private static final long serialVersionUID = -4221875145350667269L;

    private final StatisticLogger logger;

    private final JPanel itemPanel;
    private boolean closedByOk;

    private final AtomicInteger sourceIdCounter = new AtomicInteger();
    private boolean allSourcesAdded;
    private final Map<String, JPanel> openSources = new HashMap<>();

    public ChecklistGui(final StatisticLogger logger) {
        super(null, "TortoiseChecklist", ModalityType.APPLICATION_MODAL);
        this.setMinimumSize(new Dimension(600, 400));

        this.logger = logger;

        this.itemPanel = new JPanel();
        this.itemPanel.setLayout(new BoxLayout(this.itemPanel, BoxLayout.PAGE_AXIS));

        final JPanel buttonPanel = this.initButtons();

        this.setLayout(new BorderLayout());
        this.add(new JScrollPane(this.itemPanel), BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

        this.addWindowFocusListener(new WindowFocusListener() {

            @Override
            public void windowLostFocus(final WindowEvent e) {
                logger.log("focus", "lost");
            }

            @Override
            public void windowGainedFocus(final WindowEvent e) {
                logger.log("focus", "gained");
            }
        });

        this.pack();
        this.positionNearMouse();
    }

    private JPanel initButtons() {
        final JButton okButton = new JButton("Commit durchführen", OK_ICON);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                ChecklistGui.this.checkAndClose();
            }
        });

        final JButton cancelButton = new JButton("Commit abbrechen", CANCEL_ICON);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                ChecklistGui.this.setVisible(false);
            }
        });

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    private void positionNearMouse() {
        final Point mousePosition = MouseInfo.getPointerInfo().getLocation();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int height = this.getHeight();
        final int width = this.getWidth();
        this.setLocation(
                determineWindowPosFromMousePos(screenSize.width, width, mousePosition.x),
                determineWindowPosFromMousePos(screenSize.height, height, mousePosition.y));
    }

    private static int determineWindowPosFromMousePos(final int screenSize, final int windowSize, final int mousePos) {
        return Math.max(SCREEN_BORDER, Math.min(screenSize - windowSize - SCREEN_BORDER, mousePos - windowSize / 2));
    }

    private void closeIfAllOkAndNoQuestions() {
        if (this.hasStillOpenSources()) {
            return;
        }
        if (!this.getMissingOks().isEmpty()) {
            return;
        }
        this.logger.log("autoClose", "");
        this.closedByOk = true;
        this.setVisible(false);
    }

    private boolean hasStillOpenSources() {
        return !(this.allSourcesAdded && this.openSources.isEmpty());
    }

    private List<String> getMissingOks() {
        final List<String> ret = new ArrayList<>();
        //ggf. später mal etwas schöner implementieren
        for (final Component sourceComponent : this.itemPanel.getComponents()) {
            final JPanel panel = (JPanel) sourceComponent;
            for (final Component checkComponent : panel.getComponents()) {
                if (checkComponent instanceof JCheckBox) {
                    final JCheckBox cb = (JCheckBox) checkComponent;
                    if (!cb.isSelected()) {
                        ret.add(cb.getText());
                    }
                } else {
                    ret.add(((JTextPane) checkComponent).getText());
                }
            }
        }
        return ret;
    }

    private void checkAndClose() {
        this.logger.log("clickOnOK", "");
        for (final String text : this.getMissingOks()) {
            final int result = JOptionPane.showConfirmDialog(
                    this,
                    "Ein Checklisteneintrag wurde nicht bearbeitet:\n"
                    + text
                    + "\n\nDrücken Sie OK, um trotzdem zu commiten.",
                    "Fehlende Prüfung",
                    JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) {
                return;
            }
        }

        this.closedByOk = true;
        this.setVisible(false);
    }

    public boolean isCommitConfirmed() {
        return this.closedByOk;
    }

    @Override
    public String addSource(final String description) {
        final String id = Integer.toString(this.sourceIdCounter.incrementAndGet());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final JPanel panel = ChecklistGui.this.createWaitingPanel(description);
                assert !ChecklistGui.this.allSourcesAdded;
                ChecklistGui.this.openSources.put(id, panel);
                ChecklistGui.this.itemPanel.add(panel);
                ChecklistGui.this.logger.log("addSource", id + "," + description);
            }
        });
        return id;
    }

    private JPanel createWaitingPanel(final String description) {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(new JLabel("Warte auf Ergebnisse: " + description, WAIT_ICON, SwingConstants.LEADING));
        return panel;
    }

    @Override
    public synchronized void allSourcesAdded() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ChecklistGui.this.allSourcesAdded = true;
                ChecklistGui.this.closeIfAllOkAndNoQuestions();
            }
        });
    }

    @Override
    public synchronized void setChecklistItemsForSource(final String id, final List<? extends ChecklistItem> items) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ChecklistGui.this.logSourceResult(id, items);

                final JPanel panelForSource = ChecklistGui.this.openSources.remove(id);
                if (items.isEmpty()) {
                    ChecklistGui.this.itemPanel.remove(panelForSource);
                } else {
                    ChecklistGui.this.fillWithItems(panelForSource, items);
                }
                ChecklistGui.this.closeIfAllOkAndNoQuestions();
                ChecklistGui.this.itemPanel.validate();
                ChecklistGui.this.itemPanel.repaint();
            }
        });
    }

    private void logSourceResult(final String id, final List<? extends ChecklistItem> items) {
        this.logger.log("itemCountForSource_" + id, Integer.toString(items.size()));
        for (final ChecklistItem item : items) {
            if (item.isQuestion()) {
                this.logger.log("questionForSource_" + id, item.getText());
            } else {
                this.logger.log("violationForSource_" + id, item.getText());
            }
        }
    }

    private void fillWithItems(final JPanel panel, final List<? extends ChecklistItem> items) {
        panel.removeAll();
        for (final ChecklistItem item : items) {
            panel.add(this.createChecklistItemComponent(item));
        }
    }

    private JComponent createChecklistItemComponent(final ChecklistItem item) {
        if (item.isQuestion()) {
            final JCheckBox box = new JCheckBox(this.format(item.getText()));
            box.setHorizontalTextPosition(SwingConstants.LEADING);
            box.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    ChecklistGui.this.logger.log(box.isSelected() ? "boxActivate" : "boxDeactivate", item.getText());
                }
            });
            box.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            return box;
        } else {
            final JTextPane textPane = new JTextPane();
            textPane.setContentType("text/html");
            textPane.setEditable(false);
            textPane.setBackground(null);
            textPane.insertIcon(VIOLATION_ICON);
            textPane.setText("<html>"
                                + "<img src=\"" + VIOLATION_ICON_URL.toString() + "\"></img>"
                                + "<b>" + this.format(item.getText()) + "</b>"
                            + "</html>");
            textPane.setPreferredSize(null);
            textPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            return textPane;
        }
    }

    private String format(final String text) {
        return text.replace("<", "&lt;").replace("\n", "<br>");
    }

}
