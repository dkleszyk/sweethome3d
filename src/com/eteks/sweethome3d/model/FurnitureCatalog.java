/*
 * FurnitureCatalog.java 7 avr. 2006
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Furniture catalog.
 * @author Emmanuel Puybaret
 */
public class FurnitureCatalog {
  private List<FurnitureCategory> categories = new ArrayList<FurnitureCategory>();
  private final CollectionChangeSupport<CatalogPieceOfFurniture> furnitureChangeSupport =
                             new CollectionChangeSupport<CatalogPieceOfFurniture>(this);

  /**
   * Returns the categories list sorted by name.
   * @return an unmodifiable list of categories.
   */
  public List<FurnitureCategory> getCategories() {
    return Collections.unmodifiableList(this.categories);
  }

  /**
   * Returns the count of categories in this catalog.
   */
  public int getCategoriesCount() {
    return this.categories.size();
  }

  /**
   * Returns the category at a given <code>index</code>.
   */
  public FurnitureCategory getCategory(int index) {
    return this.categories.get(index);
  }

  /**
   * Adds the furniture <code>listener</code> in parameter to this catalog.
   */
  public void addFurnitureListener(CollectionListener<CatalogPieceOfFurniture> listener) {
    this.furnitureChangeSupport.addCollectionListener(listener);
  }

  /**
   * Removes the furniture <code>listener</code> in parameter from this catalog.
   */
  public void removeFurnitureListener(CollectionListener<CatalogPieceOfFurniture> listener) {
    this.furnitureChangeSupport.removeCollectionListener(listener);
  }

  /**
   * Adds <code>piece</code> of a given <code>category</code> to this catalog.
   * Once the <code>piece</code> is added, furniture listeners added to this catalog will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification.
   * @param category the category of the piece.
   * @param piece    a piece of furniture.
   */
  public void add(FurnitureCategory category, CatalogPieceOfFurniture piece) {
    int index = Collections.binarySearch(this.categories, category);
    // If category doesn't exist yet, add it to categories
    if (index < 0) {
      category = new FurnitureCategory(category.getName());
      this.categories.add(-index - 1, category);
    } else {
      category = this.categories.get(index);
    }
    // Add current piece of furniture to category list
    category.add(piece);

    this.furnitureChangeSupport.fireCollectionChanged(piece,
        category.getIndexOfPieceOfFurniture(piece), CollectionEvent.Type.ADD);
  }

  /**
   * Deletes the <code>piece</code> from this catalog.
   * If then piece category is empty, it will be removed from the categories of this catalog.
   * Once the <code>piece</code> is deleted, furniture listeners added to this catalog will receive a
   * {@link CollectionListener#collectionChanged(CollectionEvent) collectionChanged}
   * notification.
   * @param piece a piece of furniture in that category.
   */
  public void delete(CatalogPieceOfFurniture piece) {
    FurnitureCategory category = piece.getCategory();
    // Remove piece from its category
    if (category != null) {
      int pieceIndex = category.getIndexOfPieceOfFurniture(piece);
      if (pieceIndex >= 0) {
        category.delete(piece);

        if (category.getFurnitureCount() == 0) {
          //  Make a copy of the list to avoid conflicts in the list returned by getCategories
          this.categories = new ArrayList<FurnitureCategory>(this.categories);
          this.categories.remove(category);
        }

        this.furnitureChangeSupport.fireCollectionChanged(piece, pieceIndex, CollectionEvent.Type.DELETE);
        return;
      }
    }

    throw new IllegalArgumentException("catalog doesn't contain piece " + piece.getName());
  }

  /**
   * Returns the piece of furniture with the given <code>id</code> if it exists.
   * @since 7.2
   */
  public CatalogPieceOfFurniture getPieceOfFurnitureWithId(String id) {
    for (FurnitureCategory category : this.categories) {
      for (CatalogPieceOfFurniture piece : category.getFurniture()) {
        if (id.equals(piece.getId())) {
          return piece;
        }
      }
    }
    return null;
  }
}
