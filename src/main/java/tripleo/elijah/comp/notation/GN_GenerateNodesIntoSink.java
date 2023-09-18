/*  -*- Mode: Java; tab-width: 4; indent-tabs-mode: t; c-basic-offset: 4 -*- */
/*
 * Elijjah compiler, copyright Tripleo <oluoluolu+elijah@gmail.com>
 *
 * The contents of this library are released under the LGPL licence v3,
 * the GNU Lesser General Public License text was downloaded from
 * http://www.gnu.org/licenses/lgpl.html from `Version 3, 29 June 2007'
 *
 */
package tripleo.elijah.comp.notation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tripleo.elijah.comp.i.CompilationEnclosure;
import tripleo.elijah.util.NotImplementedException;
import tripleo.elijah.work.WorkManager;
import tripleo.elijah.world.i.WorldModule;

import java.util.List;

public class GN_GenerateNodesIntoSink implements GN_Notable, CompilationEnclosure.ModuleListener {
	private final GN_GenerateNodesIntoSinkEnv env;

	public GN_GenerateNodesIntoSink(final GN_GenerateNodesIntoSinkEnv aEnv) {
		env = aEnv;

		env.pa().getCompilationEnclosure().addModuleListener(this);
	}

	@Contract(value = "_ -> new", pure = true)
	@SuppressWarnings("unused")
	public static @NotNull GN_Notable getFactoryEnv(GN_Env aEnv) {
		return new GN_GenerateNodesIntoSink((GN_GenerateNodesIntoSinkEnv) aEnv);
	}

	@Override
	public void run() {
		final WorkManager       wm   = new WorkManager();
		final List<WorldModule> mods = env.moduleList().getMods2();

		mods.stream().forEach(mod -> {
			run_one_mod(mod, wm);
		});

		wm.drain(); // README drain the WorkNanager that we created

		env.pa().getAccessBus().resolveGenerateResult(env.gr());
	}

	private void run_one_mod(final WorldModule wm, final WorkManager wmgr) {
		final GM_GenerateModuleRequest gmr  = new GM_GenerateModuleRequest(this, wm, env);
		final GM_GenerateModule        gm   = new GM_GenerateModule(gmr);
		final GM_GenerateModuleResult  ggmr = gm.getModuleResult(wmgr, env.resultSink1());
		ggmr.doResult(wmgr);
	}

	public GN_GenerateNodesIntoSinkEnv _env() {
		return env;
	}

	@Override
	public void listen(final @NotNull WorldModule module) {
		var wm = new WorkManager();
		run_one_mod(module, wm);
		wm.drain();
	}

	@Override
	public void close() {
		NotImplementedException.raise_stop();
	}
}
