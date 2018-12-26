package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.internal.RequiresEncapsulatedAccess;
import de.adito.ojcms.beans.datasource.IDataSource;

import static de.adito.ojcms.beans.BeanInternalEvents.requestEncapsulatedData;

/**
 * An extension for a type that holds {@link IEncapsulatedData}.
 * The data source of an encapsulated data core might be exchanged via this interface as well.
 *
 * @param <ENCAPSULATED> the encapsulated data core's runtime type
 * @param <DATASOURCE>   the type of the data source of the data core
 * @author Simon Danner, 20.01.2017
 * @see IEncapsulatedData
 */
@RequiresEncapsulatedAccess
interface IEncapsulatedDataHolder<ELEMENT, DATASOURCE extends IDataSource, ENCAPSULATED extends IEncapsulatedData<ELEMENT, DATASOURCE>>
{
  /**
   * The encapsulated data core of this instance.
   * This method may be used as 'virtual' field. (only method to implement in an interface with default methods only)
   *
   * @return the encapsulated data core of this instance
   */
  ENCAPSULATED getEncapsulatedData();

  /**
   * Exchanges the encapsulated data source of this instance.
   * May be used to couple a bean to a database source for example.
   *
   * @param pDataSource the new data source
   */
  default void setEncapsulatedDataSource(DATASOURCE pDataSource)
  {
    requestEncapsulatedData(this).setDataSource(pDataSource);
  }
}
