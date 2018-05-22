package de.adito.beans.persistence.datastores.sql.builder;

import java.io.Closeable;

/**
 * A marker interface for all SQL statements.
 * A statement is closeable to detach the connection to the database afterwards.
 *
 * @author Simon Danner, 26.04.2018
 */
public interface IStatement extends Closeable
{
}
