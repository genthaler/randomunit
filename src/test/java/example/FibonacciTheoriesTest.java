package example;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.experimental.theories.suppliers.TestedOn;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class FibonacciTheoriesTest {

	@Theory
	public void for_input_values_greater_than_5_Fn_greater_than_n(
			@TestedOn(ints = { 0, 1, 2, 3, 4, 5, 6 }) Integer input) {
		assumeThat(input, is(greaterThan(5)));
		assertThat(Fibonacci.compute(input), is(greaterThan(input)));
		assertThat(Fibonacci.compute(input) - Fibonacci.compute(input - 1),
				is(Fibonacci.compute(input - 2)));
	}

	@Theory
	public void test(@TestedOn(ints = { 0, 1, 2, 3, 4, 5, 6 }) Integer input) {
		assumeThat(input, is(greaterThan(5)));
		assertThat(Fibonacci.compute(input), is(greaterThan(input)));
		assertThat(Fibonacci.compute(input) - Fibonacci.compute(input - 1),
				is(Fibonacci.compute(input - 2)));
	}
}