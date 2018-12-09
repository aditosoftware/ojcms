package de.adito.ojcms.beans.datasource;

import de.adito.ojcms.beans.fields.IField;

/**
 * Data source for a bean.
 * Used to create encapsulated data cores.
 *
 * @author Simon Danner, 08.12.2018
 */
public interface IBeanDataSource extends IDataSource
{
  /**
   * The value for a bean field.
   *
   * @param pField  the bean field
   * @param <VALUE> the data type of the field
   * @return the value for the field
   */
  <VALUE> VALUE getValue(IField<VALUE> pField);

  /**
   * Sets a value for a bean field.
   *
   * @param pField         the bean field
   * @param pValue         the new value
   * @param pAllowNewField <tt>true</tt>, if a new field should be created, if it isn't existing
   * @param <VALUE>        the data type of the field
   */
  <VALUE> void setValue(IField<VALUE> pField, VALUE pValue, boolean pAllowNewField);

  /**
   * Removes a bean field.
   *
   * @param pField  the bean field to remove
   * @param <VALUE> the data type of the field
   */
  <VALUE> void removeField(IField<VALUE> pField);
}
