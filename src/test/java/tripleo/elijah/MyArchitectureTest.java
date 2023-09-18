package tripleo.elijah;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import org.junit.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

public class MyArchitectureTest {
	@Test
	public void some_architecture_rule() {
		JavaClasses importedClasses = new ClassFileImporter().importPackages("tripleo.elijah");

		ArchRule rule = classes().that() // see next section
		                         .areAnnotatedWith(Service.class)
		                         .should()
		                         .notBeInnerClasses();

		rule.check(importedClasses);
	}
}
