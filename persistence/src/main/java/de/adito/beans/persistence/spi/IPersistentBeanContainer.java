package de.adito.beans.persistence.spi;

import de.adito.beans.core.*;

/**
 * A bean container in its persistent state.
 * It can be used to create an encapsulated data core for an actual container.
 *
 * @param <BEAN> the type of the beans in this container
 * @author Simon Danner, 14.02.2018
 * @see de.adito.beans.core.EncapsulatedBuilder.IContainerEncapsulatedBuilder
 */
public interface IPersistentBeanContainer<BEAN extends IBean<BEAN>> extends EncapsulatedBuilder.IContainerEncapsulatedBuilder<BEAN>
{
}
