package de.adito.beans.core.util;

import de.adito.beans.core.*;
import de.adito.beans.core.annotations.Statistics;
import org.jetbrains.annotations.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

/**
 * A utility class for reflection of beans.
 *
 * @author Simon Danner, 23.08.2016
 */
public final class BeanReflector
{
  private BeanReflector()
  {
  }

  /**
   * Reflects the fields of a bean type.
   *
   * @param pBeanType the type of the bean, which must be an extension of {@link Bean} to own specific fields
   * @return a collection of bean fields
   */
  public static List<IField<?>> getBeanMetadata(Class<? extends IBean> pBeanType)
  {
    assert Bean.class.isAssignableFrom(pBeanType); //To make sure it isn't a transformed type.
    List<Field> declaredFields = new ArrayList<>();
    //Collect all fields, also from the superclasses
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
   * Reflects the statistic annotation of a bean type. (null if not existing)
   *
   * @param pBeanType the bean's type
   * @return the statistic annotation
   */
  @Nullable
  public static Statistics getContainerStatisticAnnotation(Class<? extends IBean> pBeanType)
  {
    return pBeanType.getAnnotation(Statistics.class);
  }

  /**
   * Reflects all field statistic annotations of a bean type.
   *
   * @param pBeanType the bean's type
   * @return a map with the field names as keys and the annotations as values
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
