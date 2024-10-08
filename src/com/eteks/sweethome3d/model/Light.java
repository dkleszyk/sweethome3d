/*
 * Light.java 12 mars 2009
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
 * A piece of furniture that contains one or more light sources.
 * @author Emmanuel Puybaret
 * @since  1.7
 */
public interface Light extends PieceOfFurniture {
  /**
   * Returns the sources managed by this light. Each light source point
   * is a percentage of the width, the depth and the height of this light.
   * @return a copy of light sources array.
   */
  public abstract LightSource [] getLightSources();

  /**
   * Returns the material names of the light sources in the 3D model managed by this light.
   * @return a copy of light source material names array.
   * @since 7.0
   */
  public abstract String [] getLightSourceMaterialNames();
}
