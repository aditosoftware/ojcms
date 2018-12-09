package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.Statistics;
import de.adito.ojcms.beans.annotations.internal.EncapsulatedData;
import de.adito.ojcms.beans.datasource.IBeanDataSource;
import de.adito.ojcms.beans.exceptions.BeanFieldDoesNotExistException;
import de.adito.ojcms.beans.fields.IField;
import de.adito.ojcms.beans.fields.util.FieldValueTuple;
import de.adito.ojcms.beans.statistics.*;
import de.adito.ojcms.beans.util.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * The encapsulated bean data core implementation based on a {@link IBeanDataSource}.
 *
 * @param <BEAN> the runtime type of the bean the data core is for
 * @author Simon Danner, 08.12.2018
 */
@EncapsulatedData
class EncapsulatedBeanData<BEAN extends IBean<BEAN>> extends AbstractEncapsulatedData<FieldValueTuple<?>, IBeanDataSource>
    implements IEncapsulatedBeanData
{
  private final List<IField<?>> fieldOrder;
  private Map<IField<?>, IStatisticData<?>> statisticData;

  /**
   * Creates the encapsulated bean data core.
   *
   * @param pDataSource the data source it is based on
   * @param pBeanType   the runtime type of the bean the data core is for
   * @param pFieldOrder the order of bean fields present as ordered list
   */
  EncapsulatedBeanData(IBeanDataSource pDataSource, Class<BEAN> pBeanType, List<IField<?>> pFieldOrder)
  {
    super(pDataSource);
    fieldOrder = new ArrayList<>(pFieldOrder);
    _createStatisticData(pBeanType);
  }

  @Override
  public <VALUE> VALUE getValue(IField<VALUE> pField)
  {
    return _ifFieldExistsWithResult(pField, getDatasource()::getValue);
  }

  @Override
  public <VALUE> void setValue(IField<VALUE> pField, VALUE pValue)
  {
    _ifFieldExists(pField, pCheckedField -> getDatasource().setValue(pCheckedField, pValue, false));
  }

  @Override
  public <VALUE> void addField(IField<VALUE> pField, int pIndex)
  {
    getDatasource().setValue(pField, null, true);
    fieldOrder.add(pIndex, pField);
  }

  @Override
  public <VALUE> void removeField(IField<VALUE> pField)
  {
    _ifFieldExists(pField, pCheckedField -> {
      getDatasource().removeField(pCheckedField);
      fieldOrder.remove(pCheckedField);
    });
  }

  @Override
  public int getFieldCount()
  {
    return fieldOrder.size();
  }

  @Override
  public Map<IField<?>, IStatisticData<?>> getStatisticData()
  {
    return Collections.unmodifiableMap(statisticData);
  }

  @Override
  public Stream<IField<?>> streamFields()
  {
    return fieldOrder.stream();
  }

  @NotNull
  @Override
  public Iterator<FieldValueTuple<?>> iterator()
  {
    final Stream<FieldValueTuple<?>> fieldValueTupleStream = fieldOrder.stream()
        .map(pField -> pField.newUntypedTuple(getValue(pField)));
    return fieldValueTupleStream.iterator();
  }

  /**
   * Creates the statistic data for this encapsulated core.
   * This data contains a set of entries with the value of a field for a certain timestamp.
   * It may contain multiple sets for every field annotated with {@link Statistics}.
   *
   * @param pBeanType the type of the bean
   */
  private void _createStatisticData(Class<? extends IBean> pBeanType)
  {
    statisticData = BeanReflector.getBeanStatisticAnnotations(pBeanType).entrySet().stream()
        .collect(Collectors.toMap(pEntry -> BeanUtil.findFieldByName(fieldOrder.stream(), pEntry.getKey()), pEntry ->
        {
          Statistics statistics = pEntry.getValue();
          return new StatisticData<>(statistics.capacity(), null);
        }));
  }

  /**
   * Checks, if a certain field is existing at a certain time.
   * Field filters are considered as well.
   * If the field is existing, a action (based on the field) will be performed and the produced result will be returned.
   *
   * @param pField   the field to check
   * @param pAction  the on the field based action to get the result from
   * @param <VALUE>  the field's data type
   * @param <RESULT> the result type
   * @return the result of the field based action
   */
  private <VALUE, RESULT> RESULT _ifFieldExistsWithResult(IField<VALUE> pField, Function<IField<VALUE>, RESULT> pAction)
  {
    if (!containsField(pField))
      throw new BeanFieldDoesNotExistException(pField);
    return pAction.apply(pField);
  }

  /**
   * Checks, if a certain field is existing at a certain time.
   * Field filters are considered as well.
   * If the field is existing, a action (based on the field) will be performed with no result
   *
   * @param pField  the field to check
   * @param pAction the on the field based action to perform
   * @param <VALUE> the field's data type
   */
  private <VALUE> void _ifFieldExists(IField<VALUE> pField, Consumer<IField<VALUE>> pAction)
  {
    if (!containsField(pField))
      throw new BeanFieldDoesNotExistException(pField);
    pAction.accept(pField);
  }
}
