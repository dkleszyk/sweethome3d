/*
 * LengthUnit.java 22 nov. 2008
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

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Unit used for lengths.
 */
public enum LengthUnit {
  /**
   * Millimeter unit.
   * @since 2.0
   */
  MILLIMETER {
    private Locale        formatLocale;
    private String        name;
    private DecimalFormat lengthFormatWithUnit;
    private DecimalFormat lengthFormat;
    private DecimalFormat areaFormatWithUnit;

    @Override
    public Format getFormatWithUnit() {
      checkLocaleChange();
      return this.lengthFormatWithUnit;
    }

    @Override
    public Format getAreaFormatWithUnit() {
      checkLocaleChange();
      return this.areaFormatWithUnit;
    }

    @Override
    public Format getFormat() {
      checkLocaleChange();
      return this.lengthFormat;
    }

    @Override
    public String getName() {
      checkLocaleChange();
      return this.name;
    }

    private void checkLocaleChange() {
      // Instantiate formats if locale changed
      if (!Locale.getDefault().equals(this.formatLocale)) {
        this.formatLocale = Locale.getDefault();
        ResourceBundle resource = ResourceBundle.getBundle(LengthUnit.class.getName());
        this.name = resource.getString("millimeterUnit");
        this.lengthFormatWithUnit = new MeterFamilyFormat("#,##0 " + this.name.replace(".", "'.'"), 10);
        this.lengthFormat = new MeterFamilyFormat("#,##0", 10);
        String squareMeterUnit = resource.getString("squareMeterUnit");
        this.areaFormatWithUnit = new SquareMeterAreaFormatWithUnit(squareMeterUnit.replace(".", "'.'"));
      }
    }

    @Override
    public float getMagnetizedLength(float length, float maxDelta) {
      return getMagnetizedMeterLength(length, maxDelta);
    }

    @Override
    public float getMinimumLength() {
      return 0.1f;
    }

    @Override
    public float getMaximumLength() {
      return 100000f;
    }

    @Override
    public float getStepSize() {
      return 0.5f;
    }

    @Override
    public float centimeterToUnit(float length) {
      return length * 10;
    }

    @Override
    public float unitToCentimeter(float length) {
      return length / 10;
    }

    @Override
    public boolean isMetric() {
      return true;
    }
  },

  /**
   * Centimeter unit.
   */
  CENTIMETER {
    private Locale        formatLocale;
    private String        name;
    private DecimalFormat lengthFormatWithUnit;
    private DecimalFormat lengthFormat;
    private DecimalFormat areaFormatWithUnit;

    @Override
    public Format getFormatWithUnit() {
      checkLocaleChange();
      return this.lengthFormatWithUnit;
    }

    @Override
    public Format getAreaFormatWithUnit() {
      checkLocaleChange();
      return this.areaFormatWithUnit;
    }

    @Override
    public Format getFormat() {
      checkLocaleChange();
      return this.lengthFormat;
    }

    @Override
    public String getName() {
      checkLocaleChange();
      return this.name;
    }

    private void checkLocaleChange() {
      // Instantiate formats if locale changed
      if (!Locale.getDefault().equals(this.formatLocale)) {
        this.formatLocale = Locale.getDefault();
        ResourceBundle resource = ResourceBundle.getBundle(LengthUnit.class.getName());
        this.name = resource.getString("centimeterUnit");
        this.lengthFormatWithUnit = new MeterFamilyFormat("#,##0.# " + this.name.replace(".", "'.'"), 1);
        this.lengthFormat = new MeterFamilyFormat("#,##0.#", 1);
        String squareMeterUnit = resource.getString("squareMeterUnit");
        this.areaFormatWithUnit = new SquareMeterAreaFormatWithUnit(squareMeterUnit.replace(".", "'.'"));
      }
    }

    @Override
    public float getMagnetizedLength(float length, float maxDelta) {
      return getMagnetizedMeterLength(length, maxDelta);
    }

    @Override
    public float getMinimumLength() {
      return 0.1f;
    }

    @Override
    public float getMaximumLength() {
      return 100000f;
    }

    @Override
    public float getStepSize() {
      return 0.5f;
    }

    @Override
    public float centimeterToUnit(float length) {
      return length;
    }

    @Override
    public float unitToCentimeter(float length) {
      return length;
    }

    @Override
    public boolean isMetric() {
      return true;
    }
  },

  /**
   * Meter unit.
   * @since 2.0
   */
  METER {
    private Locale        formatLocale;
    private String        name;
    private DecimalFormat lengthFormatWithUnit;
    private DecimalFormat lengthFormat;
    private DecimalFormat areaFormatWithUnit;

    @Override
    public Format getFormatWithUnit() {
      checkLocaleChange();
      return this.lengthFormatWithUnit;
    }

    @Override
    public Format getAreaFormatWithUnit() {
      checkLocaleChange();
      return this.areaFormatWithUnit;
    }

    @Override
    public Format getFormat() {
      checkLocaleChange();
      return this.lengthFormat;
    }

    @Override
    public String getName() {
      checkLocaleChange();
      return this.name;
    }

    private void checkLocaleChange() {
      // Instantiate formats if locale changed
      if (!Locale.getDefault().equals(this.formatLocale)) {
        this.formatLocale = Locale.getDefault();
        ResourceBundle resource = ResourceBundle.getBundle(LengthUnit.class.getName());
        this.name = resource.getString("meterUnit");
        this.lengthFormatWithUnit = new MeterFamilyFormat("#,##0.00# " + this.name.replace(".", "'.'"), 0.01f);
        this.lengthFormat = new MeterFamilyFormat("#,##0.00#", 0.01f);
        String squareMeterUnit = resource.getString("squareMeterUnit");
        this.areaFormatWithUnit = new SquareMeterAreaFormatWithUnit(squareMeterUnit.replace(".", "'.'"));
      }
    }

    @Override
    public float getMagnetizedLength(float length, float maxDelta) {
      return getMagnetizedMeterLength(length, maxDelta);
    }

    @Override
    public float getMinimumLength() {
      return 0.1f;
    }

    @Override
    public float getMaximumLength() {
      return 100000f;
    }

    @Override
    public float getStepSize() {
      return 0.5f;
    }

    @Override
    public float centimeterToUnit(float length) {
      return length / 100;
    }

    @Override
    public float unitToCentimeter(float length) {
      return length * 100;
    }

    @Override
    public boolean isMetric() {
      return true;
    }
  },

  /**
   * Foot/Inch unit followed by fraction.
   */
  INCH {
    private Locale        formatLocale;
    private String        name;
    private DecimalFormat lengthFormat;
    private DecimalFormat areaFormatWithUnit;
    private int           fractionDenominator;
    private float         fractionStep;

    @Override
    public Format getFormatWithUnit() {
      checkLocaleChange();
      return this.lengthFormat;
    }

    @Override
    public Format getFormat() {
      return getFormatWithUnit();
    }

    @Override
    public Format getAreaFormatWithUnit() {
      checkLocaleChange();
      return this.areaFormatWithUnit;
    }

    @Override
    public String getName() {
      checkLocaleChange();
      return this.name;
    }

    private void checkLocaleChange() {
      // Instantiate format if locale changed
      if (!Locale.getDefault().equals(this.formatLocale)) {
        this.formatLocale = Locale.getDefault();
        ResourceBundle resource = ResourceBundle.getBundle(LengthUnit.class.getName());
        this.name = resource.getString("inchUnit");
        this.fractionDenominator = Integer.parseInt(resource.getString("fractionDenominator"));
        this.fractionStep = 1f / (float)this.fractionDenominator;

        this.lengthFormat = new InchFractionFormat(true, this.fractionDenominator);
        String squareFootUnit = resource.getString("squareFootUnit");
        this.areaFormatWithUnit = new SquareFootAreaFormatWithUnit("#,##0 " + squareFootUnit.replace(".", "'.'"));
      }
    }

    @Override
    public float getMagnetizedLength(float length, float maxDelta) {
      return getMagnetizedInchLength(length, maxDelta);
    }

    @Override
    public float getMinimumLength() {
      return LengthUnit.inchToCentimeter(this.fractionStep);
    }

    @Override
    public float getMaximumLength() {
      return LengthUnit.footToCentimeter(3280);
    }

    @Override
    public float getStepSize() {
      return inchToCentimeter(this.fractionStep);
    }

    @Override
    public float centimeterToUnit(float length) {
      return centimeterToInch(length);
    }

    @Override
    public float unitToCentimeter(float length) {
      return inchToCentimeter(length);
    }
  },

