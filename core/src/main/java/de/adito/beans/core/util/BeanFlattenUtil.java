package de.adito.beans.core.util;

import de.adito.beans.core.*;
import de.adito.beans.core.util.exceptions.BeanFlattenException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class to create a flat copy of a bean.
 * A flat bean has no bean or bean container field, it does not contain any reference field.
 *
 * @author Simon Danner, 02.02.2017
 */
public final class BeanFlattenUtil
{
  private BeanFlattenUtil()
  {
  }

  /**
   * Creates a flat copy of a bean as a map of fields and it's associated values.
   * A flat bean has no bean or bean container field, it only consists of no reference fields.
   *
   * @param pBean the bean to flatten
   * @param pDeep <tt>true</tt> if this should be a deep copy (includes deep fields iteratively)
   * @return a flat copy of the bean as a map of fields and it's associated values
   */
  public static Map<IField<?>, Object> createFlatCopy(IBean<?> pBean, boolean pDeep)
  {
    _Shifter shifter = new _Shifter(pBean);
    do
    {
      shifter.shift();
    }
    while (pDeep && !shifter.complete());

    return shifter.flatMap;
  }

  /**
   * A shifter to flat the fields of a bean stepwise.
   * One level of fields will be shifted per step.
   * At the end of the process will be a map as data core for the flat bean copy.
   */
  private static class _Shifter
  {
    private final Map<IField<?>, Object> flatMap = new LinkedHashMap<>();
    private List<IBean<?>> toShift;

    public _Shifter(IBean<?> pBean)
    {
      toShift = Collections.singletonList(pBean);
      shift(); //Do one step to really shift some fields with the first method call
    }

    /**
     * Shifts all non-reference {@link de.adito.beans.core.fields.BeanField} fields into the new flat bean.
     * The remaining bean fields will be used in the next step.
     */
    public void shift()
    {
      toShift = toShift.stream()
          .flatMap(IBean::stream)
          .filter(pEntry ->
                  {
                    Object value = pEntry.getValue();
                    if (value instanceof IBeanContainer)
                      throw new BeanFlattenException();

                    boolean isBean = value instanceof IBean;
                    if (!isBean)
                      flatMap.put(pEntry.getKey(), value);
                    return isBean;
                  })
          .map(pEntry -> (IBean<?>) pEntry.getValue())
          .collect(Collectors.toList());
    }

    /**
     * Determines if the shifting process is complete.
     */
    public boolean complete()
    {
      return toShift.isEmpty();
    }
  }
}
