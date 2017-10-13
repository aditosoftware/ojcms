package de.adito.beans.core.fields;

import de.adito.beans.core.IAdditionalFieldInfo;
import de.adito.beans.core.IField;
import de.adito.beans.core.annotations.Detail;
import de.adito.beans.core.annotations.Identifier;
import de.adito.beans.core.annotations.OptionalField;
import de.adito.beans.core.annotations.Private;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;

/**
 * Abstrakte Basis für ein Bean-Feld.
 * Hier werden Typ, Name und Annotations verwaltet.
 *
 * @param <TYPE> legt den 'inneren' Daten-Typen des Feldes fest. (zB. String, Integer usw.)
 * @author s.danner, 23.08.2016
 */
public abstract class AbstractField<TYPE> implements IField<TYPE> {
  private final Class<TYPE> type;
  private final String name;
  private final Collection<Annotation> annotations;
  private final Map<Class, Function<?, TYPE>> toConverters = new HashMap<>();
  private final Map<Class, Function<TYPE, ?>> fromConverters = new HashMap<>();
  private final Map<IAdditionalFieldInfo, Object> additionalInformation = new HashMap<>();

  /**
   * Initialisiert das Feld mit den oben genannten Grund-Daten.
   * WICHTIG: Dieser Konstruktor muss bei allen konkreten Feldern mit diesen Parametern genau so existieren,
   * da die Felder über Reflection erzeugt werden
   *
   * @param pType        der Daten-Typ des Feldes
   * @param pName        der Name des Feldes
   * @param pAnnotations die Annotations des Feldes
   */
  public AbstractField(@NotNull Class<TYPE> pType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations) {
    type = pType;
    name = pName;
    annotations = pAnnotations;
  }

  @Override
  public Class<TYPE> getType() {
    return type;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public <SOURCE> Optional<Function<SOURCE, TYPE>> getToConverter(Class<SOURCE> pSourceType) {
    //noinspection unchecked
    return Optional.ofNullable((Function<SOURCE, TYPE>) toConverters.get(pSourceType));
  }

  @Override
  public <SOURCE> Optional<Function<TYPE, SOURCE>> getFromConverter(Class<SOURCE> pSourceType) {
    //noinspection unchecked
    return Optional.ofNullable((Function<TYPE, SOURCE>) fromConverters.get(pSourceType));
  }

  @Override
  @Nullable
  public <ANNOTATION extends Annotation> ANNOTATION getAnnotation(Class<ANNOTATION> pType) {
    //noinspection unchecked
    return (ANNOTATION) annotations.stream()
        .filter(pAnnotation -> pAnnotation.annotationType().equals(pType))
        .findAny()
        .orElse(null);
  }

  @Override
  public boolean hasAnnotation(Class<? extends Annotation> pType) {
    return annotations.stream().anyMatch(pAnno -> pAnno.annotationType().equals(pType));
  }

  @Override
  public Collection<Annotation> getAnnotations() {
    return Collections.unmodifiableCollection(annotations);
  }

  @Override
  @Nullable
  public <INFO> INFO getAdditionalInformation(IAdditionalFieldInfo<INFO> pIdentifier) {
    //noinspection unchecked
    return (INFO) additionalInformation.get(pIdentifier);
  }

  @Override
  public <INFO> void addAdditionalInformation(IAdditionalFieldInfo<INFO> pIdentifier, INFO pValue) {
    assert pIdentifier.getType().isAssignableFrom(pValue.getClass());
    additionalInformation.put(pIdentifier, pValue);
  }

  @Override
  public boolean isPrivate() {
    return hasAnnotation(Private.class);
  }

  @Override
  public boolean isIdentifier() {
    return hasAnnotation(Identifier.class);
  }

  @Override
  public boolean isOptional() {
    return hasAnnotation(OptionalField.class);
  }

  @Override
  public boolean isDetail() {
    return hasAnnotation(Detail.class);
  }

  /**
   * Registriert einen neuen Converter für dieses Feld.
   *
   * @param pSourceType    der Quell-Typen, welcher von dem Converter umgewandelt werden kann
   * @param pToConverter   der Converter, welcher von Quelle nach Feld-Typ wandelt
   * @param pFromConverter der Converter, welcher von Feld-Typ nach Quell-Typ zurück wandelt
   * @param <SOURCE>       der generische Typ des Quell-Typen
   */
  protected <SOURCE> void registerConverter(Class<SOURCE> pSourceType, Function<SOURCE, TYPE> pToConverter, Function<TYPE, SOURCE> pFromConverter) {
    toConverters.put(pSourceType, pToConverter);
    fromConverters.put(pSourceType, pFromConverter);
  }
}