  /**
   * Inch unit followed by fraction.
   * @since 7.0
   */
  INCH_FRACTION {
    private Locale        formatLocale;
    private String        name;
    private DecimalFormat lengthFormat;
    private DecimalFormat areaFormatWithUnit;
    private int           fractionDenominator;
    private float         fractionStep;

    @Override
    public Format getFormatWithUnit() {
      checkLocaleChange();
      return this.lengthFormat;
    }

    @Override
    public Format getFormat() {
      return getFormatWithUnit();
    }

    @Override
    public Format getAreaFormatWithUnit() {
      checkLocaleChange();
      return this.areaFormatWithUnit;
    }

    @Override
    public String getName() {
      checkLocaleChange();
      return this.name;
    }

    private void checkLocaleChange() {
      // Instantiate format if locale changed
      if (!Locale.getDefault().equals(this.formatLocale)) {
        this.formatLocale = Locale.getDefault();
        ResourceBundle resource = ResourceBundle.getBundle(LengthUnit.class.getName());
        this.name = resource.getString("inchUnit");
        this.fractionDenominator = Integer.parseInt(resource.getString("fractionDenominator"));
        this.fractionStep = 1f / (float)this.fractionDenominator;

        this.lengthFormat = new InchFractionFormat(false, this.fractionDenominator);
        String squareFootUnit = resource.getString("squareFootUnit");
        this.areaFormatWithUnit = new SquareFootAreaFormatWithUnit("#,##0 " + squareFootUnit.replace(".", "'.'"));
      }
    }

    @Override
    public float getMagnetizedLength(float length, float maxDelta) {
      return getMagnetizedInchLength(length, maxDelta);
    }

    @Override
    public float getMinimumLength() {
      return LengthUnit.inchToCentimeter(this.fractionStep);
    }

    @Override
    public float getMaximumLength() {
      return LengthUnit.footToCentimeter(3280);
    }

    @Override
    public float getStepSize() {
      return inchToCentimeter(this.fractionStep);
    }

    @Override
    public float centimeterToUnit(float length) {
      return centimeterToInch(length);
    }

    @Override
    public float unitToCentimeter(float length) {
      return inchToCentimeter(length);
    }
  },

  /**
   * Inch unit with decimals.
   * @since 4.0
   */
  INCH_DECIMALS {
    private Locale        formatLocale;
    private String        name;
    private DecimalFormat lengthFormat;
    private DecimalFormat lengthFormatWithUnit;
    private DecimalFormat areaFormatWithUnit;

    @Override
    public Format getFormatWithUnit() {
      checkLocaleChange();
      return this.lengthFormatWithUnit;
    }

    @Override
    public Format getFormat() {
      checkLocaleChange();
      return this.lengthFormat;
    }

    @Override
    public Format getAreaFormatWithUnit() {
      checkLocaleChange();
      return this.areaFormatWithUnit;
    }

    @Override
    public String getName() {
      checkLocaleChange();
      return this.name;
    }

    private void checkLocaleChange() {
      // Instantiate format if locale changed
      if (!Locale.getDefault().equals(this.formatLocale)) {
        this.formatLocale = Locale.getDefault();
        ResourceBundle resource = ResourceBundle.getBundle(LengthUnit.class.getName());
        this.name = resource.getString("inchUnit");

        // Create formats for inches with decimals
        class InchDecimalsFormat extends DecimalFormat {
          private final MessageFormat inchDecimalsFormat;
          private final NumberFormat  inchNumberFormat = NumberFormat.getNumberInstance();

          private InchDecimalsFormat(MessageFormat inchDecimalsFormat) {
            super("0.###");
            this.inchDecimalsFormat = inchDecimalsFormat;
          }

          @Override
          public StringBuffer format(double number, StringBuffer result,
                                     FieldPosition fieldPosition) {
            float inches = centimeterToInch((float)number);
            fieldPosition.setEndIndex(fieldPosition.getEndIndex() + 1);
            this.inchDecimalsFormat.format(new Object [] {inches}, result, fieldPosition);
            return result;
          }

          @Override
          public Number parse(String text, ParsePosition parsePosition) {
            ParsePosition numberPosition = new ParsePosition(parsePosition.getIndex());
            skipWhiteSpaces(text, numberPosition);
            // Parse inches
            Number inches = this.inchNumberFormat.parse(text, numberPosition);
            if (inches == null) {
              parsePosition.setErrorIndex(numberPosition.getErrorIndex());
              return null;
            }
            double value = inchToCentimeter(inches.floatValue());
            // Parse "
            skipWhiteSpaces(text, numberPosition);
            if (numberPosition.getIndex() < text.length()
                && text.charAt(numberPosition.getIndex()) == '\"') {
              parsePosition.setIndex(numberPosition.getIndex() + 1);
            } else {
              parsePosition.setIndex(numberPosition.getIndex());
            }
            return value;
          }
        }
        this.lengthFormat = new InchDecimalsFormat(new MessageFormat(resource.getString("inchDecimalsFormat")));
        this.lengthFormatWithUnit = new InchDecimalsFormat(new MessageFormat(resource.getString("inchDecimalsFormatWithUnit")));

        String squareFootUnit = resource.getString("squareFootUnit");
        this.areaFormatWithUnit = new SquareFootAreaFormatWithUnit("#,##0.## " + squareFootUnit.replace(".", "'.'"));
      }
    }

    @Override
    public float getMagnetizedLength(float length, float maxDelta) {
      return getMagnetizedInchLength(length, maxDelta);
    }

    @Override
    public float getMinimumLength() {
      return LengthUnit.inchToCentimeter(0.125f);
    }

    @Override
    public float getMaximumLength() {
      return LengthUnit.footToCentimeter(3280);
    }

    @Override
    public float getStepSize() {
      return inchToCentimeter(0.125f);
    }

    @Override
    public float centimeterToUnit(float length) {
      return centimeterToInch(length);
    }

    @Override
    public float unitToCentimeter(float length) {
      return inchToCentimeter(length);
    }
  },

