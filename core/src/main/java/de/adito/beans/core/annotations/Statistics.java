package de.adito.beans.core.annotations;

import java.lang.annotation.*;

/**
 * Marks a bean or a bean field as statistics provider.
 * If the annotation is used for a bean type, the statistics are collected for the element-amount in a bean container.
 * If it is used for a field, they are collected for the value of the field for each timestamp.
 *
 * @author Simon Danner, 14.02.2017
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Statistics
{
  /**
   * The maximum number of entries of the statistic data.
   * -1 could be used for no capacity.
   * But be careful: This could lead to a non stop collecting of data, which could reach memory limits.
   *
   * @return the maximum number of entries in the statistic data (-1 for non)
   */
  int capacity();
}
