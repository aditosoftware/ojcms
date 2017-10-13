package de.adito.beans.core.util;

import de.adito.beans.core.IField;

import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Spezielles Interface für ein Bean-Feld-Prädikat.
 * Dabei wird nicht nur das Feld selbst, sondern auch der zugehörige Wert bereitgestellt
 *
 * @author s.danner, 10.08.2017
 */
public interface IBeanFieldPredicate extends BiPredicate<IField<?>, Object>
{
  IBeanFieldPredicate FIELD_NOT_NULL = (pField, pValue) -> Objects.nonNull(pField);
  IBeanFieldPredicate VALUE_NOT_NULL = (pField, pValue) -> Objects.nonNull(pValue);
  IBeanFieldPredicate IS_DETAIL = (pField, pValue) -> pField.isDetail();
}