  /**
   * Foot unit with decimals.
   * @since 7.0
   */
  FOOT_DECIMALS {
    private Locale        formatLocale;
    private String        name;
    private DecimalFormat lengthFormat;
    private DecimalFormat lengthFormatWithUnit;
    private DecimalFormat areaFormatWithUnit;

    @Override
    public Format getFormatWithUnit() {
      checkLocaleChange();
      return this.lengthFormatWithUnit;
    }

    @Override
    public Format getFormat() {
      checkLocaleChange();
      return this.lengthFormat;
    }

    @Override
    public Format getAreaFormatWithUnit() {
      checkLocaleChange();
      return this.areaFormatWithUnit;
    }

    @Override
    public String getName() {
      checkLocaleChange();
      return this.name;
    }

    private void checkLocaleChange() {
      // Instantiate format if locale changed
      if (!Locale.getDefault().equals(this.formatLocale)) {
        this.formatLocale = Locale.getDefault();
        ResourceBundle resource = ResourceBundle.getBundle(LengthUnit.class.getName());
        this.name = resource.getString("footUnit");

        // Create formats for feet with decimals
        class FootDecimalsFormat extends DecimalFormat {
          private final MessageFormat footDecimalsFormat;
          private final NumberFormat  footNumberFormat = NumberFormat.getNumberInstance();

          private FootDecimalsFormat(MessageFormat footDecimalsFormat) {
            super("0.###");
            this.footDecimalsFormat = footDecimalsFormat;
          }

          @Override
          public StringBuffer format(double number, StringBuffer result,
                                     FieldPosition fieldPosition) {
            float feet = centimeterToFoot((float)number);
            fieldPosition.setEndIndex(fieldPosition.getEndIndex() + 1);
            this.footDecimalsFormat.format(new Object [] {feet}, result, fieldPosition);
            return result;
          }

          @Override
          public Number parse(String text, ParsePosition parsePosition) {
            ParsePosition numberPosition = new ParsePosition(parsePosition.getIndex());
            skipWhiteSpaces(text, numberPosition);
            // Parse feet
            Number feet = this.footNumberFormat.parse(text, numberPosition);
            if (feet == null) {
              parsePosition.setErrorIndex(numberPosition.getErrorIndex());
              return null;
            }
            double value = footToCentimeter(feet.floatValue());
            // Parse '
            skipWhiteSpaces(text, numberPosition);
            if (numberPosition.getIndex() < text.length()
                && text.charAt(numberPosition.getIndex()) == '\'') {
              parsePosition.setIndex(numberPosition.getIndex() + 1);
            } else {
              parsePosition.setIndex(numberPosition.getIndex());
            }
            return value;
          }
        }
        this.lengthFormat = new FootDecimalsFormat(new MessageFormat(resource.getString("footDecimalsFormat")));
        this.lengthFormatWithUnit = new FootDecimalsFormat(new MessageFormat(resource.getString("footDecimalsFormatWithUnit")));

        String squareFootUnit = resource.getString("squareFootUnit");
        this.areaFormatWithUnit = new SquareFootAreaFormatWithUnit("#,##0.## " + squareFootUnit.replace(".", "'.'"));
      }
    }

    @Override
    public float getMagnetizedLength(float length, float maxDelta) {
      return getMagnetizedInchLength(length, maxDelta);
    }

    @Override
    public float getMinimumLength() {
      return LengthUnit.inchToCentimeter(0.125f);
    }

    @Override
    public float getMaximumLength() {
      return LengthUnit.footToCentimeter(3280);
    }

    @Override
    public float getStepSize() {
      return inchToCentimeter(0.125f);
    }

    @Override
    public float centimeterToUnit(float length) {
      return centimeterToFoot(length);
    }

    @Override
    public float unitToCentimeter(float length) {
      return footToCentimeter(length);
    }
  };


  /**
   * Returns the <code>length</code> given in centimeters converted to inches.
   */
  public static float centimeterToInch(float length) {
    return length / 2.54f;
  }

  /**
   * Returns the <code>length</code> given in centimeters converted to feet.
   */
  public static float centimeterToFoot(float length) {
    return length / 2.54f / 12;
  }

  /**
   * Returns the <code>length</code> given in inches converted to centimeters.
   */
  public static float inchToCentimeter(float length) {
    return length * 2.54f;
  }

  /**
   * Returns the <code>length</code> given in feet converted to centimeters.
   */
  public static float footToCentimeter(float length) {
    return length * 2.54f * 12;
  }

  /**
   * Returns a format able to format lengths with their localized unit.
   */
  public abstract Format getFormatWithUnit();

  /**
   * Returns a format able to format lengths.
   */
  public abstract Format getFormat();

  /**
   * A decimal format for meter family units.
   */
  private static class MeterFamilyFormat extends DecimalFormat {
    private final float unitMultiplier;

    public MeterFamilyFormat(String pattern, float unitMultiplier) {
      super(pattern);
      this.unitMultiplier = unitMultiplier;

    }

    @Override
    public StringBuffer format(double number, StringBuffer result,
                               FieldPosition fieldPosition) {
      // Convert centimeter to millimeter
      return super.format(number * this.unitMultiplier, result, fieldPosition);
    }

    @Override
    public StringBuffer format(long number, StringBuffer result,
                               FieldPosition fieldPosition) {
      return format((double)number, result, fieldPosition);
    }

    @Override
    public Number parse(String text, ParsePosition pos) {
      Number number = super.parse(text, pos);
      if (number == null) {
        return null;
      } else {
        return number.floatValue() / this.unitMultiplier;
      }
    }
  }

  /**
   * Returns a format able to format areas with their localized unit.
   */
  public abstract Format getAreaFormatWithUnit();

  /**
   * A decimal format for square meter.
   */
  private static class SquareMeterAreaFormatWithUnit extends DecimalFormat {
    public SquareMeterAreaFormatWithUnit(String squareMeterUnit) {
      super("#,##0.## " + squareMeterUnit);
    }

    @Override
    public StringBuffer format(double number, StringBuffer result,
                               FieldPosition fieldPosition) {
      // Convert square centimeter to square meter
      return super.format(number / 10000, result, fieldPosition);
    }
  }

  /**
   * A decimal format for inch lengths with fraction.
   */
  private static class InchFractionFormat extends DecimalFormat {
    private static final char [] EIGHTH_FRACTION_CHARACTERS = {'\u215b',   // 1/8
                                                               '\u00bc',   // 1/4
                                                               '\u215c',   // 3/8
                                                               '\u00bd',   // 1/2
                                                               '\u215d',   // 5/8
                                                               '\u00be',   // 3/4
                                                               '\u215e'};  // 7/8
    private static final String [] EIGHTH_FRACTION_STRINGS  = {"1/8",
                                                               "1/4",
                                                               "3/8",
                                                               "1/2",
                                                               "5/8",
                                                               "3/4",
                                                               "7/8"};

    private static final char [] FRACTION_DENOMINATOR_CHARS = {
      '\u2080', '\u2081', '\u2082', '\u2083', '\u2084',
      '\u2085', '\u2086', '\u2087', '\u2088', '\u2089'
    };

    private static final char [] FRACTION_NUMERATOR_CHARS = {
      '\u2070', '\u00b9', '\u00b2', '\u00b3', '\u2074',
      '\u2075', '\u2076', '\u2077', '\u2078', '\u2079'
    };

    private static final char [] FRACTION_SEPARATOR_CHARS = {
      '+', '-', '~',
      '\u02d7', // modifier letter minus sign
      '\u06d4', // arabic full stop
      '\u200b', // zero width space (confusingly, not whitespace)
      '\u200c', // zero width non-joiner
      '\u200d', // zero width joiner
      '\u2010', // hyphen
      '\u2011', // non-breaking hyphen
      '\u2012', // figure dash
      '\u2013', // en dash
      '\u2043', // hyphen bullet
      '\u2060', // word joiner
      '\u2064', // invisible plus
      '\u2212', // minus sign
      '\u2796', // heavy minus sign
      '\u2cba', // coptic capital letter dialect-p ni
      '\ufe58'  // small em dash
    };

    private static final char [] FRACTION_SLASH_CHARS = {
      '/', '\\',
      '\u2044', // fraction slash
      '\u2215', // division slash
      '\u29F8'  // big solidus
    };

    private static final char [] INCH_MARKER_CHARS = {
      '"',
      '\u02ba', // modifier letter double prime
      '\u02dd', // double acute accent
      '\u02ee', // modifier letter double apostrophe
      '\u02f6', // modifier letter middle double acute accent
      '\u05f2', // hebrew ligature yiddish double yod
      '\u05f4', // hebrew punctuation gershayim
      '\u1cd3', // vedic sign nihshvasa
      '\u201c', // left double quotation mark
      '\u201d', // right double quotation mark
      '\u201f', // double high-reversed-9 quotation mark
      '\u2033', // double prime
      '\u2036', // reversed double prime
      '\u3003', // ditto mark
      '\uff02', // fullwidth quotation mark
    };

