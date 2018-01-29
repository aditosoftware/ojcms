package de.adito.beans.core.annotations;

import java.lang.annotation.*;

/**
 * Marks a bean or a bean field as statistics provider.
 * If the annotation is used for a bean type, the statistics are collected for the element-amount in a bean container.
 * If it is used for a field, they are collected for the value of the field for each timestamp.
 * This annotation provides the intervall, which determines when a statistic entry is collected, and the maximum capacity.
 *
 * @author Simon Danner, 14.02.2017
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Statistics
{
  /**
   * The intervall in seconds for the statistic entries.
   */
  int intervall();

  /**
   * The maximum capacity of this statistics in seconds. (max-entries = capacity/intervall)
   */
  int capacity();
}
