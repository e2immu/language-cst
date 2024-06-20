package org.e2immu.language.cst.api.runtime;

import org.e2immu.language.cst.api.element.*;
import org.e2immu.language.cst.api.expression.*;
import org.e2immu.language.cst.api.info.*;
import org.e2immu.language.cst.api.element.*;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.OutputElement;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.output.element.ElementarySpace;
import org.e2immu.language.cst.api.output.element.Split;
import org.e2immu.language.cst.api.statement.*;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.type.*;
import org.e2immu.language.cst.api.variable.*;
import org.e2immu.language.cst.api.expression.*;
import org.e2immu.language.cst.api.info.*;
import org.e2immu.language.cst.api.statement.*;
import org.e2immu.language.cst.api.type.*;
import org.e2immu.language.cst.api.variable.*;

import java.util.List;
import java.util.stream.Collector;

public interface Factory {

    Access accessPackage();

    Access accessPrivate();

    Access accessProtected();

    Access accessPublic();

    ParameterizedType commonType(ParameterizedType pt1, ParameterizedType pt2);

    default BooleanConstant constantFalse() {
        return newBoolean(false);
    }

    default BooleanConstant constantTrue() {
        return newBoolean(true);
    }

    Expression constructorCallWithArrayInitializer(MethodInfo constructor, ParameterizedType returnType, List<Object> of, ArrayInitializer initializer);

    DescendMode descendModeNo();

    DescendMode descendModeYes();

    Diamond diamondNo();

    Diamond diamondShowAll();

    Diamond diamondYes();

    ElementarySpace elementarySpaceNice();

    ElementarySpace elementarySpaceRelaxedNone();

    Block emptyBlock();

    FieldModifier fieldModifierFinal();

    FieldModifier fieldModifierPrivate();

    FieldModifier fieldModifierProtected();

    FieldModifier fieldModifierPublic();

    FieldModifier fieldModifierStatic();

    FieldModifier fieldModifierTransient();

    FieldModifier fieldModifierVolatile();

    IntConstant intMinusOne();

    IntConstant intOne();

    Numeric intOrDouble(double v);

    IntConstant intZero();

    Lambda.OutputVariant lambdaOutputVariantEmpty();

    MethodModifier methodModifierAbstract();

    MethodModifier methodModifierDefault();

    MethodModifier methodModifierFinal();

    MethodModifier methodModifierPrivate();

    MethodModifier methodModifierProtected();

    MethodModifier methodModifierPublic();

    MethodModifier methodModifierStatic();

    MethodModifier methodModifierSynchronized();

    MethodInfo.MethodType methodTypeAbstractMethod();

    MethodInfo.MethodType methodTypeConstructor();

    MethodInfo.MethodType methodTypeDefaultMethod();

    MethodInfo.MethodType methodTypeMethod();

    MethodInfo.MethodType methodTypeStaticMethod();

    MethodInfo.MethodType methodTypeSyntheticConstructor();

    AnnotationExpression.Builder newAnnotationExpressionBuilder();

    TypeInfo newAnonymousType(TypeInfo enclosingType, int index);

    MethodInfo newArrayCreationConstructor(ParameterizedType returnType);

    ArrayInitializer newArrayInitializer(List<Expression> expressions, ParameterizedType commonType);

    ArrayLength newArrayLength(Expression e);

    AssertStatement.Builder newAssertStatementBuilder();

    Assignment newAssignment(Expression target, Expression value);

    Assignment.Builder newAssignmentBuilder();

    BinaryOperator.Builder newBinaryOperatorBuilder();

    Block.Builder newBlockBuilder();

    BooleanConstant newBoolean(boolean value);

    BreakStatement.Builder newBreakBuilder();

    ByteConstant newByte(byte b);

    Cast newCast(Expression e, ParameterizedType parameterizedType);

    Cast.Builder newCastBuilder();

    TryStatement.CatchClause.Builder newCatchClauseBuilder();

    CharConstant newChar(char c);

    CommaExpression.Builder newCommaBuilder();

    CompilationUnit.Builder newCompilationUnitBuilder();

    MethodInfo newConstructor(TypeInfo owner);

    MethodInfo newConstructor(TypeInfo owner, MethodInfo.MethodType methodType);

    ConstructorCall.Builder newConstructorCallBuilder();

    ContinueStatement.Builder newContinueBuilder();

    DependentVariable newDependentVariable(Expression array, Expression index);

    DoStatement.Builder newDoBuilder();

    DoubleConstant newDouble(double d);

    EmptyExpression newEmptyExpression();

    EmptyExpression newEmptyExpression(String msg);

    EnclosedExpression newEnclosedExpression(Expression inner);

    Equals newEquals(Expression lhs, Expression rhs);

    ExplicitConstructorInvocation.Builder newExplicitConstructorInvocationBuilder();

    ExpressionAsStatement newExpressionAsStatement(Expression standardized);

    ExpressionAsStatement.Builder newExpressionAsStatementBuilder();

    FieldInfo newFieldInfo(String name, boolean isStatic, ParameterizedType parameterizedType, TypeInfo owner);

    FieldReference newFieldReference(FieldInfo fieldInfo);

    FieldReference newFieldReference(FieldInfo fieldInfo, Expression scope, ParameterizedType concreteReturnType);

    FloatConstant newFloat(float f);

    ForStatement.Builder newForBuilder();

    ForEachStatement.Builder newForEachBuilder();

    GreaterThanZero newGreaterThanZero(Expression e, boolean allowEquals);

    IfElseStatement.Builder newIfElseBuilder();

    ImportStatement newImportStatement(String importString);

    InlineConditional newInlineConditional(Expression condition, Expression ifTrue, Expression ifFalse);

