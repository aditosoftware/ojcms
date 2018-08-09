package de.adito.beans.core.util.beancopy;

/**
 * Enumerates all copy modes for the beans.
 *
 * @author Simon Danner, 09.08.2018
 */
public enum ECopyMode
{
  /**
   * Shallow copy, only bean field values will be transferred.
   */
  SHALLOW_ONLY_BEAN_FIELDS,

  /**
   * Shallow copy, all field values will be transferred.
   */
  SHALLOW_ALL_FIELDS,

  /**
   * Deep copy, only bean field values will be transferred.
   */
  DEEP_ONLY_BEAN_FIELDS,

  /**
   * Deep copy, all field values will be transferred.
   * Caution: Only bean values will be copied deeply. Normal fields will be copied by referenced still.
   */
  DEEP_ALL_FIELDS;

  /**
   * Determines, if the copy should include deep values.
   *
   * @param pMode the copy mode to check
   * @return <tt>true</tt>, if the copy should include deep values
   */
  public static boolean isDeep(ECopyMode pMode)
  {
    return pMode == DEEP_ONLY_BEAN_FIELDS || pMode == DEEP_ALL_FIELDS;
  }

  /**
   * Determines, if the copy should include all field values (bean fields and normal fields).
   *
   * @param pMode the copy mode to check
   * @return <tt>true</tt>, if all fields should be included
   */
  public static boolean allFields(ECopyMode pMode)
  {
    return pMode == SHALLOW_ALL_FIELDS || pMode == DEEP_ALL_FIELDS;
  }
}
