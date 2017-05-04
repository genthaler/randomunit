package example;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class FibonacciParameterizedTest {

	@Parameters(name = "{index}: fib({0})={1}")
	public static Iterable<Object[]> data() {
		return Arrays.asList(new Object[][] { { 0, 0 }, { 1, 1 }, { 2, 1 },
				{ 3, 2 }, { 4, 3 }, { 5, 5 }, { 6, 8 } });
	}

	private final int input;
	private final int expected;

	public FibonacciParameterizedTest(int input, int expected) {
		this.input = input;
		this.expected = expected;
	}

	@Test
	public void test() {
		assertThat(Fibonacci.compute(input), is(expected));
	}
}