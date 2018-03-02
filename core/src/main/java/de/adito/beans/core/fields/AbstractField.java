package de.adito.beans.core.fields;

import de.adito.beans.core.*;
import de.adito.beans.core.annotations.*;
import org.jetbrains.annotations.*;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;

/**
 * Abstract base class for a bean field.
 * Handles data type, name and annotations.
 *
 * @param <TYPE> the data type that is wrapped by this field type (e.g. String, Integer etc)
 * @author Simon Danner, 23.08.2016
 */
abstract class AbstractField<TYPE> implements IField<TYPE>
{
  private final Class<TYPE> type;
  private final String name;
  private final Collection<Annotation> annotations;
  private final Map<Class, Function<?, TYPE>> toConverters = new HashMap<>();
  private final Map<Class, Function<TYPE, ?>> fromConverters = new HashMap<>();
  private final Map<IAdditionalFieldInfo, Object> additionalInformation = new HashMap<>();

  /**
   * Initialises the field with the base data mentioned above.
   * Important: This constructor is used by some reflection calls, which expects every specific field to have this parameters.
   *
   * @param pType        the inner data type of this field
   * @param pName        the name of this field
   * @param pAnnotations a collection of annotations of this field
   */
  public AbstractField(@NotNull Class<TYPE> pType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    type = pType;
    name = pName;
    annotations = pAnnotations;
  }

  @Override
  public Class<TYPE> getType()
  {
    return type;
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
  public <SOURCE> Optional<Function<TYPE, SOURCE>> getFromConverter(Class<SOURCE> pSourceType)
  {
    //noinspection unchecked
    return Optional.ofNullable((Function<TYPE, SOURCE>) fromConverters.get(pSourceType));
  }

  @Override
  @Nullable
  public <ANNOTATION extends Annotation> ANNOTATION getAnnotation(Class<ANNOTATION> pType)
  {
    //noinspection unchecked
    return (ANNOTATION) annotations.stream()
        .filter(pAnnotation -> pAnnotation.annotationType().equals(pType))
        .findAny()
        .orElse(null);
  }

  @Override
  public boolean hasAnnotation(Class<? extends Annotation> pType)
  {
    return annotations.stream().anyMatch(pAnno -> pAnno.annotationType().equals(pType));
  }

  @Override
  public Collection<Annotation> getAnnotations()
  {
    return Collections.unmodifiableCollection(annotations);
  }

  @Override
  @Nullable
  public <INFO> INFO getAdditionalInformation(IAdditionalFieldInfo<INFO> pIdentifier)
  {
    //noinspection unchecked
    return (INFO) additionalInformation.get(pIdentifier);
  }

  @Override
  public <INFO> void addAdditionalInformation(IAdditionalFieldInfo<INFO> pIdentifier, INFO pValue)
  {
    assert pIdentifier.getType().isAssignableFrom(pValue.getClass());
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

  /**
   * Registers a converter for this field type.
   *
   * @param pSourceType    the source type for this converter (the type that will be converted to this field's type)
   * @param pToConverter   the converter that converts from source type to field type
   * @param pFromConverter the converter that converts from field type to source type
   * @param <SOURCE>       the generic source type
   */
  protected <SOURCE> void registerConverter(Class<SOURCE> pSourceType, Function<SOURCE, TYPE> pToConverter, Function<TYPE, SOURCE> pFromConverter)
  {
    toConverters.put(pSourceType, pToConverter);
    fromConverters.put(pSourceType, pFromConverter);
  }
}
