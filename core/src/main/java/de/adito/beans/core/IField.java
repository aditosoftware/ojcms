package de.adito.beans.core;

import de.adito.beans.core.util.IClientInfo;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;

/**
 * Beschreibt das Feld einer Bean.
 * Es besitzt einen 'inneren' Daten-Typen.
 *
 * @author s.danner, 23.08.2016
 */
public interface IField<TYPE>
{
  /**
   * Liefert den 'inneren' Daten-Typen des Feldes.
   */
  Class<TYPE> getType();

  /**
   * Liefert den Namen des Feldes.
   */
  String getName();

  /**
   * Liefert einen Standard-Wert für den Daten-Typen des Feldes.
   */
  default TYPE getDefaultValue()
  {
    return null;
  }

  /**
   * Präsentiert den Wert des Feldes als String (Standard = toString())
   *
   * @param pValue             der Wert des Feldes
   * @param pClientSessionInfo Informationen zur Client-Session (Locale, TimeZone etc.)
   * @return der Wert als String repräsentiert
   */
  default String display(TYPE pValue, IClientInfo pClientSessionInfo)
  {
    return Objects.toString(pValue);
  }

  /**
   * Liefert (falls vorhanden) einen Converter für dieses Feld.
   * Der Converter kann dabei den Wert eines bestimmten Quell-Datentypen in den Typen dieses Feld umwandeln.
   *
   * @param pSourceType der Typ des Quell-Wertes
   * @return der Converter zur Umwandlung des Quell-Wertes (Optional, falls dieses Feld einen solchen Converter kennt)
   */
  <SOURCE> Optional<Function<SOURCE, TYPE>> getToConverter(Class<SOURCE> pSourceType);

  /**
   * Liefert (falls vorhanden) einen Converter für dieses Feld.
   * Der Converter kann dabei den Wert dieses Feldes zurück in den bestimmten Datentypen wandeln.
   *
   * @param pSourceType der Typ des Quell-Wertes
   * @return der Converter zur Zurück-Umwandlung Feld-Wertes (Optional, falls dieses Feld einen solchen Converter kennt)
   */
  <SOURCE> Optional<Function<TYPE, SOURCE>> getFromConverter(Class<SOURCE> pSourceType);

  /**
   * Liefert die Annotation des Feldes anhand des Annotation-Typen. Wenn nicht vorhanden, null.
   *
   * @param pType der Typ der Annotation
   * @return die bestimmte Annotation des Feldes oder null, wenn nicht vorhanden
   */
  @Nullable
  <ANNOTATION extends Annotation> ANNOTATION getAnnotation(Class<ANNOTATION> pType);

  /**
   * Gibt an, ob das Feld eine bestimmte Annotation besitzt.
   *
   * @param pType der Typ der Annotation
   * @return <tt>true</tt>, wenn das Feld die Annotation besitzt
   */
  boolean hasAnnotation(Class<? extends Annotation> pType);

  /**
   * Liefert alle Annotations dieses Feldes.
   */
  Collection<Annotation> getAnnotations();

  /**
   * Stellt eine beliebige zusätzliche Information dieses Feldes bereit
   *
   * @param pIdentifier der Name der zusätzlichen Information
   * @return die beliebige Information
   */
  @Nullable
  <INFO> INFO getAdditionalInformation(IAdditionalFieldInfo<INFO> pIdentifier);

  /**
   * Fügt dem Feld eine beliebige zusätzliche Information zur Verfügung
   *
   * @param pIdentifier der Name der zusätzlichen Information
   * @param pValue      die beliebige Information
   */
  <INFO> void addAdditionalInformation(IAdditionalFieldInfo<INFO> pIdentifier, INFO pValue);

  /**
   * Gibt an, ob es sich um ein datengekapseltes Feld handelt.
   */
  boolean isPrivate();

  /**
   * Gibt an, ob das Feld ein Identifier für Container ist.
   */
  boolean isIdentifier();

  /**
   * Gibt, an ob es sich um ein optionales Feld handelt.
   * Wenn optional, existiert nur unter einer bestimmten Bedingung
   */
  boolean isOptional();

  /**
   * Gibt an, ob das Feld als Detail markiert ist.
   */
  boolean isDetail();
}
