/*
 * SelectionEvent.java 26 juin 2006
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
package com.eteks.sweethome3d.model;

import java.util.EventObject;
import java.util.List;

/**
 * Type of event notified when selection changes in home or furniture catalog.
 * @author Emmanuel Puybaret
 */
public class SelectionEvent extends EventObject {
  private List<? extends Object> oldSelectedItems;
  private List<? extends Object> selectedItems;

  /**
   * Creates an event with an associated list of selected items.
   */
  public SelectionEvent(Object source, List<? extends Object> selectedItems) {
    this(source, null, selectedItems);
  }

  /**
   * Creates an event with an associated list of selected items.
   * @since 7.2
   */
  public SelectionEvent(Object source, List<? extends Object> oldSelectedItems, List<? extends Object> selectedItems) {
    super(source);
    this.oldSelectedItems = oldSelectedItems;
    this.selectedItems = selectedItems;
  }

  /**
   * Returns the previously selected items or <code>null</code> if not known.
   * @since 7.2
   */
  public List<? extends Object> getOldSelectedItems() {
    return this.oldSelectedItems;
  }

  /**
   * Returns the selected items.
   */
  public List<? extends Object> getSelectedItems() {
    return this.selectedItems;
  }
}
