package de.adito.beans.core;

import java.util.Map;

/**
 * Eine kopierte Bean.
 * Die Daten stammen von einer bereits existenten Bean, welche z.B. weniger Felder enthält.
 * Auch der Active-Supplier für Felder muss hier bereitgestellt werden, da der Typ der Bean nun verändert wurde.
 *
 * @author s.danner, 18.08.2017
 */
public class BeanCopy implements IBean
{
  private final IBeanEncapsulated<?> encapsulated;
  private final IBeanFieldActivePredicate originalActiveSupplier;

  /**
   * Erzeugt die Kopie einer Bean.
   *
   * @param pData                   die Daten, welche die Bean enthalten soll (evtl. reduziert im Vergleich zum Original)
   * @param pOriginalActiveSupplier der Active-Supplier für Felder der Original-Bean
   */
  public BeanCopy(Map<IField<?>, Object> pData, IBeanFieldActivePredicate pOriginalActiveSupplier)
  {
    encapsulated = new BeanMapEncapsulated<>(getClass(), pData);
    originalActiveSupplier = pOriginalActiveSupplier;
  }

  @Override
  public IEncapsulated getEncapsulated()
  {
    return encapsulated;
  }

  @Override
  public IBeanFieldActivePredicate getFieldActiveSupplier()
  {
    return originalActiveSupplier;
  }
}
