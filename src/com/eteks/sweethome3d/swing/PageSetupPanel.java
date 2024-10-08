/*
 * PageSetupPanel.java 27 aout 07
 *
 * Sweet Home 3D, Copyright (c) 2024 Space Mushrooms <info@sweethome3d.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.eteks.sweethome3d.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.eteks.sweethome3d.j3d.Component3DManager;
import com.eteks.sweethome3d.model.HomePrint;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.NullableSpinner.NullableSpinnerNumberModel;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.PageSetupController;
import com.eteks.sweethome3d.viewcontroller.View;

/**
 * Home page setup editing panel.
 * @author Emmanuel Puybaret
 */
public class PageSetupPanel extends JPanel implements DialogView {
  private static final int DEFAULT_SCALE = 100;

  private final PageSetupController controller;
  private PageFormat          pageFormat;
  private JButton             pageFormatButton;
  private JCheckBox           furniturePrintedCheckBox;
  private JCheckBox           planPrintedCheckBox;
  private JRadioButton        bestFitPlanScaleRadioButton;
  private JRadioButton        userPlanScaleRadioButton;
  private JSpinner            userPlanScaleSpinner;
  private JLabel              printedLevelsLabel;
  private JRadioButton        selectedLevelRadioButton;
  private JRadioButton        printedLevelsRadioButton;
  private JList               printedLevelsList;
  private JCheckBox           view3DPrintedCheckBox;
  private JLabel              headerFormatLabel;
  private JTextField          headerFormatTextField;
  private JLabel              footerFormatLabel;
  private JTextField          footerFormatTextField;
  private JLabel              variablesLabel;
  private JToolBar            variableButtonsToolBar;
  private String              dialogTitle;

  /**
   * Creates a panel that displays page setup.
   * @param controller the controller of this panel
   */
  public PageSetupPanel(UserPreferences preferences,
                        PageSetupController controller) {
    super(new GridBagLayout());
    this.controller = controller;
    createActions(preferences);
    createComponents(preferences, controller);
    setMnemonics(preferences);
    layoutComponents();
  }

  /**
   * Creates actions for variables.
   */
  private void createActions(UserPreferences preferences) {
    ActionMap actions = getActionMap();
    for (final HomePrintableComponent.Variable variable :
                      HomePrintableComponent.Variable.values()) {
      actions.put(variable,
          new ResourceAction(preferences, PageSetupPanel.class, "INSERT_" + variable.name()) {
            @Override
            public void actionPerformed(ActionEvent ev) {
              insertVariable(variable.getUserCode());
            }
          });
    }
  }

  /**
   * Inserts a code in the text field that has the focus and selects it.
   */
  private void insertVariable(String userCode) {
    Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    if (focusOwner instanceof JTextField) {
      JTextField textField = (JTextField)focusOwner;
      textField.replaceSelection(userCode);
      int lastCharacter = textField.getCaretPosition();
      int firstCharacter = lastCharacter - userCode.length();
      textField.select(firstCharacter, lastCharacter);
    }
  }

