package de.adito.ojcms.beans.annotations.internal;

import de.adito.ojcms.beans.EReferableResolver;

import java.lang.annotation.*;

/**
 * Annotates bean field types which values can hold references created by the annotated field instance.
 *
 * @author Simon Danner, 24.11.2018
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReferenceField
{
  /**
   * A resolver type to get referable elements from a field's value to which the annotated field instance refers to.
   * It will be differentiated between single bean or multi bean referables (single bean or container)
   *
   * @return a resolver type to obtain the referables to which the annotated field instance refers to
   */
  EReferableResolver resolverType();
}