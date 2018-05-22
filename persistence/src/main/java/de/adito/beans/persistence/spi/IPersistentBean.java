package de.adito.beans.persistence.spi;

import de.adito.beans.core.*;

/**
 * A bean in its persistent state.
 * It can be used to create an encapsulated data core for an actual bean.
 *
 * @author Simon Danner, 14.02.2018
 * @see de.adito.beans.core.EncapsulatedBuilder.IBeanEncapsulatedBuilder
 */
public interface IPersistentBean extends EncapsulatedBuilder.IBeanEncapsulatedBuilder
{
}
