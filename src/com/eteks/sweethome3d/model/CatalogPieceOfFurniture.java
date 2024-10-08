/*
 * CatalogPieceOfFurniture.java 7 avr. 2006
 *
 * Sweet Home 3D, Copyright (c) 2024 Space Mushrooms <info@sweethome3d.com>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.eteks.sweethome3d.model;

import java.math.BigDecimal;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A catalog piece of furniture.
 * @author Emmanuel Puybaret
 */
public class CatalogPieceOfFurniture implements Comparable<CatalogPieceOfFurniture>, PieceOfFurniture, CatalogItem, Cloneable {
  private static final byte [][]    EMPTY_CRITERIA     = new byte [0][];

  private final String              id;
  private final String              name;
  private final String              description;
  private final String              information;
  private final String              license;
  private final String []           tags;
  private final Long                creationDate;
  private final Float               grade;
  private final Content             icon;
  private final Content             planIcon;
  private final Content             model;
  private final float               width;
  private final float               depth;
  private final float               height;
  private final boolean             proportional;
  private final float               elevation;
  private final float               dropOnTopElevation;
  private final boolean             movable;
  private final boolean             doorOrWindow;
  private final String              staircaseCutOutShape;
  private final float [][]          modelRotation;
  private final int                 modelFlags;
  private final Long                modelSize;
  private final String              creator;
  private final Integer             color;
  private final float               iconYaw;
  private final float               iconPitch;
  private final float               iconScale;
  private final boolean             modifiable;
  private final boolean             resizable;
  private final boolean             deformable;
  private final boolean             texturable;
  private final boolean             horizontallyRotatable;
  private final BigDecimal          price;
  private final BigDecimal          valueAddedTaxPercentage;
  private final String              currency;
  private final Map<String, Object> properties;

  private FurnitureCategory          category;
  private byte []                    filterCollationKey;

  private static final Collator               COMPARATOR;
  private static final Map<String, byte [][]> recentFilters;

  static {
    COMPARATOR = Collator.getInstance();
    COMPARATOR.setStrength(Collator.PRIMARY);
    recentFilters = Collections.synchronizedMap(new WeakHashMap<String, byte[][]>());
  }

  /**
   * Creates a catalog piece of furniture.
   * @param name  the name of the new piece
   * @param icon content of the icon of the new piece
   * @param model content of the 3D model of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param doorOrWindow if <code>true</code>, the new piece is a door or a window
   * @deprecated As of version 1.7, use constructor without <code>doorOrWindow</code>
   *             parameter since a catalog door and window is supposed to be an instance
   *             of {@link CatalogDoorOrWindow}
   */
  public CatalogPieceOfFurniture(String name, Content icon, Content model,
                                 float width, float depth, float height,
                                 boolean movable, boolean doorOrWindow) {
    this(null, name, null, icon, model, width, depth, height, 0, movable, doorOrWindow,
        IDENTITY_ROTATION, null, true, null, null);
  }

  /**
   * Creates an unmodifiable catalog piece of furniture of the default catalog.
   * @param id    the id of the new piece or <code>null</code>
   * @param name  the name of the new piece
   * @param description the description of the new piece
   * @param icon content of the icon of the new piece
   * @param model content of the 3D model of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param elevation  the elevation in centimeters of the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param doorOrWindow if <code>true</code>, the new piece is a door or a window
   * @param modelRotation the rotation 3 by 3 matrix applied to the piece model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new piece may be edited
   * @param price the price of the new piece or <code>null</code>
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the
   *             price of the new piece or <code>null</code>
   * @deprecated As of version 1.7, use constructor without <code>doorOrWindow</code>
   *             parameter since a catalog door and window is supposed to be an instance
   *             of {@link CatalogDoorOrWindow}
   */
  public CatalogPieceOfFurniture(String id, String name, String description, Content icon, Content model,
                                 float width, float depth, float height, float elevation,
                                 boolean movable, boolean doorOrWindow,
                                 float [][] modelRotation, String creator,
                                 boolean resizable, BigDecimal price, BigDecimal valueAddedTaxPercentage) {
    this(id, name, description, icon, model, width, depth, height, elevation, movable,
        modelRotation, creator, resizable, price, valueAddedTaxPercentage);
  }

  /**
   * Creates an unmodifiable catalog piece of furniture of the default catalog.
   * @param id    the id of the new piece or <code>null</code>
   * @param name  the name of the new piece
   * @param description the description of the new piece
   * @param icon content of the icon of the new piece
   * @param model content of the 3D model of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param elevation  the elevation in centimeters of the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param modelRotation the rotation 3 by 3 matrix applied to the piece model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new piece may be edited
   * @param price the price of the new piece or <code>null</code>
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the
   *             price of the new piece or <code>null</code>
   * @since 1.7
   */
  public CatalogPieceOfFurniture(String id, String name, String description, Content icon, Content model,
                                 float width, float depth, float height, float elevation,
                                 boolean movable, float [][] modelRotation, String creator,
                                 boolean resizable, BigDecimal price, BigDecimal valueAddedTaxPercentage) {
    this(id, name, description, icon, null, model, width, depth, height, elevation, movable,
        modelRotation, creator, resizable, price, valueAddedTaxPercentage);
  }

