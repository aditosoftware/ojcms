package de.adito.ojcms.transactions.api;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.utils.StringUtility;

import java.util.*;

/**
 * Defines an addition of a bean and its data to a bean container at a certain index.
 *
 * @author Simon Danner, 18.03.2020
 */
public final class BeanAddition extends PersistentBeanData
{
  private final Class<? extends IBean> beanType;
  private final String containerId;

  /**
   * Initializes the bean addition data wrapper.
   *
   * @param pCurrentIndex index for the bean data or -1 if none
   * @param pData         map based bean data (value for every field)
   */
  public BeanAddition(int pCurrentIndex, Map<IField<?>, Object> pData, Class<? extends IBean> pBeanType, String pContainerId)
  {
    super(pCurrentIndex, pData);
    beanType = Objects.requireNonNull(pBeanType, "The bean type is required!");
    containerId = StringUtility.requireNotEmpty(pContainerId, "container id");
  }

  /**
   * The type of the added bean.
   */
  public Class<? extends IBean> getBeanType()
  {
    return beanType;
  }

  /**
   * The id of the container the bean has been added to.
   */
  public String getContainerId()
  {
    return containerId;
  }

  @Override
  public boolean equals(Object pOther)
  {
    if (this == pOther)
      return true;
    if (pOther == null || getClass() != pOther.getClass())
      return false;
    if (!super.equals(pOther))
      return false;

    final BeanAddition that = (BeanAddition) pOther;
    return Objects.equals(beanType, that.beanType) && Objects.equals(containerId, that.containerId);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(super.hashCode(), beanType, containerId);
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "{" + //
        "index=" + getIndex() + //
        ", data=" + getData() + //
        ", beanType=" + beanType.getName() + //
        ", containerId=" + containerId + //
        '}';

  }
}
