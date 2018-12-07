package de.adito.beans.core.fields;

import de.adito.beans.core.annotations.TypeDefaultField;
import de.adito.beans.core.fields.serialization.ISerializableFieldToString;
import de.adito.beans.core.util.IClientInfo;
import de.adito.beans.core.util.beancopy.*;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A bean field that holds a text/string.
 *
 * @author Simon Danner, 23.08.2016
 */
@TypeDefaultField(types = String.class)
public class TextField extends AbstractField<String> implements ISerializableFieldToString<String>
{
  public TextField(@NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(String.class, pName, pAnnotations);
  }

  @Override
  public String display(String pValue, IClientInfo pClientInfo)
  {
    return pValue != null ? pValue : "";
  }

  @Override
  public String copyValue(String pValue, ECopyMode pMode, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    //noinspection RedundantStringConstructorCall
    return pValue == null ? null : new String(pValue);
  }

  @Override
  public String fromPersistent(String pSerialString)
  {
    return pSerialString;
  }
}