  /**
   * Creates an unmodifiable catalog piece of furniture of the default catalog.
   * @param id    the id of the new piece or <code>null</code>
   * @param name  the name of the new piece
   * @param description the description of the new piece
   * @param icon content of the icon of the new piece
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param elevation  the elevation in centimeters of the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param modelRotation the rotation 3 by 3 matrix applied to the piece model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new piece may be edited
   * @param price the price of the new piece or <code>null</code>
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the
   *             price of the new piece or <code>null</code>
   * @since 2.2
   */
  public CatalogPieceOfFurniture(String id, String name, String description,
                                 Content icon, Content planIcon, Content model,
                                 float width, float depth, float height, float elevation,
                                 boolean movable, float [][] modelRotation, String creator,
                                 boolean resizable, BigDecimal price, BigDecimal valueAddedTaxPercentage) {
    this(id, name, description, icon, planIcon, model, width, depth, height, elevation, movable,
        modelRotation, creator, resizable, true, true, price, valueAddedTaxPercentage);
  }

  /**
   * Creates an unmodifiable catalog piece of furniture of the default catalog.
   * @param id    the id of the new piece or <code>null</code>
   * @param name  the name of the new piece
   * @param description the description of the new piece
   * @param icon content of the icon of the new piece
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param elevation  the elevation in centimeters of the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param modelRotation the rotation 3 by 3 matrix applied to the piece model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new piece may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture.
   * @param price the price of the new piece or <code>null</code>
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the
   *             price of the new piece or <code>null</code>
   * @since 3.0
   */
  public CatalogPieceOfFurniture(String id, String name, String description,
                                 Content icon, Content planIcon, Content model,
                                 float width, float depth, float height, float elevation,
                                 boolean movable, float [][] modelRotation, String creator,
                                 boolean resizable, boolean deformable, boolean texturable,
                                 BigDecimal price, BigDecimal valueAddedTaxPercentage) {
    this(id, name, description, icon, planIcon, model, width, depth, height, elevation,
        movable, null, modelRotation, creator, resizable, deformable, texturable,
        price, valueAddedTaxPercentage, null);
  }

  /**
   * Creates an unmodifiable catalog piece of furniture of the default catalog.
   * @param id    the id of the new piece or <code>null</code>
   * @param name  the name of the new piece
   * @param description the description of the new piece
   * @param icon content of the icon of the new piece
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param elevation  the elevation in centimeters of the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param staircaseCutOutShape the shape used to cut out upper levels when they intersect
   *            with the piece like a staircase
   * @param modelRotation the rotation 3 by 3 matrix applied to the piece model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new piece may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture.
   * @param price the price of the new piece or <code>null</code>
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the
   *             price of the new piece or <code>null</code>
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code>
   * @since 3.4
   */
  public CatalogPieceOfFurniture(String id, String name, String description,
                                 Content icon, Content planIcon, Content model,
                                 float width, float depth, float height,
                                 float elevation, boolean movable, String staircaseCutOutShape,
                                 float [][] modelRotation, String creator,
                                 boolean resizable, boolean deformable, boolean texturable,
                                 BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency) {
    this(id, name, description, null, new String [0], null, null, icon, planIcon, model, width, depth,
        height, elevation, movable, staircaseCutOutShape, modelRotation, creator, resizable, deformable,
        texturable, price, valueAddedTaxPercentage, currency);
  }

  /**
   * Creates an unmodifiable catalog piece of furniture of the default catalog.
   * @param id    the id of the new piece or <code>null</code>
   * @param name  the name of the new piece
   * @param description the description of the new piece
   * @param information additional information associated to the new piece
   * @param tags tags associated to the new piece
   * @param creationDate creation date of the new piece in milliseconds since the epoch
   * @param grade grade of the piece of furniture or <code>null</code>
   * @param icon content of the icon of the new piece
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param elevation  the elevation in centimeters of the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param staircaseCutOutShape the shape used to cut out upper levels when they intersect
   *            with the piece like a staircase
   * @param modelRotation the rotation 3 by 3 matrix applied to the piece model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new piece may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture.
   * @param price the price of the new piece or <code>null</code>
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the
   *             price of the new piece or <code>null</code>
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code>
   * @since 3.6
   */
  public CatalogPieceOfFurniture(String id, String name, String description,
                                 String information, String [] tags, Long creationDate, Float grade,
                                 Content icon, Content planIcon, Content model,
                                 float width, float depth, float height,
                                 float elevation, boolean movable, String staircaseCutOutShape,
                                 float [][] modelRotation, String creator,
                                 boolean resizable, boolean deformable, boolean texturable,
                                 BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency) {
    this(id, name, description, null, information, tags, creationDate, grade, icon, planIcon, model, width, depth,
        height, elevation, 1f, movable, false, staircaseCutOutShape, null, modelRotation, 0, null, creator, resizable, deformable,
        texturable, true, price, valueAddedTaxPercentage, currency, null, null, (float)Math.PI / 8, 0, 1, true, false);
  }

  /**
   * Creates an unmodifiable catalog piece of furniture of the default catalog.
   * @param id    the id of the new piece or <code>null</code>
   * @param name  the name of the new piece
   * @param description the description of the new piece
   * @param information additional information associated to the new piece
   * @param tags tags associated to the new piece
   * @param creationDate creation date of the new piece in milliseconds since the epoch
   * @param grade grade of the piece of furniture or <code>null</code>
   * @param icon content of the icon of the new piece
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param elevation  the elevation in centimeters of the new piece
   * @param dropOnTopElevation  a percentage of the height at which should be placed
   *            an object dropped on the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param staircaseCutOutShape the shape used to cut out upper levels when they intersect
   *            with the piece like a staircase
   * @param modelRotation the rotation 3 by 3 matrix applied to the piece model
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new piece may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture.
   * @param price the price of the new piece or <code>null</code>
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the
   *             price of the new piece or <code>null</code>
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code>
   * @since 4.4
   */
  public CatalogPieceOfFurniture(String id, String name, String description,
                                 String information, String [] tags, Long creationDate, Float grade,
                                 Content icon, Content planIcon, Content model,
                                 float width, float depth, float height,
                                 float elevation, float dropOnTopElevation,
                                 boolean movable, String staircaseCutOutShape,
                                 float [][] modelRotation, String creator,
                                 boolean resizable, boolean deformable, boolean texturable,
                                 BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency) {
    this(id, name, description, information, tags, creationDate, grade, icon, planIcon, model, width, depth, height,
        elevation, dropOnTopElevation, movable, staircaseCutOutShape, modelRotation, false,
        creator, resizable, deformable, texturable, price, valueAddedTaxPercentage, currency);
  }

