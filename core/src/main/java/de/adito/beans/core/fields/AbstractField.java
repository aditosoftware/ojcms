package de.adito.beans.core.fields;

import de.adito.beans.core.IField;
import de.adito.beans.core.annotations.*;
import de.adito.beans.core.fields.util.IAdditionalFieldInfo;
import org.jetbrains.annotations.*;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;

/**
 * Abstract base class for bean fields.
 * Handles basic and common data like name, data type, annotations etc.
 *
 * @param <VALUE> the data type that is wrapped by this field (e.g. String, Integer etc)
 * @author Simon Danner, 23.08.2016
 */
abstract class AbstractField<VALUE> implements IField<VALUE>
{
  private final Class<VALUE> dataType;
  private final String name;
  private final Collection<Annotation> annotations;
  private final Map<Class, Function<?, VALUE>> toConverters = new HashMap<>();
  private final Map<Class, Function<VALUE, ?>> fromConverters = new HashMap<>();
  private final Map<IAdditionalFieldInfo, Object> additionalInformation = new HashMap<>();

  /**
   * Initialises the field with the base data mentioned above.
   * Important: This constructor is used by some reflection calls, which expects every specific field to have this parameters.
   *
   * @param pDataType    the inner data type of this field
   * @param pName        the name of this field
   * @param pAnnotations a collection of annotations of this field
   */
  protected AbstractField(@NotNull Class<VALUE> pDataType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    dataType = pDataType;
    name = pName;
    annotations = pAnnotations;
  }

  @Override
  public Class<VALUE> getDataType()
  {
    return dataType;
  }

  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public <SOURCE> Optional<Function<SOURCE, VALUE>> getToConverter(Class<SOURCE> pSourceType)
  {
    //noinspection unchecked
    return Optional.ofNullable((Function<SOURCE, VALUE>) toConverters.get(pSourceType));
  }

  @Override
  public <TARGET> Optional<Function<VALUE, TARGET>> getFromConverter(Class<TARGET> pTargetType)
  {
    //noinspection unchecked
    return Optional.ofNullable((Function<VALUE, TARGET>) fromConverters.get(pTargetType));
  }

  @Override
  @Nullable
  public <ANNOTATION extends Annotation> Optional<ANNOTATION> getAnnotation(Class<ANNOTATION> pType)
  {
    //noinspection unchecked
    return annotations.stream()
        .filter(pAnnotation -> pAnnotation.annotationType().equals(pType))
        .findAny()
        .map(pAnnotation -> (ANNOTATION) pAnnotation);
  }

  @Override
  public boolean hasAnnotation(Class<? extends Annotation> pType)
  {
    return annotations.stream().anyMatch(pAnnotation -> pAnnotation.annotationType().equals(pType));
  }

  @Override
  public Collection<Annotation> getAnnotations()
  {
    return Collections.unmodifiableCollection(annotations);
  }

  @Override
  public <INFO> Optional<INFO> getAdditionalInformation(IAdditionalFieldInfo<INFO> pIdentifier)
  {
    //noinspection unchecked
    return Optional.ofNullable(additionalInformation.get(pIdentifier))
        .map(pInfo -> (INFO) pInfo);
  }

  @Override
  public <INFO> void addAdditionalInformation(IAdditionalFieldInfo<INFO> pIdentifier, INFO pValue)
  {
    assert pIdentifier.getDataType().isAssignableFrom(pValue.getClass());
    additionalInformation.put(pIdentifier, pValue);
  }

  @Override
  public boolean isPrivate()
  {
    return hasAnnotation(Private.class);
  }

  @Override
  public boolean isIdentifier()
  {
    return hasAnnotation(Identifier.class);
  }

  @Override
  public boolean isOptional()
  {
    return hasAnnotation(OptionalField.class);
  }

  @Override
  public boolean isDetail()
  {
    return hasAnnotation(Detail.class);
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "{" +
        "dataType=" + dataType +
        ", name='" + name + '\'' +
        ", annotations=" + annotations +
        ", additionalInformation=" + additionalInformation +
        '}';
  }

  /**
   * Registers a converter for this field's data type
   *
   * @param pSourceType    the source data type for this converter (the data type that will be converted to this field's type)
   * @param pToConverter   the converter that converts from the source's data type to field's data type
   * @param pFromConverter the converter that converts from the field's data type to the source's data type
   * @param <SOURCE>       the generic source data type
   */
  protected <SOURCE> void registerConverter(Class<SOURCE> pSourceType, Function<SOURCE, VALUE> pToConverter, Function<VALUE, SOURCE> pFromConverter)
  {
    toConverters.put(pSourceType, pToConverter);
    fromConverters.put(pSourceType, pFromConverter);
  }
}