    InlineConditional.Builder newInlineConditionalBuilder();

    Instance newInstanceForTooComplex(ParameterizedType parameterizedType);

    InstanceOf newInstanceOf(ParameterizedType parameterizedType, Expression expression, LocalVariable patternVariable);

    IntConstant newInt(int i);

    Lambda.Builder newLambdaBuilder();

    LocalVariable newLocalVariable(String name, ParameterizedType parameterizedType);

    LocalVariable newLocalVariable(String name, ParameterizedType parameterizedType, Expression assignmentExpression);

    LocalVariableCreation newLocalVariableCreation(LocalVariable lvc);

    LocalVariableCreation.Builder newLocalVariableCreationBuilder();

    LongConstant newLong(long l);

    MethodInfo newMethod(TypeInfo owner, String name, MethodInfo.MethodType methodType);

    MethodCall newMethodCall(Expression object, MethodInfo methodInfo, List<Expression> parameterExpressions);

    MethodCall.Builder newMethodCallBuilder();

    MethodCall.Builder newMethodCallBuilder(MethodCall methodCall);

    MethodReference newMethodReference(Expression e, MethodInfo methodInfo, ParameterizedType parameterizedType);

    MethodReference.Builder newMethodReferenceBuilder();

    Expression newMultiExpressions(List<Expression> newExpressions);

    Comment newMultilineComment(String comment);

    ConstructorCall newObjectCreation(Expression scope, MethodInfo constructor, ParameterizedType parameterizedType, Diamond diamond, List<Expression> newParams);

    OutputBuilder newOutputBuilder();

    ParameterizedType newParameterizedType(TypeInfo typeInfo, List<ParameterizedType> newParameters);

    ParameterizedType newParameterizedType(TypeInfo typeInfo, int arrays);

    ParameterizedType newParameterizedType(TypeParameter typeParameter, int index, Wildcard wildCard);

    ParameterizedType newParameterizedType(TypeInfo typeInfo, int arrays, Wildcard wildCard, List<ParameterizedType> parameters);

    Source newParserSource(Element parent, String index, int beginLine, int beginPos, int endLine, int endPos);

    ReturnStatement.Builder newReturnBuilder();

    ReturnStatement newReturnStatement(Expression expression);

    ShortConstant newShort(short s);

    Comment newSingleLineComment(String comment);

    SwitchEntry newStatementsSwitchEntry(VariableExpression selector,
                                         List<Expression> labels, List<Statement> statements);

    StringConstant newStringConstant(String string);

    SwitchExpression newSwitchExpression(VariableExpression selector,
                                         List<SwitchEntry> switchEntries, ParameterizedType parameterizedType,
                                         List<Expression> expressions);

    SynchronizedStatement.Builder newSynchronizedBuilder();

    OutputElement newText(String text);

    This newThis(TypeInfo typeInfo);

    ThrowStatement.Builder newThrowBuilder();

    TranslationMap.Builder newTranslationMapBuilder();

    TranslationMap.Builder newTranslationMapBuilder(TranslationMap startingPoint);

    TryStatement.Builder newTryBuilder();

    TypeExpression newTypeExpression(ParameterizedType parameterizedType, Diamond diamond);

    TypeInfo newTypeInfo(TypeInfo typeInfo, String simpleName);

    TypeInfo newTypeInfo(CompilationUnit cu, String simpleName);

    TypeParameter newTypeParameter(int index, String simpleName, MethodInfo owner);

    TypeParameter newTypeParameter(int index, String simpleName, TypeInfo owner);

    UnaryOperator newUnaryOperator(MethodInfo operator, Expression e, Precedence precedence);

    VariableExpression newVariableExpression(Variable variable);

    VariableExpression.Builder newVariableExpressionBuilder();

    VariableExpression.Suffix newVariableFieldSuffix(int statementTime, String latestAssignment);

    WhileStatement.Builder newWhileBuilder();

    YieldStatement.Builder newYieldBuilder();

    Expression nullConstant();

    Expression nullValue(TypeInfo typeInfo);

    ConstructorCall objectCreation(Expression scope, MethodInfo constructor, ParameterizedType parameterizedType,
                                   Diamond diamond, List<Expression> parameterExpressions);

    Collector<OutputBuilder, OutputBuilder, OutputBuilder> outputBuilderJoining(OutputElement outputElement);

    ParameterizedType parameterizedTypeReturnTypeOfConstructor();

    ParameterizedType parameterizedTypeWildcard();

    Precedence precedenceAdditive();

    Precedence precedenceAnd();

    Precedence precedenceArrayAccess();

    Precedence precedenceAssignment();

    Precedence precedenceEquality();

    Precedence precedenceGreaterThan();

    Precedence precedenceMultiplicative();

    Precedence precedenceOr();

    Precedence precedenceUnary();

    Qualification qualificationDoNotQualifyImplicit();

    Qualification qualificationFullyQualifiedNames();

    Split splitNever();

    OutputElement symbolColon();

    OutputElement symbolComma();

    OutputElement symbolLeftParenthesis();

    OutputElement symbolRightParenthesis();

    TypeModifier typeModifierAbstract();

    TypeModifier typeModifierFinal();

    TypeModifier typeModifierNonSealed();

    TypeModifier typeModifierPrivate();

    TypeModifier typeModifierProtected();

    TypeModifier typeModifierPublic();

    TypeModifier typeModifierSealed();

    TypeModifier typeModifierStatic();

    TypeNature typeNatureAnnotation();

    TypeNature typeNatureClass();

    TypeNature typeNatureEnum();

    TypeNature typeNatureInterface();

    TypeNature typeNatureRecord();

    Wildcard wildcardExtends();

    Wildcard wildcardSuper();

}