    private static final char [] FOOT_MARKER_CHARS = {
      '\'',
      '\u0060', // grave accent
      '\u00b4', // acute accent
      '\u02b9', // modifier letter prime
      '\u02bb', // modifier letter turned comma
      '\u02bc', // modifier letter apostrophe
      '\u02bd', // modifier letter reversed comma
      '\u02be', // modifier letter right half ring
      '\u02c8', // modifier letter vertical line
      '\u02ca', // modifier letter acute accent
      '\u02cb', // modifier letter grave accent
      '\u02f4', // modifier letter middle grave accent
      '\u0374', // greek numeral sign
      '\u0384', // greek tonos
      '\u055a', // armenian apostrophe
      '\u055d', // armenian comma
      '\u05d9', // hebrew letter yod
      '\u05f3', // hebrew punctuation geresh
      '\u07f4', // nko high tone apostrophe
      '\u07f5', // nko low tone apostrophe
      '\u144a', // canadian syllabics west-cree p
      '\u16cc', // runic letter short-twig-sol s
      '\u1fbd', // greek koronis
      '\u1fbf', // greek psili
      '\u1fef', // greek varia
      '\u1ffd', // greek oxia
      '\u1ffe', // greek dasia
      '\u2018', // left single quotation mark
      '\u2019', // right single quotation mark
      '\u201b', // single high-reversed-9 quotation mark
      '\u2032', // prime
      '\u2035', // reversed prime
      '\ua78c', // latin small letter saltillo
      '\uff07', // fullwidth apostrophe
      '\uff40'  // fullwidth grave accent
    };

    private static final String [][] VULGAR_FRACTION_STRINGS = {
      // x->     1         2         3         4         5      6        7      8     9
      null,                                                                                 // x/0
      null,                                                                                 // x/1
      {null, "\u00bd"},                                                                     // x/2
      {null, "\u2153", "\u2154"},                                                           // x/3
      {null, "\u00bc", null,     "\u00be"},                                                 // x/4
      {null, "\u2155", "\u2156", "\u2157", "\u2158"},                                       // x/5
      {null, "\u2159", null,     null,     null,     "\u215a"},                             // x/6
      {null, "\u2150", null,     null,     null,     null,     null},                       // x/7
      {null, "\u215b", null,     "\u215c", null,     "\u215d", null, "\u215e"},             // x/8
      {null, "\u2151", null,     null,     null,     null,     null, null,     null},       // x/9
      {null, "\u2152", null,     null,     null,     null,     null, null,     null, null}, // x/10
    };

    private static final char [] VULGAR_FRACTION_CHARS;
    private static final double [] VULGAR_FRACTION_VALUES;

    static {
      int count = 0;
      for (int d = 0; d < VULGAR_FRACTION_STRINGS.length; d++) {
        if (VULGAR_FRACTION_STRINGS [d] != null) {
          for (int n = 0; n < VULGAR_FRACTION_STRINGS [d].length; n++) {
            count += VULGAR_FRACTION_STRINGS [d][n] != null ? 1 : 0;
          }
        }
      }
      VULGAR_FRACTION_CHARS = new char [count];
      VULGAR_FRACTION_VALUES = new double [count];

      int i = 0;
      for (int d = 0; d < VULGAR_FRACTION_STRINGS.length; d++) {
        if (VULGAR_FRACTION_STRINGS [d] != null) {
          for (int n = 0; n < VULGAR_FRACTION_STRINGS [d].length; n++) {
            if (VULGAR_FRACTION_STRINGS [d][n] != null) {
              assert VULGAR_FRACTION_STRINGS [d][n].length() == 1;
              VULGAR_FRACTION_CHARS [i] = VULGAR_FRACTION_STRINGS [d][n].charAt(0);
              VULGAR_FRACTION_VALUES [i] = ((double)n) / ((double)d);
              i += 1;
            }
          }
        }
      }
    }

    /* status flags for subparseNumber */
    private static final int STATUS_NEGATIVE = 0;
    private static final int STATUS_DECIMAL = 1;
    private static final int STATUS_FRACTION = 2;
    private static final int STATUS_INCH = 3;
    private static final int STATUS_FOOT = 4;
    private static final int STATUS_LENGTH = 5;

    /* values for subparseFraction */
    private static final int MAX_SIGNIFICANT_DIGITS = 19;
    private static final long [] DIGIT_MULTIPLIERS = {
      1000000000000000000L,
      100000000000000000L,
      10000000000000000L,
      1000000000000000L,
      100000000000000L,
      10000000000000L,
      1000000000000L,
      100000000000L,
      10000000000L,
      1000000000L,
      100000000L,
      10000000L,
      1000000L,
      100000L,
      10000L,
      1000L,
      100L,
      10L,
      1L
    };

    private final boolean       footInch;
    private final Integer       fractionDenominator;
    private final MessageFormat positiveFootFormat;
    private final MessageFormat positiveFootInchFormat;
    private final MessageFormat positiveFootInchFractionFormat;
    private final MessageFormat negativeFootFormat;
    private final MessageFormat negativeFootInchFormat;
    private final MessageFormat negativeFootInchFractionFormat;
    private final String        footInchSeparator;
    private final String        fractionSeparator;
    private final MessageFormat positiveInchFormat;
    private final MessageFormat positiveInchFractionFormat;
    private final MessageFormat negativeInchFormat;
    private final MessageFormat negativeInchFractionFormat;
    private final NumberFormat  integerNumberFormat;
    private final NumberFormat  decimalNumberFormat;

    private transient String [][] vulgarFractionStrings;

    public InchFractionFormat(boolean footInch, int fractionDenominator) {
      super("0.000\"");
      this.footInch = footInch;
      this.fractionDenominator = fractionDenominator != 8 ? (Integer)fractionDenominator : (Integer)null;

      ResourceBundle resource = ResourceBundle.getBundle(LengthUnit.class.getName());
      this.positiveFootFormat = new MessageFormat(resource.getString("footFormat"));
      this.positiveFootInchFormat = new MessageFormat(resource.getString("footInchFormat"));
      this.positiveFootInchFractionFormat = new MessageFormat(resource.getString("footInchFractionFormat"));
      this.negativeFootFormat = new MessageFormat("-" + resource.getString("footFormat"));
      this.negativeFootInchFormat = new MessageFormat("-" + resource.getString("footInchFormat"));
      this.negativeFootInchFractionFormat = new MessageFormat("-" + resource.getString("footInchFractionFormat"));
      this.footInchSeparator = resource.getString("footInchSeparator");
      this.fractionSeparator = resource.getString("fractionSeparator");

      this.positiveInchFormat = new MessageFormat(resource.getString("inchFormat"));
      this.positiveInchFractionFormat = new MessageFormat(resource.getString("inchFractionFormat"));
      this.negativeInchFormat = new MessageFormat("-" + resource.getString("inchFormat"));
      this.negativeInchFractionFormat = new MessageFormat("-" + resource.getString("inchFractionFormat"));

      this.integerNumberFormat = NumberFormat.getIntegerInstance();
      this.decimalNumberFormat = NumberFormat.getNumberInstance();
    }

    @Override
    public StringBuffer format(double number, StringBuffer result,
                               FieldPosition fieldPosition) {
      if (this.fractionDenominator != null) {
        return formatVariableDenominator(number, result, fieldPosition);
      }
      float absoluteValue = Math.abs((float)number);
      double feet = Math.floor(centimeterToFoot(absoluteValue));
      float remainingInches = centimeterToInch((float)absoluteValue - footToCentimeter((float)feet));
      if (remainingInches >= 11.9375f) {
        feet++;
        remainingInches -= 12;
      }
      fieldPosition.setEndIndex(fieldPosition.getEndIndex() + 1);
      // Format remaining inches only if it's larger that 0.0005
      if (remainingInches >= 0.0005f) {
        // Try to format decimals with 1/8, 1/4, 1/2 fractions first
        int integerPart = (int)Math.floor(remainingInches);
        float fractionPart = remainingInches - integerPart;
        int eighth = Math.round(fractionPart * 8);
        if (eighth == 0 || eighth == 8) {

          if (this.footInch) {
            (number >= 0 ? this.positiveFootInchFormat : this.negativeFootInchFormat).format(
                new Object [] {feet, Math.round(remainingInches * 8) / 8f}, result, fieldPosition);
          } else {
            (number >= 0 ? this.positiveInchFormat : this.negativeInchFormat).format(
                new Object [] {feet * 12 + Math.round(remainingInches * 8) / 8f}, result, fieldPosition);
          }
        } else {
          if (this.footInch) {
            (number >= 0 ? this.positiveFootInchFractionFormat : this.negativeFootInchFractionFormat).format(
                new Object [] {feet, integerPart, EIGHTH_FRACTION_CHARACTERS [eighth - 1]}, result, fieldPosition);
          } else {
            (number >= 0 ? this.positiveInchFractionFormat : this.negativeInchFractionFormat).format(
                new Object [] {feet * 12 + integerPart, EIGHTH_FRACTION_CHARACTERS [eighth - 1]}, result, fieldPosition);
          }
        }
      } else {
        if (this.footInch) {
          (number >= 0 ? this.positiveFootFormat : this.negativeFootFormat).format(
              new Object [] {feet}, result, fieldPosition);
        } else {
          (number >= 0 ? this.positiveInchFormat : this.negativeInchFormat).format(
              new Object [] {feet * 12}, result, fieldPosition);
        }
      }
      return result;
    }

