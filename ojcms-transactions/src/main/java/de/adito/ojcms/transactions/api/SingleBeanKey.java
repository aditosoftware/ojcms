package de.adito.ojcms.transactions.api;

import java.util.Objects;

/**
 * A bean key identifying a single persistent bean.
 *
 * @author Simon Danner, 01.01.2020
 */
public final class SingleBeanKey
{
  private final String singleBeanId;

  public SingleBeanKey(String pSingleBeanId)
  {
    singleBeanId = pSingleBeanId;
  }

  /**
   * The id of the single bean.
   */
  public String getBeanId()
  {
    return singleBeanId;
  }

  @Override
  public boolean equals(Object pOther)
  {
    if (this == pOther)
      return true;
    if (pOther == null || getClass() != pOther.getClass())
      return false;

    final SingleBeanKey that = (SingleBeanKey) pOther;
    return Objects.equals(singleBeanId, that.singleBeanId);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(singleBeanId);
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "{" +
        "singleBeanId='" + singleBeanId + '\'' +
        '}';
  }
}
