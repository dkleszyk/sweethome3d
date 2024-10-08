/*
 * PageSetupController.java 27 aout 07
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
package com.eteks.sweethome3d.viewcontroller;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePrint;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for home page setup view.
 * @author Emmanuel Puybaret
 */
public class PageSetupController implements Controller {
  /**
   * The property that may be edited by the view associated to this controller.
   */
  public enum Property {PRINT}

  private final Home                  home;
  private final UserPreferences       preferences;
  private final ViewFactory           viewFactory;
  private final UndoableEditSupport   undoSupport;
  private final PropertyChangeSupport propertyChangeSupport;
  private DialogView                  pageSetupView;

  private HomePrint print;

  /**
   * Creates the controller of page setup with undo support.
   */
  public PageSetupController(Home home,
                             UserPreferences preferences,
                             ViewFactory viewFactory,
                             UndoableEditSupport undoSupport) {
    this.home = home;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.undoSupport = undoSupport;
    this.propertyChangeSupport = new PropertyChangeSupport(this);

    setPrint(home.getPrint());
  }

  /**
   * Returns the view associated with this controller.
   */
  public DialogView getView() {
    // Create view lazily only once it's needed
    if (this.pageSetupView == null) {
      this.pageSetupView = this.viewFactory.createPageSetupView(this.preferences, this);
    }
    return this.pageSetupView;
  }

  /**
   * Displays the view controlled by this controller.
   */
  public void displayView(View parentView) {
    getView().displayView(parentView);
  }

  /**
   * Adds the property change <code>listener</code> in parameter to this controller.
   */
  public void addPropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(property.name(), listener);
  }

  /**
   * Removes the property change <code>listener</code> in parameter from this controller.
   */
  public void removePropertyChangeListener(Property property, PropertyChangeListener listener) {
    this.propertyChangeSupport.removePropertyChangeListener(property.name(), listener);
  }

  /**
   * Sets the edited print attributes.
   */
  public void setPrint(HomePrint print) {
    if (print != this.print) {
      HomePrint oldPrint = this.print;
      this.print = print;
      this.propertyChangeSupport.firePropertyChange(Property.PRINT.name(), oldPrint, print);
    }
  }

  /**
   * Returns the edited print attributes.
   */
  public HomePrint getPrint() {
    return this.print;
  }

  /**
   * Returns home printable levels.
   */
  public List<Level> getPrintableLevels() {
    return this.home.getLevels();
  }

  /**
   * Controls the modification of home print attributes.
   */
  public void modifyPageSetup() {
    HomePrint oldHomePrint = this.home.getPrint();
    HomePrint homePrint = getPrint();
    this.home.setPrint(homePrint);
    UndoableEdit undoableEdit = new HomePrintModificationUndoableEdit(
        this.home, this.preferences,oldHomePrint, homePrint);
    this.undoSupport.postEdit(undoableEdit);
  }

  /**
   * Undoable edit for home print modification. This class isn't anonymous to avoid
   * being bound to controller and its view.
   */
  private static class HomePrintModificationUndoableEdit extends LocalizedUndoableEdit {
    private final Home            home;
    private final HomePrint       oldHomePrint;
    private final HomePrint       homePrint;

    private HomePrintModificationUndoableEdit(Home home,
                                              UserPreferences preferences,
                                              HomePrint oldHomePrint,
                                              HomePrint homePrint) {
      super(preferences, PageSetupController.class, "undoPageSetupName");
      this.home = home;
      this.oldHomePrint = oldHomePrint;
      this.homePrint = homePrint;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      this.home.setPrint(this.oldHomePrint);
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      this.home.setPrint(this.homePrint);
    }
  }
}