    /**
     * Formats a double into a fractional-inch string with a variable denominator
     */
    private StringBuffer formatVariableDenominator(final double number,
                                                   final StringBuffer result,
                                                   final FieldPosition fieldPosition) {
      assert this.fractionDenominator != null && this.fractionDenominator > 1;
      final int feet;
      final int inches;
      final int fractionNumerator;
      final int fractionDenominator = (int)this.fractionDenominator;
      {
        final double decimalInches = (double)centimeterToInch(Math.abs((float)number));
        final long totalFractions = Math.round(decimalInches * (double)fractionDenominator);
        final long fractionPerInch = (long)fractionDenominator;
        final long fractionPerFoot = fractionPerInch * 12l;
        /* final int */ feet = this.footInch ? (int)(totalFractions / fractionPerFoot) : 0;
        final long feetFractions = fractionPerFoot * (long)feet;
        /* final int */ inches = (int)((totalFractions - feetFractions) / fractionPerInch);
        final long inchFractions = fractionPerInch * (long)inches;
        /* final int */ fractionNumerator = (int)(totalFractions - feetFractions - inchFractions);
      }

      fieldPosition.setEndIndex(fieldPosition.getEndIndex() + 1);
      if (fractionNumerator == 0) {
        if (this.footInch) {
          if (inches == 0) {
            (number >= 0 ? this.positiveFootFormat : this.negativeFootFormat).format(
                new Object [] {feet}, result, fieldPosition);
          } else {
            (number >= 0 ? this.positiveFootInchFormat : this.negativeFootInchFormat).format(
                new Object [] {feet, inches}, result, fieldPosition);
          }
        } else {
          (number >= 0 ? this.positiveInchFormat : this.negativeInchFormat).format(
              new Object [] {inches}, result, fieldPosition);
        }
      } else {
        final int reducedNumerator;
        final int reducedDenominator;
        {
          final int d = gcd(fractionNumerator, fractionDenominator);
          reducedNumerator = fractionNumerator / d;
          reducedDenominator = fractionDenominator / d;
          assert reducedDenominator > 1;
        }
        final String [][] vulgarFractions = getVulgarFractionStrings();
        final String fractionString;
        if (reducedDenominator <= 10
            && vulgarFractions [reducedDenominator] != null
            && vulgarFractions [reducedDenominator][reducedNumerator] != null) {
          fractionString = vulgarFractions [reducedDenominator][reducedNumerator];
        } else {
          final StringBuilder chars = new StringBuilder();
          // denominator
          for (int n = reducedDenominator, d = reducedDenominator % 10;
               n != 0;
               n = n / 10, d = n % 10) {
            chars.append(FRACTION_DENOMINATOR_CHARS [d]);
          }
          // numerator
          if (reducedNumerator == 1) {
            chars.append('\u215F'); // fraction numerator one
          } else {
            chars.append('\u2044'); // fraction slash
            for (int n = reducedNumerator, d = reducedNumerator % 10;
                 n != 0;
                 n = n / 10, d = n % 10) {
              chars.append(FRACTION_NUMERATOR_CHARS [d]);
            }
          }
          for (int i = this.fractionSeparator.length() - 1; i >= 0; i--) {
            chars.append(this.fractionSeparator.charAt(i));
          }
          fractionString = chars.reverse().toString();
        }

        if (this.footInch) {
          (number >= 0 ? this.positiveFootInchFractionFormat : this.negativeFootInchFractionFormat).format(
              new Object [] {feet, inches, fractionString}, result, fieldPosition);
        } else {
          (number >= 0 ? this.positiveInchFractionFormat : this.negativeInchFractionFormat).format(
              new Object [] {inches, fractionString}, result, fieldPosition);
        }
      }

      return result;
    }

    /**
     * Returns a two-dimensional array containing the pre-formatted strings for vulgar fractions,
     * indexed by denominator then by numerator.
     */
    private String [][] getVulgarFractionStrings() {
      if (this.vulgarFractionStrings == null) {
        this.vulgarFractionStrings = getVulgarFractionStringsWithSeparator(this.fractionSeparator);
      }
      return this.vulgarFractionStrings;
    }

    @Override
    public Number parse(String text, ParsePosition parsePosition) {
      if (this.fractionDenominator != null) {
        return parseVariableDenominator(text, parsePosition);
      }
      double value = 0;
      ParsePosition numberPosition = new ParsePosition(parsePosition.getIndex());
      skipWhiteSpaces(text, numberPosition);
      // Parse feet
      int quoteIndex = text.indexOf('\'', parsePosition.getIndex());
      boolean negative = numberPosition.getIndex() < text.length()
          && text.charAt(numberPosition.getIndex()) == this.getDecimalFormatSymbols().getMinusSign();
      boolean footValue = false;
      if (quoteIndex != -1) {
        Number feet = this.integerNumberFormat.parse(text, numberPosition);
        if (feet == null) {
          parsePosition.setErrorIndex(numberPosition.getErrorIndex());
          return null;
        }
        skipWhiteSpaces(text, numberPosition);
        if (numberPosition.getIndex() == quoteIndex) {
          value = footToCentimeter(feet.intValue());
          footValue = true;
          numberPosition = new ParsePosition(quoteIndex + 1);
          skipWhiteSpaces(text, numberPosition);
          // Test optional foot inch separator
          if (numberPosition.getIndex() < text.length()
              && this.footInchSeparator.indexOf(text.charAt(numberPosition.getIndex())) >= 0) {
            numberPosition.setIndex(numberPosition.getIndex() + 1);
            skipWhiteSpaces(text, numberPosition);
          }
          if (numberPosition.getIndex() == text.length()) {
            parsePosition.setIndex(text.length());
            return value;
          }
        } else {
          if (getDecimalFormatSymbols().getDecimalSeparator() == text.charAt(numberPosition.getIndex())) {
            ParsePosition decimalNumberPosition = new ParsePosition(parsePosition.getIndex());
            if (this.decimalNumberFormat.parse(text, decimalNumberPosition) != null
                && decimalNumberPosition.getIndex() == quoteIndex) {
              // Don't allow a decimal number in front of a quote
              parsePosition.setErrorIndex(numberPosition.getErrorIndex());
              return null;
            }
          }

          // Try to parse beginning as inches
          numberPosition.setIndex(parsePosition.getIndex());
        }
      }
      // Parse inches
      Number inches = this.decimalNumberFormat.parse(text, numberPosition);
      if (inches == null) {
        if (footValue) {
          parsePosition.setIndex(numberPosition.getIndex());
          return value;
        } else {
          parsePosition.setErrorIndex(numberPosition.getErrorIndex());
          return null;
        }
      }
      if (negative) {
        if (quoteIndex == -1) {
          value = inchToCentimeter(inches.floatValue());
        } else {
          value -= inchToCentimeter(inches.floatValue());
        }
      } else {
        value += inchToCentimeter(inches.floatValue());
      }
      // Parse fraction
      skipWhiteSpaces(text, numberPosition);
      if (numberPosition.getIndex() == text.length()) {
        parsePosition.setIndex(text.length());
        return value;
      }
      if (text.charAt(numberPosition.getIndex()) == '\"') {
        parsePosition.setIndex(numberPosition.getIndex() + 1);
        return value;
      }

      char fractionChar = text.charAt(numberPosition.getIndex());
      String fractionString = text.length() - numberPosition.getIndex() >= 3
          ? text.substring(numberPosition.getIndex(), numberPosition.getIndex() + 3)
          : null;
      for (int i = 0; i < EIGHTH_FRACTION_CHARACTERS.length; i++) {
        if (EIGHTH_FRACTION_CHARACTERS [i] == fractionChar
            || EIGHTH_FRACTION_STRINGS [i].equals(fractionString)) {
          // Check no decimal fraction was specified
          int lastDecimalSeparatorIndex = text.lastIndexOf(getDecimalFormatSymbols().getDecimalSeparator(),
              numberPosition.getIndex() - 1);
          if (lastDecimalSeparatorIndex > quoteIndex) {
            return null;
          } else {
            if (negative) {
              value -= inchToCentimeter((i + 1) / 8f);
            } else {
              value += inchToCentimeter((i + 1) / 8f);
            }
            parsePosition.setIndex(numberPosition.getIndex()
                + (EIGHTH_FRACTION_CHARACTERS [i] == fractionChar ? 1 : 3));
            skipWhiteSpaces(text, parsePosition);
            if (parsePosition.getIndex() < text.length()
                && text.charAt(parsePosition.getIndex()) == '\"') {
              parsePosition.setIndex(parsePosition.getIndex() + 1);
            }
            return value;
          }
        }
      }

      parsePosition.setIndex(numberPosition.getIndex());
      return value;
    }

