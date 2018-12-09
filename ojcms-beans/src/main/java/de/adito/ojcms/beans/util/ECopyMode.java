package de.adito.ojcms.beans.util;

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
  SHALLOW_ONLY_BEAN_FIELDS(false, false),

  /**
   * Shallow copy, all field values will be transferred.
   */
  SHALLOW_ALL_FIELDS(false, true),

  /**
   * Deep copy, only bean field values will be transferred.
   */
  DEEP_ONLY_BEAN_FIELDS(true, false),

  /**
   * Deep copy, all field values will be transferred.
   * Caution: Only bean values will be copied deeply. Normal fields will be copied by referenced still.
   */
  DEEP_ALL_FIELDS(true, true);

  private final boolean shouldCopyDeep;
  private final boolean shouldCopyAllFields;

  /**
   * Creates a copy mode.
   *
   * @param pShouldCopyDeep      <tt>true</tt>, if the copy should include deep values
   * @param pShouldCopyAllFields <tt>true</tt>, if the copy should include bean and normal fields
   */
  ECopyMode(boolean pShouldCopyDeep, boolean pShouldCopyAllFields)
  {
    shouldCopyDeep = pShouldCopyDeep;
    shouldCopyAllFields = pShouldCopyAllFields;
  }

  /**
   * Determines, if the copy should include deep values.
   *
   * @return <tt>true</tt>, if the copy should include deep values
   */
  public boolean shouldCopyDeep()
  {
    return shouldCopyDeep;
  }

  /**
   * Determines, if the copy should include all field values (bean fields and normal fields).
   *
   * @return <tt>true</tt>, if all fields should be included
   */
  public boolean shouldCopyAllFields()
  {
    return shouldCopyAllFields;
  }
}
