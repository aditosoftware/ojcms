package de.adito.ojcms.persistence.datasource;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Qualifier to identify persistent containers (includes single bean 'containers').
 *
 * @author Simon Danner, 31.12.2019
 */
@Documented
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ContainerQualifier
{
  /**
   * The id of the container to qualify.
   *
   * @return the id the persistent data
   */
  String containerId() default "";

  /**
   * A literal for this qualifier annotation.
   */
  class Literal implements ContainerQualifier
  {
    private final String containerId;

    /**
     * Creates a new Literal for a given container id.
     *
     * @param pContainerId the container id to qualify the persistent data
     * @return the created literal
     */
    public static Literal forContainerId(String pContainerId)
    {
      return new Literal(pContainerId);
    }

    private Literal(String pContainerId)
    {
      containerId = pContainerId;
    }

    @Override
    public String containerId()
    {
      return containerId;
    }

    @Override
    public Class<? extends Annotation> annotationType()
    {
      return ContainerQualifier.class;
    }
  }
}
