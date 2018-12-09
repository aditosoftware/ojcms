package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.internal.EncapsulatedData;
import de.adito.ojcms.beans.datasource.IDataSource;
import org.jetbrains.annotations.NotNull;

import java.util.stream.*;

/**
 * An encapsulated data core that is based on a {@link IDataSource}.
 * Defines the private scope of objects on a Java level.
 * Important: This interface must be package protected to enable data encapsulation.
 *
 * @param <ELEMENT>    the type of the elements in the data core
 * @param <DATASOURCE> the type of the data source
 * @author Simon Danner, 25.01.2017
 */
@EncapsulatedData
interface IEncapsulatedData<ELEMENT, DATASOURCE extends IDataSource> extends Iterable<ELEMENT>, IReferable, IEventReceiver, IEventPublisher
{
  /**
   * Sets a new data source for the encapsulated data core.
   *
   * @param pDataSource the new data source
   */
  void setDataSource(@NotNull DATASOURCE pDataSource);

  /**
   * A stream of all elements of this data core.
   *
   * @return a stream of the core elements
   */
  default Stream<ELEMENT> stream()
  {
    return StreamSupport.stream(spliterator(), false);
  }

  /**
   * A parallel stream of all elements of this data core.
   *
   * @return a parallel stream of the core elements
   */
  default Stream<ELEMENT> parallelStream()
  {
    return StreamSupport.stream(spliterator(), true);
  }
}
