/*
 * FurnitureController.java 15 mai 2006
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

import java.awt.geom.Line2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.DoorOrWindow;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeDoorOrWindow;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomeLight;
import com.eteks.sweethome3d.model.ObjectProperty;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeShelfUnit;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.Light;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.ShelfUnit;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * A MVC controller for the home furniture table.
 * @author Emmanuel Puybaret
 */
public class FurnitureController implements Controller {
  private final Home                home;
  private final UserPreferences     preferences;
  private final ViewFactory         viewFactory;
  private final ContentManager      contentManager;
  private final UndoableEditSupport undoSupport;
  private View                      furnitureView;
  private HomePieceOfFurniture      leadSelectedPieceOfFurniture;

  /**
   * Creates the controller of home furniture view.
   * @param home the home edited by this controller and its view
   * @param preferences the preferences of the application
   * @param viewFactory a factory able to create the furniture view managed by this controller
   */
  public FurnitureController(Home home,
                             UserPreferences preferences,
                             ViewFactory viewFactory) {
    this(home, preferences, viewFactory, null, null);
  }

  /**
   * Creates the controller of home furniture view with undo support.
   */
  public FurnitureController(final Home home,
                             UserPreferences preferences,
                             ViewFactory viewFactory,
                             ContentManager contentManager,
                             UndoableEditSupport undoSupport) {
    this.home = home;
    this.preferences = preferences;
    this.viewFactory = viewFactory;
    this.undoSupport = undoSupport;
    this.contentManager = contentManager;

    addModelListeners();
  }

  /**
   * Returns the view associated with this controller.
   */
  public View getView() {
    // Create view lazily only once it's needed
    if (this.furnitureView == null) {
      this.furnitureView = this.viewFactory.createFurnitureView(this.home, this.preferences, this);
    }
    return this.furnitureView;
  }