  /**
   * Creates an unmodifiable catalog piece of furniture of the default catalog.
   * @param id    the id of the new piece or <code>null</code>
   * @param name  the name of the new piece
   * @param description the description of the new piece
   * @param information additional information associated to the new piece
   * @param tags tags associated to the new piece
   * @param creationDate creation date of the new piece in milliseconds since the epoch
   * @param grade grade of the piece of furniture or <code>null</code>
   * @param icon content of the icon of the new piece
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param elevation  the elevation in centimeters of the new piece
   * @param dropOnTopElevation  a percentage of the height at which should be placed
   *            an object dropped on the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param staircaseCutOutShape the shape used to cut out upper levels when they intersect
   *            with the piece like a staircase
   * @param modelRotation the rotation 3 by 3 matrix applied to the piece model
   * @param backFaceShown <code>true</code> if back face should be shown instead of front faces
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new piece may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture.
   * @param price the price of the new piece or <code>null</code>
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the
   *             price of the new piece or <code>null</code>
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code>
   * @since 5.3
   */
  public CatalogPieceOfFurniture(String id, String name, String description,
                                 String information, String [] tags, Long creationDate, Float grade,
                                 Content icon, Content planIcon, Content model,
                                 float width, float depth, float height,
                                 float elevation, float dropOnTopElevation,
                                 boolean movable, String staircaseCutOutShape,
                                 float [][] modelRotation, boolean backFaceShown, String creator,
                                 boolean resizable, boolean deformable, boolean texturable,
                                 BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency) {
    this(id, name, description, information, tags, creationDate, grade, icon, planIcon, model, width, depth,
        height, elevation, dropOnTopElevation, movable, staircaseCutOutShape, modelRotation, backFaceShown, null,
        creator, resizable, deformable, texturable, true, price, valueAddedTaxPercentage, currency);
  }

  /**
   * Creates an unmodifiable catalog piece of furniture of the default catalog.
   * @param id    the id of the new piece or <code>null</code>
   * @param name  the name of the new piece
   * @param description the description of the new piece
   * @param information additional information associated to the new piece
   * @param tags tags associated to the new piece
   * @param creationDate creation date of the new piece in milliseconds since the epoch
   * @param grade grade of the piece of furniture or <code>null</code>
   * @param icon content of the icon of the new piece
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param elevation  the elevation in centimeters of the new piece
   * @param dropOnTopElevation  a percentage of the height at which should be placed
   *            an object dropped on the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param staircaseCutOutShape the shape used to cut out upper levels when they intersect
   *            with the piece like a staircase
   * @param modelRotation the rotation 3 by 3 matrix applied to the piece model
   * @param backFaceShown <code>true</code> if back face should be shown instead of front faces
   * @param modelSize size of the 3D model of the new piece
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new piece may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture
   * @param horizontallyRotatable if <code>false</code> this piece
   *            should not rotate around an horizontal axis
   * @param price the price of the new piece or <code>null</code>
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the
   *             price of the new piece or <code>null</code>
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code>
   * @since 5.5
   */
  public CatalogPieceOfFurniture(String id, String name, String description,
                                 String information, String [] tags, Long creationDate, Float grade,
                                 Content icon, Content planIcon, Content model,
                                 float width, float depth, float height,
                                 float elevation, float dropOnTopElevation,
                                 boolean movable, String staircaseCutOutShape,
                                 float [][] modelRotation, boolean backFaceShown, Long modelSize, String creator,
                                 boolean resizable, boolean deformable, boolean texturable, boolean horizontallyRotatable,
                                 BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency) {
    this(id, name, description, information, tags, creationDate, grade, icon, planIcon, model, width, depth,
        height, elevation, dropOnTopElevation, movable, staircaseCutOutShape, modelRotation, backFaceShown, modelSize,
        creator, resizable, deformable, texturable, horizontallyRotatable, price, valueAddedTaxPercentage, currency, null);
  }

  /**
   * Creates an unmodifiable catalog piece of furniture of the default catalog.
   * @param id    the id of the new piece or <code>null</code>
   * @param name  the name of the new piece
   * @param description the description of the new piece
   * @param information additional information associated to the new piece
   * @param tags tags associated to the new piece
   * @param creationDate creation date of the new piece in milliseconds since the epoch
   * @param grade grade of the piece of furniture or <code>null</code>
   * @param icon content of the icon of the new piece
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param elevation  the elevation in centimeters of the new piece
   * @param dropOnTopElevation  a percentage of the height at which should be placed
   *            an object dropped on the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param staircaseCutOutShape the shape used to cut out upper levels when they intersect
   *            with the piece like a staircase
   * @param modelRotation the rotation 3 by 3 matrix applied to the piece model
   * @param backFaceShown <code>true</code> if back face should be shown instead of front faces
   * @param modelSize size of the 3D model of the new piece
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new piece may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture
   * @param horizontallyRotatable if <code>false</code> this piece
   *            should not rotate around an horizontal axis
   * @param price the price of the new piece or <code>null</code>
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the
   *             price of the new piece or <code>null</code>
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code>
   * @param properties additional properties associating a key to a value or <code>null</code>
   * @since 5.7
   */
  public CatalogPieceOfFurniture(String id, String name, String description,
                                 String information, String [] tags, Long creationDate, Float grade,
                                 Content icon, Content planIcon, Content model,
                                 float width, float depth, float height,
                                 float elevation, float dropOnTopElevation,
                                 boolean movable, String staircaseCutOutShape,
                                 float [][] modelRotation, boolean backFaceShown, Long modelSize, String creator,
                                 boolean resizable, boolean deformable, boolean texturable, boolean horizontallyRotatable,
                                 BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency,
                                 Map<String, String> properties) {
    this(id, name, description, information, tags, creationDate, grade, icon, planIcon, model, width, depth, height, elevation,
        dropOnTopElevation, movable, staircaseCutOutShape, modelRotation, backFaceShown ? SHOW_BACK_FACE : 0,
        modelSize, creator, resizable, deformable, texturable, horizontallyRotatable,
        price, valueAddedTaxPercentage, currency, properties);
  }