  /**
   * Creates and initializes components.
   */
  private void createComponents(UserPreferences preferences,
                                final PageSetupController controller) {
    this.pageFormatButton = new JButton(SwingTools.getLocalizedLabelText(preferences,
        PageSetupPanel.class, "pageFormatButton.text"));
    this.furniturePrintedCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences,
        PageSetupPanel.class, "furniturePrintedCheckBox.text"));
    this.planPrintedCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences,
        PageSetupPanel.class, "planPrintedCheckBox.text"));

    // Create scale radio buttons and user's scale spinner
    this.bestFitPlanScaleRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        PageSetupPanel.class, "bestFitPlanScaleRadioButton.text"));
    this.userPlanScaleRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        PageSetupPanel.class, "userPlanScaleRadioButton.text"));
    ButtonGroup scaleButtonsGroup = new ButtonGroup();
    scaleButtonsGroup.add(this.bestFitPlanScaleRadioButton);
    scaleButtonsGroup.add(this.userPlanScaleRadioButton);
    final NullableSpinner.NullableSpinnerNumberModel userPlanScaleSpinnerModel =
        new NullableSpinner.NullableSpinnerNumberModel(10, 1, 10000, 10);
    this.userPlanScaleSpinner = new AutoCommitSpinner(userPlanScaleSpinnerModel);

    this.printedLevelsLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        PageSetupPanel.class, "printedLevelsLabel.text"));
    this.selectedLevelRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        PageSetupPanel.class, "selectedLevelRadioButton.text"));
    this.printedLevelsRadioButton = new JRadioButton(SwingTools.getLocalizedLabelText(preferences,
        PageSetupPanel.class, "printedLevelsRadioButton.text"));
    ButtonGroup printedLevelsButtonsGroup = new ButtonGroup();
    printedLevelsButtonsGroup.add(this.selectedLevelRadioButton);
    printedLevelsButtonsGroup.add(this.printedLevelsRadioButton);
    List<Level> printableLevels = controller.getPrintableLevels();
    this.printedLevelsList = new JList(printableLevels.toArray());
    this.printedLevelsList.setCellRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
          return super.getListCellRendererComponent(list, ((Level)value).getName(), index, isSelected, cellHasFocus);
        }
      });
    this.printedLevelsList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    this.printedLevelsList.setVisibleRowCount(Math.min(printableLevels.size(), 5));

    this.view3DPrintedCheckBox = new JCheckBox(SwingTools.getLocalizedLabelText(preferences,
        PageSetupPanel.class, "view3DPrintedCheckBox.text"));

    this.headerFormatLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        PageSetupPanel.class, "headerFormatLabel.text"));
    this.headerFormatTextField = new JTextField(20);
    if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
      SwingTools.addAutoSelectionOnFocusGain(this.headerFormatTextField);
    }

    this.footerFormatLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
        PageSetupPanel.class, "footerFormatLabel.text"));
    this.footerFormatTextField = new JTextField(20);
    if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
      SwingTools.addAutoSelectionOnFocusGain(this.footerFormatTextField);
    }

    // Create variables buttons tool bar
    this.variablesLabel = new JLabel(preferences.getLocalizedString(
        PageSetupPanel.class, "variablesLabel.text"));
    this.variableButtonsToolBar = new JToolBar();
    this.variableButtonsToolBar.setFloatable(false);
    ActionMap actions = getActionMap();
    this.variableButtonsToolBar.add(actions.get(HomePrintableComponent.Variable.PAGE_NUMBER));
    this.variableButtonsToolBar.add(actions.get(HomePrintableComponent.Variable.PAGE_COUNT));
    this.variableButtonsToolBar.add(actions.get(HomePrintableComponent.Variable.PLAN_SCALE));
    this.variableButtonsToolBar.add(actions.get(HomePrintableComponent.Variable.DATE));
    this.variableButtonsToolBar.add(actions.get(HomePrintableComponent.Variable.TIME));
    this.variableButtonsToolBar.add(actions.get(HomePrintableComponent.Variable.HOME_PRESENTATION_NAME));
    this.variableButtonsToolBar.add(actions.get(HomePrintableComponent.Variable.HOME_NAME));
    this.variableButtonsToolBar.add(actions.get(HomePrintableComponent.Variable.LEVEL_NAME));
    for (int i = 0, n = this.variableButtonsToolBar.getComponentCount(); i < n; i++) {
      JComponent component = (JComponent)this.variableButtonsToolBar.getComponentAtIndex(i);
      // Remove focusable property on buttons
      component.setFocusable(false);
    }

    updateComponents(controller);

    final PropertyChangeListener printChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent ev) {
        updateComponents(controller);
      }
    };
    this.pageFormatButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          // Show the page setup dialog
          PrinterJob printerJob = PrinterJob.getPrinterJob();
          pageFormat = printerJob.pageDialog(pageFormat);
          updateController(controller);
        }
      });
    ItemListener itemListener = new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          updateController(controller);
        }
      };
    this.furniturePrintedCheckBox.addItemListener(itemListener);
    this.planPrintedCheckBox.addItemListener(itemListener);
    userPlanScaleSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          updateController(controller);
        }
      });
    this.bestFitPlanScaleRadioButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          updateController(controller);
        }
      });
    this.userPlanScaleRadioButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          if (userPlanScaleRadioButton.isSelected()
              && userPlanScaleSpinnerModel.getValue() == null) {
            userPlanScaleSpinnerModel.setValue(DEFAULT_SCALE);
          } else {
            updateController(controller);
          }
        }
      });
    this.selectedLevelRadioButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          selectedLevelRadioButton.removeActionListener(this);
          updateController(controller);
          selectedLevelRadioButton.addActionListener(this);
        }
      });
    this.printedLevelsRadioButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          printedLevelsRadioButton.removeActionListener(this);
          updateController(controller);
          printedLevelsRadioButton.addActionListener(this);
        }
      });
    this.printedLevelsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent ev) {
          printedLevelsList.getSelectionModel().removeListSelectionListener(this);
          updateController(controller);
          printedLevelsList.getSelectionModel().addListSelectionListener(this);
        }
      });
    this.view3DPrintedCheckBox.addItemListener(itemListener);
    controller.addPropertyChangeListener(PageSetupController.Property.PRINT, printChangeListener);

    DocumentListener documentListener = new DocumentListener() {
        public void changedUpdate(DocumentEvent ev) {
          controller.removePropertyChangeListener(PageSetupController.Property.PRINT, printChangeListener);
          updateController(controller);
          controller.addPropertyChangeListener(PageSetupController.Property.PRINT, printChangeListener);
        }

        public void insertUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }

        public void removeUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
      };
    this.headerFormatTextField.getDocument().addDocumentListener(documentListener);
    FocusListener textFieldFocusListener = new FocusListener() {
        public void focusGained(FocusEvent ev) {
          ActionMap actionMap = getActionMap();
          for (HomePrintableComponent.Variable field : HomePrintableComponent.Variable.values()) {
            actionMap.get(field).setEnabled(true);
          }
        }

        public void focusLost(FocusEvent ev) {
          ActionMap actionMap = getActionMap();
          for (HomePrintableComponent.Variable field : HomePrintableComponent.Variable.values()) {
            actionMap.get(field).setEnabled(false);
          }
        }
      };
    this.headerFormatTextField.addFocusListener(textFieldFocusListener);
    this.footerFormatTextField.getDocument().addDocumentListener(documentListener);
    this.footerFormatTextField.addFocusListener(textFieldFocusListener);

    this.dialogTitle = preferences.getLocalizedString(PageSetupPanel.class, "pageSetup.title");
  }

  /**
   * Updates components from <code>homePrint</code> attributes.
   */
  private void updateComponents(PageSetupController controller) {
    HomePrint homePrint = controller.getPrint();
    this.pageFormat = HomePrintableComponent.getPageFormat(homePrint);
    boolean no3D;
    try {
      no3D = Boolean.getBoolean("com.eteks.sweethome3d.no3D");
    } catch (AccessControlException ex) {
      // If com.eteks.sweethome3d.no3D property can't be read,
      // security manager won't allow to access to Java 3D DLLs required to print 3D images too
      no3D = true;
    }
    // Check if off screen image is supported
    boolean offscreenCanvas3DSupported;
    if (no3D) {
      offscreenCanvas3DSupported = false;
    } else {
      offscreenCanvas3DSupported = Component3DManager.getInstance().isOffScreenImageSupported();
    }
    final NullableSpinnerNumberModel userPlanScaleSpinnerModel =
        (NullableSpinner.NullableSpinnerNumberModel)this.userPlanScaleSpinner.getModel();
    List<Level> printableLevels = controller.getPrintableLevels();
    if (homePrint != null) {
      this.furniturePrintedCheckBox.setSelected(homePrint.isFurniturePrinted());
      this.planPrintedCheckBox.setSelected(homePrint.isPlanPrinted());
      this.bestFitPlanScaleRadioButton.setEnabled(homePrint.isPlanPrinted());
      this.userPlanScaleRadioButton.setEnabled(homePrint.isPlanPrinted());
      if (homePrint.getPlanScale() == null) {
        this.bestFitPlanScaleRadioButton.setSelected(true);
      } else {
        this.userPlanScaleRadioButton.setSelected(true);
      }
      this.userPlanScaleSpinner.setEnabled(homePrint.isPlanPrinted() && homePrint.getPlanScale() != null);
      List<Level> printedLevels = homePrint.getPrintedLevels();
      if (!this.selectedLevelRadioButton.isSelected()
          && !this.printedLevelsRadioButton.isSelected()) {
        this.selectedLevelRadioButton.setSelected(printedLevels == null);
        this.printedLevelsRadioButton.setSelected(printedLevels != null);
      }
      if (printedLevels != null) {
        int [] levelIndices = new int [printedLevels.size()];
        int i = 0;
        for (Level level : printedLevels) {
          levelIndices [i++] = printableLevels.indexOf(level);
        }
        this.printedLevelsList.setSelectedIndices(levelIndices);
      }
      this.view3DPrintedCheckBox.setSelected(homePrint.isView3DPrinted() && offscreenCanvas3DSupported);
      userPlanScaleSpinnerModel.setNullable(homePrint.getPlanScale() == null);
      userPlanScaleSpinnerModel.setValue(homePrint.getPlanScale() != null
          ? new Integer(Math.round(1 / homePrint.getPlanScale()))
          : null);
      String headerFormat = homePrint.getHeaderFormat();
      this.headerFormatTextField.setText(headerFormat != null ? headerFormat : "");
      String footerFormat = homePrint.getFooterFormat();
      this.footerFormatTextField.setText(footerFormat != null ? footerFormat : "");
    } else {
      this.furniturePrintedCheckBox.setSelected(true);
      this.planPrintedCheckBox.setSelected(true);
      this.bestFitPlanScaleRadioButton.setEnabled(true);
      this.bestFitPlanScaleRadioButton.setSelected(true);
      this.userPlanScaleRadioButton.setEnabled(true);
      this.userPlanScaleSpinner.setEnabled(false);
      this.selectedLevelRadioButton.setSelected(true);
      this.printedLevelsList.setEnabled(false);
      this.view3DPrintedCheckBox.setSelected(offscreenCanvas3DSupported);
      userPlanScaleSpinnerModel.setNullable(true);
      userPlanScaleSpinnerModel.setValue(null);
      this.headerFormatTextField.setText("");
      this.footerFormatTextField.setText("");
    }
    this.selectedLevelRadioButton.setEnabled(this.planPrintedCheckBox.isSelected() && !printableLevels.isEmpty());
    this.printedLevelsRadioButton.setEnabled(this.planPrintedCheckBox.isSelected() &&!printableLevels.isEmpty());
    this.printedLevelsList.setEnabled(this.printedLevelsRadioButton.isSelected());
    this.view3DPrintedCheckBox.setEnabled(offscreenCanvas3DSupported);
    this.view3DPrintedCheckBox.setVisible(!no3D);
  }

  /**
   * Updates controller print attributes.
   */
  public void updateController(PageSetupController controller) {
    // Return an HomePrint instance matching returnedPageFormat
    HomePrint.PaperOrientation paperOrientation;
    switch (this.pageFormat.getOrientation()) {
      case PageFormat.LANDSCAPE :
        paperOrientation = HomePrint.PaperOrientation.LANDSCAPE;
        break;
      case PageFormat.REVERSE_LANDSCAPE :
        paperOrientation = HomePrint.PaperOrientation.REVERSE_LANDSCAPE;
        break;
      default :
        paperOrientation = HomePrint.PaperOrientation.PORTRAIT;
        break;
    }
    Paper paper = this.pageFormat.getPaper();
    List<Level> printedLevels = new ArrayList<Level>();
    for (Object level : this.printedLevelsList.getSelectedValues()) {
      printedLevels.add((Level)level);
    }
    HomePrint homePrint = new HomePrint(paperOrientation, (float)paper.getWidth(), (float)paper.getHeight(),
        (float)paper.getImageableY(), (float)paper.getImageableX(),
        (float)(paper.getHeight() - paper.getImageableHeight() - paper.getImageableY()),
        (float)(paper.getWidth() - paper.getImageableWidth() - paper.getImageableX()),
        this.furniturePrintedCheckBox.isSelected(),
        this.planPrintedCheckBox.isSelected(),
        this.printedLevelsRadioButton.isSelected() && !printedLevels.isEmpty()
            ? printedLevels
            : null,
        this.view3DPrintedCheckBox.isSelected(),
        this.userPlanScaleRadioButton.isSelected() && this.userPlanScaleSpinner.getValue() != null
            ? 1f / ((Number)this.userPlanScaleSpinner.getValue()).intValue()
            : null,
        this.headerFormatTextField.getText().trim(),
        this.footerFormatTextField.getText().trim());
    controller.setPrint(homePrint);
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
    if (!OperatingSystem.isMacOSX()) {
      this.pageFormatButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PageSetupPanel.class, "pageFormatButton.mnemonic")).getKeyCode());
      this.furniturePrintedCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PageSetupPanel.class, "furniturePrintedCheckBox.mnemonic")).getKeyCode());
      this.planPrintedCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PageSetupPanel.class, "planPrintedCheckBox.mnemonic")).getKeyCode());
      this.selectedLevelRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PageSetupPanel.class, "selectedLevelRadioButton.mnemonic")).getKeyCode());
      this.printedLevelsRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PageSetupPanel.class, "printedLevelsRadioButton.mnemonic")).getKeyCode());
      this.view3DPrintedCheckBox.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PageSetupPanel.class, "view3DPrintedCheckBox.mnemonic")).getKeyCode());
      this.bestFitPlanScaleRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PageSetupPanel.class, "bestFitPlanScaleRadioButton.mnemonic")).getKeyCode());
      this.userPlanScaleRadioButton.setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PageSetupPanel.class, "userPlanScaleRadioButton.mnemonic")).getKeyCode());
      this.headerFormatLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PageSetupPanel.class, "headerFormatLabel.mnemonic")).getKeyCode());
      this.headerFormatLabel.setLabelFor(this.headerFormatTextField);
      this.footerFormatLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PageSetupPanel.class, "footerFormatLabel.mnemonic")).getKeyCode());
      this.footerFormatLabel.setLabelFor(this.footerFormatTextField);
    }
  }

  /**
   * Layouts panel components in panel with their labels.
   */
  private void layoutComponents() {
    int labelAlignment = OperatingSystem.isMacOSX()
        ? GridBagConstraints.LINE_END
        : GridBagConstraints.LINE_START;
    int standardGap = Math.round(5 * SwingTools.getResolutionScale());
    // First row
    JPanel topPanel = new JPanel(new GridBagLayout());
    topPanel.add(this.pageFormatButton, new GridBagConstraints(
        0, 0, 3, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.NONE, new Insets(0, 0, 10, 0) , 0, 0));
    Insets lastComponentInsets = new Insets(0, 0, standardGap, 0);
    // Furniture component
    topPanel.add(this.furniturePrintedCheckBox, new GridBagConstraints(
        0, 1, 3, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, lastComponentInsets , 0, 0));
    // Plan components
    topPanel.add(this.planPrintedCheckBox, new GridBagConstraints(
        0, 2, 3, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 2, 0), 0, 0));
    topPanel.add(this.bestFitPlanScaleRadioButton, new GridBagConstraints(
        0, 3, 3, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 20, 2, 0), 0, 0));
    topPanel.add(this.userPlanScaleRadioButton, new GridBagConstraints(
        0, 4, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 20, standardGap, 0), 0, 0));
    topPanel.add(this.userPlanScaleSpinner, new GridBagConstraints(
        1, 4, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, lastComponentInsets, 0, 0));
    if (this.printedLevelsList.getModel().getSize() > 1) {
      topPanel.add(this.printedLevelsLabel, new GridBagConstraints(
          0, 5, 1, 1, 0, 0, labelAlignment,
          GridBagConstraints.NONE, new Insets(0, 25, standardGap, standardGap), 0, 0));
      topPanel.add(this.selectedLevelRadioButton, new GridBagConstraints(
          1, 5, 2, 1, 0, 0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(0, 0, standardGap, 0), 0, 0));
      topPanel.add(this.printedLevelsRadioButton, new GridBagConstraints(
          1, 6, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
          GridBagConstraints.NONE, new Insets(0, 0, standardGap, 0), 0, 0));
      JScrollPane scrollPane = new JScrollPane(this.printedLevelsList);
      Dimension preferredSize = scrollPane.getPreferredSize();
      preferredSize.width = Math.min(preferredSize.width , Math.round(150 * SwingTools.getResolutionScale()));
      scrollPane.setPreferredSize(preferredSize);
      topPanel.add(scrollPane, new GridBagConstraints(
          2, 6, 1, 1, 1, 1, GridBagConstraints.CENTER,
          GridBagConstraints.BOTH, new Insets(3, standardGap, standardGap, 0), 0, 0));
    }
    // 3D view component
    topPanel.add(this.view3DPrintedCheckBox, new GridBagConstraints(
        0, 7, 3, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    add(topPanel, new GridBagConstraints(
        0, 0, 2, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, standardGap, 0), 0, 0));
    // Second row
    add(new JSeparator(), new GridBagConstraints(
        0, 1, 2, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, standardGap, 0), -10, 0));
    // Third row
    add(this.headerFormatLabel, new GridBagConstraints(
        0, 2, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    add(this.headerFormatTextField, new GridBagConstraints(
        1, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, lastComponentInsets, 0, 0));
    // Forth row
    add(this.footerFormatLabel, new GridBagConstraints(
        0, 3, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 0, standardGap, standardGap), 0, 0));
    add(this.footerFormatTextField, new GridBagConstraints(
        1, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.HORIZONTAL, lastComponentInsets, 0, 0));
    // Last row
    add(this.variablesLabel, new GridBagConstraints(
        0, 4, 1, 1, 0, 0, labelAlignment,
        GridBagConstraints.NONE, new Insets(0, 0, 0, standardGap), 0, 0));
    add(this.variableButtonsToolBar, new GridBagConstraints(
        1, 4, 1, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }

  /**
   * Displays this panel in a modal dialog box.
   */
  public void displayView(View parentView) {
    if (SwingTools.showConfirmDialog((JComponent)parentView,
            this, this.dialogTitle, this.pageFormatButton) == JOptionPane.OK_OPTION
        && this.controller != null) {
      this.controller.modifyPageSetup();
    }
  }
}
