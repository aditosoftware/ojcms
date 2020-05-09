package de.adito.ojcms.persistence;

/**
 * Marks a class to provide additional registrations for persistent beans.
 * Use {@link AdditionalPersist} or {@link AdditionalPersistAsBaseType} to define additional persistent beans.
 * This is useful if you want to use bean types in some API modules and also want to use them as persistent beans in the server module.
 *
 * @author Simon Danner, 09.05.2020
 */
public interface AdditionalPersistConfiguration
{
}
