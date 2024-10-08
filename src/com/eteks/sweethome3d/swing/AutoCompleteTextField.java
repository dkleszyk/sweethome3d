/*
 * AutoCompleteTextField.java 6 aout 2011
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

import java.util.List;

import javax.swing.JTextField;
import javax.swing.text.Document;

/**
 * A text field that suggests to the user strings stored in auto completion strings in the user preferences.
 * Inspired from the code released in public domain by Samuel Sjoberg on his
 * <a href="http://samuelsjoberg.com/archive/2009/10/autocompletion-in-swing">blog</a>.
 * @author Emmanuel Puybaret
 */
public class AutoCompleteTextField extends JTextField {
  public AutoCompleteTextField(String text, int preferredLength, List<String> autoCompletionStrings) {
    super(text, preferredLength);
    setDocument(new AutoCompleteDocument(this, autoCompletionStrings));
  }

  @Override
  public void setText(String t) {
    Document document = getDocument();
    if (document instanceof AutoCompleteDocument) {
      ((AutoCompleteDocument)document).setAutoCompletionEnabled(false);
    }
    super.setText(t);
    if (document instanceof AutoCompleteDocument) {
      ((AutoCompleteDocument)document).setAutoCompletionEnabled(true);
    }
  }
}
