package example;

import java.util.ArrayList;
import java.util.List;

public class Fibonacci {
	@SuppressWarnings("serial")
	private static List<Integer> memo = new ArrayList<Integer>() {
		{
			add(0);
			add(1);
		}
	};

	public static int compute(int input) {
		if (input < memo.size()) {
			return memo.get(input);
		}

		int fib = 0;

		do {
			fib = memo.get(memo.size() - 1) + memo.get(memo.size() - 2);
			memo.add(fib);
		} while (input >= memo.size());

		return fib;
	}
}