  private void addModelListeners() {
    // Add a selection listener that gets the lead selected piece in home
    this.home.addSelectionListener(new SelectionListener() {
        public void selectionChanged(SelectionEvent ev) {
          List<HomePieceOfFurniture> selectedFurniture =
              Home.getFurnitureSubList(home.getSelectedItems());
          if (selectedFurniture.isEmpty()) {
            leadSelectedPieceOfFurniture = null;
          } else if (leadSelectedPieceOfFurniture == null
                     || selectedFurniture.size() == 1
                     || selectedFurniture.indexOf(leadSelectedPieceOfFurniture) == -1) {
            leadSelectedPieceOfFurniture = selectedFurniture.get(0);
          }
        }
      });

    // Add listener to update base plan lock when furniture movability changes
    final PropertyChangeListener furnitureChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (HomePieceOfFurniture.Property.MOVABLE.name().equals(ev.getPropertyName())) {
            // Remove non movable pieces from selection when base plan is locked
            HomePieceOfFurniture piece = (HomePieceOfFurniture)ev.getSource();
            if (home.isBasePlanLocked()
                && isPieceOfFurniturePartOfBasePlan(piece)) {
              List<Selectable> selectedItems = home.getSelectedItems();
              if (selectedItems.contains(piece)) {
                selectedItems = new ArrayList<Selectable>(selectedItems);
                selectedItems.remove(piece);
                home.setSelectedItems(selectedItems);
              }
            }
          }
        }
      };
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      piece.addPropertyChangeListener(furnitureChangeListener);
      if (piece instanceof HomeFurnitureGroup) {
        for (HomePieceOfFurniture childPiece : ((HomeFurnitureGroup)piece).getAllFurniture()) {
          childPiece.addPropertyChangeListener(furnitureChangeListener);
        }
      }
    }
    this.home.addFurnitureListener(new CollectionListener<HomePieceOfFurniture> () {
        public void collectionChanged(CollectionEvent<HomePieceOfFurniture> ev) {
          HomePieceOfFurniture piece = ev.getItem();
          if (ev.getType() == CollectionEvent.Type.ADD) {
            piece.addPropertyChangeListener(furnitureChangeListener);
            if (piece instanceof HomeFurnitureGroup) {
              for (HomePieceOfFurniture childPiece : ((HomeFurnitureGroup)piece).getAllFurniture()) {
                childPiece.addPropertyChangeListener(furnitureChangeListener);
              }
            }
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            piece.removePropertyChangeListener(furnitureChangeListener);
            if (piece instanceof HomeFurnitureGroup) {
              for (HomePieceOfFurniture childPiece : ((HomeFurnitureGroup)piece).getAllFurniture()) {
                childPiece.removePropertyChangeListener(furnitureChangeListener);
              }
            }
          }
        }
      });
  }

  /**
   * Controls new furniture added to home.
   * Once added the furniture will be selected in view
   * and undo support will receive a new undoable edit.
   * @param furniture the furniture to add.
   */
  public void addFurniture(List<HomePieceOfFurniture> furniture) {
    addFurniture(furniture, null, null, null);
  }

  /**
   * Controls new furniture added to home.
   * Once added the furniture will be selected in view
   * and undo support will receive a new undoable edit.
   * @param furniture the furniture to add.
   * @param beforePiece the piece before which the furniture will be added
   * @since 6.3
   */
  public void addFurniture(List<HomePieceOfFurniture> furniture, HomePieceOfFurniture beforePiece) {
    addFurniture(furniture, null, null, beforePiece);
  }

  /**
   * Controls new furniture added to the given group.
   * Once added the furniture will be selected in view
   * and undo support will receive a new undoable edit.
   * @param furniture the furniture to add.
   * @param group     the group to which furniture will be added.
   */
  public void addFurnitureToGroup(List<HomePieceOfFurniture> furniture, HomeFurnitureGroup group) {
    if (group == null) {
      throw new IllegalArgumentException("Group shouldn't be null");
    }
    addFurniture(furniture, null, group, null);
  }

  private void addFurniture(List<HomePieceOfFurniture> furniture, Level [] furnitureLevels,
                            HomeFurnitureGroup group, HomePieceOfFurniture beforePiece) {
    final boolean oldBasePlanLocked = this.home.isBasePlanLocked();
    final boolean allLevelsSelection = this.home.isAllLevelsSelection();
    final List<Selectable> oldSelection = this.home.getSelectedItems();
    final HomePieceOfFurniture [] newFurniture =
        furniture.toArray(new HomePieceOfFurniture [furniture.size()]);
    // Get indices of added furniture
    final int [] newFurnitureIndex = new int [furniture.size()];
    int insertIndex = group == null
        ? this.home.getFurniture().size()
        : group.getFurniture().size();
    if (beforePiece != null) {
      List<HomePieceOfFurniture> parentFurniture = this.home.getFurniture();
      group = getPieceOfFurnitureGroup(beforePiece, null, parentFurniture);
      if (group != null) {
        parentFurniture = group.getFurniture();
      }
      insertIndex = parentFurniture.indexOf(beforePiece);
    }
    final HomeFurnitureGroup [] newFurnitureGroups = group != null
        ? new HomeFurnitureGroup [furniture.size()]
        : null;
    boolean basePlanLocked = oldBasePlanLocked;
    boolean levelUpdated = group != null || furnitureLevels == null;
    for (int i = 0; i < newFurnitureIndex.length; i++) {
      newFurnitureIndex [i] = insertIndex++;
      // Unlock base plan if the piece is a part of it
      basePlanLocked &= !isPieceOfFurniturePartOfBasePlan(newFurniture [i]);
      if (furnitureLevels != null) {
        levelUpdated |= furnitureLevels [i] == null;
      }
      if (newFurnitureGroups != null) {
        newFurnitureGroups [i] = group;
      }
    }
    final Level [] newFurnitureLevels = levelUpdated ? null : furnitureLevels;
    final boolean newBasePlanLocked = basePlanLocked;
    final Level furnitureLevel = group != null
        ? group.getLevel()
        : this.home.getSelectedLevel();

    doAddFurniture(this.home, newFurniture, newFurnitureGroups, newFurnitureIndex, furnitureLevel, newFurnitureLevels, newBasePlanLocked, false);
    if (this.undoSupport != null) {
      this.undoSupport.postEdit(new FurnitureAdditionUndoableEdit(this.home, this.preferences,
          oldSelection.toArray(new Selectable [oldSelection.size()]), oldBasePlanLocked, allLevelsSelection, newFurniture,
          newFurnitureIndex, newFurnitureGroups, newFurnitureLevels, furnitureLevel, newBasePlanLocked));
    }
  }

  /**
   * Undoable edit for furniture added to home.
   */
  private static class FurnitureAdditionUndoableEdit extends LocalizedUndoableEdit {
    private final Home                    home;
    private final boolean                 allLevelsSelection;
    private final Selectable []           oldSelection;
    private final boolean                 oldBasePlanLocked;
    private final HomePieceOfFurniture [] newFurniture;
    private final int []                  newFurnitureIndex;
    private final HomeFurnitureGroup []   newFurnitureGroups;
    private final Level []                newFurnitureLevels;
    private final Level                   furnitureLevel;
    private final boolean                 newBasePlanLocked;

    public FurnitureAdditionUndoableEdit(Home home, UserPreferences preferences, Selectable[] oldSelection,
                                         boolean oldBasePlanLocked, boolean allLevelsSelection,
                                         HomePieceOfFurniture [] newFurniture, int [] newFurnitureIndex,
                                         HomeFurnitureGroup [] newFurnitureGroups, Level [] newFurnitureLevels,
                                         Level furnitureLevel, boolean newBasePlanLocked) {
      super(preferences, FurnitureController.class, "undoAddFurnitureName");
      this.home = home;
      this.oldSelection = oldSelection;
      this.oldBasePlanLocked = oldBasePlanLocked;
      this.allLevelsSelection = allLevelsSelection;
      this.newFurniture = newFurniture;
      this.newFurnitureIndex = newFurnitureIndex;
      this.newFurnitureGroups = newFurnitureGroups;
      this.newFurnitureLevels = newFurnitureLevels;
      this.furnitureLevel = furnitureLevel;
      this.newBasePlanLocked = newBasePlanLocked;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      doDeleteFurniture(this.home, this.newFurniture, this.oldBasePlanLocked, this.allLevelsSelection);
      this.home.setSelectedItems(Arrays.asList(this.oldSelection));
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      doAddFurniture(this.home, this.newFurniture, this.newFurnitureGroups, this.newFurnitureIndex, this.furnitureLevel,
          this.newFurnitureLevels, this.newBasePlanLocked, false);
    }
  }

  private static void doAddFurniture(Home home,
                                     HomePieceOfFurniture [] furniture,
                                     HomeFurnitureGroup [] furnitureGroups,
                                     int [] furnitureIndex,
                                     Level furnitureLevel,
                                     Level [] furnitureLevels,
                                     boolean basePlanLocked,
                                     boolean allLevelsSelection) {
    for (int i = 0; i < furnitureIndex.length; i++) {
      if (furnitureGroups != null && furnitureGroups [i] != null) {
        home.addPieceOfFurnitureToGroup(furniture [i], furnitureGroups [i], furnitureIndex [i]);
        furniture [i].setVisible(furnitureGroups [i].isVisible());
      } else {
        home.addPieceOfFurniture(furniture [i], furnitureIndex [i]);
      }
      furniture [i].setLevel(furnitureLevels != null ? furnitureLevels [i] : furnitureLevel);
    }
    home.setBasePlanLocked(basePlanLocked);
    home.setSelectedItems(Arrays.asList(furniture));
    home.setAllLevelsSelection(allLevelsSelection);
  }

  /**
   * Controls the deletion of the current selected furniture in home.
   * Once the selected furniture is deleted, undo support will receive a new undoable edit.
   */
  public void deleteSelection() {
    deleteFurniture(Home.getFurnitureSubList(this.home.getSelectedItems()));
  }

  /**
   * Deletes the furniture of <code>deletedFurniture</code> from home.
   * Once the selected furniture is deleted, undo support will receive a new undoable edit.
   */
  public void deleteFurniture(List<HomePieceOfFurniture> deletedFurniture) {
    final boolean basePlanLocked = this.home.isBasePlanLocked();
    final boolean allLevelsSelection = this.home.isAllLevelsSelection();
    final List<Selectable> oldSelection = this.home.getSelectedItems();
    List<HomePieceOfFurniture> homeFurniture = this.home.getFurniture();

    // Replace pieces by their group when they have to be all deleted
    deletedFurniture = new ArrayList<HomePieceOfFurniture>(deletedFurniture);
    List<HomeFurnitureGroup> homeGroups = new ArrayList<HomeFurnitureGroup>();
    searchGroups(homeFurniture, homeGroups);
    boolean updated;
    do {
      updated = false;
      for (HomeFurnitureGroup group : homeGroups) {
        List<HomePieceOfFurniture> groupFurniture = group.getFurniture();
        if (deletedFurniture.containsAll(groupFurniture)) {
          deletedFurniture.removeAll(groupFurniture);
          deletedFurniture.add(group);
          updated = true;
        }
      }
    } while (updated);

    // Sort the deletable furniture in the ascending order of their index in home or their group
    Map<HomeFurnitureGroup, TreeMap<Integer, HomePieceOfFurniture>> deletedFurnitureMap =
        new HashMap<HomeFurnitureGroup, TreeMap<Integer, HomePieceOfFurniture>>();
    int deletedFurnitureCount = 0;
    for (HomePieceOfFurniture piece : deletedFurniture) {
      // Check piece is deletable and doesn't belong to a group
      if (isPieceOfFurnitureDeletable(piece)) {
        HomeFurnitureGroup group = getPieceOfFurnitureGroup(piece, null, homeFurniture);
        TreeMap<Integer, HomePieceOfFurniture> sortedMap = deletedFurnitureMap.get(group);
        if (sortedMap == null) {
          sortedMap = new TreeMap<Integer, HomePieceOfFurniture>();
          deletedFurnitureMap.put(group, sortedMap);
        }
        if (group == null) {
          sortedMap.put(homeFurniture.indexOf(piece), piece);
        } else {
          sortedMap.put(group.getFurniture().indexOf(piece), piece);
        }
        deletedFurnitureCount++;
      }
    }
    final HomePieceOfFurniture [] furniture = new HomePieceOfFurniture [deletedFurnitureCount];
    final int [] furnitureIndex = new int [furniture.length];
    final Level [] furnitureLevels = new Level [furniture.length];
    final HomeFurnitureGroup [] furnitureGroups = new HomeFurnitureGroup [furniture.length];
    int i = 0;
    for (Map.Entry<HomeFurnitureGroup, TreeMap<Integer, HomePieceOfFurniture>> sortedMapEntry : deletedFurnitureMap.entrySet()) {
      for (Map.Entry<Integer, HomePieceOfFurniture> pieceEntry : sortedMapEntry.getValue().entrySet()) {
        furniture [i] = pieceEntry.getValue();
        furnitureIndex [i] = pieceEntry.getKey();
        furnitureLevels [i] = furniture [i].getLevel();
        furnitureGroups [i++] = sortedMapEntry.getKey();
      }
    }
    doDeleteFurniture(this.home, furniture, basePlanLocked, false);
    if (this.undoSupport != null) {
      this.undoSupport.postEdit(new FurnitureDeletionUndoableEdit(this.home, this.preferences,
          oldSelection.toArray(new Selectable [oldSelection.size()]), basePlanLocked, allLevelsSelection,
          furniture, furnitureIndex, furnitureGroups, furnitureLevels));
    }
  }

  /**
   * Undoable edit for furniture deleted from home.
   */
  private static class FurnitureDeletionUndoableEdit extends LocalizedUndoableEdit {
    private final Home                    home;
    private final Selectable []           oldSelection;
    private final boolean                 basePlanLocked;
    private final boolean                 allLevelsSelection;
    private final HomePieceOfFurniture [] furniture;
    private final int []                  furnitureIndex;
    private final HomeFurnitureGroup []   furnitureGroups;
    private final Level []                furnitureLevels;

    public FurnitureDeletionUndoableEdit(Home home, UserPreferences preferences,
                                         Selectable [] oldSelection, boolean basePlanLocked,
                                         boolean allLevelsSelection, HomePieceOfFurniture [] furniture,
                                         int [] furnitureIndex, HomeFurnitureGroup [] furnitureGroups,
                                         Level [] furnitureLevels) {
      super(preferences, FurnitureController.class, "undoDeleteSelectionName");
      this.home = home;
      this.oldSelection = oldSelection;
      this.basePlanLocked = basePlanLocked;
      this.allLevelsSelection = allLevelsSelection;
      this.furniture = furniture;
      this.furnitureIndex = furnitureIndex;
      this.furnitureGroups = furnitureGroups;
      this.furnitureLevels = furnitureLevels;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      doAddFurniture(this.home, this.furniture, this.furnitureGroups, this.furnitureIndex, null,
          this.furnitureLevels, this.basePlanLocked, this.allLevelsSelection);
      this.home.setSelectedItems(Arrays.asList(this.oldSelection));
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      this.home.setSelectedItems(Arrays.asList(this.furniture));
      doDeleteFurniture(this.home, this.furniture, this.basePlanLocked, false);
    }
  }

  private static void doDeleteFurniture(Home home,
                                        HomePieceOfFurniture [] furniture,
                                        boolean basePlanLocked,
                                        boolean allLevelsSelection) {
    for (HomePieceOfFurniture piece : furniture) {
      home.deletePieceOfFurniture(piece);
    }
    home.setBasePlanLocked(basePlanLocked);
    home.setAllLevelsSelection(allLevelsSelection);
  }

  /**
   * Searches all the groups among furniture and its children.
   */
  private static void searchGroups(List<HomePieceOfFurniture> furniture,
                                   List<HomeFurnitureGroup> groups) {
    for (HomePieceOfFurniture piece : furniture) {
      if (piece instanceof HomeFurnitureGroup) {
        groups.add((HomeFurnitureGroup)piece);
        searchGroups(((HomeFurnitureGroup)piece).getFurniture(), groups);
      }
    }
  }

  /**
   * Returns the furniture group that contains the given <code>piece</code> or <code>null</code> if it can't be found.
   */
  private static HomeFurnitureGroup getPieceOfFurnitureGroup(HomePieceOfFurniture piece,
                                                             HomeFurnitureGroup furnitureGroup,
                                                             List<HomePieceOfFurniture> furniture) {
    for (HomePieceOfFurniture homePiece : furniture) {
      if (homePiece.equals(piece)) {
        return furnitureGroup;
      } else if (homePiece instanceof HomeFurnitureGroup) {
        HomeFurnitureGroup group = getPieceOfFurnitureGroup(piece,
            (HomeFurnitureGroup)homePiece, ((HomeFurnitureGroup)homePiece).getFurniture());
        if (group != null) {
          return group;
        }
      }
    }
    return null;
  }

  /**
   * Reorders the selected furniture in home to place it before the given piece.
   * @since 6.3
   */
  public void moveSelectedFurnitureBefore(HomePieceOfFurniture beforePiece) {
    List<HomePieceOfFurniture> movedFurniture = Home.getFurnitureSubList(this.home.getSelectedItems());
    if (!movedFurniture.isEmpty()) {
      // Store current level of the furniture
      final Level [] furnitureLevels = new Level [movedFurniture.size()];
      for (int i = 0; i < furnitureLevels.length; i++) {
        furnitureLevels [i] = movedFurniture.get(i).getLevel();
      }
      this.undoSupport.beginUpdate();
      deleteFurniture(movedFurniture);
      addFurniture(movedFurniture, furnitureLevels, null, beforePiece);
      undoSupport.postEdit(new LocalizedUndoableEdit(this.preferences, FurnitureController.class, "undoReorderName"));
      // End compound edit
      undoSupport.endUpdate();
    }
  }

  /**
   * Updates the selected furniture in home.
   */
  public void setSelectedFurniture(List<HomePieceOfFurniture> selectedFurniture) {
    setSelectedFurniture(selectedFurniture, true);
  }

  /**
   * Updates the selected furniture in home, unselecting all other kinds of selected objects
   * when <code>resetSelection</code> is <code>true</code>.
   * @since 6.1
   */
  public void setSelectedFurniture(List<HomePieceOfFurniture> selectedFurniture, boolean resetSelection) {
    if (this.home.isBasePlanLocked()) {
      selectedFurniture = getFurnitureNotPartOfBasePlan(selectedFurniture);
    }
    if (resetSelection) {
      this.home.setSelectedItems(selectedFurniture);
      this.home.setAllLevelsSelection(false);
    } else {
      List<Selectable> selectedItems = new ArrayList<Selectable>(this.home.getSelectedItems());
      selectedFurniture = new ArrayList<HomePieceOfFurniture>(selectedFurniture);
      for (int i = selectedItems.size() - 1; i >= 0; i--) {
        Selectable item = selectedItems.get(i);
        if (item instanceof HomePieceOfFurniture) {
          int index = selectedFurniture.indexOf((HomePieceOfFurniture)item);
          if (index >= 0) {
            selectedFurniture.remove(index);
          } else {
            selectedItems.remove(i);
          }
        }
      }
      selectedItems.addAll(selectedFurniture);
      this.home.setSelectedItems(selectedItems);
    }
  }

  /**
   * Selects all furniture in home.
   */
  public void selectAll() {
    setSelectedFurniture(this.home.getFurniture());
  }

  /**
   * Returns <code>true</code> if the given <code>piece</code> isn't movable.
   */
  protected boolean isPieceOfFurniturePartOfBasePlan(HomePieceOfFurniture piece) {
    return !piece.isMovable() || piece.isDoorOrWindow();
  }

  /**
   * Returns <code>true</code> if the given <code>piece</code> may be moved.
   * Default implementation always returns <code>true</code>.
   */
  protected boolean isPieceOfFurnitureMovable(HomePieceOfFurniture piece) {
    return true;
  }

  /**
   * Returns <code>true</code> if the given <code>piece</code> may be deleted.
   * Default implementation always returns <code>true</code>.
   */
  protected boolean isPieceOfFurnitureDeletable(HomePieceOfFurniture piece) {
    return true;
  }

  /**
   * Returns a new home piece of furniture created from an other given <code>piece</code> of furniture.
   */
  public HomePieceOfFurniture createHomePieceOfFurniture(PieceOfFurniture piece) {
    // Don't copy the model preset deformations properties
    List<String> properties = new ArrayList<String>(piece.getPropertyNames());
    for (int i = properties.size() - 1; i >= 0; i--) {
      String property = properties.get(i);
      if (property.startsWith("modelPresetTransformationsName_")
          || property.startsWith("modelPresetTransformations_")) {
        properties.remove(i);
      }
    }
    String [] copiedProperties = properties.toArray(new String [properties.size()]);

    if (piece instanceof DoorOrWindow) {
      return new HomeDoorOrWindow((DoorOrWindow)piece, copiedProperties);
    } else if (piece instanceof Light) {
      return new HomeLight((Light)piece, copiedProperties);
    } else if (piece instanceof ShelfUnit) {
      return new HomeShelfUnit((ShelfUnit)piece, copiedProperties);
    } else {
      return new HomePieceOfFurniture(piece, copiedProperties);
    }
  }

  /**
   * Returns the furniture among the given list that are not part of the base plan.
   */
  private List<HomePieceOfFurniture> getFurnitureNotPartOfBasePlan(List<HomePieceOfFurniture> furniture) {
    List<HomePieceOfFurniture> furnitureNotPartOfBasePlan = new ArrayList<HomePieceOfFurniture>();
    for (HomePieceOfFurniture piece : furniture) {
      if (!isPieceOfFurniturePartOfBasePlan(piece)) {
        furnitureNotPartOfBasePlan.add(piece);
      }
    }
    return furnitureNotPartOfBasePlan;
  }

  /**
   * Uses <code>furniturePropertyName</code> to sort home furniture
   * or cancels home furniture sort if home is already sorted on <code>furnitureProperty</code>
   * @param furniturePropertyName a property of {@link HomePieceOfFurniture HomePieceOfFurniture} class.
   * @since 7.2
   */
  public void toggleFurnitureSort(String furniturePropertyName) {
    if (furniturePropertyName.equals(this.home.getFurnitureSortedPropertyName())) {
      this.home.setFurnitureSortedPropertyName(null);
    } else {
      this.home.setFurnitureSortedPropertyName(furniturePropertyName);
    }
  }

  /**
   * Uses <code>furnitureProperty</code> to sort home furniture
   * or cancels home furniture sort if home is already sorted on <code>furnitureProperty</code>
   * @param furnitureProperty a property of {@link HomePieceOfFurniture HomePieceOfFurniture} class.
   * @deprecated {@link #toggleFurnitureSort(HomePieceOfFurniture.SortableProperty)}
   *     should be replaced by calls to {@link #toggleFurnitureSort(String)}
   *     to allow displaying additional properties.
   */
  public void toggleFurnitureSort(HomePieceOfFurniture.SortableProperty furnitureProperty) {
    if (furnitureProperty.equals(this.home.getFurnitureSortedProperty())) {
      this.home.setFurnitureSortedProperty(null);
    } else {
      this.home.setFurnitureSortedProperty(furnitureProperty);
    }
  }

  /**
   * Toggles home furniture sort order.
   */
  public void toggleFurnitureSortOrder() {
    this.home.setFurnitureDescendingSorted(!this.home.isFurnitureDescendingSorted());
  }

   /**
   * Controls the sort of the furniture in home. If home furniture isn't sorted
   * or is sorted on an other property, it will be sorted on the given
   * <code>furnitureProperty</code> in ascending order. If home furniture is already
   * sorted on the given <code>furnitureProperty</code>, it will be sorted in descending
   * order, if the sort is in ascending order, otherwise it won't be sorted at all
   * and home furniture will be listed in insertion order.
   * @param furniturePropertyName  the furniture property on which the view wants
   *          to sort the furniture it displays.
   * @since 7.2
   */
  public void sortFurniture(String furniturePropertyName) {
    // Compute sort algorithm described in javadoc
    final String oldPropertyName = this.home.getFurnitureSortedPropertyName();
    final boolean oldDescending = this.home.isFurnitureDescendingSorted();
    boolean descending = false;
    if (furniturePropertyName.equals(oldPropertyName)) {
      if (oldDescending) {
        furniturePropertyName = null;
      } else {
        descending = true;
      }
    }
    this.home.setFurnitureSortedPropertyName(furniturePropertyName);
    this.home.setFurnitureDescendingSorted(descending);
  }

  /**
   * Controls the sort of the furniture in home.
   * @param furnitureProperty  the furniture property on which the view wants
   *          to sort the furniture it displays.
   * @deprecated {@link #sortFurniture(HomePieceOfFurniture.SortableProperty)}
   *     should be replaced by calls to {@link #sortFurniture(String)}
   *     to allow displaying additional properties.
   */
  public void sortFurniture(HomePieceOfFurniture.SortableProperty furnitureProperty) {
    final HomePieceOfFurniture.SortableProperty oldProperty = this.home.getFurnitureSortedProperty();
    final boolean oldDescending = this.home.isFurnitureDescendingSorted();
    boolean descending = false;
    if (furnitureProperty.equals(oldProperty)) {
      if (oldDescending) {
        furnitureProperty = null;
      } else {
        descending = true;
      }
    }
    this.home.setFurnitureSortedProperty(furnitureProperty);
    this.home.setFurnitureDescendingSorted(descending);
  }

  /**
   * Updates the furniture visible properties in home.
   */
  public void setFurnitureVisiblePropertyNames(List<String> furnitureVisiblePropertyNames) {
    this.home.setFurnitureVisiblePropertyNames(furnitureVisiblePropertyNames);
  }

  /**
   * Updates the furniture visible properties in home.
   * @deprecated {@link #setFurnitureVisibleProperties(List<HomePieceOfFurniture.SortableProperty>)}
   *     should be replaced by calls to {@link #setFurnitureVisiblePropertyNames(List<String>)}
   *     to allow displaying additional properties.
   */
  public void setFurnitureVisibleProperties(List<HomePieceOfFurniture.SortableProperty> furnitureVisibleProperties) {
    this.home.setFurnitureVisibleProperties(furnitureVisibleProperties);
  }

  /**
   * Toggles furniture property visibility in home.
   * @since 7.2
   */
  public void toggleFurnitureVisibleProperty(String furniturePropertyName) {
    List<String> furnitureVisiblePropertyNames =
        new ArrayList<String>(this.home.getFurnitureVisiblePropertyNames());
    if (furnitureVisiblePropertyNames.contains(furniturePropertyName)) {
      furnitureVisiblePropertyNames.remove(furniturePropertyName);
      // Ensure at least one column is visible
      if (furnitureVisiblePropertyNames.isEmpty()) {
        furnitureVisiblePropertyNames.add(HomePieceOfFurniture.SortableProperty.NAME.name());
      }
    } else {
      // Add furniture property after the visible property that has the previous index in
      // the following list
      List<String> propertiesOrder = new ArrayList<String>(
          Arrays.asList(new String [] {
              HomePieceOfFurniture.SortableProperty.CATALOG_ID.name(),
              HomePieceOfFurniture.SortableProperty.NAME.name(),
              HomePieceOfFurniture.SortableProperty.DESCRIPTION.name(),
              HomePieceOfFurniture.SortableProperty.CREATOR.name(),
              HomePieceOfFurniture.SortableProperty.LICENSE.name(),
              HomePieceOfFurniture.SortableProperty.WIDTH.name(),
              HomePieceOfFurniture.SortableProperty.DEPTH.name(),
              HomePieceOfFurniture.SortableProperty.HEIGHT.name(),
              HomePieceOfFurniture.SortableProperty.X.name(),
              HomePieceOfFurniture.SortableProperty.Y.name(),
              HomePieceOfFurniture.SortableProperty.ELEVATION.name(),
              HomePieceOfFurniture.SortableProperty.ANGLE.name(),
              HomePieceOfFurniture.SortableProperty.LEVEL.name(),
              HomePieceOfFurniture.SortableProperty.MODEL_SIZE.name(),
              HomePieceOfFurniture.SortableProperty.COLOR.name(),
              HomePieceOfFurniture.SortableProperty.TEXTURE.name(),
              HomePieceOfFurniture.SortableProperty.MOVABLE.name(),
              HomePieceOfFurniture.SortableProperty.DOOR_OR_WINDOW.name(),
              HomePieceOfFurniture.SortableProperty.VISIBLE.name(),
              HomePieceOfFurniture.SortableProperty.PRICE.name(),
              HomePieceOfFurniture.SortableProperty.VALUE_ADDED_TAX_PERCENTAGE.name(),
              HomePieceOfFurniture.SortableProperty.VALUE_ADDED_TAX.name(),
              HomePieceOfFurniture.SortableProperty.PRICE_VALUE_ADDED_TAX_INCLUDED.name()}));
      for (ObjectProperty property : this.home.getFurnitureAdditionalProperties()) {
        propertiesOrder.add(property.getName());
      }
      int propertyIndex = propertiesOrder.indexOf(furniturePropertyName) - 1;
      if (propertyIndex > 0) {
        while (propertyIndex > 0) {
          int visiblePropertyIndex = furnitureVisiblePropertyNames.indexOf(propertiesOrder.get(propertyIndex));
          if (visiblePropertyIndex >= 0) {
            propertyIndex = visiblePropertyIndex + 1;
            break;
          } else {
            propertyIndex--;
          }
        }
      }
      if (propertyIndex < 0) {
        propertyIndex = 0;
      }
      furnitureVisiblePropertyNames.add(propertyIndex, furniturePropertyName);
    }
    this.home.setFurnitureVisiblePropertyNames(furnitureVisiblePropertyNames);
  }

  /**
   * Toggles furniture property visibility in home.
   * @deprecated {@link #toggleFurnitureVisibleProperty(HomePieceOfFurniture.SortableProperty)}
   *     should be replaced by calls to {@link #toggleFurnitureVisibleProperty(String)}
   *     to allow displaying additional properties.
   */
  public void toggleFurnitureVisibleProperty(HomePieceOfFurniture.SortableProperty furnitureProperty) {
    toggleFurnitureVisibleProperty(furnitureProperty.name());
  }

  /**
   * Controls the modification of selected furniture.
   */
  public void modifySelectedFurniture() {
    if (!Home.getFurnitureSubList(this.home.getSelectedItems()).isEmpty()) {
      new HomeFurnitureController(this.home, this.preferences,
          this.viewFactory, this.contentManager, this.undoSupport).displayView(getView());
    }
  }

  /**
   * Controls the modification of the visibility of the selected piece of furniture.
   */
  public void toggleSelectedFurnitureVisibility() {
    if (Home.getFurnitureSubList(this.home.getSelectedItems()).size() == 1) {
      HomeFurnitureController controller = new HomeFurnitureController(this.home, this.preferences,
          this.viewFactory, this.contentManager, this.undoSupport);
      controller.setVisible(!controller.getVisible());
      controller.modifyFurniture();
    }
  }

  /**
   * Groups the selected furniture as one piece of furniture.
   */
  public void groupSelectedFurniture() {
    HomePieceOfFurniture [] selectedFurniture = getMovableSelectedFurniture();
    if (selectedFurniture.length > 0) {
      final boolean basePlanLocked = this.home.isBasePlanLocked();
      final boolean allLevelsSelection = this.home.isAllLevelsSelection();
      final List<Selectable> oldSelection = this.home.getSelectedItems();
      List<HomePieceOfFurniture> homeFurniture = this.home.getFurniture();
      // Sort the grouped furniture in the ascending order of their index in home or their group
      Map<HomeFurnitureGroup, TreeMap<Integer, HomePieceOfFurniture>> groupedFurnitureMap =
          new HashMap<HomeFurnitureGroup, TreeMap<Integer, HomePieceOfFurniture>>();
      int groupedFurnitureCount = 0;
      for (HomePieceOfFurniture piece : selectedFurniture) {
        HomeFurnitureGroup group = getPieceOfFurnitureGroup(piece, null, homeFurniture);
        TreeMap<Integer, HomePieceOfFurniture> sortedMap = groupedFurnitureMap.get(group);
        if (sortedMap == null) {
          sortedMap = new TreeMap<Integer, HomePieceOfFurniture>();
          groupedFurnitureMap.put(group, sortedMap);
        }
        if (group == null) {
          sortedMap.put(homeFurniture.indexOf(piece), piece);
        } else {
          sortedMap.put(group.getFurniture().indexOf(piece), piece);
        }
        groupedFurnitureCount++;
      }
      final HomePieceOfFurniture [] groupedPieces = new HomePieceOfFurniture [groupedFurnitureCount];
      final int [] groupedPiecesIndex = new int [groupedPieces.length];
      final Level [] groupedPiecesLevel = new Level [groupedPieces.length];
      final float [] groupedPiecesElevation = new float [groupedPieces.length];
      final boolean [] groupedPiecesVisible = new boolean [groupedPieces.length];
      final HomeFurnitureGroup [] groupedPiecesGroups = new HomeFurnitureGroup [groupedPieces.length];
      Level minLevel = this.home.getSelectedLevel();
      int i = 0;
      for (Map.Entry<HomeFurnitureGroup, TreeMap<Integer, HomePieceOfFurniture>> sortedMapEntry : groupedFurnitureMap.entrySet()) {
        for (Map.Entry<Integer, HomePieceOfFurniture> pieceEntry : sortedMapEntry.getValue().entrySet()) {
          HomePieceOfFurniture piece = pieceEntry.getValue();
          groupedPieces [i] = piece;
          groupedPiecesIndex [i] = pieceEntry.getKey();
          groupedPiecesLevel [i] = piece.getLevel();
          groupedPiecesElevation [i] = piece.getElevation();
          groupedPiecesVisible [i] = piece.isVisible();
          groupedPiecesGroups [i] = sortedMapEntry.getKey();
          if (groupedPiecesLevel [i] != null) {
            if (minLevel == null
                || groupedPiecesLevel [i].getElevation() < minLevel.getElevation()) {
              minLevel = groupedPiecesLevel [i];
            }
          }
          i++;
        }
      }
      final HomeFurnitureGroup newGroup;
      List<HomePieceOfFurniture> groupedFurniture = Arrays.asList(groupedPieces);
      if (groupedFurniture.indexOf(this.leadSelectedPieceOfFurniture) > 0) {
        newGroup = createHomeFurnitureGroup(groupedFurniture, this.leadSelectedPieceOfFurniture);
      } else {
        newGroup = createHomeFurnitureGroup(groupedFurniture);
      }
      // Store piece elevation that could have been updated during grouping
      final float [] groupPiecesNewElevation = new float [groupedPieces.length];
      i = 0;
      for (HomePieceOfFurniture piece : groupedPieces) {
        groupPiecesNewElevation [i++] = piece.getElevation();
      }
      TreeMap<Integer, HomePieceOfFurniture> homeSortedMap = groupedFurnitureMap.get(null);
      final int groupIndex = homeSortedMap != null
          ? homeSortedMap.lastKey() + 1 - groupedPieces.length
          : homeFurniture.size();
      final boolean movable = newGroup.isMovable();
      final Level groupLevel = minLevel;

      doGroupFurniture(this.home, groupedPieces, new HomeFurnitureGroup [] {newGroup},
          null, new int [] {groupIndex}, new Level [] {groupLevel}, basePlanLocked, false);
      if (this.undoSupport != null) {
        this.undoSupport.postEdit(new FurnitureGroupingUndoableEdit(this.home, this.preferences,
            oldSelection.toArray(new Selectable [oldSelection.size()]), basePlanLocked, allLevelsSelection,
            groupedPieces, groupedPiecesIndex, groupedPiecesGroups, groupedPiecesLevel, groupedPiecesElevation, groupedPiecesVisible,
            newGroup, groupIndex, groupLevel, groupPiecesNewElevation, movable));
      }
    }
  }

  /**
   * Undoable edit for furniture grouping.
   */
  private static class FurnitureGroupingUndoableEdit extends LocalizedUndoableEdit {
    private final Home                    home;
    private final Selectable []           oldSelection;
    private final boolean                 basePlanLocked;
    private final boolean                 allLevelsSelection;
    private final HomePieceOfFurniture [] groupedPieces;
    private final int []                  groupedPiecesIndex;
    private final HomeFurnitureGroup []   groupedPiecesGroups;
    private final Level []                groupedPiecesLevel;
    private final float []                groupedPiecesElevation;
    private final boolean []              groupedPiecesVisible;
    private final HomeFurnitureGroup      newGroup;
    private final int                     groupIndex;
    private final Level                   groupLevel;
    private final float []                groupPiecesNewElevation;
    private final boolean                 movable;

    public FurnitureGroupingUndoableEdit(Home home, UserPreferences preferences,
                                         Selectable[] oldSelection, boolean basePlanLocked, boolean allLevelsSelection,
                                         HomePieceOfFurniture [] groupedPieces, int [] groupedPiecesIndex,
                                         HomeFurnitureGroup [] groupedPiecesGroups, Level [] groupedPiecesLevel,
                                         float [] groupedPiecesElevation, boolean [] groupedPiecesVisible,
                                         HomeFurnitureGroup newGroup, int groupIndex,
                                         Level groupLevel, float [] groupPiecesNewElevation, boolean movable) {
      super(preferences, FurnitureController.class, "undoGroupName");
      this.home = home;
      this.basePlanLocked = basePlanLocked;
      this.oldSelection = oldSelection;
      this.allLevelsSelection = allLevelsSelection;
      this.groupedPieces = groupedPieces;
      this.groupedPiecesIndex = groupedPiecesIndex;
      this.groupedPiecesGroups = groupedPiecesGroups;
      this.groupedPiecesLevel = groupedPiecesLevel;
      this.groupedPiecesElevation = groupedPiecesElevation;
      this.groupedPiecesVisible = groupedPiecesVisible;
      this.newGroup = newGroup;
      this.groupIndex = groupIndex;
      this.groupLevel = groupLevel;
      this.groupPiecesNewElevation = groupPiecesNewElevation;
      this.movable = movable;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      doUngroupFurniture(this.home, new HomeFurnitureGroup [] {this.newGroup}, this.groupedPieces,
          this.groupedPiecesGroups, this.groupedPiecesIndex, this.groupedPiecesLevel, this.basePlanLocked, this.allLevelsSelection);
      for (int i = 0; i < this.groupedPieces.length; i++) {
        this.groupedPieces [i].setElevation(this.groupedPiecesElevation [i]);
        this.groupedPieces [i].setVisible(this.groupedPiecesVisible [i]);
      }
      this.home.setSelectedItems(Arrays.asList(this.oldSelection));
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      for (int i = 0; i < this.groupedPieces.length; i++) {
        this.groupedPieces [i].setElevation(this.groupPiecesNewElevation [i]);
        this.groupedPieces [i].setLevel(null);
      }
      this.newGroup.setMovable(this.movable);
      this.newGroup.setVisible(true);
      doGroupFurniture(this.home, this.groupedPieces, new HomeFurnitureGroup [] {this.newGroup},
          null, new int [] {this.groupIndex}, new Level [] {this.groupLevel}, this.basePlanLocked, false);
    }
  }

  /**
   * Returns a new furniture group for the given furniture list.
   */
  protected HomeFurnitureGroup createHomeFurnitureGroup(List<HomePieceOfFurniture> furniture) {
    return createHomeFurnitureGroup(furniture, furniture.get(0));
  }

  /**
   * Returns a new furniture group for the given furniture list.
   * @since 4.5
   */
  protected HomeFurnitureGroup createHomeFurnitureGroup(List<HomePieceOfFurniture> furniture, HomePieceOfFurniture leadingPiece) {
    String furnitureGroupName = this.preferences.getLocalizedString(
        FurnitureController.class, "groupName", getFurnitureGroupCount(this.home.getFurniture()) + 1);
    final HomeFurnitureGroup furnitureGroup = new HomeFurnitureGroup(furniture, leadingPiece, furnitureGroupName);
    return furnitureGroup;
  }

  /**
   * Returns the count of furniture groups among the given list.
   */
  private static int getFurnitureGroupCount(List<HomePieceOfFurniture> furniture) {
    int i = 0;
    for (HomePieceOfFurniture piece : furniture) {
      if (piece instanceof HomeFurnitureGroup) {
        i += 1 + getFurnitureGroupCount(((HomeFurnitureGroup)piece).getFurniture());
      }
    }
    return i;
  }

  private static void doGroupFurniture(Home home,
                                       HomePieceOfFurniture [] groupedPieces,
                                       HomeFurnitureGroup [] groups,
                                       HomeFurnitureGroup [] groupsGroups,
                                       int [] groupsIndex,
                                       Level [] groupsLevels,
                                       boolean basePlanLocked,
                                       boolean allLevelsSelection) {
    doDeleteFurniture(home, groupedPieces, basePlanLocked, allLevelsSelection);
    doAddFurniture(home, groups, groupsGroups, groupsIndex, null, groupsLevels, basePlanLocked, allLevelsSelection);
  }

  private static void doUngroupFurniture(Home home,
                                         HomeFurnitureGroup [] groups,
                                         HomePieceOfFurniture [] ungroupedPieces,
                                         HomeFurnitureGroup [] ungroupedPiecesGroups,
                                         int [] ungroupedPiecesIndex,
                                         Level [] ungroupedPiecesLevels,
                                         boolean basePlanLocked,
                                         boolean allLevelsSelection) {
    doDeleteFurniture(home, groups, basePlanLocked, allLevelsSelection);
    doAddFurniture(home, ungroupedPieces, ungroupedPiecesGroups, ungroupedPiecesIndex, null, ungroupedPiecesLevels, basePlanLocked, allLevelsSelection);
  }

  /**
   * Ungroups the selected groups of furniture.
   */
  public void ungroupSelectedFurniture() {
    List<HomeFurnitureGroup> movableSelectedFurnitureGroups = new ArrayList<HomeFurnitureGroup>();
    for (Selectable item : this.home.getSelectedItems()) {
      if (item instanceof HomeFurnitureGroup) {
        HomeFurnitureGroup group = (HomeFurnitureGroup)item;
        if (isPieceOfFurnitureMovable(group)) {
          movableSelectedFurnitureGroups.add(group);
        }
      }
    }
    if (!movableSelectedFurnitureGroups.isEmpty()) {
      List<HomePieceOfFurniture> homeFurniture = this.home.getFurniture();
      final boolean oldBasePlanLocked = this.home.isBasePlanLocked();
      final boolean allLevelsSelection = this.home.isAllLevelsSelection();
      final List<Selectable> oldSelection = this.home.getSelectedItems();
      // Sort the groups in the ascending order of their index in home or their group
      Map<HomeFurnitureGroup, TreeMap<Integer, HomeFurnitureGroup>> groupsMap =
          new HashMap<HomeFurnitureGroup, TreeMap<Integer, HomeFurnitureGroup>>();
      int groupsCount = 0;
      for (HomeFurnitureGroup piece : movableSelectedFurnitureGroups) {
        HomeFurnitureGroup groupGroup = getPieceOfFurnitureGroup(piece, null, homeFurniture);
        TreeMap<Integer, HomeFurnitureGroup> sortedMap = groupsMap.get(groupGroup);
        if (sortedMap == null) {
          sortedMap = new TreeMap<Integer, HomeFurnitureGroup>();
          groupsMap.put(groupGroup, sortedMap);
        }
        if (groupGroup == null) {
          sortedMap.put(homeFurniture.indexOf(piece), piece);
        } else {
          sortedMap.put(groupGroup.getFurniture().indexOf(piece), piece);
        }
        groupsCount++;
      }
      final HomeFurnitureGroup [] groups = new HomeFurnitureGroup [groupsCount];
      final HomeFurnitureGroup [] groupsGroups = new HomeFurnitureGroup [groups.length];
      final int [] groupsIndex = new int [groups.length];
      final Level [] groupsLevels = new Level [groups.length];
      int i = 0;
      List<HomePieceOfFurniture> ungroupedPiecesList = new ArrayList<HomePieceOfFurniture>();
      List<Integer> ungroupedPiecesIndexList = new ArrayList<Integer>();
      List<HomeFurnitureGroup> ungroupedPiecesGroupsList = new ArrayList<HomeFurnitureGroup>();
      for (Map.Entry<HomeFurnitureGroup, TreeMap<Integer, HomeFurnitureGroup>> sortedMapEntry : groupsMap.entrySet()) {
        TreeMap<Integer, HomeFurnitureGroup> sortedMap = sortedMapEntry.getValue();
        int endIndex = sortedMap.lastKey() + 1 - sortedMap.size();
        for (Map.Entry<Integer, HomeFurnitureGroup> groupEntry : sortedMap.entrySet()) {
          HomeFurnitureGroup group = groupEntry.getValue();
          groups [i] = group;
          groupsGroups [i] = sortedMapEntry.getKey();
          groupsIndex [i] = groupEntry.getKey();
          groupsLevels [i++] = group.getLevel();
          for (HomePieceOfFurniture groupPiece : group.getFurniture()) {
            ungroupedPiecesList.add(groupPiece);
            ungroupedPiecesGroupsList.add(sortedMapEntry.getKey());
            ungroupedPiecesIndexList.add(endIndex++);
          }
        }
      }
      final HomePieceOfFurniture [] ungroupedPieces =
          ungroupedPiecesList.toArray(new HomePieceOfFurniture [ungroupedPiecesList.size()]);
      final HomeFurnitureGroup [] ungroupedPiecesGroups =
          ungroupedPiecesGroupsList.toArray(new HomeFurnitureGroup [ungroupedPiecesGroupsList.size()]);
      final int [] ungroupedPiecesIndex = new int [ungroupedPieces.length];
      final Level [] ungroupedPiecesLevels = new Level [ungroupedPieces.length];
      boolean basePlanLocked = oldBasePlanLocked;
      for (i = 0; i < ungroupedPieces.length; i++) {
        ungroupedPiecesIndex [i] = ungroupedPiecesIndexList.get(i);
        ungroupedPiecesLevels [i] = ungroupedPieces [i].getLevel();
        // Unlock base plan if the piece is a part of it
        basePlanLocked &= !isPieceOfFurniturePartOfBasePlan(ungroupedPieces [i]);
      }
      final boolean newBasePlanLocked = basePlanLocked;

      doUngroupFurniture(this.home, groups, ungroupedPieces, ungroupedPiecesGroups,
          ungroupedPiecesIndex, ungroupedPiecesLevels, newBasePlanLocked, false);
      if (this.undoSupport != null) {
        this.undoSupport.postEdit(new FurnitureUngroupingUndoableEdit(this.home, this.preferences,
            oldSelection.toArray(new Selectable [oldSelection.size()]), oldBasePlanLocked, allLevelsSelection,
            groups, groupsIndex, groupsGroups, groupsLevels, ungroupedPieces, ungroupedPiecesIndex,
            ungroupedPiecesGroups, ungroupedPiecesLevels, newBasePlanLocked));
      }
    }
  }

  /**
   * Undoable edit for furniture ungrouping.
   */
  private static class FurnitureUngroupingUndoableEdit extends LocalizedUndoableEdit {
    private final Home                    home;
    private final boolean                 oldBasePlanLocked;
    private final Selectable []           oldSelection;
    private final boolean                 allLevelsSelection;
    private final HomeFurnitureGroup []   groups;
    private final int []                  groupsIndex;
    private final HomeFurnitureGroup []   groupsGroups;
    private final Level []                groupsLevels;
    private final HomePieceOfFurniture [] ungroupedPieces;
    private final int []                  ungroupedPiecesIndex;
    private final HomeFurnitureGroup []   ungroupedPiecesGroups;
    private final Level []                ungroupedPiecesLevels;
    private final boolean                 newBasePlanLocked;

    public FurnitureUngroupingUndoableEdit(Home home, UserPreferences preferences,
                                           Selectable [] oldSelection, boolean oldBasePlanLocked, boolean allLevelsSelection,
                                           HomeFurnitureGroup [] groups, int [] groupsIndex,
                                           HomeFurnitureGroup [] groupsGroups, Level [] groupsLevels,
                                           HomePieceOfFurniture [] ungroupedPieces, int [] ungroupedPiecesIndex,
                                           HomeFurnitureGroup [] ungroupedPiecesGroups,
                                           Level [] ungroupedPiecesLevels, boolean newBasePlanLocked) {
      super(preferences, FurnitureController.class, "undoUngroupName");
      this.home = home;
      this.oldSelection = oldSelection;
      this.oldBasePlanLocked = oldBasePlanLocked;
      this.allLevelsSelection = allLevelsSelection;
      this.groups = groups;
      this.groupsIndex = groupsIndex;
      this.groupsGroups = groupsGroups;
      this.groupsLevels = groupsLevels;
      this.ungroupedPieces = ungroupedPieces;
      this.ungroupedPiecesIndex = ungroupedPiecesIndex;
      this.ungroupedPiecesGroups = ungroupedPiecesGroups;
      this.ungroupedPiecesLevels = ungroupedPiecesLevels;
      this.newBasePlanLocked = newBasePlanLocked;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      doGroupFurniture(this.home, this.ungroupedPieces, this.groups, this.groupsGroups, this.groupsIndex, this.groupsLevels,
          this.oldBasePlanLocked, this.allLevelsSelection);
      this.home.setSelectedItems(Arrays.asList(this.oldSelection));
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      doUngroupFurniture(this.home, this.groups, this.ungroupedPieces, this.ungroupedPiecesGroups, this.ungroupedPiecesIndex,
          this.ungroupedPiecesLevels, this.newBasePlanLocked, false);
    }
  }

  /**
   * Displays the wizard that helps to import furniture to home.
   */
  public void importFurniture() {
    new ImportedFurnitureWizardController(this.home, this.preferences, this, this.viewFactory,
        this.contentManager, this.undoSupport).displayView(getView());
  }

  /**
   * Displays the wizard that helps to import furniture to home with a
   * given model name.
   */
  public void importFurniture(String modelName) {
    new ImportedFurnitureWizardController(this.home, modelName, this.preferences, this,
        this.viewFactory, this.contentManager, this.undoSupport).displayView(getView());
  }

  /**
   * Controls the alignment of selected furniture on top of the first selected piece.
   */
  public void alignSelectedFurnitureOnTop() {
    List<Selectable> oldSelection = this.home.getSelectedItems();
    alignSelectedFurniture(new FurnitureTopAlignmentUndoableEdit(this.home, this.preferences,
        oldSelection.toArray(new Selectable [oldSelection.size()]),
        getMovableSelectedFurniture(), this.leadSelectedPieceOfFurniture));
  }

  private static class FurnitureTopAlignmentUndoableEdit extends FurnitureAlignmentUndoableEdit {
    public FurnitureTopAlignmentUndoableEdit(Home home, UserPreferences preferences,
                                             Selectable [] oldSelection, HomePieceOfFurniture [] selectedFurniture,
                                             HomePieceOfFurniture leadPiece) {
      super(home, preferences, oldSelection, selectedFurniture, leadPiece);
    }

    @Override
    void alignFurniture(HomePieceOfFurniture [] alignedFurniture, HomePieceOfFurniture leadPiece) {
      float minYLeadPiece = getMinY(leadPiece);
      for (HomePieceOfFurniture piece : alignedFurniture) {
        float minY = getMinY(piece);
        piece.setY(piece.getY() + minYLeadPiece - minY);
      }
    }
  }

  /**
   * Controls the alignment of selected furniture on bottom of the first selected piece.
   */
  public void alignSelectedFurnitureOnBottom() {
    List<Selectable> oldSelection = this.home.getSelectedItems();
    alignSelectedFurniture(new FurnitureBottomAlignmentUndoableEdit(this.home, this.preferences,
        oldSelection.toArray(new Selectable [oldSelection.size()]),
        getMovableSelectedFurniture(), this.leadSelectedPieceOfFurniture));
  }

  private static class FurnitureBottomAlignmentUndoableEdit extends FurnitureAlignmentUndoableEdit {
    public FurnitureBottomAlignmentUndoableEdit(Home home, UserPreferences preferences,
                                                Selectable [] oldSelection, HomePieceOfFurniture [] selectedFurniture,
                                                HomePieceOfFurniture leadPiece) {
      super(home, preferences, oldSelection, selectedFurniture, leadPiece);
    }

    @Override
    void alignFurniture(HomePieceOfFurniture [] alignedFurniture, HomePieceOfFurniture leadPiece) {
      float maxYLeadPiece = getMaxY(leadPiece);
      for (HomePieceOfFurniture piece : alignedFurniture) {
        float maxY = getMaxY(piece);
        piece.setY(piece.getY() + maxYLeadPiece - maxY);
      }
    }
  }

  /**
   * Controls the alignment of selected furniture on left of the first selected piece.
   */
  public void alignSelectedFurnitureOnLeft() {
    List<Selectable> oldSelection = this.home.getSelectedItems();
    alignSelectedFurniture(new FurnitureLeftAlignmentUndoableEdit(this.home, this.preferences,
        oldSelection.toArray(new Selectable [oldSelection.size()]),
        getMovableSelectedFurniture(), this.leadSelectedPieceOfFurniture));
  }

  private static class FurnitureLeftAlignmentUndoableEdit extends FurnitureAlignmentUndoableEdit {
    public FurnitureLeftAlignmentUndoableEdit(Home home, UserPreferences preferences,
                                              Selectable [] oldSelection, HomePieceOfFurniture [] selectedFurniture,
                                              HomePieceOfFurniture leadPiece) {
      super(home, preferences, oldSelection, selectedFurniture, leadPiece);
    }

    @Override
    void alignFurniture(HomePieceOfFurniture [] alignedFurniture, HomePieceOfFurniture leadPiece) {
      float minXLeadPiece = getMinX(leadPiece);
      for (HomePieceOfFurniture piece : alignedFurniture) {
        float minX = getMinX(piece);
        piece.setX(piece.getX() + minXLeadPiece - minX);
      }
    }
  }

  /**
   * Controls the alignment of selected furniture on right of the first selected piece.
   */
  public void alignSelectedFurnitureOnRight() {
    List<Selectable> oldSelection = this.home.getSelectedItems();
    alignSelectedFurniture(new FurnitureRightAlignmentUndoableEdit(this.home, this.preferences,
        oldSelection.toArray(new Selectable [oldSelection.size()]),
        getMovableSelectedFurniture(), this.leadSelectedPieceOfFurniture));
  }

  private static class FurnitureRightAlignmentUndoableEdit extends FurnitureAlignmentUndoableEdit {
    public FurnitureRightAlignmentUndoableEdit(Home home, UserPreferences preferences,
                                               Selectable [] oldSelection, HomePieceOfFurniture [] selectedFurniture,
                                              HomePieceOfFurniture leadPiece) {
      super(home, preferences, oldSelection, selectedFurniture, leadPiece);
    }

    @Override
    void alignFurniture(HomePieceOfFurniture [] alignedFurniture, HomePieceOfFurniture leadPiece) {
      float maxXLeadPiece = getMaxX(leadPiece);
      for (HomePieceOfFurniture piece : alignedFurniture) {
        float maxX = getMaxX(piece);
        piece.setX(piece.getX() + maxXLeadPiece - maxX);
      }
    }
  }

  /**
   * Controls the alignment of selected furniture on the front side of the first selected piece.
   */
  public void alignSelectedFurnitureOnFrontSide() {
    List<Selectable> oldSelection = this.home.getSelectedItems();
    alignSelectedFurniture(new FurnitureFrontSideAlignmentUndoableEdit(this.home, this.preferences,
        oldSelection.toArray(new Selectable [oldSelection.size()]),
        getMovableSelectedFurniture(), this.leadSelectedPieceOfFurniture));
  }

  private static class FurnitureFrontSideAlignmentUndoableEdit extends FurnitureAlignmentUndoableEdit {
    public FurnitureFrontSideAlignmentUndoableEdit(Home home, UserPreferences preferences,
                                                   Selectable [] oldSelection, HomePieceOfFurniture [] selectedFurniture,
                                                   HomePieceOfFurniture leadPiece) {
      super(home, preferences, oldSelection, selectedFurniture, leadPiece);
    }

    @Override
    void alignFurniture(HomePieceOfFurniture [] alignedFurniture, HomePieceOfFurniture leadPiece) {
      float [][] points = leadPiece.getPoints();
      Line2D frontLine = new Line2D.Float(points [2][0], points [2][1], points [3][0], points [3][1]);
      for (HomePieceOfFurniture piece : alignedFurniture) {
        alignPieceOfFurnitureAlongSides(piece, leadPiece, frontLine, true, null, 0);
      }
    }
  }

  /**
   * Controls the alignment of selected furniture on the back side of the first selected piece.
   */
  public void alignSelectedFurnitureOnBackSide() {
    List<Selectable> oldSelection = this.home.getSelectedItems();
    alignSelectedFurniture(new FurnitureBackSideAlignmentUndoableEdit(this.home, this.preferences,
        oldSelection.toArray(new Selectable [oldSelection.size()]),
        getMovableSelectedFurniture(), this.leadSelectedPieceOfFurniture));
  }

  private static class FurnitureBackSideAlignmentUndoableEdit extends FurnitureAlignmentUndoableEdit {
    public FurnitureBackSideAlignmentUndoableEdit(Home home, UserPreferences preferences,
                                                  Selectable [] oldSelection, HomePieceOfFurniture [] selectedFurniture,
                                                  HomePieceOfFurniture leadPiece) {
      super(home, preferences, oldSelection, selectedFurniture, leadPiece);
    }

    @Override
    void alignFurniture(HomePieceOfFurniture [] alignedFurniture, HomePieceOfFurniture leadPiece) {
      float [][] points = leadPiece.getPoints();
      Line2D backLine = new Line2D.Float(points [0][0], points [0][1], points [1][0], points [1][1]);
      for (HomePieceOfFurniture piece : alignedFurniture) {
        alignPieceOfFurnitureAlongSides(piece, leadPiece, backLine, false, null, 0);
      }
    }
  }

  /**
   * Controls the alignment of selected furniture on the left side of the first selected piece.
   */
  public void alignSelectedFurnitureOnLeftSide() {
    List<Selectable> oldSelection = this.home.getSelectedItems();
    alignSelectedFurniture(new FurnitureLeftSideAlignmentUndoableEdit(this.home, this.preferences,
        oldSelection.toArray(new Selectable [oldSelection.size()]),
        getMovableSelectedFurniture(), this.leadSelectedPieceOfFurniture));
  }

  private static class FurnitureLeftSideAlignmentUndoableEdit extends FurnitureAlignmentUndoableEdit {
    public FurnitureLeftSideAlignmentUndoableEdit(Home home, UserPreferences preferences,
                                                  Selectable [] oldSelection, HomePieceOfFurniture [] selectedFurniture,
                                                  HomePieceOfFurniture leadPiece) {
      super(home, preferences, oldSelection, selectedFurniture, leadPiece);
    }

    @Override
    void alignFurniture(HomePieceOfFurniture [] alignedFurniture, HomePieceOfFurniture leadPiece) {
      float [][] points = leadPiece.getPoints();
      Line2D leftLine = new Line2D.Float(points [3][0], points [3][1], points [0][0], points [0][1]);
      for (HomePieceOfFurniture piece : alignedFurniture) {
        alignPieceOfFurnitureAlongLeftOrRightSides(piece, leadPiece, leftLine, false);
      }
    }
  }

  /**
   * Controls the alignment of selected furniture on the right side of the first selected piece.
   */
  public void alignSelectedFurnitureOnRightSide() {
    List<Selectable> oldSelection = this.home.getSelectedItems();
    alignSelectedFurniture(new FurnitureRightSideAlignmentUndoableEdit(this.home, this.preferences,
        oldSelection.toArray(new Selectable [oldSelection.size()]),
        getMovableSelectedFurniture(), this.leadSelectedPieceOfFurniture));
  }

  private static class FurnitureRightSideAlignmentUndoableEdit extends FurnitureAlignmentUndoableEdit {
    public FurnitureRightSideAlignmentUndoableEdit(Home home, UserPreferences preferences,
                                                   Selectable [] oldSelection, HomePieceOfFurniture [] selectedFurniture,
                                                   HomePieceOfFurniture leadPiece) {
      super(home, preferences, oldSelection, selectedFurniture, leadPiece);
    }

    @Override
    void alignFurniture(HomePieceOfFurniture [] alignedFurniture, HomePieceOfFurniture leadPiece) {
      float [][] points = leadPiece.getPoints();
      Line2D rightLine = new Line2D.Float(points [1][0], points [1][1], points [2][0], points [2][1]);
      for (HomePieceOfFurniture alignedPiece : alignedFurniture) {
        alignPieceOfFurnitureAlongLeftOrRightSides(alignedPiece, leadPiece, rightLine, true);
      }
    }
  }

  /**
   * Controls the alignment of selected furniture on the sides of the first selected piece.
   */
  public void alignSelectedFurnitureSideBySide() {
    List<Selectable> oldSelection = this.home.getSelectedItems();
    alignSelectedFurniture(new FurnitureSideBySideAlignmentUndoableEdit(this.home, this.preferences,
        oldSelection.toArray(new Selectable [oldSelection.size()]),
        getMovableSelectedFurniture(), this.leadSelectedPieceOfFurniture));
  }

  private static class FurnitureSideBySideAlignmentUndoableEdit extends FurnitureAlignmentUndoableEdit {
    public FurnitureSideBySideAlignmentUndoableEdit(Home home, UserPreferences preferences,
                                                    Selectable [] oldSelection, HomePieceOfFurniture [] selectedFurniture,
                                                    HomePieceOfFurniture leadPiece) {
      super(home, preferences, oldSelection, selectedFurniture, leadPiece);
    }

    @Override
    void alignFurniture(HomePieceOfFurniture [] alignedFurniture, HomePieceOfFurniture leadPiece) {
      float [][] points = leadPiece.getPoints();
      final Line2D centerLine = new Line2D.Float(leadPiece.getX(), leadPiece.getY(),
          (points [0][0] + points [1][0]) / 2, (points [0][1] + points [1][1]) / 2);
      List<HomePieceOfFurniture> furnitureSortedAlongBackLine = sortFurniture(alignedFurniture, leadPiece, centerLine);

      int leadPieceIndex = furnitureSortedAlongBackLine.indexOf(leadPiece);
      Line2D backLine = new Line2D.Float(points [0][0], points [0][1], points [1][0], points [1][1]);
      float sideDistance = leadPiece.getWidthInPlan() / 2;
      for (int i = leadPieceIndex + 1; i < furnitureSortedAlongBackLine.size(); i++) {
        sideDistance += alignPieceOfFurnitureAlongSides(furnitureSortedAlongBackLine.get(i),
            leadPiece, backLine, false, centerLine, sideDistance);
      }
      sideDistance = -leadPiece.getWidthInPlan() / 2;
      for (int i = leadPieceIndex - 1; i >= 0; i--) {
        sideDistance -= alignPieceOfFurnitureAlongSides(furnitureSortedAlongBackLine.get(i),
            leadPiece, backLine, false, centerLine, sideDistance);
      }
    }
  }

  /**
   * Returns a list containing aligned furniture and lead piece sorted in the order of their distribution along
   * a line orthogonal to the given axis.
   */
  private static List<HomePieceOfFurniture> sortFurniture(HomePieceOfFurniture [] furniture,
                                                          HomePieceOfFurniture leadPiece,
                                                          final Line2D orthogonalAxis) {
    List<HomePieceOfFurniture> sortedFurniture = new ArrayList<HomePieceOfFurniture>(furniture.length + 1);
    if (leadPiece != null) {
      sortedFurniture.add(leadPiece);
    }
    sortedFurniture.addAll(Arrays.asList(furniture));
    Collections.sort(sortedFurniture, new Comparator<HomePieceOfFurniture>() {
        public int compare(HomePieceOfFurniture p1, HomePieceOfFurniture p2) {
          return Double.compare(orthogonalAxis.ptLineDistSq(p2.getX(), p2.getY()) * orthogonalAxis.relativeCCW(p2.getX(), p2.getY()),
              orthogonalAxis.ptLineDistSq(p1.getX(), p1.getY()) * orthogonalAxis.relativeCCW(p1.getX(), p1.getY()));
        }
      });
    return sortedFurniture;
  }

  /**
   * Aligns the given <code>piece</code> along the front or back side of the lead piece and its left or right side
   * at a distance equal to <code>sideDistance</code>, and returns the width of the bounding box of
   * the <code>piece</code> along the back side axis.
   */
  private static double alignPieceOfFurnitureAlongSides(HomePieceOfFurniture piece, HomePieceOfFurniture leadPiece,
                                                        Line2D frontOrBackLine, boolean frontLine,
                                                        Line2D centerLine, float sideDistance) {
    // Search the distance required to align piece on the front or back side
    double distance = frontOrBackLine.relativeCCW(piece.getX(), piece.getY()) * frontOrBackLine.ptLineDist(piece.getX(), piece.getY())
        + getPieceBoundingRectangleHeight(piece, -leadPiece.getAngle()) / 2;
    if (frontLine) {
      distance = -distance;
    }
    double sinLeadPieceAngle = Math.sin(leadPiece.getAngle());
    double cosLeadPieceAngle = Math.cos(leadPiece.getAngle());
    float deltaX = (float)(-distance * sinLeadPieceAngle);
    float deltaY = (float)(distance * cosLeadPieceAngle);

    double rotatedBoundingBoxWidth = getPieceBoundingRectangleWidth(piece, -leadPiece.getAngle());
    if (centerLine != null) {
      // Search the distance required to align piece on the side of the previous piece
      int location = centerLine.relativeCCW(piece.getX(), piece.getY());
      if (location == 0) {
        location = frontLine ? 1 : -1;
      }
      distance = sideDistance + location
          * (centerLine.ptLineDist(piece.getX(), piece.getY()) - rotatedBoundingBoxWidth / 2);
      deltaX += (float)(distance * cosLeadPieceAngle);
      deltaY += (float)(distance * sinLeadPieceAngle);
    }

    piece.move(deltaX, deltaY);
    return rotatedBoundingBoxWidth;
  }

  /**
   * Aligns the given <code>piece</code> along the left or right side of the lead piece.
   */
  private static void alignPieceOfFurnitureAlongLeftOrRightSides(HomePieceOfFurniture piece, HomePieceOfFurniture leadPiece,
                                                                 Line2D leftOrRightLine, boolean rightLine) {
    // Search the distance required to align piece on the side of the lead piece
    double distance = leftOrRightLine.relativeCCW(piece.getX(), piece.getY()) * leftOrRightLine.ptLineDist(piece.getX(), piece.getY())
        + getPieceBoundingRectangleWidth(piece, -leadPiece.getAngle()) / 2;
    if (rightLine) {
      distance = -distance;
    }
    piece.move((float)(distance * Math.cos(leadPiece.getAngle())), (float)(distance * Math.sin(leadPiece.getAngle())));
  }

  /**
   * Returns the bounding box width of the given piece when it's rotated of an additional angle.
   */
  private static double getPieceBoundingRectangleWidth(HomePieceOfFurniture piece, float additionalAngle) {
    return Math.abs(piece.getWidthInPlan() * Math.cos(additionalAngle + piece.getAngle()))
        + Math.abs(piece.getDepthInPlan() * Math.sin(additionalAngle + piece.getAngle()));
  }

  /**
   * Returns the bounding box height of the given piece when it's rotated of an additional angle.
   */
  private static double getPieceBoundingRectangleHeight(HomePieceOfFurniture piece, float additionalAngle) {
    return Math.abs(piece.getWidthInPlan() * Math.sin(additionalAngle + piece.getAngle()))
        + Math.abs(piece.getDepthInPlan() * Math.cos(additionalAngle + piece.getAngle()));
  }

  /**
   * Controls the alignment of selected furniture.
   */
  private void alignSelectedFurniture(final FurnitureAlignmentUndoableEdit alignmentEdit) {
    HomePieceOfFurniture [] selectedFurniture = getMovableSelectedFurniture();
    if (selectedFurniture.length >= 2) {
      this.home.setSelectedItems(Arrays.asList(selectedFurniture));
      alignmentEdit.alignFurniture();
      if (this.undoSupport != null) {
        this.undoSupport.postEdit(alignmentEdit);
      }
    }
  }

  /**
   * Undoable edit for furniture alignment.
   */
  private static abstract class FurnitureAlignmentUndoableEdit extends LocalizedUndoableEdit {
    private final Home                    home;
    private final Selectable []           oldSelection;
    private final HomePieceOfFurniture [] selectedFurniture;
    private final HomePieceOfFurniture    leadPiece;
    private final HomePieceOfFurniture [] alignedFurniture;
    private final float []                oldX;
    private final float []                oldY;

    public FurnitureAlignmentUndoableEdit(Home home, UserPreferences preferences,
                                          Selectable [] oldSelection, HomePieceOfFurniture [] selectedFurniture,
                                          HomePieceOfFurniture leadPiece) {
      super(preferences, FurnitureController.class, "undoAlignName");
      this.home = home;
      this.oldSelection = oldSelection;
      this.selectedFurniture = selectedFurniture;
      this.leadPiece = leadPiece;
      this.alignedFurniture = new HomePieceOfFurniture[leadPiece == null  ? selectedFurniture.length  : selectedFurniture.length - 1];
      this.oldX = new float [this.alignedFurniture.length];
      this.oldY = new float [this.alignedFurniture.length];
      int i = 0;
      for (HomePieceOfFurniture piece : selectedFurniture) {
        if (piece != leadPiece) {
          this.alignedFurniture [i] = piece;
          this.oldX [i] = piece.getX();
          this.oldY [i] = piece.getY();
          i++;
        }
      }
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      undoAlignFurniture(this.alignedFurniture, this.oldX, this.oldY);
      this.home.setSelectedItems(Arrays.asList(this.oldSelection));
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      this.home.setSelectedItems(Arrays.asList(this.selectedFurniture));
      alignFurniture();
    }

    public void alignFurniture() {
      alignFurniture(this.alignedFurniture, this.leadPiece);
    }

    abstract void alignFurniture(HomePieceOfFurniture [] alignedFurniture,
                                 HomePieceOfFurniture leadPiece);
  }

  private HomePieceOfFurniture [] getMovableSelectedFurniture() {
    List<HomePieceOfFurniture> movableSelectedFurniture = new ArrayList<HomePieceOfFurniture>();
    for (Selectable item : this.home.getSelectedItems()) {
      if (item instanceof HomePieceOfFurniture) {
        HomePieceOfFurniture piece = (HomePieceOfFurniture)item;
        if (isPieceOfFurnitureMovable(piece)) {
          movableSelectedFurniture.add(piece);
        }
      }
    }
    return movableSelectedFurniture.toArray(new HomePieceOfFurniture [movableSelectedFurniture.size()]);
  }

  private static void undoAlignFurniture(HomePieceOfFurniture [] alignedFurniture, float [] x, float [] y) {
    for (int i = 0; i < alignedFurniture.length; i++) {
      HomePieceOfFurniture piece = alignedFurniture [i];
      piece.setX(x [i]);
      piece.setY(y [i]);
    }
  }

  /**
   * Returns the minimum abscissa of the vertices of <code>piece</code>.
   */
  private static float getMinX(HomePieceOfFurniture piece) {
    float [][] points = piece.getPoints();
    float minX = Float.POSITIVE_INFINITY;
    for (float [] point : points) {
      minX = Math.min(minX, point [0]);
    }
    return minX;
  }

  /**
   * Returns the maximum abscissa of the vertices of <code>piece</code>.
   */
  private static float getMaxX(HomePieceOfFurniture piece) {
    float [][] points = piece.getPoints();
    float maxX = Float.NEGATIVE_INFINITY;
    for (float [] point : points) {
      maxX = Math.max(maxX, point [0]);
    }
    return maxX;
  }

  /**
   * Returns the minimum ordinate of the vertices of <code>piece</code>.
   */
  private static float getMinY(HomePieceOfFurniture piece) {
    float [][] points = piece.getPoints();
    float minY = Float.POSITIVE_INFINITY;
    for (float [] point : points) {
      minY = Math.min(minY, point [1]);
    }
    return minY;
  }

  /**
   * Returns the maximum ordinate of the vertices of <code>piece</code>.
   */
  private static float getMaxY(HomePieceOfFurniture piece) {
    float [][] points = piece.getPoints();
    float maxY = Float.NEGATIVE_INFINITY;
    for (float [] point : points) {
      maxY = Math.max(maxY, point [1]);
    }
    return maxY;
  }

  /**
   * Controls the distribution of the selected furniture along horizontal axis.
   */
  public void distributeSelectedFurnitureHorizontally() {
    distributeSelectedFurniture(true);
  }

  /**
   * Controls the distribution of the selected furniture along vertical axis.
   */
  public void distributeSelectedFurnitureVertically() {
    distributeSelectedFurniture(false);
  }

  /**
   * Controls the distribution of the selected furniture along the axis orthogonal to the given one.
   */
  public void distributeSelectedFurniture(final boolean horizontal) {
    final HomePieceOfFurniture [] alignedFurniture = getMovableSelectedFurniture();
    if (alignedFurniture.length >= 3) {
      final List<Selectable> oldSelection = this.home.getSelectedItems();
      final float [] oldX = new float [alignedFurniture.length];
      final float [] oldY = new float [alignedFurniture.length];
      for (int i = 0; i < alignedFurniture.length; i++) {
        oldX [i] = alignedFurniture [i].getX();
        oldY [i] = alignedFurniture [i].getY();
      }
      this.home.setSelectedItems(Arrays.asList(alignedFurniture));
      doDistributeFurnitureAlongAxis(alignedFurniture, horizontal);
      if (this.undoSupport != null) {
        this.undoSupport.postEdit(new FurnitureDistributionUndoableEdit(this.home, this.preferences,
            oldSelection.toArray(new Selectable [oldSelection.size()]), oldX, oldY, alignedFurniture, horizontal));
      }
    }
  }

  /**
   * Undoable edit for furniture distribution.
   */
  private static class FurnitureDistributionUndoableEdit extends LocalizedUndoableEdit {
    private final Home                    home;
    private final Selectable []           oldSelection;
    private float []                      oldX;
    private float []                      oldY;
    private final HomePieceOfFurniture [] alignedFurniture;
    private final boolean                 horizontal;

    public FurnitureDistributionUndoableEdit(Home home,
                                             UserPreferences preferences,
                                             Selectable [] oldSelection, float [] oldX, float[] oldY,
                                             HomePieceOfFurniture [] alignedFurniture, boolean horizontal) {
      super(preferences, FurnitureController.class, "undoDistributeName");
      this.home = home;
      this.oldSelection = oldSelection;
      this.oldX = oldX;
      this.oldY = oldY;
      this.alignedFurniture = alignedFurniture;
      this.horizontal = horizontal;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      undoAlignFurniture(this.alignedFurniture, this.oldX, this.oldY);
      this.home.setSelectedItems(Arrays.asList(this.oldSelection));
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      this.home.setSelectedItems(Arrays.asList(this.alignedFurniture));
      doDistributeFurnitureAlongAxis(this.alignedFurniture, this.horizontal);
    }
  }

  private static void doDistributeFurnitureAlongAxis(HomePieceOfFurniture [] alignedFurniture,
                                                     boolean horizontal) {
    Line2D orthogonalAxis = horizontal ? new Line2D.Float(0, 0, 0, -1) : new Line2D.Float(0, 0, 1, 0);
    List<HomePieceOfFurniture> furnitureHorizontallySorted = sortFurniture(alignedFurniture, null, orthogonalAxis);
    float axisAngle = (float)(horizontal ? 0 : Math.PI / 2);
    HomePieceOfFurniture firstPiece = furnitureHorizontallySorted.get(0);
    double firstPieceBoundingRectangleHalfWidth = getPieceBoundingRectangleWidth(firstPiece, axisAngle) / 2;
    HomePieceOfFurniture lastPiece = furnitureHorizontallySorted.get(furnitureHorizontallySorted.size() - 1);
    double lastPieceBoundingRectangleHalfWidth = getPieceBoundingRectangleWidth(lastPiece, axisAngle) / 2;
    double gap = Math.abs(
          orthogonalAxis.ptLineDist(lastPiece.getX(), lastPiece.getY())
          * orthogonalAxis.relativeCCW(lastPiece.getX(), lastPiece.getY())
        - orthogonalAxis.ptLineDist(firstPiece.getX(), firstPiece.getY())
          * orthogonalAxis.relativeCCW(firstPiece.getX(), firstPiece.getY()))
        - lastPieceBoundingRectangleHalfWidth
        - firstPieceBoundingRectangleHalfWidth;
    double [] furnitureWidthsAlongAxis = new double [furnitureHorizontallySorted.size() - 2];
    for (int i = 1; i < furnitureHorizontallySorted.size() - 1; i++) {
      HomePieceOfFurniture piece = furnitureHorizontallySorted.get(i);
      furnitureWidthsAlongAxis [i - 1] = getPieceBoundingRectangleWidth(piece, axisAngle);
      gap -= furnitureWidthsAlongAxis [i - 1];
    }
    gap /= furnitureHorizontallySorted.size() - 1;
    float xOrY = (horizontal ? firstPiece.getX() : firstPiece.getY())
        + (float)(firstPieceBoundingRectangleHalfWidth + gap);
    for (int i = 1; i < furnitureHorizontallySorted.size() - 1; i++) {
      HomePieceOfFurniture piece = furnitureHorizontallySorted.get(i);
      if (horizontal) {
        piece.setX((float)(xOrY + furnitureWidthsAlongAxis [i - 1] / 2));
      } else {
        piece.setY((float)(xOrY + furnitureWidthsAlongAxis [i - 1] / 2));
      }
      xOrY += gap + furnitureWidthsAlongAxis [i - 1];
    }
  }

  /**
   * Resets the elevation of the selected furniture to its default elevation.
   * @since 4.4
   */
  public void resetFurnitureElevation() {
    final HomePieceOfFurniture [] selectedFurniture = getMovableSelectedFurniture();
    if (selectedFurniture.length >= 1) {
      final List<Selectable> oldSelection = this.home.getSelectedItems();
      final float [] furnitureOldElevation = new float [selectedFurniture.length];
      final float [] furnitureNewElevation = new float [selectedFurniture.length];
      for (int i = 0; i < selectedFurniture.length; i++) {
        HomePieceOfFurniture piece = selectedFurniture [i];
        furnitureOldElevation [i] = piece.getElevation();
        HomePieceOfFurniture highestSurroundingPiece = getHighestSurroundingPieceOfFurniture(piece, Arrays.asList(selectedFurniture));
        if (highestSurroundingPiece != null) {
          float elevation = highestSurroundingPiece.getElevation();
          if (highestSurroundingPiece.isHorizontallyRotated()) {
            elevation += highestSurroundingPiece.getHeightInPlan();
          } else {
            elevation += highestSurroundingPiece.getHeight() * highestSurroundingPiece.getDropOnTopElevation();
          }
          if (highestSurroundingPiece.getLevel() != null) {
            elevation += highestSurroundingPiece.getLevel().getElevation() - piece.getLevel().getElevation();
          }
          furnitureNewElevation [i] = Math.max(0, elevation);
        } else {
          furnitureNewElevation [i] = 0;
        }
      }
      this.home.setSelectedItems(Arrays.asList(selectedFurniture));
      doSetFurnitureElevation(selectedFurniture, furnitureNewElevation);
      if (this.undoSupport != null) {
        this.undoSupport.postEdit(new FurnitureElevationResetUndoableEdit(this.home, this.preferences,
            oldSelection.toArray(new Selectable [oldSelection.size()]), furnitureOldElevation, selectedFurniture, furnitureNewElevation));
      }
    }
  }

  /**
   * Undoable edit for furniture elevation reset.
   */
  private static class FurnitureElevationResetUndoableEdit extends LocalizedUndoableEdit {
    private final Home                    home;
    private final Selectable []           oldSelection;
    private final float []                furnitureOldElevation;
    private final HomePieceOfFurniture [] selectedFurniture;
    private float []                      furnitureNewElevation;

    public FurnitureElevationResetUndoableEdit(Home home, UserPreferences preferences,
                                               Selectable [] oldSelection, float [] furnitureOldElevation,
                                               HomePieceOfFurniture [] selectedFurniture, float [] furnitureNewElevation) {
      super(preferences, FurnitureController.class, "undoResetElevation");
      this.home = home;
      this.oldSelection = oldSelection;
      this.furnitureOldElevation = furnitureOldElevation;
      this.selectedFurniture = selectedFurniture;
      this.furnitureNewElevation = furnitureNewElevation;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      doSetFurnitureElevation(this.selectedFurniture, this.furnitureOldElevation);
      this.home.setSelectedItems(Arrays.asList(this.oldSelection));
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      this.home.setSelectedItems(Arrays.asList(this.selectedFurniture));
      doSetFurnitureElevation(this.selectedFurniture, this.furnitureNewElevation);
    }
  }

  private static void doSetFurnitureElevation(HomePieceOfFurniture [] selectedFurniture, float [] furnitureNewElevation) {
    for (int i = 0; i < selectedFurniture.length; i++) {
      selectedFurniture [i].setElevation(furnitureNewElevation [i]);
    }
  }

  /**
   * Returns the highest piece of furniture that includes the given <code>piece</code>
   * with a margin error of 5% of the smallest side length.
   * @since 4.4
   */
  protected HomePieceOfFurniture getHighestSurroundingPieceOfFurniture(HomePieceOfFurniture piece) {
    List<HomePieceOfFurniture> ignoredFurniture = Collections.emptyList();
    return getHighestSurroundingPieceOfFurniture(piece, ignoredFurniture);
  }

  private HomePieceOfFurniture getHighestSurroundingPieceOfFurniture(HomePieceOfFurniture piece,
                                                                     List<HomePieceOfFurniture> ignoredFurniture) {
    HomePieceOfFurniture highestSurroundingPiece = null;
    float highestElevation = Float.MIN_VALUE;
    for (HomePieceOfFurniture surroundingPiece : getSurroundingFurniture(piece, ignoredFurniture, 0.05f, false)) {
      float elevation = surroundingPiece.getElevation();
      if (surroundingPiece.isHorizontallyRotated()) {
        elevation += surroundingPiece.getHeightInPlan();
      } else {
        elevation += surroundingPiece.getHeight() * surroundingPiece.getDropOnTopElevation();
      }
      if (elevation > highestElevation) {
        highestElevation = elevation;
        highestSurroundingPiece = surroundingPiece;
      }
    }
    return highestSurroundingPiece;
  }

  /**
   * Returns the shelf units which include the given <code>piece</code>
   * with a margin error of 20% of the smallest side length.
   * @since 7.2
   */
  protected List<HomePieceOfFurniture> getSurroundingFurniture(HomePieceOfFurniture piece) {
    List<HomePieceOfFurniture> ignoredFurniture = Collections.emptyList();
    return getSurroundingFurniture(piece, ignoredFurniture, 0.2f, true);
  }

  private List<HomePieceOfFurniture> getSurroundingFurniture(HomePieceOfFurniture piece,
                                                             List<HomePieceOfFurniture> ignoredFurniture,
                                                             float marginError,
                                                             boolean includeShelfUnits) {
    float [][] piecePoints = piece.getPoints();
    float margin = Math.min(piece.getWidthInPlan(), piece.getDepthInPlan()) * marginError;
    List<HomePieceOfFurniture> surroundingFurniture = new ArrayList<HomePieceOfFurniture>();
    for (HomePieceOfFurniture homePiece : getFurnitureInSameGroup(piece)) {
      if (homePiece != piece
          && !ignoredFurniture.contains(homePiece)
          && isPieceOfFurnitureVisibleAtSelectedLevel(homePiece)
          && (homePiece.getDropOnTopElevation() >= 0
              || (includeShelfUnits && homePiece instanceof HomeShelfUnit))) {
        boolean surroundingPieceContainsPiece = true;
        for (float [] point : piecePoints) {
          if (!homePiece.containsPoint(point [0], point [1], margin)) {
            surroundingPieceContainsPiece = false;
            break;
          }
        }
        if (surroundingPieceContainsPiece) {
          surroundingFurniture.add(homePiece);
        }
      }
    }
    return surroundingFurniture;
  }

  /**
   * Returns the furniture list of the given <code>piece</code> which belongs to same group
   * or home furniture if it doesn't belong to home furniture.
   * @since 5.0
   */
  protected List<HomePieceOfFurniture> getFurnitureInSameGroup(HomePieceOfFurniture piece) {
    List<HomePieceOfFurniture> homeFurniture = this.home.getFurniture();
    List<HomePieceOfFurniture> furnitureInSameGroup = getFurnitureInSameGroup(piece, homeFurniture);
    if (furnitureInSameGroup != null) {
      return furnitureInSameGroup;
    } else {
      return homeFurniture;
    }
  }

  private static List<HomePieceOfFurniture> getFurnitureInSameGroup(HomePieceOfFurniture piece, List<HomePieceOfFurniture> furniture) {
    for (HomePieceOfFurniture piece2 : furniture) {
      if (piece2 == piece) {
        return furniture;
      } else if (piece2 instanceof HomeFurnitureGroup) {
        List<HomePieceOfFurniture> siblingFurniture = getFurnitureInSameGroup(piece, ((HomeFurnitureGroup)piece2).getFurniture());
        if (siblingFurniture != null) {
          return siblingFurniture;
        }
      }
    }
    return null;
  }

  /**
   * Returns <code>true</code> if the given piece is viewable and
   * its height and elevation make it viewable at the selected level in home.
   * @since 4.4
   */
  protected boolean isPieceOfFurnitureVisibleAtSelectedLevel(HomePieceOfFurniture piece) {
    Level selectedLevel = this.home.getSelectedLevel();
    return piece.isVisible()
        && (piece.getLevel() == null
            || piece.getLevel().isViewable())
        && (piece.getLevel() == selectedLevel
            || piece.isAtLevel(selectedLevel));
  }

  /**
   * Controls the change of value of a visual property in home.
   * @deprecated {@link #setVisualProperty(String, Object) setVisualProperty} should be replaced by a call to
   * {@link #setHomeProperty(String, String)} to ensure the property can be easily saved and read.
   * @since 5.0
   */
  public void setVisualProperty(String propertyName,
                                Object propertyValue) {
    this.home.setVisualProperty(propertyName, propertyValue);
  }

  /**
   * Controls the change of value of a property in home.
   * @since 5.2
   */
  public void setHomeProperty(String propertyName,
                                String propertyValue) {
    this.home.setProperty(propertyName, propertyValue);
  }
}
