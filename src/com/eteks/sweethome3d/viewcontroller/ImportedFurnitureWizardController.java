/*
 * ImportedFurnitureWizardStepsController.java 4 juil. 07
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.CatalogDoorOrWindow;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.model.Sash;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * Wizard controller to manage furniture importation.
 * @author Emmanuel Puybaret
 */
public class ImportedFurnitureWizardController extends WizardController
                                               implements Controller {
  public enum Property {STEP, NAME, CREATOR, MODEL, WIDTH, DEPTH, HEIGHT, ELEVATION, MOVABLE,
      DOOR_OR_WINDOW, COLOR, CATEGORY, BACK_FACE_SHOWN, EDGE_COLOR_MATERIAL_HIDDEN, MODEL_SIZE, MODEL_ROTATION, STAIRCASE_CUT_OUT_SHAPE,
      ICON_YAW, ICON_PITCH, ICON_SCALE, PROPORTIONAL}

  public enum Step {MODEL, ROTATION, ATTRIBUTES, ICON};

  private final Home                             home;
  private final CatalogPieceOfFurniture          piece;
  private final String                           modelName;
  private final UserPreferences                  preferences;
  private final FurnitureController              furnitureController;
  private final ContentManager                   contentManager;
  private final UndoableEditSupport              undoSupport;
  private final PropertyChangeSupport            propertyChangeSupport;

  private final ImportedFurnitureWizardStepState furnitureModelStepState;
  private final ImportedFurnitureWizardStepState furnitureOrientationStepState;
  private final ImportedFurnitureWizardStepState furnitureAttributesStepState;
  private final ImportedFurnitureWizardStepState furnitureIconStepState;
  private ImportedFurnitureWizardStepsView       stepsView;

  private Step                             step;
  private String                           name;
  private String                           creator;
  private Content                          model;
  private float                            width;
  private float                            proportionalWidth;
  private float                            depth;
  private float                            proportionalDepth;
  private float                            height;
  private float                            proportionalHeight;
  private float                            elevation;
  private boolean                          movable;
  private boolean                          doorOrWindow;
  private String                           staircaseCutOutShape;
  private Integer                          color;
  private FurnitureCategory                category;
  private long                             modelSize;
  private float [][]                       modelRotation;
  private boolean                          edgeColorMaterialHidden;
  private boolean                          backFaceShown;
  private float                            iconYaw;
  private float                            iconPitch;
  private float                            iconScale;
  private boolean                          proportional;
  private final ViewFactory viewFactory;

  /**
   * Creates a controller that edits a new catalog piece of furniture.
   */
  public ImportedFurnitureWizardController(UserPreferences preferences,
                                           ViewFactory    viewFactory,
                                           ContentManager contentManager) {
    this(null, null, null, preferences, null, viewFactory, contentManager, null);
  }

  /**
   * Creates a controller that edits a new catalog piece of furniture with a given
   * <code>modelName</code>.
   */
  public ImportedFurnitureWizardController(String modelName,
                                           UserPreferences preferences,
                                           ViewFactory    viewFactory,
                                           ContentManager contentManager) {
    this(null, null, modelName, preferences, null, viewFactory, contentManager, null);
  }

  /**
   * Creates a controller that edits <code>piece</code> values.
   */
  public ImportedFurnitureWizardController(CatalogPieceOfFurniture piece,
                                           UserPreferences preferences,
                                           ViewFactory    viewFactory,
                                           ContentManager contentManager) {
    this(null, piece, null, preferences, null, viewFactory, contentManager, null);
  }

  /**
   * Creates a controller that edits a new imported home piece of furniture.
   */
  public ImportedFurnitureWizardController(Home home,
                                           UserPreferences preferences,
                                           FurnitureController furnitureController,
                                           ViewFactory    viewFactory,
                                           ContentManager contentManager,
                                           UndoableEditSupport undoSupport) {
    this(home, null, null, preferences, furnitureController, viewFactory, contentManager, undoSupport);
  }

  /**
   * Creates a controller that edits a new imported home piece of furniture
   * with a given <code>modelName</code>.
   */
  public ImportedFurnitureWizardController(Home home,
                                           String modelName,
                                           UserPreferences preferences,
                                           FurnitureController furnitureController,
                                           ViewFactory    viewFactory,
                                           ContentManager contentManager,
                                           UndoableEditSupport undoSupport) {
    this(home, null, modelName, preferences, furnitureController, viewFactory, contentManager, undoSupport);
  }

  /**
   * Creates a controller that edits <code>piece</code> values.
   */
  private ImportedFurnitureWizardController(Home home,
                                            CatalogPieceOfFurniture piece,
                                            String modelName,
                                            UserPreferences preferences,
                                            FurnitureController furnitureController,
                                            ViewFactory    viewFactory,
                                            ContentManager contentManager,
                                            UndoableEditSupport undoSupport) {
    super(preferences, viewFactory);
    this.home = home;
    this.piece = piece;
    this.modelName = modelName;
    this.preferences = preferences;
    this.furnitureController = furnitureController;
    this.viewFactory = viewFactory;
    this.undoSupport = undoSupport;
    this.contentManager = contentManager;
    this.propertyChangeSupport = new PropertyChangeSupport(this);
    setTitle(this.preferences.getLocalizedString(ImportedFurnitureWizardController.class,
        piece == null
            ? "importFurnitureWizard.title"
            : "modifyFurnitureWizard.title"));
    // Initialize states
    this.furnitureModelStepState = new FurnitureModelStepState();
    this.furnitureOrientationStepState = new FurnitureOrientationStepState();
    this.furnitureAttributesStepState = new FurnitureAttributesStepState();
    this.furnitureIconStepState = new FurnitureIconStepState();
    setStepState(this.furnitureModelStepState);
  }

  /**
   * Imports piece in catalog and/or home and posts an undoable operation.
   */
  @Override
  public void finish() {
    CatalogPieceOfFurniture newPiece;
    int modelFlags = (isBackFaceShown() ? PieceOfFurniture.SHOW_BACK_FACE : 0)
        | (isEdgeColorMaterialHidden() ? PieceOfFurniture.HIDE_EDGE_COLOR_MATERIAL : 0);
    if (isDoorOrWindow()) {
      newPiece = new CatalogDoorOrWindow(getName(), getIcon(), getModel(),
          getWidth(), getDepth(), getHeight(), getElevation(),
          isMovable(), 1, 0, new Sash [0], getColor(),
          getModelRotation(), modelFlags, getModelSize(), getCreator(),
          getIconYaw(), getIconPitch(), getIconScale(), isProportional());
    } else {
      newPiece = new CatalogPieceOfFurniture(getName(), getIcon(), getModel(), getWidth(),
          getDepth(), getHeight(), getElevation(), isMovable(),
          getStaircaseCutOutShape(), getColor(),
          getModelRotation(), modelFlags, getModelSize(), getCreator(),
          getIconYaw(), getIconPitch(), getIconScale(), isProportional());
    }

    if (this.home != null) {
      // Add new piece to home
      addPieceOfFurniture(this.furnitureController.createHomePieceOfFurniture(newPiece));
    }
    // Remove the edited piece from catalog
    FurnitureCatalog catalog = this.preferences.getFurnitureCatalog();
    if (this.piece != null) {
      catalog.delete(this.piece);
    }
    // If a category exists, add new piece to catalog
    if (this.category != null) {
      catalog.add(this.category, newPiece);
    }
  }

  /**
   * Controls new piece added to home.
   * Once added the furniture will be selected in view
   * and undo support will receive a new undoable edit.
   * @param piece the piece of furniture to add.
   */
  public void addPieceOfFurniture(HomePieceOfFurniture piece) {
    boolean basePlanLocked = this.home.isBasePlanLocked();
    boolean allLevelsSelection = this.home.isAllLevelsSelection();
    List<Selectable> oldSelection = this.home.getSelectedItems();
    // Get index of the piece added to home
    int pieceIndex = this.home.getFurniture().size();

    this.home.addPieceOfFurniture(piece, pieceIndex);
    this.home.setSelectedItems(Arrays.asList(piece));
    if (!piece.isMovable() && basePlanLocked) {
      this.home.setBasePlanLocked(false);
    }
    this.home.setAllLevelsSelection(false);
    if (this.undoSupport != null) {
      UndoableEdit undoableEdit = new PieceOfFurnitureImportationUndoableEdit(
          this.home, this.preferences, oldSelection.toArray(new Selectable [oldSelection.size()]),
          basePlanLocked, allLevelsSelection, piece, pieceIndex);
      this.undoSupport.postEdit(undoableEdit);
    }
  }

  /**
   * Undoable edit for piece importation. This class isn't anonymous to avoid
   * being bound to controller and its view.
   */
  private static class PieceOfFurnitureImportationUndoableEdit extends LocalizedUndoableEdit {
    private final Home                 home;
    private final Selectable []        oldSelection;
    private final boolean              oldBasePlanLocked;
    private final boolean              oldAllLevelsSelection;
    private final HomePieceOfFurniture piece;
    private final int                  pieceIndex;

    private PieceOfFurnitureImportationUndoableEdit(Home home,
                                                    UserPreferences preferences,
                                                    Selectable [] oldSelection,
                                                    boolean oldBasePlanLocked,
                                                    boolean oldAllLevelsSelection,
                                                    HomePieceOfFurniture piece,
                                                    int pieceIndex) {
      super(preferences, ImportedFurnitureWizardController.class, "undoImportFurnitureName");
      this.home = home;
      this.oldSelection = oldSelection;
      this.oldBasePlanLocked = oldBasePlanLocked;
      this.oldAllLevelsSelection = oldAllLevelsSelection;
      this.piece = piece;
      this.pieceIndex = pieceIndex;
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      this.home.deletePieceOfFurniture(this.piece);
      this.home.setSelectedItems(Arrays.asList(this.oldSelection));
      this.home.setAllLevelsSelection(this.oldAllLevelsSelection);
      this.home.setBasePlanLocked(this.oldBasePlanLocked);
    }

    @Override
    public void redo() throws CannotRedoException {
      super.redo();
      this.home.addPieceOfFurniture(this.piece, this.pieceIndex);
      this.home.setSelectedItems(Arrays.asList(this.piece));
      if (!piece.isMovable() && this.oldBasePlanLocked) {
        this.home.setBasePlanLocked(false);
      }
      this.home.setAllLevelsSelection(false);
    }
  }

  /**
   * Returns the content manager of this controller.
   */
  public ContentManager getContentManager() {
    return this.contentManager;
  }

  /**
   * Returns the current step state.
   */
  @Override
  protected ImportedFurnitureWizardStepState getStepState() {
    return (ImportedFurnitureWizardStepState)super.getStepState();
  }

  /**
   * Returns the furniture choice step state.
   */
  protected ImportedFurnitureWizardStepState getFurnitureModelStepState() {
    return this.furnitureModelStepState;
  }

  /**
   * Returns the furniture orientation step state.
   */
  protected ImportedFurnitureWizardStepState getFurnitureOrientationStepState() {
    return this.furnitureOrientationStepState;
  }

  /**
   * Returns the furniture attributes step state.
   */
  protected ImportedFurnitureWizardStepState getFurnitureAttributesStepState() {
    return this.furnitureAttributesStepState;
  }

  /**
   * Returns the furniture icon step state.
   */
  protected ImportedFurnitureWizardStepState getFurnitureIconStepState() {
    return this.furnitureIconStepState;
  }

  /**
   * Returns the unique wizard view used for all steps.
   */
  protected ImportedFurnitureWizardStepsView getStepsView() {
    // Create view lazily only once it's needed
    if (this.stepsView == null) {
      this.stepsView = this.viewFactory.createImportedFurnitureWizardStepsView(
          this.piece, this.modelName, this.home != null, this.preferences, this);
    }
    return this.stepsView;
  }

  /**
   * Switch in the wizard view to the given <code>step</code>.
   */
  protected void setStep(Step step) {
    if (step != this.step) {
      Step oldStep = this.step;
      this.step = step;
      this.propertyChangeSupport.firePropertyChange(Property.STEP.name(), oldStep, step);
    }
  }

  /**
   * Returns the current step in wizard view.
   */
  public Step getStep() {
    return this.step;
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
   * Returns the model content of the imported piece.
   */
  public Content getModel() {
    return this.model;
  }

  /**
   * Sets the model content of the imported piece.
   */
  public void setModel(Content model) {
    if (model != this.model) {
      Content oldModel = this.model;
      this.model = model;
      this.propertyChangeSupport.firePropertyChange(Property.MODEL.name(), oldModel, model);
    }
  }

  /**
   * Returns <code>true</code> if imported piece back face should be shown.
   */
  public boolean isBackFaceShown() {
    return this.backFaceShown;
  }

  /**
   * Sets whether imported piece back face should be shown.
   */
  public void setBackFaceShown(boolean backFaceShown) {
    if (backFaceShown != this.backFaceShown) {
      this.backFaceShown = backFaceShown;
      this.propertyChangeSupport.firePropertyChange(Property.BACK_FACE_SHOWN.name(), !backFaceShown, backFaceShown);
    }
  }

  /**
   * Returns the model size of the imported piece.
   */
  public long getModelSize() {
    return this.modelSize;
  }

  /**
   * Sets the model size of the content of the imported piece.
   */
  public void setModelSize(long modelSize) {
    if (modelSize != this.modelSize) {
      long oldModelSize = this.modelSize;
      this.modelSize = modelSize;
      this.propertyChangeSupport.firePropertyChange(Property.MODEL_SIZE.name(), oldModelSize, modelSize);
    }
  }

  /**
   * Returns the pitch angle of the imported piece model.
   */
  public float [][] getModelRotation() {
    return this.modelRotation;
  }

  /**
   * Sets the orientation pitch angle of the imported piece model.
   */
  public void setModelRotation(float [][] modelRotation) {
    if (modelRotation != this.modelRotation) {
      float [][] oldModelRotation = this.modelRotation;
      this.modelRotation = modelRotation;
      this.propertyChangeSupport.firePropertyChange(Property.MODEL_ROTATION.name(), oldModelRotation, modelRotation);
    }
  }

  /**
   * Returns <code>true</code> if edge color materials should be hidden.
   * @since 7.0
   */
  public boolean isEdgeColorMaterialHidden() {
    return this.edgeColorMaterialHidden;
  }

  /**
   * Sets whether edge color materials should be hidden or not.
   * @since 7.0
   */
  public void setEdgeColorMaterialHidden(boolean edgeColorMaterialHidden) {
    if (edgeColorMaterialHidden != this.edgeColorMaterialHidden) {
      this.edgeColorMaterialHidden = edgeColorMaterialHidden;
      this.propertyChangeSupport.firePropertyChange(Property.EDGE_COLOR_MATERIAL_HIDDEN.name(), !edgeColorMaterialHidden, edgeColorMaterialHidden);
    }
  }

  /**
   * Returns the name of the imported piece.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name of the imported piece.
   */
  public void setName(String name) {
    if (name != this.name) {
      String oldName = this.name;
      this.name = name;
      if (this.propertyChangeSupport != null) {
        this.propertyChangeSupport.firePropertyChange(Property.NAME.name(), oldName, name);
      }
    }
  }

  /**
   * Returns the creator of the imported piece.
   * @since 5.5
   */
  public String getCreator() {
    return this.creator;
  }

  /**
   * Sets the creator of the imported piece.
   * @since 5.5
   */
  public void setCreator(String creator) {
    if (creator != this.creator) {
      String oldCreator = this.creator;
      this.creator = creator;
      if (this.propertyChangeSupport != null) {
        this.propertyChangeSupport.firePropertyChange(Property.CREATOR.name(), oldCreator, creator);
      }
    }
  }

  /**
   * Returns the width.
   */
  public float getWidth() {
    return this.width;
  }

  /**
   * Sets the width of the imported piece.
   */
  public void setWidth(float width) {
    setWidth(width, false);
  }

  /**
   * Sets the width of the imported piece.
   */
  private void setWidth(float width, boolean keepProportionalWidthUnchanged) {
    float adjustedWidth = Math.max(width, 0.001f);
    if (adjustedWidth == width || !keepProportionalWidthUnchanged) {
      this.proportionalWidth = width;
    }
    if (adjustedWidth != this.width) {
      float oldWidth = this.width;
      this.width = adjustedWidth;
      this.propertyChangeSupport.firePropertyChange(Property.WIDTH.name(), oldWidth, adjustedWidth);
    }
  }

  /**
   * Returns the depth of the imported piece.
   */
  public float getDepth() {
    return this.depth;
  }

  /**
   * Sets the depth of the imported piece.
   */
  public void setDepth(float depth) {
    setDepth(depth, false);
  }

  /**
   * Sets the depth of the imported piece.
   */
  private void setDepth(float depth, boolean keepProportionalDepthUnchanged) {
    float adjustedDepth = Math.max(depth, 0.001f);
    if (adjustedDepth == depth || !keepProportionalDepthUnchanged) {
      this.proportionalDepth = depth;
    }
    if (adjustedDepth != this.depth) {
      float oldDepth = this.depth;
      this.depth = adjustedDepth;
      this.propertyChangeSupport.firePropertyChange(Property.DEPTH.name(), oldDepth, adjustedDepth);
    }
  }

  /**
   * Returns the height.
   */
  public float getHeight() {
    return this.height;
  }

  /**
   * Sets the size of the imported piece.
   */
  public void setHeight(float height) {
    setHeight(height, false);
  }

  /**
   * Sets the size of the imported piece.
   */
  private void setHeight(float height, boolean keepProportionalHeightUnchanged) {
    float adjustedHeight = Math.max(height, 0.001f);
    if (adjustedHeight == height || !keepProportionalHeightUnchanged) {
      this.proportionalHeight = height;
    }
    if (adjustedHeight != this.height) {
      float oldHeight = this.height;
      this.height = adjustedHeight;
      this.propertyChangeSupport.firePropertyChange(Property.HEIGHT.name(), oldHeight, adjustedHeight);
    }
  }

  /**
   * Returns the elevation of the imported piece.
   */
  public float getElevation() {
    return this.elevation;
  }

  /**
   * Sets the elevation of the imported piece.
   */
  public void setElevation(float elevation) {
    if (elevation != this.elevation) {
      float oldElevation = this.elevation;
      this.elevation = elevation;
      this.propertyChangeSupport.firePropertyChange(Property.ELEVATION.name(), oldElevation, elevation);
    }
  }

  /**
   * Returns <code>true</code> if imported piece is movable.
   */
  public boolean isMovable() {
    return this.movable;
  }

  /**
   * Sets whether imported piece is movable.
   */
  public void setMovable(boolean movable) {
    if (movable != this.movable) {
      this.movable = movable;
      this.propertyChangeSupport.firePropertyChange(Property.MOVABLE.name(), !movable, movable);
    }
  }

  /**
   * Returns <code>true</code> if imported piece is a door or a window.
   */
  public boolean isDoorOrWindow() {
    return this.doorOrWindow;
  }

  /**
   * Sets whether imported piece is a door or a window.
   */
  public void setDoorOrWindow(boolean doorOrWindow) {
    if (doorOrWindow != this.doorOrWindow) {
      this.doorOrWindow = doorOrWindow;
      this.propertyChangeSupport.firePropertyChange(Property.DOOR_OR_WINDOW.name(), !doorOrWindow, doorOrWindow);
      if (doorOrWindow) {
        setStaircaseCutOutShape(null);
        setMovable(false);
      }
    }
  }

  /**
   * Returns the shape used to cut out upper levels at its intersection with a staircase.
   */
  public String getStaircaseCutOutShape() {
    return this.staircaseCutOutShape;
  }

  /**
   * Sets the shape used to cut out upper levels at its intersection with a staircase.
   */
  public void setStaircaseCutOutShape(String staircaseCutOutShape) {
    if (staircaseCutOutShape != this.staircaseCutOutShape) {
      String oldStaircaseCutOutShape = this.staircaseCutOutShape;
      this.staircaseCutOutShape = staircaseCutOutShape;
      if (this.propertyChangeSupport != null) {
        this.propertyChangeSupport.firePropertyChange(Property.STAIRCASE_CUT_OUT_SHAPE.name(), oldStaircaseCutOutShape, staircaseCutOutShape);
      }
      if (this.staircaseCutOutShape != null) {
        setDoorOrWindow(false);
        setMovable(false);
      }
    }
  }

  /**
   * Returns the color of the imported piece.
   */
  public Integer getColor() {
    return this.color;
  }

  /**
   * Sets the color of the imported piece.
   */
  public void setColor(Integer color) {
    if (color != this.color) {
      Integer oldColor = this.color;
      this.color = color;
      this.propertyChangeSupport.firePropertyChange(Property.COLOR.name(), oldColor, color);
    }
  }

  /**
   * Returns the category of the imported piece.
   */
  public FurnitureCategory getCategory() {
    return this.category;
  }

  /**
   * Sets the category of the imported piece.
   */
  public void setCategory(FurnitureCategory category) {
    if (category != this.category) {
      FurnitureCategory oldCategory = this.category;
      this.category = category;
      this.propertyChangeSupport.firePropertyChange(Property.CATEGORY.name(), oldCategory, category);
    }
  }

  /**
   * Returns the icon of the imported piece.
   */
  private Content getIcon() {
    return getStepsView().getIcon();
  }

  /**
   * Returns the yaw angle of the piece icon.
   */
  public float getIconYaw() {
    return this.iconYaw;
  }

  /**
   * Sets the yaw angle of the piece icon.
   */
  public void setIconYaw(float iconYaw) {
    if (iconYaw != this.iconYaw) {
      float oldIconYaw = this.iconYaw;
      this.iconYaw = iconYaw;
      this.propertyChangeSupport.firePropertyChange(Property.ICON_YAW.name(), oldIconYaw, iconYaw);
    }
  }

  /**
   * Returns the pitch angle of the piece icon.
   * @since 7.0
   */
  public float getIconPitch() {
    return this.iconPitch;
  }

  /**
   * Sets the pitch angle of the piece icon.
   * @since 7.0
   */
  public void setIconPitch(float iconPitch) {
    if (iconPitch != this.iconPitch) {
      float oldIconPitch = this.iconPitch;
      this.iconPitch = iconPitch;
      this.propertyChangeSupport.firePropertyChange(Property.ICON_PITCH.name(), oldIconPitch, iconPitch);
    }
  }

  /**
   * Returns the scale of the piece icon.
   * @since 7.0
   */
  public float getIconScale() {
    return this.iconScale;
  }

  /**
   * Sets the scale of the piece icon.
   * @since 7.0
   */
  public void setIconScale(float iconScale) {
    if (iconScale != this.iconScale) {
      float oldIconScale = this.iconScale;
      this.iconScale = iconScale;
      this.propertyChangeSupport.firePropertyChange(Property.ICON_SCALE.name(), oldIconScale, iconScale);
    }
  }

  /**
   * Returns <code>true</code> if piece proportions should be kept.
   */
  public boolean isProportional() {
    return this.proportional;
  }

  /**
   * Sets whether piece proportions should be kept or not.
   */
  public void setProportional(boolean proportional) {
    if (proportional != this.proportional) {
      this.proportional = proportional;
      this.propertyChangeSupport.firePropertyChange(Property.PROPORTIONAL.name(), !proportional, proportional);
    }
  }

  /**
   * Returns <code>true</code> if piece name is valid.
   */
  public boolean isPieceOfFurnitureNameValid() {
    return this.name != null
        && this.name.length() > 0;
  }

  /**
   * Step state superclass. All step state share the same step view,
   * that will display a different component depending on their class name.
   */
  protected abstract class ImportedFurnitureWizardStepState extends WizardControllerStepState {
    private URL icon = ImportedFurnitureWizardController.class.getResource("resources/importedFurnitureWizard.png");

    public abstract Step getStep();

    @Override
    public void enter() {
      setStep(getStep());
    }

    @Override
    public View getView() {
      return getStepsView();
    }

    @Override
    public URL getIcon() {
      return this.icon;
    }
  }

  /**
   * Furniture model step state (first step).
   */
  private class FurnitureModelStepState extends ImportedFurnitureWizardStepState {
    private PropertyChangeListener modelChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          setNextStepEnabled(getModel() != null);
        }
      };

    @Override
    public void enter() {
      super.enter();
      setFirstStep(true);
      // First step is valid once a model is available
      setNextStepEnabled(getModel() != null);
      addPropertyChangeListener(Property.MODEL, this.modelChangeListener);
    }

    @Override
    public Step getStep() {
      return Step.MODEL;
    }

    @Override
    public void goToNextStep() {
      setStepState(getFurnitureOrientationStepState());
    }

    @Override
    public void exit() {
      removePropertyChangeListener(Property.MODEL, this.modelChangeListener);
    }
  }

  /**
   * Furniture orientation step state (second step).
   */
  private class FurnitureOrientationStepState extends ImportedFurnitureWizardStepState {
    @Override
    public void enter() {
      super.enter();
      // Step always valid by default
      setNextStepEnabled(true);
    }

    @Override
    public Step getStep() {
      return Step.ROTATION;
    }

    @Override
    public void goBackToPreviousStep() {
      setStepState(getFurnitureModelStepState());
    }

    @Override
    public void goToNextStep() {
      setStepState(getFurnitureAttributesStepState());
    }
  }

  /**
   * Furniture attributes step state (third step).
   */
  private class FurnitureAttributesStepState extends ImportedFurnitureWizardStepState {
    PropertyChangeListener widthChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (isProportional()) {
            ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.DEPTH, depthChangeListener);
            ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.HEIGHT, heightChangeListener);

            // If proportions should be kept, update depth and height
            float ratio = (Float)ev.getNewValue() / (Float)ev.getOldValue();
            setDepth(proportionalDepth * ratio, true);
            setHeight(proportionalHeight * ratio, true);

            ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.DEPTH, depthChangeListener);
            ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.HEIGHT, heightChangeListener);
          }
        }
      };
    PropertyChangeListener depthChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (isProportional()) {
            ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.WIDTH, widthChangeListener);
            ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.HEIGHT, heightChangeListener);

            // If proportions should be kept, update width and height
            float ratio = (Float)ev.getNewValue() / (Float)ev.getOldValue();
            setWidth(proportionalWidth * ratio, true);
            setHeight(proportionalHeight * ratio, true);

            ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.WIDTH, widthChangeListener);
            ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.HEIGHT, heightChangeListener);
          }
        }
      };
    PropertyChangeListener heightChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (isProportional()) {
            ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.WIDTH, widthChangeListener);
            ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.DEPTH, depthChangeListener);

            // If proportions should be kept, update width and depth
            float ratio = (Float)ev.getNewValue() / (Float)ev.getOldValue();
            setWidth(proportionalWidth * ratio, true);
            setDepth(proportionalDepth * ratio, true);

            ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.WIDTH, widthChangeListener);
            ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.DEPTH, depthChangeListener);
          }
        }
      };
    PropertyChangeListener nameAndCategoryChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          checkPieceOfFurnitureNameInCategory();
        }
      };

    @Override
    public void enter() {
      super.enter();
      ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.WIDTH, this.widthChangeListener);
      ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.DEPTH, this.depthChangeListener);
      ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.HEIGHT, this.heightChangeListener);
      ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.NAME, this.nameAndCategoryChangeListener);
      ImportedFurnitureWizardController.this.addPropertyChangeListener(Property.CATEGORY, this.nameAndCategoryChangeListener);
      checkPieceOfFurnitureNameInCategory();
    }

    private void checkPieceOfFurnitureNameInCategory() {
      setNextStepEnabled(isPieceOfFurnitureNameValid());
    }

    @Override
    public Step getStep() {
      return Step.ATTRIBUTES;
    }

    @Override
    public void goBackToPreviousStep() {
      setStepState(getFurnitureOrientationStepState());
    }

    @Override
    public void goToNextStep() {
      setStepState(getFurnitureIconStepState());
    }

    @Override
    public void exit() {
      ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.WIDTH, this.widthChangeListener);
      ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.DEPTH, this.depthChangeListener);
      ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.HEIGHT, this.heightChangeListener);
      ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.NAME, this.nameAndCategoryChangeListener);
      ImportedFurnitureWizardController.this.removePropertyChangeListener(Property.CATEGORY, this.nameAndCategoryChangeListener);
    }
  }

  /**
   * Furniture icon step state (last step).
   */
  private class FurnitureIconStepState extends ImportedFurnitureWizardStepState {
    @Override
    public void enter() {
      super.enter();
      setLastStep(true);
      // Step always valid by default
      setNextStepEnabled(true);
    }

    @Override
    public Step getStep() {
      return Step.ICON;
    }

    @Override
    public void goBackToPreviousStep() {
      setStepState(getFurnitureAttributesStepState());
    }
  }
}
