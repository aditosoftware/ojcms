package de.adito.beans.core.util.beancopy;

import de.adito.beans.core.*;
import de.adito.beans.core.fields.FieldTuple;
import de.adito.beans.core.mappers.IBeanFlatDataMapper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Utility class to create a flat copy of a bean.
 * A flat bean has no bean or bean container field, it does not contain any reference field.
 *
 * @author Simon Danner, 02.02.2017
 */
public final class BeanFlattenUtil
{
  private static final _FlatMapper NON_DEEP = new _FlatMapper(false);
  private static final _FlatMapper DEEP = new _FlatMapper(true);

  private BeanFlattenUtil()
  {
  }

  /**
   * Makes an existing bean flat.
   * A flat bean has no bean reference field.
   * It's not possible to flat container fields!
   * An internal data mapper will be added for this purpose.
   *
   * @param pBean  the bean to flat
   * @param pDeep  <tt>true</tt> if fields should flattened deeply (includes deep fields iteratively)
   * @param pCopy  <tt>true</tt> if a copy of the bean should be created before flattening
   * @param <BEAN> the type of the bean to flat
   * @return a flat bean with no reference fields
   */
  public static <BEAN extends IBean<BEAN>> BEAN makeFlat(BEAN pBean, boolean pDeep, boolean pCopy, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    if (pCopy)
      pBean = pBean.createCopy(pDeep, pCustomFieldCopies);
    return _addFlatMapper(pBean, pDeep);
  }

  /**
   * Makes a copy of an original bean flat.
   * A flat bean has no bean reference field.
   * It's not possible to flat container fields!
   * An internal data mapper will be added for this purpose.
   *
   * @param pBean        the bean to flat
   * @param pDeep        <tt>true</tt> if fields should flattened deeply (includes deep fields iteratively)
   * @param pCopyCreator a function to create a copy of an original bean (custom constructor call)
   * @param <BEAN>       the type of the bean to flat
   * @return a flat bean with no reference fields
   */
  public static <BEAN extends IBean<BEAN>> BEAN makeFlat(BEAN pBean, boolean pDeep, @NotNull Function<BEAN, BEAN> pCopyCreator,
                                                         CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return _addFlatMapper(pBean.createCopy(pDeep, pCopyCreator, pCustomFieldCopies), pDeep);
  }

  /**
   * Makes a bean non-flat/normal again.
   * The internal data mapper will be removed.
   *
   * @param pBean  the bean to normalize
   * @param <BEAN> the type of the bean
   * @return the normalized bean instance
   */
  public static <BEAN extends IBean<BEAN>> BEAN normalize(BEAN pBean)
  {
    if (!pBean.removeDataMapper(DEEP))
      pBean.removeDataMapper(NON_DEEP);
    return pBean;
  }

  /**
   * Adds the internal flat data mapper to a bean instance.
   *
   * @param pInstance the bean instance
   * @param pDeep     <tt>true</tt> if fields should flattened deeply (includes deep fields iteratively)
   * @param <BEAN>    the type of the bean
   * @return the bean instance
   */
  private static <BEAN extends IBean<BEAN>> BEAN _addFlatMapper(BEAN pInstance, boolean pDeep)
  {
    pInstance.addDataMapper(pDeep ? DEEP : NON_DEEP);
    return pInstance;
  }

  /**
   * Data mapper implementation to create flat bean tuples.
   */
  private static class _FlatMapper implements IBeanFlatDataMapper
  {
    private final boolean deep;

    private _FlatMapper(boolean pDeep)
    {
      deep = pDeep;
    }

    @Override
    public Stream<FieldTuple<?>> flatMapTuple(IField<?> pField, Object pValue)
    {
      assert pValue instanceof IBean;
      //noinspection unchecked
      return ((IBean) pValue).stream();
    }

    @Override
    public boolean isCompleted(int pLastIterationChangesCount)
    {
      return !deep || pLastIterationChangesCount == 0;
    }

    @Override
    public boolean affectsTuple(IField<?> pField, Object pValue)
    {
      return pValue instanceof IBean; //Flat bean values only!
    }
  }
}
