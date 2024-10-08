/*
 * TextureImage.java 5 oct. 07
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

import java.io.Serializable;

/**
 * An image used as texture.
 * @author Emmanuel Puybaret
 */
public interface TextureImage extends Serializable {
  /**
   * Returns the name of this texture image.
   */
  public abstract String getName();

  /**
   * Returns the creator of this texture.
   * @since 5.5
   */
  public String getCreator();

  /**
   * Returns the content of the image used for this texture. 
   */
  public abstract Content getImage();

  /**
   * Returns the width of the image in centimeters.
   */
  public abstract float getWidth();

  /**
   * Returns the height of the image in centimeters.
   */
  public abstract float getHeight();

}