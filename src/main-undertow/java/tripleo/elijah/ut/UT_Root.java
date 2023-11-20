package tripleo.elijah.ut;

import tripleo.elijah.comp.ICompilationBus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static tripleo.elijah.util.Helpers.List_of;

class UT_Root {
	List<Path> paths = List_of();

	private final Map<String, ICompilationBus.CB_Action> dcs = new HashMap<>();

	public UT_Root() {
		final Path path = Paths.get("test");

		try {
			paths = listFiles(path);
		} catch (final IOException aE) {
//			throw new RuntimeException(aE);
			System.err.println(aE);
			return;
		}

		paths.forEach(x -> System.out.println(x));
	}

	public static List<Path> listFiles(final Path path) throws IOException {
		final List<Path> result;
		try (final Stream<Path> walk = Files.walk(path)) {
			result = walk.filter(Files::isRegularFile)
			             .filter(x -> x.toFile().getName().endsWith(".ez"))
			             .collect(Collectors.toList());
		}
		return result;
	}

	public void dcs(final String aF, final ICompilationBus.CB_Action aAction) {
		dcs.put(aF, aAction);
	}

	public ICompilationBus.CB_Action dcs(final String aF) {
		return dcs.get(aF);
	}
}
