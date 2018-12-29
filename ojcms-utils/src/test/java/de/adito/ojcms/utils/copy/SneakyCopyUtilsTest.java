package de.adito.ojcms.utils.copy;

import de.adito.ojcms.utils.copy.exceptions.CopyUnsupportedException;
import org.apache.commons.lang3.ClassUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;

import static de.adito.ojcms.utils.copy.SneakyCopyUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SneakyCopyUtils}.
 *
 * @author Simon Danner, 28.12.2018
 */
public class SneakyCopyUtilsTest
{
  @Test
  public void testShallowCopy() throws IllegalAccessException, CopyUnsupportedException
  {
    _testWithAllClasses(false);
  }

  @Test
  public void testDeepCopy() throws IllegalAccessException, CopyUnsupportedException
  {
    _testWithAllClasses(true);
  }

  @Test
  public void testPrimitiveGenericTypeDeepCopy() throws CopyUnsupportedException
  {
    final List<Integer> list = Arrays.asList(1, 2, 3);
    final List<Integer> copiedList = createDeepCopy(list, List.class, Integer.class);
    assertNotSame(list, copiedList);
    assertEquals(list, copiedList);
  }

  @Test
  public void testComplexGenericTypeDeepCopy() throws CopyUnsupportedException
  {
    final List<_ThirdClass> list = Arrays.asList(new _ThirdClass(), new _ThirdClass());
    final List<_ThirdClass> copiedList = createDeepCopy(list, List.class, _ThirdClass.class);
    assertNotSame(list, copiedList);
    assertNotEquals(list, copiedList);
  }

  /**
   * Tests copies of all three classed.
   *
   * @param pDeep <tt>true</tt> if the tested copies should be deep
   */
  private static void _testWithAllClasses(boolean pDeep) throws IllegalAccessException, CopyUnsupportedException
  {
    final _FirstClass original1 = new _FirstClass();
    final _SecondClass original2 = new _SecondClass();
    final _ThirdClass original3 = new _ThirdClass();
    _checkCopy(original1, _createCopy(original1, pDeep), pDeep);
    _checkCopy(original2, _createCopy(original2, pDeep), pDeep);
    _checkCopy(original3, _createCopy(original3, pDeep), pDeep);
  }

  /**
   * Creates a copy of any value through {@link SneakyCopyUtils}.
   *
   * @param pOriginal the original value
   * @param pDeep     <tt>true</tt> if the copy should be deep
   * @param <VALUE>   the generic type of the value
   * @return the copied value
   */
  private static <VALUE> VALUE _createCopy(VALUE pOriginal, boolean pDeep) throws CopyUnsupportedException
  {
    //noinspection unchecked
    final Class<VALUE> type = (Class<VALUE>) pOriginal.getClass();
    return pDeep ? createDeepCopy(pOriginal, type) : createShallowCopy(pOriginal);
  }

  /**
   * Checks if a copy is correct.
   * Also includes deep values of the instances to check.
   *
   * @param pOriginal the original instance
   * @param pCopy     the copied instance
   * @param pDeep     <tt>true</tt> if it is a deep copy
   * @param <VALUE>   the type of the instances to check
   */
  private static <VALUE> void _checkCopy(VALUE pOriginal, VALUE pCopy, boolean pDeep) throws IllegalAccessException
  {
    _assertCopySuccessful(pOriginal, pCopy);
    if (pDeep && pOriginal != null)
    {
      final Class<?> type = pOriginal.getClass();
      if (!_isPrimitiveOrEnumOrTypeOrString(type) && !Collection.class.isAssignableFrom(type) && !Map.class.isAssignableFrom(type))
        for (Field field : reflectDeclaredFields(type))
          _checkCopy(field.get(pOriginal), field.get(pCopy), true);
    }

  }

  /**
   * Makes an assertion if two values are copied correctly.
   * There will be a special treatment for primitive, enum, string and class types. They should not be copied.
   * Also collection and map types are tested for their content.
   *
   * @param pOriginal the original value
   * @param pCopy     the copied value
   * @param <VALUE>   the type of the values
   */
  private static <VALUE> void _assertCopySuccessful(VALUE pOriginal, VALUE pCopy) throws IllegalAccessException
  {
    if (pOriginal == null && pCopy == null)
      return;
    assertNotNull(pOriginal);
    assertNotNull(pCopy);

    if (_isPrimitiveOrEnumOrTypeOrString(pOriginal.getClass()))
      assertEquals(pOriginal, pCopy);
    else if (pOriginal.getClass().isArray() && !pOriginal.getClass().getComponentType().isPrimitive())
      _assertNonPrimitiveArrayCopySuccessful((Object[]) pOriginal, (Object[]) pCopy);
    else if (pOriginal instanceof Collection)
      _assertCollectionCopySuccessful((Collection<?>) pOriginal, (Collection<?>) pCopy);
    else if (pOriginal instanceof Map)
      _assertMapCopySuccessful((Map<?, ?>) pOriginal, (Map<?, ?>) pCopy);
    else
      assertNotSame(pOriginal, pCopy, "type: " + pOriginal.getClass().getName() + ", original: " + pOriginal + ", copy: " + pCopy);
  }

