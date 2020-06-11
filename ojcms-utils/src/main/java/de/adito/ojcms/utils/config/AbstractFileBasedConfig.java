package de.adito.ojcms.utils.config;

import de.adito.ojcms.utils.StringUtility;

import java.io.*;
import java.util.*;

/**
 * Base class for file based config classes.
 *
 * @author Simon Danner, 21.04.2020
 */
public abstract class AbstractFileBasedConfig
{
  protected final Properties properties;

  /**
   * Initializes the config by loading the properties from the provided path.
   */
  protected AbstractFileBasedConfig(String pConfigPath)
  {
    try
    {
      StringUtility.requireNotEmpty(pConfigPath, "config path");
      properties = new Properties();
      properties.load(new FileInputStream(pConfigPath));
    }
    catch (IOException pE)
    {
      throw new RuntimeException("Unable to load properties! Provide config file: " + pConfigPath + "!", pE);
    }
  }

  /**
   * Reads a string based property value. Throws an {@link IllegalArgumentException} if the property is not set.
   *
   * @param pPropertyName the name of the property to read
   * @return the value of the property
   */
  protected String readMandatoryProperty(String pPropertyName)
  {
    return readOptionalProperty(pPropertyName) //
        .orElseThrow(() -> new IllegalArgumentException("Property " + pPropertyName + " not set!"));
  }

  /**
   * Tries to read a string based property value.
   *
   * @param pPropertyName the name of the property to read
   * @return the value of the property or empty if not set
   */
  protected Optional<String> readOptionalProperty(String pPropertyName)
  {
    return Optional.ofNullable(properties.getProperty(pPropertyName));
  }

  /**
   * Reads an int property value. Throws an {@link IllegalArgumentException} if the property is not set.
   *
   * @param pPropertyName the name of the property to read
   * @return the value of the property
   */
  protected int readMandatoryIntProperty(String pPropertyName)
  {
    return readOptionalIntProperty(pPropertyName) //
        .orElseThrow(() -> new IllegalArgumentException("Property " + pPropertyName + " not set!"));
  }

  /**
   * Tries to read an int property value.
   *
   * @param pPropertyName the name of the property to read
   * @return the value of the property or empty if not set
   */
  protected OptionalInt readOptionalIntProperty(String pPropertyName)
  {
    final String value = properties.getProperty(pPropertyName);
    if (value == null)
      return OptionalInt.empty();

    return OptionalInt.of(Integer.parseInt(value));
  }
}