  /**
   * Creates an unmodifiable catalog piece of furniture of the default catalog.
   * @param id    the id of the new piece or <code>null</code>
   * @param name  the name of the new piece
   * @param description the description of the new piece
   * @param information additional information associated to the new piece
   * @param tags tags associated to the new piece
   * @param creationDate creation date of the new piece in milliseconds since the epoch
   * @param grade grade of the piece of furniture or <code>null</code>
   * @param icon content of the icon of the new piece
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param elevation  the elevation in centimeters of the new piece
   * @param dropOnTopElevation  a percentage of the height at which should be placed
   *            an object dropped on the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param staircaseCutOutShape the shape used to cut out upper levels when they intersect
   *            with the piece like a staircase
   * @param modelRotation the rotation 3 by 3 matrix applied to the piece model
   * @param modelFlags flags which should be applied to piece model
   * @param modelSize size of the 3D model of the new piece
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new piece may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture
   * @param horizontallyRotatable if <code>false</code> this piece
   *            should not rotate around an horizontal axis
   * @param price the price of the new piece or <code>null</code>
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the
   *             price of the new piece or <code>null</code>
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code>
   * @param properties additional properties associating a key to a value or <code>null</code>
   * @since 7.0
   */
  public CatalogPieceOfFurniture(String id, String name, String description,
                                 String information, String [] tags, Long creationDate, Float grade,
                                 Content icon, Content planIcon, Content model,
                                 float width, float depth, float height,
                                 float elevation, float dropOnTopElevation,
                                 boolean movable, String staircaseCutOutShape,
                                 float [][] modelRotation, int modelFlags, Long modelSize, String creator,
                                 boolean resizable, boolean deformable, boolean texturable, boolean horizontallyRotatable,
                                 BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency,
                                 Map<String, String> properties) {
    this(id, name, description, information, null, tags, creationDate, grade, icon, planIcon, model, width, depth,
        height, elevation, dropOnTopElevation, movable, staircaseCutOutShape, modelRotation, modelFlags,
        modelSize, creator, resizable, deformable, texturable, horizontallyRotatable,
        price, valueAddedTaxPercentage, currency, properties, null);
  }

  /**
   * Creates an unmodifiable catalog piece of furniture of the default catalog.
   * @param id    the id of the new piece or <code>null</code>
   * @param name  the name of the new piece
   * @param description the description of the new piece
   * @param information additional information associated to the new piece
   * @param license license of the new piece
   * @param tags tags associated to the new piece
   * @param creationDate creation date of the new piece in milliseconds since the epoch
   * @param grade grade of the piece of furniture or <code>null</code>
   * @param icon content of the icon of the new piece
   * @param planIcon content of the icon of the new piece displayed in plan
   * @param model content of the 3D model of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param elevation  the elevation in centimeters of the new piece
   * @param dropOnTopElevation  a percentage of the height at which should be placed
   *            an object dropped on the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param staircaseCutOutShape the shape used to cut out upper levels when they intersect
   *            with the piece like a staircase
   * @param modelRotation the rotation 3 by 3 matrix applied to the piece model
   * @param modelFlags flags which should be applied to piece model
   * @param modelSize size of the 3D model of the new piece
   * @param creator the creator of the model
   * @param resizable if <code>true</code>, the size of the new piece may be edited
   * @param deformable if <code>true</code>, the width, depth and height of the new piece may
   *            change independently from each other
   * @param texturable if <code>false</code> this piece should always keep the same color or texture
   * @param horizontallyRotatable if <code>false</code> this piece
   *            should not rotate around an horizontal axis
   * @param price the price of the new piece or <code>null</code>
   * @param valueAddedTaxPercentage the Value Added Tax percentage applied to the
   *             price of the new piece or <code>null</code>
   * @param currency the price currency, noted with ISO 4217 code, or <code>null</code>
   * @param properties additional properties associating a key to a value or <code>null</code>
   * @param contents   additional contents associating a key to a value or <code>null</code>
   * @since 7.2
   */
  public CatalogPieceOfFurniture(String id, String name, String description,
                                 String information, String license,
                                 String [] tags, Long creationDate, Float grade,
                                 Content icon, Content planIcon, Content model,
                                 float width, float depth, float height,
                                 float elevation, float dropOnTopElevation,
                                 boolean movable, String staircaseCutOutShape,
                                 float [][] modelRotation, int modelFlags, Long modelSize, String creator,
                                 boolean resizable, boolean deformable, boolean texturable, boolean horizontallyRotatable,
                                 BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency,
                                 Map<String, String> properties, Map<String, Content> contents) {
    this(id, name, description, information, license, tags, creationDate, grade, icon, planIcon, model, width, depth,
        height, elevation, dropOnTopElevation,
        movable, false, staircaseCutOutShape, null, modelRotation, modelFlags,
        modelSize, creator, resizable, deformable, texturable, horizontallyRotatable,
        price, valueAddedTaxPercentage, currency, properties, contents, (float)Math.PI / 8, 0, 1, true, false);
  }