    /**
     * Parses a double from a foot-inch string where the numeric portions
     * may have a fraction part with a variable denominator.
     */
    private Number parseVariableDenominator(final String text,
                                            final ParsePosition parsePosition) {
      boolean [] status = new boolean [STATUS_LENGTH];

      // first number (feet or inches)
      final Number firstNumber;
      final boolean isNegative;
      {
        firstNumber = subparseNumber(text, parsePosition, status);
        if (firstNumber == null) {
          return null;
        }
        if (status [STATUS_INCH] || !status [STATUS_FOOT]) {
          return (double)inchToCentimeter(firstNumber.floatValue());
        }
        isNegative = status [STATUS_NEGATIVE];
      }

      // second number (inches)
      final Number secondNumber;
      {
        final int savedIndex = parsePosition.getIndex();
        secondNumber = subparseNumber(text, parsePosition, status);
        if (secondNumber == null /* 1'abc */
            || status [STATUS_FOOT] /* 1'2' */
            || status [STATUS_NEGATIVE] /* 1'-2" */) {
          parsePosition.setIndex(savedIndex);
          if (parsePosition.getErrorIndex() > 0) {
            // clear error if secondNumber == null
            // because we got valid value from firstNumber
            parsePosition.setErrorIndex(-1);
          }
          return (double)footToCentimeter(firstNumber.floatValue());
        }
      }

      return (double)(isNegative
                      ? footToCentimeter(firstNumber.floatValue()) - inchToCentimeter(secondNumber.floatValue())
                      : footToCentimeter(firstNumber.floatValue()) + inchToCentimeter(secondNumber.floatValue()));
    }

    /**
     * Parses a double from a string, checking for a unit specifier afterwards.
     * The numeric portion may have a decimal part or a fraction part with a
     * variable denominator.
     */
    private Number subparseNumber(final String text,
                                  final ParsePosition parsePosition,
                                  final boolean [] status) {
      assert !isParseBigDecimal();
      final char zero = getDecimalFormatSymbols().getZeroDigit();

      // parse initial numeric portion
      final Number initialNumber;
      final boolean hasDecimal;
      final boolean skipFraction;
      final boolean isNegative;
      {
        skipWhiteSpaces(text, parsePosition);
        final int savedIndex = parsePosition.getIndex();
        final Number n = this.integerNumberFormat.parse(text, parsePosition);
        if (n == null) {
          Arrays.fill(status, false);
          return null;
        }
        final char c = text.length() > parsePosition.getIndex()
                       ? text.charAt(parsePosition.getIndex())
                       : '\u0000';
        if (linearSearch(FRACTION_SLASH_CHARS, c) > -1) {
          // if integer followed by fraction slash,
          // check to see if parseable as fraction
          final int nEndIndex = parsePosition.getIndex();
          isNegative = n instanceof Double
                       ? Double.doubleToLongBits((Double)n) < 0L
                       : ((Long)n) < 0L;
          // subparseFraction doesn't handle negative numbers,
          // so need to skip past negative prefix if present
          parsePosition.setIndex(savedIndex
                                 + (isNegative ? getNegativePrefix().length() : 0));
          final Double f = subparseFraction(text, parsePosition, zero);
          if (f == null) {
            // wasn't parseable as fraction.
            // set position to end index of parsed integer
            // and use original integer as numeric portion
            parsePosition.setIndex(nEndIndex);
            initialNumber = n;
          } else {
            // was parseable as fraction.
            // need to manually adjust sign because subparseFraction
            // doesn't handle negative numbers
            initialNumber = isNegative ? f : -f;
          }
          hasDecimal = false;
          skipFraction = true;
        } else {
          if (c == getDecimalFormatSymbols().getDecimalSeparator()) {
            // if integer followed by decimal separator,
            // parse as decimal
            parsePosition.setIndex(savedIndex);
            initialNumber = this.decimalNumberFormat.parse(text, parsePosition);
            hasDecimal = skipFraction = true;
            assert initialNumber != null;
          } else {
            // plain integer as numeric portion
            initialNumber = n;
            hasDecimal = skipFraction = false;
          }
          // need to know if negative to properly combine foot/inch/fraction.
          // DecimalFormat returns -0.0d if "-0", so this should work for all cases
          isNegative = initialNumber instanceof Double
                       ? Double.doubleToLongBits((Double)initialNumber) < 0L
                       : ((Long)initialNumber) < 0L;
        }
        skipWhiteSpaces(text, parsePosition);
      }

      // check for fraction part, and foot and inch unit markers
      final Double fractionPart;
      final boolean hasInch;
      final boolean hasFoot;
      textEnd: switch (0) {
        default:
          if (text.length() > parsePosition.getIndex()) {
            if (skipFraction) {
              fractionPart = null;
            } else {
              final int savedIndex = parsePosition.getIndex();
              final char c = text.charAt(parsePosition.getIndex());
              // check for optional fraction separator.
              // fraction separator may not be present in two cases:
              // 1) fraction separated only by whitespace (skipped after
              //    parsing numeric portion)
              // 2) fraction starts with superscript digit (not consumed
              //    by integerNumberFormat or decimalNumberFormat)
              if (linearSearch(FRACTION_SEPARATOR_CHARS, c) > -1) {
                parsePosition.setIndex(parsePosition.getIndex() + 1);
                skipWhiteSpaces(text, parsePosition);
                if (parsePosition.getIndex() >= text.length()) {
                  parsePosition.setIndex(savedIndex);
                  fractionPart = null;
                  hasInch = hasFoot = false;
                  break textEnd;
                }
              }
              fractionPart = subparseFraction(text, parsePosition, zero);
              if (fractionPart == null) {
                parsePosition.setIndex(savedIndex);
              } else if (parsePosition.getIndex() >= text.length()) {
                // do not reset parsePosition here.
                // successfully got a fraction, but then the string ended
                hasInch = hasFoot = false;
                break textEnd;
              }
            }

            if (tryConsumeInchUnitMarker(text, parsePosition)) {
              hasInch = true;
              hasFoot = false;
            } else if (tryConsumeFootUnitMarker(text, parsePosition)) {
              hasInch = false;
              hasFoot = true;
            } else {
              hasInch = false;
              hasFoot = false;
            }
          } else {
            fractionPart = null;
            hasInch = false;
            hasFoot = false;
          }
      } // :textEnd:

      // set status flags and return
      status [STATUS_NEGATIVE] = isNegative;
      status [STATUS_DECIMAL] = hasDecimal;
      status [STATUS_INCH] = hasInch;
      status [STATUS_FOOT] = hasFoot;
      if (fractionPart != null) {
        status [STATUS_FRACTION] = true;
        return isNegative ? initialNumber.doubleValue() - fractionPart
                          : initialNumber.doubleValue() + fractionPart;
      } else {
        status [STATUS_FRACTION] = false;
        return initialNumber;
      }
    }

