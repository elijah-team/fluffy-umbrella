package mal;

import mal.env.Env;
import mal.types.MalContinue;
import mal.types.MalFunction;
import mal.types.MalHashMap;
import mal.types.MalList;
import mal.types.MalSymbol;
import mal.types.MalThrowable;
import mal.types.MalVal;
import mal.types.MalVector;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class step5_tco {
	// read
	public static MalVal READ(final String str) throws MalThrowable {
		return reader.read_str(str);
	}

	// eval
	public static MalVal eval_ast(final MalVal ast, final Env env) throws MalThrowable {
		if (ast instanceof MalSymbol) {
			return env.get((MalSymbol) ast);
		} else if (ast instanceof MalList) {
			final MalList old_lst = (MalList) ast;
			final MalList new_lst = ast.list_Q() ? new MalList()
			  : new MalVector();
			for (final MalVal mv : (List<MalVal>) old_lst.value) {
				new_lst.conj_BANG(EVAL(mv, env));
			}
			return new_lst;
		} else if (ast instanceof MalHashMap) {
			final MalHashMap new_hm = new MalHashMap();
			final Iterator   it     = ((MalHashMap) ast).value.entrySet().iterator();
			while (it.hasNext()) {
				final Map.Entry entry = (Map.Entry) it.next();
				new_hm.value.put(entry.getKey(), EVAL((MalVal) entry.getValue(), env));
			}
			return new_hm;
		} else {
			return ast;
		}
	}

	public static MalVal EVAL(MalVal orig_ast, Env env) throws MalThrowable {
		MalVal       a0;
		MalVal       a1;
		MalVal       a2;
		MalVal       a3;
		final MalVal res;
		MalList      el;

		while (true) {

			//System.out.println("EVAL: " + printer._pr_str(orig_ast, true));
			if (!orig_ast.list_Q()) {
				return eval_ast(orig_ast, env);
			}

			// apply list
			final MalList ast = (MalList) orig_ast;
			if (ast.size() == 0) {
				return ast;
			}
			a0 = ast.nth(0);
			final String a0sym = a0 instanceof MalSymbol ? ((MalSymbol) a0).getName()
			  : "__<*fn*>__";
			switch (a0sym) {
			case "def!":
				a1 = ast.nth(1);
				a2 = ast.nth(2);
				res = EVAL(a2, env);
				env.set(((MalSymbol) a1), res);
				return res;
			case "let*":
				a1 = ast.nth(1);
				a2 = ast.nth(2);
				MalSymbol key;
				MalVal val;
				final Env let_env = new Env(env);
				for (int i = 0; i < ((MalList) a1).size(); i += 2) {
					key = (MalSymbol) ((MalList) a1).nth(i);
					val = ((MalList) a1).nth(i + 1);
					let_env.set(key, EVAL(val, let_env));
				}
				orig_ast = a2;
				env = let_env;
				break;
			case "do":
				eval_ast(ast.slice(1, ast.size() - 1), env);
				orig_ast = ast.nth(ast.size() - 1);
				break;
			case "if":
				a1 = ast.nth(1);
				final MalVal cond = EVAL(a1, env);
				if (cond == types.Nil || cond == types.False) {
					// eval false slot form
					if (ast.size() > 3) {
						orig_ast = ast.nth(3);
					} else {
						return types.Nil;
					}
				} else {
					// eval true slot form
					orig_ast = ast.nth(2);
				}
				break;
			case "fn*":
				final MalList a1f = (MalList) ast.nth(1);
				final MalVal a2f = ast.nth(2);
				final Env cur_env = env;
				return new MalFunction(a2f, env, a1f) {
					public MalVal apply(final MalList args) throws MalThrowable {
						return EVAL(a2f, new Env(cur_env, a1f, args));
					}
				};
			default:
				el = (MalList) eval_ast(ast, env);
				final MalFunction f = (MalFunction) el.nth(0);
				final MalVal fnast = f.getAst();
				if (fnast != null) {
					orig_ast = fnast;
					env      = f.genEnv(el.slice(1));
				} else {
					return f.apply(el.rest());
				}
			}

		}
	}

	// print
	public static String PRINT(final MalVal exp) {
		return printer._pr_str(exp, true);
	}

	// repl
	public static MalVal RE(final Env env, final String str) throws MalThrowable {
		return EVAL(READ(str), env);
	}

	public static void main(final String[] args) throws MalThrowable {
		final String prompt = "user> ";

		final Env repl_env = new Env(null);

		// core.java: defined using Java
		for (final String key : core.ns.keySet()) {
			repl_env.set(new MalSymbol(key), core.ns.get(key));
		}

		// core.mal: defined using the language itself
		RE(repl_env, "(def! not (fn* (a) (if a false true)))");

		if (args.length > 0 && args[0].equals("--raw")) {
			readline.mode = readline.Mode.JAVA;
		}
		while (true) {
			final String line;
			try {
				line = readline.readline(prompt);
				if (line == null) {
					continue;
				}
			} catch (final readline.EOFException e) {
				break;
			} catch (final IOException e) {
				System.out.println("IOException: " + e.getMessage());
				break;
			}
			try {
				System.out.println(PRINT(RE(repl_env, line)));
			} catch (final MalContinue e) {
				continue;
			} catch (final MalThrowable t) {
				System.out.println("Error: " + t.getMessage());
				continue;
			} catch (final Throwable t) {
				System.out.println("Uncaught " + t + ": " + t.getMessage());
				continue;
			}
		}
	}
}