  /**
   * Makes an assertion that a non primitive array was copied properly.
   *
   * @param pOriginal the original array
   * @param pCopy     the copied array
   */
  private static void _assertNonPrimitiveArrayCopySuccessful(Object[] pOriginal, Object[] pCopy) throws IllegalAccessException
  {
    assertNotSame(pOriginal, pCopy);
    assertEquals(pOriginal.length, pCopy.length);
    for (int i = 0; i < pOriginal.length; i++)
      _checkCopy(pOriginal[i], pCopy[i], true);
  }

  /**
   * Makes an assertion that a collection was copied properly.
   *
   * @param pOriginal the original collection
   * @param pCopy     the copied collection
   */
  private static void _assertCollectionCopySuccessful(Collection<?> pOriginal, Collection<?> pCopy) throws IllegalAccessException
  {
    assertNotSame(pOriginal, pCopy);
    assertEquals(pOriginal.size(), pCopy.size());
    final Iterator<?> originalIterator = pOriginal.iterator();
    final Iterator<?> copyIterator = pCopy.iterator();
    while (originalIterator.hasNext())
    {
      final Object original = originalIterator.next();
      final Object copy = copyIterator.next();
      if (original != null && copy.getClass() != null)
        assertSame(original.getClass(), copy.getClass());
      _checkCopy(original, copy, true);
    }
  }

  /**
   * Makes an assertion that a map was copied properly.
   *
   * @param pOriginal the original map
   * @param pCopy     the copied map
   */
  private static void _assertMapCopySuccessful(Map<?, ?> pOriginal, Map<?, ?> pCopy) throws IllegalAccessException
  {
    assertNotSame(pOriginal, pCopy);
    assertEquals(pOriginal.size(), pCopy.size());
    final Iterator<? extends Map.Entry<?, ?>> originalIterator = pOriginal.entrySet().iterator();
    final Iterator<? extends Map.Entry<?, ?>> copyIterator = pCopy.entrySet().iterator();
    while (originalIterator.hasNext())
    {
      final Map.Entry<?, ?> originalEntry = originalIterator.next();
      final Map.Entry<?, ?> copiedEntry = copyIterator.next();
      if (originalEntry.getKey() != null && copiedEntry.getKey() != null)
        assertSame(originalEntry.getKey().getClass(), copiedEntry.getKey().getClass());
      if (originalEntry.getValue() != null && copiedEntry.getValue() != null)
        assertSame(originalEntry.getValue().getClass(), copiedEntry.getValue().getClass());
      _checkCopy(originalEntry.getKey(), copiedEntry.getKey(), true);
      _checkCopy(originalEntry.getValue(), copiedEntry.getValue(), true);
    }
  }

  /**
   * Determines, if a type is either primitive, an enum, an class or a string type.
   *
   * @param pType the type to check
   * @return <tt>true</tt> if one of the types above is accurate
   */
  private static boolean _isPrimitiveOrEnumOrTypeOrString(Class<?> pType)
  {
    return ClassUtils.isPrimitiveOrWrapper(pType) || Enum.class.isAssignableFrom(pType) || Class.class.isAssignableFrom(pType) ||
        String.class.isAssignableFrom(pType);
  }

  /**
   * Outer class to copy.
   */
  @SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
  private static class _FirstClass
  {
    private final boolean someBoolean = true;
    private final int number = 5;
    private final String text = "text";
    private final _ESomeEnum someEnum = _ESomeEnum.INSTANCE;
    private final List<Integer> primitiveList = Arrays.asList(1, 2, 3);
    private final List<_SecondClass> complexList = Arrays.asList(new _SecondClass(), new _SecondClass());
    private final Class<_FirstClass> classType = _FirstClass.class;
    private final Map<String, _SecondClass> someMap = new HashMap<>();
    private final boolean[] primitiveArray = new boolean[]{true, false, false};
    private final _ThirdClass[] complexArray = new _ThirdClass[]{new _ThirdClass(), new _ThirdClass()};

    private _FirstClass()
    {
      someMap.put("key1", new _SecondClass());
      someMap.put("key2", new _SecondClass());
      someMap.put("key3", new _SecondClass());
    }
  }

  /**
   * Second level class to copy.
   */
  @SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
  private static class _SecondClass
  {
    private final float decimalNumber = 5.5f;
    private final String text = "secondText";
    private final Set<_ThirdClass> someSet = new HashSet<>(Collections.singletonList(new _ThirdClass()));
    private final List<String> emptyList = Collections.emptyList();
    private final _ThirdClass evenDeeper = new _ThirdClass();
  }

  /**
   * Third level class to copy.
   */
  @SuppressWarnings("unused")
  private static class _ThirdClass
  {
    private final String muh = "muh";
  }

  /**
   * Any enum.
   */
  private enum _ESomeEnum
  {
    INSTANCE
  }
}
