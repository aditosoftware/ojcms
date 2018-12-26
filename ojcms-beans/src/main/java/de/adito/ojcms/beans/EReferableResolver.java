package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.internal.RequiresEncapsulatedAccess;
import de.adito.ojcms.beans.exceptions.OJInternalException;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Variants to resolve {@link IReferable} instances from a bean value.
 *
 * @author Simon Danner, 24.11.2018
 */
@RequiresEncapsulatedAccess
public enum EReferableResolver
{
  /**
   * Resolver for single referable values. (example: single beans of a bean field)
   */
  SINGLE(EReferableResolver::_single),

  /**
   * Resolver for multi referable values. (example: bean container)
   */
  MULTI(EReferableResolver::_withMulti);

  private final Function<Object, Stream<IReferable>> resolver;

  /**
   * A resolver type to get referables from a field's value.
   *
   * @param pResolver the resolver function
   */
  EReferableResolver(Function<Object, Stream<IReferable>> pResolver)
  {
    resolver = pResolver;
  }

  /**
   * The resolver function to transform a field's value into a stream of referables.
   *
   * @return the resolver function
   */
  Function<Object, Stream<IReferable>> getResolver()
  {
    return resolver;
  }

  /**
   * An either one element or empty {@link IReferable} stream for a single bean value.
   *
   * @param pValue the value of the field to resolve referables from
   * @return an either one element or empty stream of referables
   */
  private static Stream<IReferable> _single(Object pValue)
  {
    return _tryGetEncapsulated(pValue)
        .map(pEncapsulated -> Stream.of((IReferable) pEncapsulated))
        .orElse(Stream.empty());
  }

  /**
   * Takes the encapsulated data core from a bean field's value as {@link IReferable}
   * and tries to add more referables to a stream from the data core's elements, if they contain encapsulated data cores as well.
   *
   * @param pValue the value of the field to resolve referables from
   * @return a stream of referables retrieved from the field's value
   */
  private static Stream<IReferable> _withMulti(Object pValue)
  {
    return _tryGetEncapsulated(pValue)
        .map(pEncapsulated -> Stream.concat(pEncapsulated.stream()
                                                .filter(pElement -> pElement instanceof IEncapsulatedDataHolder)
                                                .map(EReferableResolver::_toEncapsulated)
                                                .map(pInnerEncapsulated -> (IReferable) pInnerEncapsulated),
                                            Stream.of(pEncapsulated)))
        .orElse(Stream.empty());
  }

  /**
   * Retrieves an encapsulated data core from a bean field's value that is an {@link IEncapsulatedDataHolder}.
   *
   * @param pValue the value of the field
   * @return an optional retrieved encapsulated data core (empty if the value is null or wrong type)
   * @throws OJInternalException if the value is no {@link IEncapsulatedDataHolder}
   */
  private static Optional<IEncapsulatedData<?, ?>> _tryGetEncapsulated(Object pValue)
  {
    return Optional.ofNullable(pValue)
        .map(pHolder -> _toEncapsulated(pValue));
  }

  /**
   * Retrieves an encapsulated data core from a bean value that is expected to be a {@link IEncapsulatedDataHolder}.
   *
   * @param pValue the value to retrieve the data core from
   * @return the encapsulated data core
   */
  private static IEncapsulatedData<?, ?> _toEncapsulated(Object pValue)
  {
    try
    {
      return ((IEncapsulatedDataHolder<?, ?, ?>) pValue).getEncapsulatedData();
    }
    catch (ClassCastException pE)
    {
      throw new OJInternalException("The field's value should hold an encapsulated data core! type " + pValue.getClass().getName());
    }
  }
}
