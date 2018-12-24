package de.adito.ojcms.utils;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link IndexChecker}.
 *
 * @author Simon Danner, 24.12.2018
 */
public class IndexCheckerTest
{
  private List<String> list;

  @BeforeEach
  public void initList()
  {
    list = new ArrayList<>(Arrays.asList("test1", "test2", "test3"));
  }

  @Test
  public void testSuccessfulCheck()
  {
    _check(1);
  }

  @Test
  public void testCheckFailure()
  {
    _assertFailure(3);
    _assertFailure(-1);
  }

  @Test
  public void testWithListModification()
  {
    list.remove(0);
    _check(1);
    _assertFailure(2);
    list.clear();
    _assertFailure(0);
  }

  /**
   * Makes the assertion that the index checker will fail with an {@link IndexOutOfBoundsException}.
   *
   * @param pIndex the index to check
   */
  private void _assertFailure(int pIndex)
  {
    assertThrows(IndexOutOfBoundsException.class, () -> _check(pIndex));
  }

  /**
   * Checks a specific index. Uses the static check method and creates an checker instance and tests with that as well.
   *
   * @param pIndex the index to check
   */
  private void _check(int pIndex)
  {
    IndexChecker.check(list::size, pIndex);
    IndexChecker.create(list::size).check(pIndex);
  }
}
