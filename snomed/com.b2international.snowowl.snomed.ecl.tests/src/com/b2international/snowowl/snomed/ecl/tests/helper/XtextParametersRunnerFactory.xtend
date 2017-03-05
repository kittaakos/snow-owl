package com.b2international.snowowl.snomed.ecl.tests.helper

import java.util.List
import org.eclipse.xtext.testing.XtextRunner
import org.junit.runners.model.InitializationError
import org.junit.runners.parameterized.ParametersRunnerFactory
import org.junit.runners.parameterized.TestWithParameters

/**
 * Factory for parameterized Xtext tests with injection support.
 * 
 * <p>
 * Usage:
 * <pre>
 * &#64;FinalFieldsConstructor
 * &#64;RunWith(Parameterized)
 * &#64;InjectWith(MyDslInjectorProvider)
 * &#64;Parameterized.UseParametersRunnerFactory(XtextParametersRunnerFactory)
 * class MyDslParsingTest {
 * <br>
 *   &#64;Parameterized.Parameters
 *   def static data() {
 *     return #[1, 2, 3];
 *   }
 * <br>
 *   val int value;
 * <br>
 *   &#64;Test
 *   def void test() {
 *     // Use 'value'
 *   }
 * <br>
 * }
 * </pre>
 */
class XtextParametersRunnerFactory implements ParametersRunnerFactory {

	@Override
	override createRunnerForTestWithParameters(TestWithParameters test) throws InitializationError {
		new ParameterizedXtextRunner(test);
	}

	static class ParameterizedXtextRunner extends XtextRunner {

		val Object[] parameters;

		new(TestWithParameters test) throws InitializationError {
			super(test.testClass.javaClass);
			parameters = test.parameters;
		}

		@Override
		override protected createTest() throws Exception {
			val object = testClass.onlyConstructor.newInstance(parameters)
			val injectorProvider = getOrCreateInjectorProvider();
			if (injectorProvider !== null) {
				val injector = injectorProvider.injector;
				if (injector !== null)
					injector.injectMembers(object);
			}
			return object;
		}

		@Override
		override protected void validateConstructor(List<Throwable> errors) {
			validateOnlyOneConstructor(errors);
		}

	}

}
