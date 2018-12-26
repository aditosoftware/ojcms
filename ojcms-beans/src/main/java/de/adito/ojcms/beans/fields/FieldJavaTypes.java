package de.adito.ojcms.beans.fields;

import de.adito.ojcms.beans.annotations.internal.TypeDefaultField;
import de.adito.ojcms.beans.exceptions.OJInternalException;
import de.adito.picoservice.IPicoRegistry;

import java.util.*;
import java.util.stream.*;

/**
 * Maps Java types to bean field types. The mapping depends on fields annotated by {@link TypeDefaultField}.
 *
 * @author Simon Danner, 25.12.2018
 */
final class FieldJavaTypes
{
  private static Map<Class, Class<? extends IField>> typeFieldMapping;

  private FieldJavaTypes()
  {
  }

  /**
   * Tries to find the bean field type for a certain inner data type.
   * This depends on the field types annotated with {@link TypeDefaultField}.
   * They determine what field type is the default for the searched data type.
   * The field's value type might not be the data type directly because of converters or sub types.
   *
   * @param pDataType the inner data type of a field to find the bean field type for
   * @return an optional default field type for the data data type
   */
  static Optional<Class<IField<?>>> findFieldTypeFromDataType(Class<?> pDataType)
  {
    if (typeFieldMapping == null)
      typeFieldMapping = IPicoRegistry.INSTANCE.find(IField.class, TypeDefaultField.class).entrySet().stream()
          .flatMap(pEntry -> Stream.of(pEntry.getValue().types())
              .map(pType -> new AbstractMap.SimpleEntry<>(pType, pEntry.getKey())))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                    (pFieldType1, pFieldType2) ->
                                    {
                                      throw new OJInternalException("Incorrect default data types for bean field: " + pFieldType1.getSimpleName() +
                                                                        " supports the same data type as " + pFieldType2.getSimpleName());
                                    }));
    //noinspection unchecked
    return typeFieldMapping.entrySet().stream()
        .filter(pEntry -> pEntry.getKey().isAssignableFrom(pDataType))
        .findAny()
        .map(pEntry -> (Class<IField<?>>) pEntry.getValue());
  }
}
