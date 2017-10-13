package de.adito.beans.core.util;

import de.adito.beans.core.Bean;
import de.adito.beans.core.IBean;
import de.adito.beans.core.IField;
import de.adito.beans.core.annotations.Statistics;
import org.jetbrains.annotations.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

/**
 * Hilfs-Klasse für Bean-Reflection.
 *
 * @author s.danner, 23.08.2016
 */
public final class BeanReflector
{
  private BeanReflector()
  {
  }

  /**
   * Liefert die Felder einen Bean-Typen.
   *
   * @param pBeanType der Typ des Beans (Muss hier auf der Klasse Bean basieren, da es sich um ein 'echtes' Modell handeln muss)
   * @return eine Menge von Bean-Feldern
   */
  public static List<IField<?>> getBeanMetadata(Class<? extends IBean> pBeanType)
  {
    assert Bean.class.isAssignableFrom(pBeanType); //Nicht transformiert!
    List<Field> declaredFields = new ArrayList<>();
    //Alle Felder (auch der Super-Klassen) ermitteln
    Class current = pBeanType;
    do
    {
      declaredFields.addAll(Arrays.asList(current.getDeclaredFields()));
    }
    while ((current = current.getSuperclass()) != null && !current.equals(Bean.class));

    List<IField<?>> metadata = new ArrayList<>();
    for (Field field : declaredFields)
      if (Modifier.isStatic(field.getModifiers()) && IField.class.isAssignableFrom(field.getType()))
        try
        {
          metadata.add((IField) field.get(null));
        }
        catch (IllegalAccessException pE)
        {
          throw new RuntimeException(pE);
        }

    return metadata;
  }

  /**
   * Liefert die Statistik-Annotation eines Bean-Typen.
   *
   * @param pBeanType der Typ des Beans
   * @return die Statistik-Annotation
   */
  @Nullable
  public static Statistics getContainerStatisticAnnotation(Class<? extends IBean> pBeanType)
  {
    return pBeanType.getAnnotation(Statistics.class);
  }

  /**
   * Liefert alle Feld-Statistik-Annotations eines Bean-Typen als Map.
   *
   * @param pBeanType der Typ der Bean
   * @return eine Map mit dem Feld-Namen als Key und der Annotation als Value
   */
  @NotNull
  @SuppressWarnings("unchecked")
  public static Map<String, Statistics> getBeanStatisticAnnotations(Class<? extends IBean> pBeanType)
  {
    return Stream.of(pBeanType.getDeclaredFields())
        .filter(pField -> pField.getAnnotation(Statistics.class) != null)
        .collect(Collectors.toMap(Field::getName, pField -> pField.getAnnotation(Statistics.class)));
  }
}
