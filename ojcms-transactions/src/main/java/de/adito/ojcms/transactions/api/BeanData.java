package de.adito.ojcms.transactions.api;

import de.adito.ojcms.beans.literals.fields.IField;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Holds value based data for a bean. These data consist of a value for every {@link IField} of the bean.
 * A generic key identifies these data.
 *
 * @param <KEY> the generic type of the key that identifies the bean data
 * @author Simon Danner, 27.12.2019
 */
public final class BeanData<KEY>
{
  private final KEY key;
  private final Map<IField<?>, Object> data;

  /**
   * Initializes the bean data with the identifying key and the actual data.
   *
   * @param pKey  the key of the bean data
   * @param pData map based bean data (value for every field)
   */
  public BeanData(KEY pKey, Map<IField<?>, Object> pData)
  {
    key = pKey;
    data = pData;
  }

  /**
   * The identifying key of the bean data.
   *
   * @return the key of the bean data
   */
  public KEY getKey()
  {
    return key;
  }

  /**
   * Creates a {@link ContainerIndexKey} from these bean data.
   *
   * @param pContainerId the container id the key to create is associated with
   * @return the created container identifier based key
   */
  public ContainerIdentifierKey getIdentifierKey(String pContainerId)
  {
    final Map<IField<?>, Object> identifiers = getData().entrySet().stream()
        .filter(pEntry -> pEntry.getKey().isIdentifier())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    return new ContainerIdentifierKey(pContainerId, identifiers);
  }

  /**
   * Map based bean data. It holds a value for every {@link IField}.
   *
   * @return the actual bean data values
   */
  public Map<IField<?>, Object> getData()
  {
    return new HashMap<>(data);
  }
}
