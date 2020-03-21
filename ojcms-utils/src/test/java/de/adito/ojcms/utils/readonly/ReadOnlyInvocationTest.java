package de.adito.ojcms.utils.readonly;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests the basic functionality of {@link ReadOnlyInvocationHandler}.
 *
 * @author Simon Danner, 08.12.2018
 */
public class ReadOnlyInvocationTest
{
  @Test
  public void testReadOnly()
  {
    final _TestInterface readOnly = ReadOnlyInvocationHandler.createReadOnlyInstance(_TestInterface.class, new _Impl());
    assertThrows(UnsupportedOperationException.class, readOnly::changeData);
    assertThrows(UnsupportedOperationException.class, readOnly::changeData2);
    readOnly.getData(); //Should work
    assertThrows(UnsupportedOperationException.class, () -> readOnly.getList().clear());
    assertThrows(UnsupportedOperationException.class, () -> readOnly.getSet().clear());
    assertThrows(UnsupportedOperationException.class, () -> readOnly.getMap().clear());
  }

  /**
   * Interface to test read only operations.
   */
  public interface _TestInterface
  {
    @WriteOperation
    void changeData();

    @WriteOperation
    void changeData2();

    int getData();

    List<String> getList();

    Set<String> getSet();

    Map<String, String> getMap();
  }

  /**
   * Example implementation of the test interface.
   */
  public static class _Impl implements _TestInterface
  {
    private int data = 0;

    @Override
    public void changeData()
    {
      data = 1;
    }

    @Override
    public void changeData2()
    {
      data = 2;
    }

    @Override
    public int getData()
    {
      return data;
    }

    @Override
    public List<String> getList()
    {
      return Stream.of("1", "2").collect(Collectors.toList());
    }

    @Override
    public Set<String> getSet()
    {
      return Stream.of("1", "2").collect(Collectors.toSet());
    }

    @Override
    public Map<String, String> getMap()
    {
      return Stream.of("1", "2").collect(Collectors.toMap(Function.identity(), Function.identity()));
    }
  }
}
