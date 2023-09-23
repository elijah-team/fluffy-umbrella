package tripleo.elijah.gen;

import tripleo.elijah.contexts.ClassContext;
import tripleo.elijah.lang.AccessNotation;
import tripleo.elijah.lang.AliasStatement;
import tripleo.elijah.lang.CaseConditional;
import tripleo.elijah.lang.ClassStatement;
import tripleo.elijah.lang.ConstructStatement;
import tripleo.elijah.lang.ConstructorDef;
import tripleo.elijah.lang.DefFunctionDef;
import tripleo.elijah.lang.DestructorDef;
import tripleo.elijah.lang.FormalArgListItem;
import tripleo.elijah.lang.FuncExpr;
import tripleo.elijah.lang.FunctionDef;
import tripleo.elijah.lang.FunctionItem;
import tripleo.elijah.lang.IdentExpression;
import tripleo.elijah.lang.IfConditional;
import tripleo.elijah.lang.ImportStatement;
import tripleo.elijah.lang.Loop;
import tripleo.elijah.lang.MatchConditional;
import tripleo.elijah.lang.NamespaceStatement;
import tripleo.elijah.lang.OS_Module;
import tripleo.elijah.lang.PropertyStatement;
import tripleo.elijah.lang.StatementWrapper;
import tripleo.elijah.lang.SyntacticBlock;
import tripleo.elijah.lang.TypeAliasStatement;
import tripleo.elijah.lang.VariableSequence;
import tripleo.elijah.lang.VariableStatement;
import tripleo.elijah.lang.WithStatement;
import tripleo.elijah.lang.YieldExpression;

public interface ICodeGen {


	void addClass(ClassStatement klass);

	void addFunctionItem(FunctionItem element);

//	private void addModuleItem(ModuleItem element) ;

//	private void addImport(ImportStatement imp) ;

//	private void addClassItem(ClassItem element) ;

	void addModule(OS_Module module);

	void visitAccessNotation(AccessNotation aAccessNotation);

	void visitAliasStatement(AliasStatement aAliasStatement);

	void visitCaseConditional(CaseConditional aCaseConditional);

	void visitCaseScope(CaseConditional.CaseScope aCaseScope);

	void visitConstructorDef(ConstructorDef aConstructorDef);

	void visitConstructStatement(ConstructStatement aConstructExpression);

	void visitDefFunction(DefFunctionDef aDefFunctionDef);

	void visitDestructor(DestructorDef aDestructorDef);

	void visitFormalArgListItem(FormalArgListItem aFormalArgListItem);

	void visitFuncExpr(FuncExpr aFuncExpr);

	void visitFunctionDef(FunctionDef aFunctionDef);

	void visitIdentExpression(IdentExpression aIdentExpression);

	void visitIfConditional(IfConditional aIfConditional);

	void visitImportStatment(ImportStatement aImportStatement);

	void visitLoop(Loop aLoop);

	void visitMatchConditional(MatchConditional aMatchConditional);

	void visitMC1(MatchConditional.MC1 aMC1);

	void visitNamespaceStatement(NamespaceStatement aNamespaceStatement);

	void visitPropertyStatement(PropertyStatement aPropertyStatement);

	void visitStatementWrapper(StatementWrapper aStatementWrapper);

	void visitSyntacticBlock(SyntacticBlock aSyntacticBlock);

	void visitTypeAlias(TypeAliasStatement aTypeAliasStatement);

	void visitTypeNameElement(ClassContext.OS_TypeNameElement aOS_typeNameElement);

	void visitVariableSequence(VariableSequence aVariableSequence);

	void visitVariableStatement(VariableStatement aVariableStatement);

	void visitWithStatement(WithStatement aWithStatement);

	void visitYield(YieldExpression aYieldExpression);

	// return, continue, next
}

//
//
//
