package de.adito.ojcms.transactions.api;

import de.adito.ojcms.beans.literals.fields.IField;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Holds value based persistent data for a bean. These data consist of a value for every {@link IField} of the bean.
 *
 * @author Simon Danner, 27.12.2019
 */
public final class PersistentBeanData
{
  private final int index;
  private final Map<IField<?>, Object> data;

  /**
   * Initializes the bean data wrapper with an index for the data and the actual data.
   *
   * @param pIndex index for the bean data or -1 if none
   * @param pData  map based bean data (value for every field)
   */
  public PersistentBeanData(int pIndex, Map<IField<?>, Object> pData)
  {
    index = pIndex;
    data = Objects.requireNonNull(pData);
  }

  /**
   * The index of these persistent bean data.
   *
   * @return an index identifying this data, -1 if none
   */
  public int getIndex()
  {
    return index;
  }

  /**
   * Creates a {@link BeanIndexKey} from these bean data.
   *
   * @param pContainerId the container id the key to create is associated with
   * @return the created index based key
   */
  public BeanIndexKey createIndexKey(String pContainerId)
  {
    return new BeanIndexKey(pContainerId, index);
  }

  /**
   * Creates a {@link BeanIdentifiersKey} from these bean data.
   *
   * @param pContainerId the container id the key to create is associated with
   * @return the created container identifier based key
   */
  public BeanIdentifiersKey createIdentifierKey(String pContainerId)
  {
    final Map<IField<?>, Object> identifiers = getData().entrySet().stream()
        .filter(pEntry -> pEntry.getKey().isIdentifier())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    return new BeanIdentifiersKey(pContainerId, identifiers);
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

  /**
   * Integrates changed bean values into this bean data and creates a new instance with the resulting values map.
   * If the changes are empty, the existing bean data instance will be returned.
   *
   * @param pChangedValues a map containing all changed bean values
   * @return the bean data with integrated changes (new instance if changes present)
   */
  public PersistentBeanData integrateChanges(Map<IField<?>, Object> pChangedValues)
  {
    if (pChangedValues.isEmpty())
      return this;

    final Map<IField<?>, Object> valuesCopy = getData();
    pChangedValues.forEach(valuesCopy::put);
    return new PersistentBeanData(index, valuesCopy);
  }

  @Override
  public boolean equals(Object pOther)
  {
    if (this == pOther)
      return true;
    if (pOther == null || getClass() != pOther.getClass())
      return false;

    final PersistentBeanData that = (PersistentBeanData) pOther;
    return index == that.index && Objects.equals(data, that.data);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(index, data);
  }
}