  /**
   * Creates a modifiable catalog piece of furniture with all its values.
   * @param name  the name of the new piece
   * @param icon content of the icon of the new piece
   * @param model content of the 3D model of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param elevation  the elevation in centimeters of the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param doorOrWindow if <code>true</code>, the new piece is a door or a window
   * @param color the color of the piece as RGB code or <code>null</code> if piece color is unchanged
   * @param modelRotation the rotation 3 by 3 matrix applied to the piece model
   * @param backFaceShown <code>true</code> if back face should be shown instead of front faces
   * @param iconYaw the yaw angle used to create the piece icon
   * @param proportional if <code>true</code>, size proportions will be kept
   * @deprecated As of version 1.7, use constructor without <code>doorOrWindow</code>
   *             parameter since a catalog door and window is supposed to be an instance
   *             of {@link CatalogDoorOrWindow}
   */
  public CatalogPieceOfFurniture(String name, Content icon, Content model,
                                 float width, float depth, float height, float elevation,
                                 boolean movable, boolean doorOrWindow, Integer color,
                                 float [][] modelRotation, boolean backFaceShown,
                                 float iconYaw, boolean proportional) {
    this(name, icon, model, width, depth, height, elevation, movable,
        color, modelRotation, backFaceShown, iconYaw, proportional);
  }

  /**
   * Creates a modifiable catalog piece of furniture with all its values.
   * @param name  the name of the new piece
   * @param icon content of the icon of the new piece
   * @param model content of the 3D model of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param elevation  the elevation in centimeters of the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param color the color of the piece as RGB code or <code>null</code> if piece color is unchanged
   * @param modelRotation the rotation 3 by 3 matrix applied to the piece model
   * @param backFaceShown <code>true</code> if back face should be shown
   * @param iconYaw the yaw angle used to create the piece icon
   * @param proportional if <code>true</code>, size proportions will be kept
   * @since 1.7
   */
  public CatalogPieceOfFurniture(String name, Content icon, Content model,
                                 float width, float depth, float height, float elevation,
                                 boolean movable, Integer color,
                                 float [][] modelRotation, boolean backFaceShown,
                                 float iconYaw, boolean proportional) {
    this(name, icon, model, width, depth, height, elevation, movable,
        null, color, modelRotation, backFaceShown, iconYaw, proportional);
  }

  /**
   * Creates a modifiable catalog piece of furniture with all its values.
   * @param name  the name of the new piece
   * @param icon content of the icon of the new piece
   * @param model content of the 3D model of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param elevation  the elevation in centimeters of the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param staircaseCutOutShape the shape used to cut out upper levels when they intersect
   *            with the piece like a staircase
   * @param color the color of the piece as RGB code or <code>null</code> if piece color is unchanged
   * @param modelRotation the rotation 3 by 3 matrix applied to the piece model
   * @param backFaceShown <code>true</code> if back face should be shown
   * @param iconYaw the yaw angle used to create the piece icon
   * @param proportional if <code>true</code>, size proportions will be kept
   * @since 3.4
   */
  public CatalogPieceOfFurniture(String name, Content icon, Content model,
                                 float width, float depth, float height, float elevation,
                                 boolean movable, String staircaseCutOutShape,
                                 Integer color, float [][] modelRotation,
                                 boolean backFaceShown, float iconYaw, boolean proportional) {
    this(name, icon, model, width, depth, height, elevation, movable, staircaseCutOutShape,
         color, modelRotation, backFaceShown, null, null, iconYaw, proportional);
  }

  /**
   * Creates a modifiable catalog piece of furniture with all its values.
   * @param name  the name of the new piece
   * @param icon content of the icon of the new piece
   * @param model content of the 3D model of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param elevation  the elevation in centimeters of the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param staircaseCutOutShape the shape used to cut out upper levels when they intersect
   *            with the piece like a staircase
   * @param color the color of the piece as RGB code or <code>null</code> if piece color is unchanged
   * @param modelRotation the rotation 3 by 3 matrix applied to the piece model
   * @param backFaceShown <code>true</code> if back face should be shown
   * @param modelSize size of the 3D model of the new piece
   * @param creator the creator of the model
   * @param iconYaw the yaw angle used to create the piece icon
   * @param proportional if <code>true</code>, size proportions will be kept
   * @since 5.5
   */
  public CatalogPieceOfFurniture(String name, Content icon, Content model,
                                 float width, float depth, float height, float elevation,
                                 boolean movable, String staircaseCutOutShape,
                                 Integer color, float [][] modelRotation, boolean backFaceShown, Long modelSize,
                                 String creator, float iconYaw, boolean proportional) {
    this(null, name, null, null, null, new String [0], System.currentTimeMillis(), null, icon, null, model, width, depth, height, elevation, 1f,
        movable, false, staircaseCutOutShape, color, modelRotation, backFaceShown ? SHOW_BACK_FACE : 0,
        modelSize, creator, true, true, true, true, null, null, null,
        null, null, iconYaw, (float)(-Math.PI / 16), 1, proportional, true);
  }

