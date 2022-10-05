package tripleo.elijah.stages.deduce.fluffy.i;

/**
 * Anything inside a module
 *
 * - don't know about the rest (lsp, ci, compilation)
 */
public interface FluffyMember {

	FluffyMember parent();

}
