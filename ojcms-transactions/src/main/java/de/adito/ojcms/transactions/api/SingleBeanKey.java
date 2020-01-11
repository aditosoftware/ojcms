package de.adito.ojcms.transactions.api;

/**
 * A bean key identifying a single persistent bean.
 *
 * @author Simon Danner, 01.01.2020
 */
public final class SingleBeanKey implements IBeanKey
{
  private final String singleBeanId;

  public SingleBeanKey(String pSingleBeanId)
  {
    singleBeanId = pSingleBeanId;
  }

  @Override
  public String getContainerId()
  {
    return singleBeanId;
  }
}