  /**
   * Creates a modifiable catalog piece of furniture with all its values.
   * @param name  the name of the new piece
   * @param icon content of the icon of the new piece
   * @param model content of the 3D model of the new piece
   * @param width  the width in centimeters of the new piece
   * @param depth  the depth in centimeters of the new piece
   * @param height  the height in centimeters of the new piece
   * @param elevation  the elevation in centimeters of the new piece
   * @param movable if <code>true</code>, the new piece is movable
   * @param staircaseCutOutShape the shape used to cut out upper levels when they intersect
   *            with the piece like a staircase
   * @param color the color of the piece as RGB code or <code>null</code> if piece color is unchanged
   * @param modelRotation the rotation 3 by 3 matrix applied to the piece model
   * @param modelFlags flags which should be applied to piece model
   * @param modelSize size of the 3D model of the new piece
   * @param creator the creator of the model
   * @param iconYaw the yaw angle used to create the piece icon
   * @param iconPitch the pich angle used to create the piece icon
   * @param iconScale the scale used to create the piece icon
   * @param proportional if <code>true</code>, size proportions will be kept
   * @since 7.0
   */
  public CatalogPieceOfFurniture(String name, Content icon, Content model,
                                 float width, float depth, float height, float elevation,
                                 boolean movable, String staircaseCutOutShape,
                                 Integer color, float [][] modelRotation, int modelFlags, Long modelSize,
                                 String creator, float iconYaw, float iconPitch, float iconScale, boolean proportional) {
    this(null, name, null, null, null, new String [0], System.currentTimeMillis(), null, icon, null, model, width, depth, height, elevation, 1f,
        movable, false, staircaseCutOutShape, color, modelRotation, modelFlags, modelSize, creator, true, true, true, true, null, null, null,
        null, null, iconYaw, iconPitch, iconScale, proportional, true);
  }