    /**
     * Parses a variable-denominator fraction into a double.
     */
    private static Double subparseFraction(final String text,
                                           final ParsePosition parsePosition,
                                           final char zero) {
      // although this allows digits that are not superscript/subscript characters,
      // this is not meant to be a wholly generic integer fraction parser.
      // we explicitly do not handle negative numbers, digit grouping, decimals,
      // exponents, whitespaces, or anything other than [digits] '/' [digits]
      final byte [] digitBuf = new byte [MAX_SIGNIFICANT_DIGITS * 2];
      int textIndex = parsePosition.getIndex();
      char c = text.charAt(textIndex);

      // check for vulgar fraction values
      for (int i = linearSearch(VULGAR_FRACTION_CHARS, c); i > -1; ) {
        parsePosition.setIndex(textIndex + 1);
        return VULGAR_FRACTION_VALUES [i];
      }

      // parse numerator and fraction slash
      int numeratorDigits = 0;
      numerator: switch (c) {
        case '\u215F': // fraction numerator one
          digitBuf [0] = 1;
          numeratorDigits = 1;
          textIndex += 1;
          break;
        default:
          int d = digitValue(c, zero);
          if (d < 0) {
            return null;
          }
          digits: switch (d) {
            case 0: // skip leading zeroes
              zeroes: while (true) {
                if (++textIndex >= text.length()) {
                  return null;
                }
                c = text.charAt(textIndex);
                d = digitValue(c, zero);
                if (d < 0) {
                  break digits;
                }
                if (d > 0) {
                  break zeroes;
                }
              } // :zeroes:
              // intended fall thru
            default:
              significantDigits: while (true) {
                digitBuf [numeratorDigits++] = (byte)d;
                if (++textIndex >= text.length()) {
                  break digits;
                }
                c = text.charAt(textIndex);
                d = digitValue(c, zero);
                if (d < 0) {
                  break digits;
                }
                if (numeratorDigits >= MAX_SIGNIFICANT_DIGITS) {
                  break significantDigits;
                }
              } // :significantDigits:
              while (true) {
                numeratorDigits += 1;
                if (++textIndex >= text.length()) {
                  break digits;
                }
                c = text.charAt(textIndex);
                d = digitValue(c, zero);
                if (d < 0) {
                  break digits;
                }
              }
          } // :digits:

          // check for fraction slash
          if (linearSearch(FRACTION_SLASH_CHARS, c) < 0
              || ++textIndex >= text.length()) {
            return null;
          }
          c = text.charAt(textIndex);
      } // :numerator:

      // parse denominator
      int denominatorDigits = 0;
      denominator: switch (0) {
        default:
          int d = digitValue(c, zero);
          if (d < 0) {
            return null;
          }
          digits: switch (d) {
            case 0: // skip leading zeroes
              zeroes: while (true) {
                if (++textIndex >= text.length()) {
                  break digits;
                }
                c = text.charAt(textIndex);
                d = digitValue(c, zero);
                if (d < 0) {
                  break digits;
                }
                if (d > 0) {
                  break zeroes;
                }
              } // :zeroes:
              // intended fall thru
            default:
              significantDigits: while (true) {
                // offset denominator digits in digit buf
                digitBuf [MAX_SIGNIFICANT_DIGITS + denominatorDigits++] = (byte)d;
                if (++textIndex >= text.length()) {
                  break digits;
                }
                c = text.charAt(textIndex);
                d = digitValue(c, zero);
                if (d < 0) {
                  break digits;
                }
                if (denominatorDigits >= MAX_SIGNIFICANT_DIGITS) {
                  break significantDigits;
                }
              } // :significantDigits:
              while (true) {
                denominatorDigits += 1;
                if (++textIndex >= text.length()) {
                  break digits;
                }
                c = text.charAt(textIndex);
                d = digitValue(c, zero);
                if (d < 0) {
                  break digits;
                }
              }
          } // :digits:
      } // :denominator:

      // got a valid numerator and denominator.
      // update parse position
      parsePosition.setIndex(textIndex);

      // return zero for 0/X and X/0
      if (numeratorDigits == 0 || denominatorDigits == 0) {
        return Double.valueOf(0.0d);
      }

      // accumulate numerator
      long longNumerator = 0L;
      double doubleNumerator = 0.0d;
      boolean numeratorFitsIntoLong = true;
      {
        final int numeratorSignificantDigits = Math.min(numeratorDigits, MAX_SIGNIFICANT_DIGITS);
        int multiplierIndex = MAX_SIGNIFICANT_DIGITS - numeratorSignificantDigits;
        int digitIndex = 0;
        final int digitEndIndex = digitIndex + numeratorSignificantDigits;
        acc: switch (0) {
          default:
            // label can't be on loop because we need to skip over "break acc"
            // if loop doesn't completely successfully
            longAcc: switch (0) {
              default:
                for ( ; digitIndex < digitEndIndex; digitIndex++, multiplierIndex++) {
                  final long scaledDigit = digitBuf [digitIndex] * DIGIT_MULTIPLIERS [multiplierIndex];
                  final long r = longNumerator + scaledDigit;
                  if (r < longNumerator) {
                    // overflow
                    break longAcc;
                  }
                  longNumerator = r;
                }
                break acc;
            } // :longAcc:
            doubleNumerator = longNumerator;
            numeratorFitsIntoLong = false;
            for ( ; digitIndex < digitEndIndex; digitIndex++, multiplierIndex++) {
              final long scaledDigit = digitBuf [digitIndex] * DIGIT_MULTIPLIERS [multiplierIndex];
              doubleNumerator += scaledDigit;
            }
        } // :acc:
        if (numeratorDigits > MAX_SIGNIFICANT_DIGITS) {
          if (numeratorFitsIntoLong) {
            doubleNumerator = longNumerator;
            numeratorFitsIntoLong = false;
          }
          doubleNumerator *= Math.pow(10.0d, numeratorDigits - MAX_SIGNIFICANT_DIGITS);
          // return zero for inf/X
          if (Double.isInfinite(doubleNumerator)) {
            return Double.valueOf(0.0d);
          }
        }
      }

      // accumulate denominator
      long longDenominator = 0L;
      double doubleDenominator = 0.0d;
      boolean denominatorFitsIntoLong = true;
      {
        final int denominatorSignificantDigits = Math.min(denominatorDigits, MAX_SIGNIFICANT_DIGITS);
        int multiplierIndex = MAX_SIGNIFICANT_DIGITS - denominatorSignificantDigits;
        int digitIndex = MAX_SIGNIFICANT_DIGITS;
        final int digitEndIndex = digitIndex + denominatorSignificantDigits;
        acc: switch (0) {
          default:
            // label can't be on loop because we need to skip over "break acc"
            // if loop doesn't completely successfully
            longAcc: switch (0) {
              default:
                for ( ; digitIndex < digitEndIndex; digitIndex++, multiplierIndex++) {
                  final long scaledDigit = digitBuf [digitIndex] * DIGIT_MULTIPLIERS [multiplierIndex];
                  final long r = longDenominator + scaledDigit;
                  if (r < longDenominator) {
                    // overflow
                    break longAcc;
                  }
                  longDenominator = r;
                }
                break acc;
            } // :longAcc:
            doubleDenominator = longDenominator;
            denominatorFitsIntoLong = false;
            for ( ; digitIndex < digitEndIndex; digitIndex++, multiplierIndex++) {
              final long scaledDigit = digitBuf [digitIndex] * DIGIT_MULTIPLIERS [multiplierIndex];
              doubleDenominator += scaledDigit;
            }
        } // :acc:
        if (denominatorDigits > MAX_SIGNIFICANT_DIGITS) {
          if (denominatorFitsIntoLong) {
            doubleDenominator = longDenominator;
            denominatorFitsIntoLong = false;
          }
          doubleDenominator *= Math.pow(10.0d, denominatorDigits - MAX_SIGNIFICANT_DIGITS);
          // return zero for X/inf
          if (Double.isInfinite(doubleDenominator)) {
            return Double.valueOf(0.0d);
          }
        }
      }

      if (!numeratorFitsIntoLong) {
        if (!denominatorFitsIntoLong) {
           return doubleNumerator / doubleDenominator;
        }
        return doubleNumerator / longDenominator;
      } else if (!denominatorFitsIntoLong) {
        return longNumerator / doubleDenominator;
      }

      final long g = gcd(longNumerator, longDenominator);
      longNumerator /= g;
      longDenominator /= g;
      return ((double)longNumerator) / ((double)longDenominator);
    }

