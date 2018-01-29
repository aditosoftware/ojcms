package de.adito.beans.core.util;

import de.adito.beans.core.IField;

import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Describes a predicate belonging to a certain bean field and its associated value.
 * The predicate may be used in any use case, which requires some filtering of bean fields.
 * This interface is provided with some default predicates. They might be extended.
 *
 * @author Simon Danner, 10.08.2017
 */
public interface IBeanFieldPredicate extends BiPredicate<IField<?>, Object>
{
  IBeanFieldPredicate FIELD_NOT_NULL = (pField, pValue) -> Objects.nonNull(pField);
  IBeanFieldPredicate VALUE_NOT_NULL = (pField, pValue) -> Objects.nonNull(pValue);
  IBeanFieldPredicate IS_DETAIL = (pField, pValue) -> pField.isDetail();
}
