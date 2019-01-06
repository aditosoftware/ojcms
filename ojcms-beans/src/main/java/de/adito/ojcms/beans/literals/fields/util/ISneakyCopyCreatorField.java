package de.adito.ojcms.beans.literals.fields.util;

import de.adito.ojcms.beans.exceptions.copy.BeanCopyNotSupportedException;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.util.ECopyMode;
import de.adito.ojcms.utils.copy.SneakyCopyUtils;
import de.adito.ojcms.utils.copy.exceptions.CopyUnsupportedException;

/**
 * A bean field creating its value copies with {@link SneakyCopyUtils}.
 * This is necessary because of generic values for which it is not possible to define a general copy mechanism.
 *
 * @author Simon Danner, 28.12.2018
 */
public interface ISneakyCopyCreatorField<VALUE> extends IField<VALUE>
{
  @Override
  default VALUE copyValue(VALUE pValue, ECopyMode pMode, CustomFieldCopy<?>... pCustomFieldCopies) throws BeanCopyNotSupportedException
  {
    try
    {
      return pMode.shouldCopyDeep() ? SneakyCopyUtils.createDeepCopy(pValue, getDataType()) : SneakyCopyUtils.createShallowCopy(pValue);
    }
    catch (CopyUnsupportedException pE)
    {
      throw new BeanCopyNotSupportedException(this, pE);
    }
  }
}