  private CatalogPieceOfFurniture(String id, String name, String description,
                                  String information, String license,
                                  String [] tags, Long creationDate, Float grade,
                                  Content icon, Content planIcon, Content model,
                                  float width, float depth, float height,
                                  float elevation, float dropOnTopElevation,
                                  boolean movable, boolean doorOrWindow, String staircaseCutOutShape,
                                  Integer color, float [][] modelRotation, int modelFlags,
                                  Long modelSize, String creator, boolean resizable,
                                  boolean deformable, boolean texturable, boolean horizontallyRotatable,
                                  BigDecimal price, BigDecimal valueAddedTaxPercentage, String currency,
                                  Map<String, String> properties, Map<String, Content> contents,
                                  float iconYaw, float iconPitch, float iconScale, boolean proportional,
                                  boolean modifiable) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.information = information;
    this.license = license;
    this.tags = tags;
    this.creationDate = creationDate;
    this.grade = grade;
    this.icon = icon;
    this.planIcon = planIcon;
    this.model = model;
    this.width = width;
    this.depth = depth;
    this.height = height;
    this.elevation = elevation;
    this.dropOnTopElevation = dropOnTopElevation;
    this.movable = movable;
    this.doorOrWindow = doorOrWindow;
    this.color = color;
    this.staircaseCutOutShape = staircaseCutOutShape;
    this.creator = creator;
    this.horizontallyRotatable = horizontallyRotatable;
    this.price = price;
    this.valueAddedTaxPercentage = valueAddedTaxPercentage;
    this.currency = currency;
    // Merge properties and contents in the same map
    if (properties == null || properties.size() == 0) {
      if (contents == null || contents.size() == 0) {
        this.properties = Collections.<String, Object>emptyMap();
      } else if (contents.size() == 1) {
        this.properties = Collections.singletonMap(contents.keySet().iterator().next(), (Object)contents.values().iterator().next());
      } else {
        this.properties = new HashMap<String, Object>(contents);
      }
    } else if (properties.size() == 1 && (contents == null || contents.size() == 0)) {
      this.properties = Collections.singletonMap(properties.keySet().iterator().next(), (Object)properties.values().iterator().next());
    } else {
      this.properties = new HashMap<String, Object>(properties);
      if (contents != null) {
        this.properties.putAll(contents);
      }
    }
    if (modelRotation == null) {
      this.modelRotation = IDENTITY_ROTATION;
    } else {
      this.modelRotation = deepClone(modelRotation);
    }
    this.modelFlags = modelFlags;
    this.modelSize = modelSize;
    this.resizable = resizable;
    this.deformable = deformable;
    this.texturable = texturable;
    this.iconYaw = iconYaw;
    this.iconPitch = iconPitch;
    this.iconScale = iconScale;
    this.proportional = proportional;
    this.modifiable = modifiable;
  }

  /**
   * Returns the ID of this piece of furniture or <code>null</code>.
   */
  public String getId() {
    return this.id;
  }

  /**
   * Returns the name of this piece of furniture.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Returns the description of this piece of furniture.
   * The returned value may be <code>null</code>.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Returns the additional information associated to this piece, or <code>null</code>.
   * @since 3.6
   */
  public String getInformation() {
    return this.information;
  }

  /**
   * Returns the license associated to this piece, or <code>null</code>.
   * @since 7.2
   */
  public String getLicense() {
    return this.license;
  }

  /**
   * Returns the tags associated to this piece.
   * @since 3.6
   */
  public String [] getTags() {
    return this.tags;
  }

  /**
   * Returns the creation date of this piece in milliseconds since the epoch,
   * or <code>null</code> if no date is given to this piece.
   * @since 3.6
   */
  public Long getCreationDate() {
    return this.creationDate;
  }

  /**
   * Returns the grade of this piece, or <code>null</code> if no grade is given to this piece.
   * @since 3.6
   */
  public Float getGrade() {
    return this.grade;
  }

  /**
   * Returns the depth of this piece of furniture.
   */
  public float getDepth() {
    return this.depth;
  }

  /**
   * Returns the height of this piece of furniture.
   */
  public float getHeight() {
    return this.height;
  }

  /**
   * Returns the width of this piece of furniture.
   */
  public float getWidth() {
    return this.width;
  }

  /**
   * Returns the elevation of this piece of furniture.
   */
  public float getElevation() {
    return this.elevation;
  }

  /**
   * Returns the elevation at which should be placed an object dropped on this piece.
   * @return a percentage of the height of this piece. A negative value means that the piece
   *         should be ignored when an object is dropped on it.
   * @since 4.4
   */
  public float getDropOnTopElevation() {
    return this.dropOnTopElevation;
  }

  /**
   * Returns <code>true</code> if this piece of furniture is movable.
   */
  public boolean isMovable() {
    return this.movable;
  }

  /**
   * Returns <code>true</code> if this piece of furniture is a door or a window.
   * As this method existed before {@linkplain CatalogDoorOrWindow CatalogDoorOrWindow} class,
   * you shouldn't rely on the value returned by this method to guess if a piece
   * is an instance of <code>DoorOrWindow</code> class.
   */
  public boolean isDoorOrWindow() {
    return this.doorOrWindow;
  }

  /**
   * Returns the icon of this piece of furniture.
   */
  public Content getIcon() {
    return this.icon;
  }

  /**
   * Returns the icon of this piece of furniture displayed in plan or <code>null</code>.
   * @since 2.2
   */
  public Content getPlanIcon() {
    return this.planIcon;
  }

  /**
   * Returns the 3D model of this piece of furniture.
   */
  public Content getModel() {
    return this.model;
  }

  /**
   * Returns the flags which should be applied to the 3D model of this piece of furniture.
   * @since 7.0
   */
  public int getModelFlags() {
    return this.modelFlags;
  }

  /**
   * Returns the size of the 3D model of this piece of furniture.
   * @since 5.5
   */
  public Long getModelSize() {
    return this.modelSize;
  }

  /**
   * Returns the rotation 3 by 3 matrix of this piece of furniture that ensures
   * its model is correctly oriented.
   */
  public float [][] getModelRotation() {
    // Return a deep clone to avoid any misuse of piece data
    return deepClone(this.modelRotation);
  }

  /**
   * Returns a deep copy of the given array.
   */
  static float [][] deepClone(float [][] array) {
    float [][] clone = new float [array.length][];
    for (int i = 0; i < array.length; i++) {
      clone [i] = array [i].clone();
    }
    return clone;
  }

  /**
   * Returns the shape used to cut out upper levels when they intersect with the piece
   * like a staircase.
   * @since 3.4
   */
  public String getStaircaseCutOutShape() {
    return this.staircaseCutOutShape;
  }

  /**
   * Returns the creator of this piece.
   */
  public String getCreator() {
    return this.creator;
  }

  /**
   * Returns <code>true</code> if the back face of the piece of furniture
   * model should be displayed.
   */
  public boolean isBackFaceShown() {
    return (this.modelFlags & SHOW_BACK_FACE) == SHOW_BACK_FACE;
  }

  /**
   * Returns the color of this piece of furniture.
   */
  public Integer getColor() {
    return this.color;
  }

  /**
   * Returns the yaw angle used to create the piece icon.
   */
  public float getIconYaw() {
    return this.iconYaw;
  }

  /**
   * Returns the pitch angle used to create the piece icon.
   * @since 7.0
   */
  public float getIconPitch() {
    return this.iconPitch;
  }

  /**
   * Returns the scale used to create the piece icon.
   * @since 7.0
   */
  public float getIconScale() {
    return this.iconScale;
  }

  /**
   * Returns <code>true</code> if size proportions should be kept.
   */
  public boolean isProportional() {
    return this.proportional;
  }

  /**
   * Returns <code>true</code> if this piece is modifiable (not read from resources).
   */
  public boolean isModifiable() {
    return this.modifiable;
  }

  /**
   * Returns <code>true</code> if this piece is resizable.
   */
  public boolean isResizable() {
    return this.resizable;
  }

  /**
   * Returns <code>true</code> if this piece is deformable.
   * @since 3.0
   */
  public boolean isDeformable() {
    return this.deformable;
  }

  /**
   * Returns <code>true</code> if this piece is deformable.
   * @since 5.5
   */
  public boolean isWidthDepthDeformable() {
    return isDeformable();
  }

  /**
   * Returns <code>false</code> if this piece should always keep the same color or texture.
   * @since 3.0
   */
  public boolean isTexturable() {
    return this.texturable;
  }

  /**
   * Returns <code>false</code> if this piece should not rotate around an horizontal axis.
   * @since 5.5
   */
  public boolean isHorizontallyRotatable() {
    return this.horizontallyRotatable;
  }

  /**
   * Returns the price of this piece of furniture or <code>null</code>.
   */
  public BigDecimal getPrice() {
    return this.price;
  }

  /**
   * Returns the Value Added Tax percentage applied to the price of this piece of furniture.
   */
  public BigDecimal getValueAddedTaxPercentage() {
    return this.valueAddedTaxPercentage;
  }

  /**
   * Returns the price currency, noted with ISO 4217 code, or <code>null</code>
   * if it has no price or default currency should be used.
   * @since 3.4
   */
  public String getCurrency() {
    return this.currency;
  }

  /**
   * Returns the value of an additional property <code>name</code> of this piece.
   * @return the value of the property or <code>null</code> if it doesn't exist or if it's not a string.
   */
  public String getProperty(String name) {
    Object propertyValue = this.properties.get(name);
    if (propertyValue instanceof String) {
      return (String)propertyValue;
    } else {
      return null;
    }
  }

  /**
   * Returns the names of the additional properties of this piece.
   * @return a collection of all the names of the properties
   */
  public Collection<String> getPropertyNames() {
    return this.properties.keySet();
  }

  /**
   * Returns the value of an additional content <code>name</code> associated to this piece.
   * @return the value of the content or <code>null</code> if it doesn't exist or if it's not a content.
   * @since 7.2
   */
  public Content getContentProperty(String name) {
    Object propertyValue = this.properties.get(name);
    if (propertyValue instanceof Content) {
      return (Content)propertyValue;
    } else {
      return null;
    }
  }

  /**
   * Returns <code>true</code> if the type of given additional property is a content.
   * @since 7.2
   */
  public boolean isContentProperty(String name) {
    return this.properties.get(name) instanceof Content;
  }

  /**
   * Returns the category of this piece of furniture.
   */
  public FurnitureCategory getCategory() {
    return this.category;
  }

  /**
   * Sets the category of this piece of furniture.
   */
  void setCategory(FurnitureCategory category) {
    this.category = category;
  }

  /**
   * Returns <code>true</code> if this piece and the one in parameter are the same objects.
   * Note that, from version 3.6, two pieces of furniture can have the same name.
   */
  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  /**
   * Returns default hash code.
   */
   @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * Compares the names of this piece and the one in parameter.
   */
  public int compareTo(CatalogPieceOfFurniture piece) {
    int nameComparison = COMPARATOR.compare(this.name, piece.name);
    if (nameComparison != 0) {
      return nameComparison;
    } else {
      return this.modifiable == piece.modifiable
          ? 0
          : (this.modifiable ? 1 : -1);
    }
  }

  /**
   * Returns <code>true</code> if this piece matches the given <code>filter</code> text.
   * Each substring of the <code>filter</code> is considered as a search criterion that can match
   * the name, the category name, the creator, the license, the description or the tags of this piece.
   * @since 4.2
   */
  public boolean matchesFilter(String filter) {
    byte [][] filterCriteriaCollationKeys = getFilterCollationKeys(filter);
    int checkedCriteria = 0;
    if (filterCriteriaCollationKeys.length > 0) {
      byte [] furnitureCollationKey = getPieceOfFurnitureCollationKey();
      for (int i = 0; i < filterCriteriaCollationKeys.length; i++) {
        if (isSubCollationKey(furnitureCollationKey, filterCriteriaCollationKeys [i], 0)) {
          checkedCriteria++;
        } else {
          break;
        }
      }
    }
    return checkedCriteria == filterCriteriaCollationKeys.length;
  }

  /**
   * Returns the collation key bytes of each criterion in the given <code>filter</code>.
   */
  private byte [][] getFilterCollationKeys(String filter) {
    if (filter.length() == 0) {
      return EMPTY_CRITERIA;
    }
    byte [][] filterCollationKeys = recentFilters.get(filter);
    if (filterCollationKeys == null) {
      // Each substring in filter is a search criterion that must be verified
      String [] filterCriteria = filter.split("\\s|\\p{Punct}|\\|");
      List<byte []> filterCriteriaCollationKeys = new ArrayList<byte []>(filterCriteria.length);
      for (String criterion : filterCriteria) {
        if (criterion.length() > 0) {
          filterCriteriaCollationKeys.add(COMPARATOR.getCollationKey(criterion).toByteArray());
        }
      }
      if (filterCriteriaCollationKeys.size() == 0) {
        filterCollationKeys = EMPTY_CRITERIA;
      } else {
        filterCollationKeys = filterCriteriaCollationKeys.toArray(new byte [filterCriteriaCollationKeys.size()][]);
      }
      recentFilters.put(filter, filterCollationKeys);
    }
    return filterCollationKeys;
  }

  /**
   * Returns the collation key bytes used to compare the given <code>piece</code> with filter.
   */
  private byte [] getPieceOfFurnitureCollationKey() {
    if (this.filterCollationKey == null) {
      // Prepare filter string collation key
      StringBuilder search = new StringBuilder();
      for (String criterion : getFilterCriteria()) {
        if (search.length() != 0) {
          search.append('|');
        }
        search.append(criterion);
      }
      this.filterCollationKey = COMPARATOR.getCollationKey(search.toString()).toByteArray();
    }
    return this.filterCollationKey;
  }

  /**
   * Returns the strings used as criteria for filtering (name, category, creator, license, description and tags).
   * @see CatalogPieceOfFurniture#matchesFilter(String)
   * @since 6.2
   */
  protected String [] getFilterCriteria() {
    List<String> criteria = new ArrayList<String>();
    criteria.add(getName());
    if (getCategory() != null) {
      criteria.add(getCategory().getName());
    }
    if (getCreator() != null) {
      criteria.add(getCreator());
    }
    if (getDescription() != null) {
      criteria.add(getDescription());
    }
    if (getLicense() != null) {
      criteria.add(getLicense());
    }
    for (String tag : getTags()) {
      criteria.add(tag);
    }
    return criteria.toArray(new String [criteria.size()]);
  }

  /**
   * Returns <code>true</code> if the given filter collation key is a sub part of the first array collator key.
   */
  private boolean isSubCollationKey(byte [] collationKey, byte [] filterCollationKey, int start) {
    // Ignore the last 4 bytes of the collator key
    for (int i = start, n = collationKey.length - 4, m = filterCollationKey.length - 4; i < n && i < n - m + 1; i++) {
      if (collationKey [i] == filterCollationKey [0]) {
        for (int j = 1; j < m; j++) {
          if (collationKey [i + j] != filterCollationKey [j]) {
            return isSubCollationKey(collationKey, filterCollationKey, i + 1);
          }
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Returns a clone of this piece.
   * @since 5.8
   */
  @Override
  public CatalogPieceOfFurniture clone() {
    try {
      CatalogPieceOfFurniture clone = (CatalogPieceOfFurniture)super.clone();
      clone.category = null;
      return clone;
    } catch (CloneNotSupportedException ex) {
      throw new IllegalStateException("Super class isn't cloneable");
    }
  }
}
