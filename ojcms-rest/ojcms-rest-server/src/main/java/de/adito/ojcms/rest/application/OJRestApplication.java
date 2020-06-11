package de.adito.ojcms.rest.application;

import de.adito.ojcms.persistence.AdditionalPersistConfiguration;
import de.adito.ojcms.rest.serialization.GSONSerializationProvider;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Application;

import java.util.*;

/**
 * Base class for an OJCMS REST application.
 * Mark the implementing sub class with {@link ApplicationPath} to define the base path for your REST interfaces.
 *
 * @author Simon Danner, 02.04.2020
 */
public abstract class OJRestApplication extends Application implements AdditionalPersistConfiguration
{
  protected final Set<Class<?>> providerAndResourceTypes = new HashSet<>();
  protected final Set<Object> providerAndResourceInstances = new HashSet<>();

  /**
   * Initializes the application with all REST resources to register.
   *
   * @param pRestResources the REST resources to register
   */
  protected OJRestApplication(Class<?>... pRestResources)
  {
    if (!getClass().isAnnotationPresent(ApplicationPath.class))
      throw new IllegalStateException("Invalid OJRestApplication: " + getClass().getName() + "! Annotate the class with @ApplicationPath!");

    for (Class<?> restResource : pRestResources)
    {
      if (!restResource.isAnnotationPresent(Path.class))
        throw new IllegalArgumentException("Not a REST resource: " + restResource.getName());

      providerAndResourceTypes.add(restResource);
    }

    providerAndResourceTypes.add(GSONSerializationProvider.class);
  }

  @Override
  public Set<Class<?>> getClasses()
  {
    return providerAndResourceTypes;
  }

  @Override
  public Set<Object> getSingletons()
  {
    return providerAndResourceInstances;
  }
}
