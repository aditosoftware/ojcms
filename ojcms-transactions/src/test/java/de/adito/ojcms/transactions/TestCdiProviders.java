package de.adito.ojcms.transactions;

import de.adito.ojcms.transactions.spi.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import static org.mockito.Mockito.mock;

/**
 * Mocked CDI producers for testing.
 *
 * @author Simon Danner, 27.12.2019
 */
@ApplicationScoped
public class TestCdiProviders
{
  @ApplicationScoped
  @Produces
  public static IBeanDataLoader produceLoader()
  {
    return mock(IBeanDataLoader.class);
  }

  @ApplicationScoped
  @Produces
  public static IBeanDataStorage produceStorage()
  {
    return mock(IBeanDataStorage.class);
  }
}
