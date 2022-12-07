package tripleo.elijah.comp;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ApacheOptionsProcessor implements OptionsProcessor {
	final Options           options = new Options();
	final CommandLineParser clp     = new DefaultParser();

	@Contract(pure = true)
	public ApacheOptionsProcessor() {
		options.addOption("s", true, "stage: E: parse; O: output");
		options.addOption("showtree", false, "show tree");
		options.addOption("out", false, "make debug files");
		options.addOption("silent", false, "suppress DeduceType output to console");
	}

	@Override
	public String[] process(final @NotNull Compilation c,
							final @NotNull List<String> args) throws Exception {
		final CommandLine cmd;
		//try {
		cmd = clp.parse(options, args.toArray(new String[args.size()]));
		//} catch (ParseException aE) {
		//	throw new RuntimeException(aE);
		//}

		if (cmd.hasOption("s")) {
			new CC_SetStage(cmd.getOptionValue('s')).apply(c);
		}
		if (cmd.hasOption("showtree")) {
			new CC_SetShowTree(true).apply(c);
		}
		if (cmd.hasOption("out")) {
			new CC_SetDoOut(true).apply(c);
		}

		if (Compilation.isGitlab_ci() || cmd.hasOption("silent")) {
			new CC_SetSilent(true).apply(c);
		}

		return cmd.getArgs();
	}
}
