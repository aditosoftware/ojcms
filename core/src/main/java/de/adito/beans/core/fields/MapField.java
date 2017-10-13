package de.adito.beans.core.fields;

import de.adito.beans.core.IBean;
import de.adito.beans.core.IField;
import de.adito.beans.core.MapBean;
import org.jetbrains.annotations.*;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Predicate;

/**
 * Beschreibt ein Bean-Feld, welches eine Map beinhaltet.
 * Die Map wird dabei in eine Bean umgewandelt, da eine Bean im Prinzip auch eine Map ist.
 *
 * @param <TYPE> der Value-Typ der Map, welche hier gespeichert wird.
 * @author s.danner, 01.02.2017
 */
public class MapField<TYPE> extends AbstractField<MapBean<TYPE>>
{
  private final Set<IField<TYPE>> fieldCache = new HashSet<>();

  public MapField(@NotNull Class<MapBean<TYPE>> pType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(pType, pName, pAnnotations);
  }

  /**
   * Erzeugt die Bean anhand einer beliebigen Map.
   *
   * @param pMap       die Map, die in eine Bean umgewandelt werden soll
   * @param pValueType der Value-Typ der Map.
   * @return eine (modifizierbare) Bean, welche die Map verkörpert
   */
  public MapBean<TYPE> createBeanFromMap(Map<String, Object> pMap, Class<TYPE> pValueType)
  {
    return createBeanFromMap(pMap, pValueType, null);
  }

  /**
   * Erzeugt die Bean anhand einer beliebigen Map.
   *
   * @param pMap            die Map, die in eine Bean umgewandelt werden soll
   * @param pValueType      der Value-Typ der Map.
   * @param pFieldPredicate ein Feld-Prädikat, welches bestimmt, welche Felder der Map nicht übernommen werden sollen
   * @return eine (modifizierbare) Bean, welche die Map verkörpert
   */
  public MapBean<TYPE> createBeanFromMap(Map<String, Object> pMap, Class<TYPE> pValueType, @Nullable Predicate<IField<TYPE>> pFieldPredicate)
  {
    MapBean<TYPE> bean = new MapBean<>(pMap, pValueType, fieldCache::add, (pFieldType, pName) ->
        fieldCache.stream()
            .filter(pField -> pField.getClass() == pFieldType && pField.getName().equals(pName))
            .findAny());
    if (pFieldPredicate != null)
      bean.removeFieldIf(pFieldPredicate);
    return bean;
  }

  /**
   * Wandelt die Map-Bean wieder in eine normale Map zurück.
   *
   * @param pBean      die Bean, worauf dieses Feld liegt
   * @param pValueType der Typ der Werte dieser Map
   * @return die Map, welche durch den Bean verkörpert wurde
   */
  public Map<String, TYPE> createMapFromBean(IBean<?> pBean, Class<TYPE> pValueType)
  {
    MapBean<TYPE> mapBean = pBean.getValue(this);
    return mapBean.streamFields()
        .collect(LinkedHashMap::new,
                 (pMap, pField) -> pMap.put(pField.getName(), (TYPE) mapBean.getValueConverted(pField, pValueType)),
                 LinkedHashMap::putAll);
  }
}
