package de.adito.ojcms.beans.literals.fields.types;

import de.adito.ojcms.beans.annotations.internal.TypeDefaultField;
import de.adito.ojcms.beans.literals.fields.serialization.IAutoSerializableField;
import de.adito.ojcms.beans.literals.fields.util.CustomFieldCopy;
import de.adito.ojcms.beans.util.*;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A bean field that holds a text/string.
 *
 * @author Simon Danner, 23.08.2016
 */
@TypeDefaultField(types = String.class)
public class TextField extends AbstractField<String> implements IAutoSerializableField<String>
{
  protected TextField(@NotNull String pName, Collection<Annotation> pAnnotations, boolean pIsOptional, boolean pIsPrivate)
  {
    super(String.class, pName, pAnnotations, pIsOptional, pIsPrivate);
  }

  @Override
  public String display(String pValue, IClientInfo pClientInfo)
  {
    return pValue != null ? pValue : "";
  }

  @Override
  public String copyValue(String pText, ECopyMode pMode, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return pText;
  }
}
