package org.e2immu.language.cst.api.runtime;

import org.e2immu.language.cst.api.element.*;
import org.e2immu.language.cst.api.expression.*;
import org.e2immu.language.cst.api.info.*;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.OutputElement;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.output.element.ElementarySpace;
import org.e2immu.language.cst.api.output.element.Split;
import org.e2immu.language.cst.api.statement.*;
import org.e2immu.language.cst.api.statement.SwitchEntry;
import org.e2immu.language.cst.api.translate.TranslationMap;
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

    int isAssignableFromCovariantErasure(ParameterizedType typeOfParameter, ParameterizedType actualType);

    int isNotAssignable();

    Lambda.OutputVariant lambdaOutputVariantEmpty();

    Lambda.OutputVariant lambdaOutputVariantTyped();

    Lambda.OutputVariant lambdaOutputVariantVar();

    LocalVariableCreation.Modifier localVariableModifierFinal();

    LocalVariableCreation.Modifier localVariableModifierVar();

    MethodModifier methodModifierAbstract();

    MethodModifier methodModifierDefault();

    MethodModifier methodModifierFinal();

    MethodModifier methodModifierPrivate();

    MethodModifier methodModifierProtected();

    MethodModifier methodModifierPublic();

    MethodModifier methodModifierStatic();

    MethodModifier methodModifierSynchronized();

    MethodInfo.MethodType methodTypeAbstractMethod();

    MethodInfo.MethodType methodTypeCompactConstructor();

    MethodInfo.MethodType methodTypeConstructor();

    MethodInfo.MethodType methodTypeDefaultMethod();

    MethodInfo.MethodType methodTypeMethod();

    MethodInfo.MethodType methodTypeStaticBlock();

    MethodInfo.MethodType methodTypeStaticMethod();

    MethodInfo.MethodType methodTypeSyntheticConstructor();

    AnnotationExpression.Builder newAnnotationExpressionBuilder();

    And.Builder newAndBuilder();

    TypeInfo newAnonymousType(TypeInfo enclosingType, int index);

    MethodInfo newArrayCreationConstructor(ParameterizedType returnType);

    ArrayInitializer.Builder newArrayInitializerBuilder();

    ArrayLength.Builder newArrayLengthBuilder();

    AssertStatement.Builder newAssertBuilder();

    Assignment newAssignment(VariableExpression target, Expression value);

    Assignment.Builder newAssignmentBuilder();

    BinaryOperator.Builder newBinaryOperatorBuilder();

    BitwiseNegation newBitwiseNegation(Expression value);

    Block.Builder newBlockBuilder();

    BooleanConstant newBoolean(boolean value);

    BreakStatement.Builder newBreakBuilder();

    ByteConstant newByte(byte b);

    Cast newCast(Expression e, ParameterizedType parameterizedType);

    Cast.Builder newCastBuilder();

    TryStatement.CatchClause.Builder newCatchClauseBuilder();

    CharConstant newChar(char c);

    ClassExpression newClassExpression(TypeInfo typeInfo);

    CommaExpression.Builder newCommaBuilder();

    CompilationUnit.Builder newCompilationUnitBuilder();

    Precedence precedenceOfBinaryOperator(MethodInfo op);

    Source newCompiledClassSource(CompilationUnit compilationUnit);

    MethodInfo newConstructor(TypeInfo owner);

    MethodInfo newConstructor(TypeInfo owner, MethodInfo.MethodType methodType);

    ConstructorCall.Builder newConstructorCallBuilder();

    ContinueStatement.Builder newContinueBuilder();

    DependentVariable newDependentVariable(Expression array, Expression index);

    // Direct access, useful for synthetic constructs. Preferably use other method.
    DependentVariable newDependentVariable(Variable arrayVariable, ParameterizedType parameterizedType, Variable indexVariable);

    DoStatement.Builder newDoBuilder();

    DoubleConstant newDouble(double d);

    EmptyExpression newEmptyExpression();

    EmptyExpression newEmptyExpression(String msg);

    EmptyStatement.Builder newEmptyStatementBuilder();

    EnclosedExpression.Builder newEnclosedExpressionBuilder();

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

    ImportStatement newImportStatement(String importString, boolean isStatic);

    InlineConditional newInlineConditional(Expression condition, Expression ifTrue, Expression ifFalse);

    InlineConditional.Builder newInlineConditionalBuilder();

    InstanceOf.Builder newInstanceOfBuilder();

    IntConstant newInt(int i);

    Lambda.Builder newLambdaBuilder();

    LocalVariable newLocalVariable(String name, ParameterizedType parameterizedType);

    LocalVariable newLocalVariable(String name, ParameterizedType parameterizedType, Expression assignmentExpression);

    LocalVariableCreation newLocalVariableCreation(LocalVariable lvc);

    LocalVariableCreation.Builder newLocalVariableCreationBuilder();

    LongConstant newLong(long l);

    MethodInfo newMethod(TypeInfo owner, String name, MethodInfo.MethodType methodType);

    MethodCall.Builder newMethodCallBuilder();

    MethodCall.Builder newMethodCallBuilder(MethodCall methodCall);

    MethodReference.Builder newMethodReferenceBuilder();

    DependentVariable newDependentVariable(Variable arrayVariable,
                                           ParameterizedType parameterizedType,
                                           Expression indexExpression);

    Expression newMultiExpressions(List<Expression> newExpressions);

    Comment newMultilineComment(String comment);

    Or.Builder newOrBuilder();

    MethodInfo.MethodType methodTypeSyntheticArrayConstructor();

    OutputBuilder newOutputBuilder();

    ParameterizedType newParameterizedType(TypeInfo typeInfo, List<ParameterizedType> newParameters);

    ParameterizedType newParameterizedType(TypeInfo typeInfo, int arrays);

    ParameterizedType newParameterizedType(TypeParameter typeParameter, int arrays, Wildcard wildCard);

    ParameterizedType newParameterizedType(TypeInfo typeInfo, int arrays, Wildcard wildCard, List<ParameterizedType> parameters);

    Source newParserSource(Element parent, String index, int beginLine, int beginPos, int endLine, int endPos);

    ReturnStatement.Builder newReturnBuilder();

    ReturnStatement newReturnStatement(Expression expression);

    ShortConstant newShort(short s);

    Comment newSingleLineComment(String comment);

    SwitchEntry newStatementsSwitchEntry(VariableExpression selector,
                                         List<Expression> labels, List<Statement> statements);

    Expression newStringConcat(Expression l, Expression r);

    StringConstant newStringConstant(String string);

    SwitchEntry.Builder newSwitchEntryBuilder();

    SwitchExpression.Builder newSwitchExpressionBuilder();

    SwitchStatementOldStyle.SwitchLabel newSwitchLabelOldStyle(Expression literal, int pos,
                                                               LocalVariable patternVariable,
                                                               Expression whenExpression);

    SwitchStatementNewStyle.Builder newSwitchStatementNewStyleBuilder();

    SwitchStatementOldStyle.Builder newSwitchStatementOldStyleBuilder();

    SynchronizedStatement.Builder newSynchronizedBuilder();

    OutputElement newText(String text);

    default This newThis(ParameterizedType parameterizedType) {
        return newThis(parameterizedType, null, false);
    }

    This newThis(ParameterizedType parameterizedType, TypeInfo explicitlyWriteType, boolean writeSuper);

    ThrowStatement.Builder newThrowBuilder();

    TranslationMap.Builder newTranslationMapBuilder();

    TranslationMap.Builder newTranslationMapBuilder(TranslationMap startingPoint);

    TryStatement.Builder newTryBuilder();

    TypeExpression newTypeExpression(ParameterizedType parameterizedType, Diamond diamond);

    TypeInfo newTypeInfo(TypeInfo typeInfo, String simpleName);

    TypeInfo newTypeInfo(CompilationUnit cu, String simpleName);

    default TypeParameter newTypeParameter(int index, String simpleName, Info owner) {
        return newTypeParameter(index, simpleName, owner, List.of());
    }

    TypeParameter newTypeParameter(int index, String simpleName, Info owner, List<AnnotationExpression> annotations);

    UnaryOperator newUnaryOperator(MethodInfo operator, Expression e, Precedence precedence);

    VariableExpression newVariableExpression(Variable variable);

    VariableExpression.Builder newVariableExpressionBuilder();

    VariableExpression.Suffix newVariableFieldSuffix(int statementTime, String latestAssignment);

    WhileStatement.Builder newWhileBuilder();

    YieldStatement.Builder newYieldBuilder();

    Expression notNull(Expression expression);

    Expression nullConstant();

    Expression nullValue(ParameterizedType parameterizedType);

    Collector<OutputBuilder, OutputBuilder, OutputBuilder> outputBuilderJoining(OutputElement outputElement);

    ParameterizedType parameterizedTypeNullConstant();

    ParameterizedType parameterizedTypeReturnTypeOfConstructor();

    ParameterizedType parameterizedTypeWildcard();

    Precedence precedenceAdditive();

    Precedence precedenceBitwiseAnd();

    Precedence precedenceArrayAccess();

    Precedence precedenceAssignment();

    Precedence precedenceBottom();

    Precedence precedenceEquality();

    Precedence precedenceRelational();

    Precedence precedenceMultiplicative();

    Precedence precedenceBitwiseOr();

    Precedence precedenceShift();

    Precedence precedenceTop();

    Precedence precedenceUnary();

    Precedence precedenceBitwiseXor();

    Precedence precedenceLogicalAnd();

    Precedence precedenceLogicalOr();

    Qualification qualificationFullyQualifiedNames();

    Qualification qualificationQualifyFromPrimaryType();

    Qualification qualificationQualifyFromPrimaryType(Qualification.Decorator decorator);

    Qualification qualificationSimpleNames();

    void setGetSetField(MethodInfo getSetMethod, FieldInfo fieldInfo, boolean setter, int parameterIndexOfIndex);

    void setModificationComponent(MethodInfo methodInfo, FieldInfo component);

    Split splitNever();

    OutputElement symbolColon();

    OutputElement symbolComma();

    OutputElement symbolLeftParenthesis();

    OutputElement symbolRightParenthesis();

    Variable translateVariableRecursively(TranslationMap translationMap, Variable variable);

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
