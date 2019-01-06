package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.Statistics;
import de.adito.ojcms.beans.annotations.internal.EncapsulatedData;
import de.adito.ojcms.beans.datasource.IBeanDataSource;
import de.adito.ojcms.beans.exceptions.field.BeanFieldDoesNotExistException;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.literals.fields.util.FieldValueTuple;
import de.adito.ojcms.beans.statistics.*;
import de.adito.ojcms.utils.IndexChecker;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * The encapsulated bean data core implementation based on a {@link IBeanDataSource}.
 *
 * @author Simon Danner, 08.12.2018
 */
@EncapsulatedData
class EncapsulatedBeanData extends AbstractEncapsulatedData<FieldValueTuple<?>, IBeanDataSource>
    implements IEncapsulatedBeanData
{
  private final List<IField<?>> fieldOrder;
  private final Map<IField<?>, IStatisticData<?>> statisticData;
  private final IndexChecker indexChecker = IndexChecker.create(this::getFieldCount);

  /**
   * Creates the encapsulated bean data core.
   *
   * @param pDataSource the data source it is based on
   * @param pFieldOrder the order of bean fields present as ordered list
   */
  EncapsulatedBeanData(IBeanDataSource pDataSource, List<IField<?>> pFieldOrder)
  {
    super(pDataSource);
    fieldOrder = new ArrayList<>(pFieldOrder);
    statisticData = _createBeanStatisticMapping();
  }

  @Override
  public <VALUE> VALUE getValue(IField<VALUE> pField)
  {
    return _ifFieldExistsReturn(pField, getDatasource()::getValue);
  }

  @Override
  public <VALUE> void setValue(IField<VALUE> pField, VALUE pValue)
  {
    _ifFieldExists(pField, pCheckedField -> getDatasource().setValue(pCheckedField, pValue, false));
  }

  @Override
  public <VALUE> void addField(IField<VALUE> pField, int pIndex)
  {
    getDatasource().setValue(pField, pField.getInitialValue(), true);
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
  public IField<?> removeFieldAtIndex(int pIndex)
  {
    final IField<?> removedField = fieldOrder.remove(indexChecker.check(pIndex));
    getDatasource().removeField(removedField);
    return removedField;
  }

  @Override
  public int getFieldCount()
  {
    return fieldOrder.size();
  }

  @Override
  public IField<?> getFieldAtIndex(int pIndex)
  {
    return fieldOrder.get(indexChecker.check(pIndex));
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
   * Takes all bean fields annotated with {@link Statistics} and then creates a mapping for the statistic data for these fields.
   *
   * @return a map with the field as key and initial statistic data as value
   */
  private Map<IField<?>, IStatisticData<?>> _createBeanStatisticMapping()
  {
    return fieldOrder.stream()
        .filter(pField -> pField.hasAnnotation(Statistics.class))
        .collect(Collectors.toMap(Function.identity(),
                                  pField -> new StatisticData<>(pField.getAnnotationOrThrow(Statistics.class).capacity(), null)));
  }

  /**
   * Checks, if a certain field is existing at a certain time. (
   * If the field is existing, an action (based on the field) will be performed and the produced result will be returned.
   *
   * @param pField   the field to check
   * @param pAction  the on the field based action to get the result from
   * @param <VALUE>  the field's data type
   * @param <RESULT> the result type
   * @return the result of the field based action
   */
  private <VALUE, RESULT> RESULT _ifFieldExistsReturn(IField<VALUE> pField, Function<IField<VALUE>, RESULT> pAction)
  {
    if (!containsField(pField))
      throw new BeanFieldDoesNotExistException(pField);
    return pAction.apply(pField);
  }

  /**
   * Checks, if a certain field is existing at a certain time. Field filters are considered as well)
   * If the field is existing, an action (based on the field) will be performed with no result.
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
