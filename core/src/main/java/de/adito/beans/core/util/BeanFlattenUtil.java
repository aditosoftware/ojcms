package de.adito.beans.core.util;

import de.adito.beans.core.IBean;
import de.adito.beans.core.IBeanContainer;
import de.adito.beans.core.IField;
import de.adito.beans.core.util.exceptions.BeanFlattenException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hilfs-Klasse um eine flache Kopie einer Bean zu erzeugen.
 * Dabei werden jegliche Bean-Felder der Bean solange 'geflattet' bis nur noch normale Felder übrig sind.
 *
 * @author s.danner, 02.02.2017
 */
public final class BeanFlattenUtil
{
  private BeanFlattenUtil()
  {
  }

  /**
   * Erzeugt eine flache Kopie einer Bean.
   * Dabei werden jegliche Bean-Felder der Bean, solange 'geflattet' bis nur noch normale Felder übrig sind.
   *
   * @param pBean die Bean, welche 'geflattet' werden soll
   * @param pDeep <tt>true</tt> wenn iterativ bis in die Tiefe 'geflattet' werden soll
   * @return die flache Kopie der Bean
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
   * Shifter, welcher pro Schritt die Felder einer Menge von Beans flach in eine Map schiebt.
   * Pro Schritt werden die neuen Elemente zum 'Flatten' bestimmt.
   */
  private static class _Shifter
  {
    private final Map<IField<?>, Object> flatMap = new LinkedHashMap<>();
    private List<IBean<?>> toShift;

    public _Shifter(IBean<?> pBean)
    {
      toShift = Collections.singletonList(pBean);
      shift(); //Ein Shift muss erfolgen, um beim ersten Shift bereits wirklich etwas zu verschieben
    }

    /**
     * Überträgt alle Felder der momentan enthaltenen Beans in die flache Map, wenn es sich um kein Bean- bzw. BeanContainer-Feld handelt.
     * Die übrigen Bean-Felder werden als neue Menge zum shiften bestimmt.
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
     * Liefert <tt>true</tt>, wenn der Shifter fertig ist.
     */
    public boolean complete()
    {
      return toShift.isEmpty();
    }
  }
}