    /**
     * Returns the digit value of a character, or a negative value if out-of-range.
     */
    private static int digitValue(final char c, final char zero) {
      // check against localized zero digit
      int d = c - zero;
      if (0 <= d && d <= 9) {
        return d;
      }
      // check against subscript zero
      d = c - '\u2080';
      if (0 <= d && d <= 9) {
        return d;
      }
      // check against superscript zero
      d = c - '\u2070';
      if (d == 0 || 4 <= d && d <= 9) {
        return d;
      }
      // else
      switch (c) {
        case '\u00b9': // superscript 1
          return 1;
        case '\u00b2': // superscript 2
          return 2;
        case '\u00b3': // superscript 3
          return 3;
        default:
          return Character.digit(c, 10);
      }
    }

    /**
     * Checks for a foot unit marker, advancing the parse position if found.
     */
    private static boolean tryConsumeFootUnitMarker(final String text,
                                                    final ParsePosition parsePosition) {
      final int index = parsePosition.getIndex();
      return linearSearch(FOOT_MARKER_CHARS, text.charAt(index)) > -1
             && setIndex(parsePosition, index + 1);
    }

    /**
     * Checks for an inch unit marker, advancing the parse position if found.
     */
    private static boolean tryConsumeInchUnitMarker(final String text,
                                                    final ParsePosition parsePosition) {
      final int index = parsePosition.getIndex();
      final char c = text.charAt(index);
      if (linearSearch(FOOT_MARKER_CHARS, c) > -1) {
        // treat duplicated foot marker as inch marker
        // i.e. '' => "
        return text.length() - 1 > index
               && text.charAt(index + 1) == c
               && setIndex(parsePosition, index + 2);
      }
      return linearSearch(INCH_MARKER_CHARS, c) > -1
             && setIndex(parsePosition, index + 1);
    }

    /**
     * Sets the index of the parse position and returns {@code true}.
     */
    private static boolean setIndex(final ParsePosition parsePosition, final int index) {
      parsePosition.setIndex(index);
      return true;
    }

    /**
     * Returns a two-dimensional string array that contains each item in {@code VULGAR_FRACTION_STRINGS}
     * with the specified digit separator prepended to the item.
     */
    private static String [][] getVulgarFractionStringsWithSeparator(final String fractionSeparator) {
      final int separatorLength = fractionSeparator.length();
      final StringBuilder buf = new StringBuilder(separatorLength + 1);
      buf.append(fractionSeparator);
      assert buf.length() == separatorLength;
      final String [][] result = new String [VULGAR_FRACTION_STRINGS.length][];
      for (int d = 0; d < VULGAR_FRACTION_STRINGS.length; d++) {
        if (VULGAR_FRACTION_STRINGS [d] != null) {
          result [d] = new String [VULGAR_FRACTION_STRINGS [d].length];
          for (int n = 0; n < VULGAR_FRACTION_STRINGS[d].length; n++) {
            if (VULGAR_FRACTION_STRINGS [d][n] != null) {
              assert VULGAR_FRACTION_STRINGS [d][n].length() == 1;
              buf.append(VULGAR_FRACTION_STRINGS [d][n]);
              result [d][n] = buf.toString();
              buf.setLength(separatorLength);
            }
          }
        }
      }
      return result;
    }

    /**
     * Searches the {@code char} array for a specified value.
     */
    private static int linearSearch(final char [] a, final char c) {
      int result = -1;
      for (int i = 0; i < a.length; i++) {
        if (a [i] == c) {
          result = i;
          break;
        }
      }
      return result;
    }

    /**
     * Returns the greatest common denominator of two {@code int} values.
     */
    private static int gcd(final int a, final int b) {
      return a == 0 ? b : gcd(b % a, a);
    }

    /**
     * Returns the greatest common denominator of two {@code long} values.
     */
    private static long gcd(final long a, final long b) {
      return a == 0L ? b : gcd(b % a, a);
    }
  }

  /**
   * A decimal format for square foot.
   */
  private static class SquareFootAreaFormatWithUnit extends DecimalFormat {
    public SquareFootAreaFormatWithUnit(String pattern) {
      super(pattern);
    }

    @Override
    public StringBuffer format(double number, StringBuffer result,
                               FieldPosition fieldPosition) {
      // Convert square centimeter to square foot
      return super.format(number / 929.0304, result, fieldPosition);
    }
  }

  /**
   * Returns a localized name of this unit.
   */
  public abstract String getName();

  /**
   * Returns the value close to the given <code>length</code> in centimeter under magnetism.
   */
  public abstract float getMagnetizedLength(float length, float maxDelta);

  /**
   * Returns the value close to the given length under magnetism for meter units.
   */
  private static float getMagnetizedMeterLength(float length, float maxDelta) {
    // Use a maximum precision of 1 mm depending on maxDelta
    maxDelta *= 2;
    float precision = 1 / 10f;
    if (maxDelta > 100) {
      precision = 100;
    } else if (maxDelta > 10) {
      precision = 10;
    } else if (maxDelta > 5) {
      precision = 5;
    } else if (maxDelta > 1) {
      precision = 1;
    } else if  (maxDelta > 0.5f) {
      precision = 0.5f;
    }
    float magnetizedLength = Math.round(length / precision) * precision;
    if (magnetizedLength == 0 && length > 0) {
      return length;
    } else {
      return magnetizedLength;
    }
  }

  /**
   * Returns the value close to the given length under magnetism for inch units.
   */
  private static float getMagnetizedInchLength(float length, float maxDelta) {
    // Use a maximum precision of 1/8 inch depending on maxDelta
    maxDelta = centimeterToInch(maxDelta) * 2;
    float precision = 1 / 8f;
    if (maxDelta > 6) {
      precision = 6;
    } else if (maxDelta > 3) {
      precision = 3;
    } else if (maxDelta > 1) {
      precision = 1;
    } else if  (maxDelta > 0.5f) {
      precision = 0.5f;
    } else if  (maxDelta > 0.25f) {
      precision = 0.25f;
    }
    float magnetizedLength = inchToCentimeter(Math.round(centimeterToInch(length) / precision) * precision);
    if (magnetizedLength == 0 && length > 0) {
      return length;
    } else {
      return magnetizedLength;
    }
  }

  /**
   * Increases the index of <code>fieldPosition</code> to skip white spaces.
   */
  private static void skipWhiteSpaces(String text, ParsePosition fieldPosition) {
    while (fieldPosition.getIndex() < text.length()
        && Character.isWhitespace(text.charAt(fieldPosition.getIndex()))) {
      fieldPosition.setIndex(fieldPosition.getIndex() + 1);
    }
  }

  /**
   * Returns the minimum value for length in centimeter.
   */
  public abstract float getMinimumLength();

  /**
   * Returns the maximum value for length in centimeter.
   * @since 3.4
   */
  public abstract float getMaximumLength();

  /**
   * Returns the preferred step value for length in centimeter.
   * @since 7.0
   */
  public abstract float getStepSize();

  /**
   * Returns the maximum value for elevation in centimeter.
   * @since 3.4
   */
  public float getMaximumElevation() {
    return getMaximumLength() / 10;
  }

  /**
   * Returns the <code>length</code> given in centimeters converted
   * to a value expressed in this unit.
   * @since 2.0
   */
  public abstract float centimeterToUnit(float length);

  /**
   * Returns the <code>length</code> given in this unit converted
   * to a value expressed in centimeter.
   * @since 2.0
   */
  public abstract float unitToCentimeter(float length);

  /**
   * Returns <code>true</code> if this unit belongs to Metric system.
   * @since 7.0
   */
  public boolean isMetric() {
    return false;
  }
}