package randomunit;

import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import randomunit.Randomizer.RandomTest;

public class Randomizer extends ParentRunner<RandomTest> {
	private List<RandomTest> children;

	protected Randomizer(Class<?> testClass) throws InitializationError {
		super(testClass);
	}

	public class RandomTest {

		private String className;
		private String name;
		private String uniqueId;

		public Description getDescription() {
			return Description.createTestDescription(className, name, uniqueId);
		}

	}

	@Override
	protected List<RandomTest> getChildren() {
		return children;
	}

	@Override
	protected Description describeChild(RandomTest child) {
		return child.getDescription();
	}

	@Override
	protected void runChild(RandomTest child, RunNotifier notifier) {
		// TODO Auto-generated method stub

	}

}