package tripleo.elijah.gen;

import tripleo.elijah.contexts.ClassContext;
import tripleo.elijah.lang.*;

public interface ICodeGen {


	void addClass(ClassStatement klass) ;

	void addModule(OS_Module module) ;

//	private void addModuleItem(ModuleItem element) ;

//	private void addImport(ImportStatement imp) ;

//	private void addClassItem(ClassItem element) ;

	void addFunctionItem(FunctionItem element) ;

	void visitAliasStatement(AliasStatement aAliasStatement);

	void visitAccessNotation(AccessNotation aAccessNotation);

	void visitCaseConditional(CaseConditional aCaseConditional);

	void visitCaseScope(CaseConditional.CaseScope aCaseScope);

	void visitTypeNameElement(ClassContext.OS_TypeNameElement aOS_typeNameElement);

	void visitConstructStatement(ConstructStatement aConstructExpression);

	void visitFormalArgListItem(FormalArgListItem aFormalArgListItem);

	void visitFuncExpr(FuncExpr aFuncExpr);

	void visitFunctionDef(FunctionDef aFunctionDef);

	void visitIdentExpression(IdentExpression aIdentExpression);

	void visitIfConditional(IfConditional aIfConditional);

	void visitLoop(Loop aLoop);

	void visitImportStatment(ImportStatement aImportStatement);

	void visitMatchConditional(MatchConditional aMatchConditional);

	void visitMC1(MatchConditional.MC1 aMC1);

	void visitNamespaceStatement(NamespaceStatement aNamespaceStatement);

	void visitPropertyStatement(PropertyStatement aPropertyStatement);

	void visitStatementWrapper(StatementWrapper aStatementWrapper);

	void visitSyntacticBlock(SyntacticBlock aSyntacticBlock);

	void visitTypeAlias(TypeAliasStatement aTypeAliasStatement);

	void visitVariableSequence(VariableSequence aVariableSequence);

	void visitWithStatement(WithStatement aWithStatement);

	void visitVariableStatement(VariableStatement aVariableStatement);

	void visitYield(YieldExpression aYieldExpression);

	void visitConstructorDef(ConstructorDef aConstructorDef);

	void visitDefFunction(DefFunctionDef aDefFunctionDef);

	void visitDestructor(DestructorDef aDestructorDef);

	// return, continue, next
}

//
//
//
