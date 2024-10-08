/*
 * ShelfUnit.java 26 mai 2023
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

/**
 * A piece of furniture whith shelves.
 * @author Emmanuel Puybaret
 * @since  7.2
 */
public interface ShelfUnit extends PieceOfFurniture {
  /**
   * Returns the elevation(s) at which other objects can be placed on this shelf unit.
   */
  public float [] getShelfElevations();

  /**
   * Returns the coordinates of the shelf box(es) in which other objects can be placed in this shelf unit.
   */
  public BoxBounds [] getShelfBoxes();
}
