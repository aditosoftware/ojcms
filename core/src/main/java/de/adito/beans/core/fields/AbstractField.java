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
 * Handles basic and common data like name, data dataType, annotations etc.
 *
 * @param <TYPE> the data dataType that is wrapped by this field dataType (e.g. String, Integer etc)
 * @author Simon Danner, 23.08.2016
 */
abstract class AbstractField<TYPE> implements IField<TYPE>
{
  private final Class<TYPE> dataType;
  private final String name;
  private final Collection<Annotation> annotations;
  private final Map<Class, Function<?, TYPE>> toConverters = new HashMap<>();
  private final Map<Class, Function<TYPE, ?>> fromConverters = new HashMap<>();
  private final Map<IAdditionalFieldInfo, Object> additionalInformation = new HashMap<>();

  /**
   * Initialises the field with the base data mentioned above.
   * Important: This constructor is used by some reflection calls, which expects every specific field to have this parameters.
   *
   * @param pDataType    the inner data type of this field
   * @param pName        the name of this field
   * @param pAnnotations a collection of annotations of this field
   */
  protected AbstractField(@NotNull Class<TYPE> pDataType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    dataType = pDataType;
    name = pName;
    annotations = pAnnotations;
  }

  @Override
  public Class<TYPE> getDataType()
  {
    return dataType;
  }

  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public <SOURCE> Optional<Function<SOURCE, TYPE>> getToConverter(Class<SOURCE> pSourceType)
  {
    //noinspection unchecked
    return Optional.ofNullable((Function<SOURCE, TYPE>) toConverters.get(pSourceType));
  }

  @Override
  public <TARGET> Optional<Function<TYPE, TARGET>> getFromConverter(Class<TARGET> pTargetType)
  {
    //noinspection unchecked
    return Optional.ofNullable((Function<TYPE, TARGET>) fromConverters.get(pTargetType));
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
  protected <SOURCE> void registerConverter(Class<SOURCE> pSourceType, Function<SOURCE, TYPE> pToConverter, Function<TYPE, SOURCE> pFromConverter)
  {
    toConverters.put(pSourceType, pToConverter);
    fromConverters.put(pSourceType, pFromConverter);
  }
}
