package org.e2immu.language.cst.impl.runtime;

import org.e2immu.language.cst.api.element.*;
import org.e2immu.language.cst.api.expression.*;
import org.e2immu.language.cst.api.info.*;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.OutputElement;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.output.element.ElementarySpace;
import org.e2immu.language.cst.api.output.element.Split;
import org.e2immu.language.cst.api.runtime.Factory;
import org.e2immu.language.cst.api.statement.*;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.impl.element.*;
import org.e2immu.language.cst.impl.expression.*;
import org.e2immu.language.cst.impl.expression.util.PrecedenceEnum;
import org.e2immu.language.cst.impl.info.*;
import org.e2immu.language.cst.impl.output.*;
import org.e2immu.language.cst.impl.statement.*;
import org.e2immu.language.cst.impl.translate.TranslationMapImpl;
import org.e2immu.language.cst.impl.type.*;
import org.e2immu.language.cst.impl.util.IntUtil;
import org.e2immu.language.cst.impl.variable.*;
import org.e2immu.language.cst.api.type.*;
import org.e2immu.language.cst.api.variable.*;
import org.e2immu.support.Either;

import java.util.List;
import java.util.stream.Collector;

public class FactoryImpl extends PredefinedImpl implements Factory {

    private final IntConstant zero;
    private final IntConstant one;
    private final IntConstant minusOne;

    public FactoryImpl() {
        zero = new IntConstantImpl(this, 0);
        one = new IntConstantImpl(this, 1);
        minusOne = new IntConstantImpl(this, -1);
    }

    @Override
    public Assignment newAssignment(VariableExpression target, Expression value) {
        return new AssignmentImpl(target, value);
    }

    @Override
    public Assignment.Builder newAssignmentBuilder() {
        return new AssignmentImpl.Builder();
    }

    @Override
    public Equals newEquals(Expression lhs, Expression rhs) {
        MethodInfo operator = lhs.isNumeric() && rhs.isNumeric() ? equalsOperatorInt()
                : equalsOperatorObject();
        return new EqualsImpl(List.of(), null, operator, PrecedenceEnum.EQUALITY, lhs, rhs, booleanParameterizedType());
    }

    @Override
    public InstanceOf newInstanceOf(Expression expression, ParameterizedType parameterizedType, LocalVariable patternVariable) {
        return new InstanceOfImpl(List.of(), null, expression, parameterizedType, patternVariable,
                booleanParameterizedType());
    }

    @Override
    public BinaryOperator.Builder newBinaryOperatorBuilder() {
        return new BinaryOperatorImpl.Builder();
    }

    @Override
    public ConstructorCall newObjectCreation(Expression scope, MethodInfo constructor, ParameterizedType parameterizedType, Diamond diamond, List<Expression> newParams) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GreaterThanZero newGreaterThanZero(Expression e, boolean allowEquals) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Cast newCast(Expression e, ParameterizedType parameterizedType) {
        return new CastImpl(List.of(), null, parameterizedType, e);
    }

    @Override
    public Cast.Builder newCastBuilder() {
        return new CastImpl.Builder();
    }

    @Override
    public MethodReference newMethodReference(Expression e, MethodInfo methodInfo, ParameterizedType parameterizedType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UnaryOperator newUnaryOperator(MethodInfo operator, Expression e, Precedence precedence) {
        return new UnaryOperatorImpl(operator, e, precedence);
    }

    @Override
    public ArrayInitializer newArrayInitializer(List<Expression> expressions, ParameterizedType commonType) {
        return new ArrayInitializerImpl(expressions, commonType);
    }

    @Override
    public ArrayLength newArrayLength(Expression scope) {
        return new ArrayLengthImpl(this, scope);
    }

    @Override
    public MethodCall newMethodCall(Expression object, MethodInfo methodInfo, List<Expression> parameterExpressions) {
        return new MethodCallImpl.Builder()
                .setObject(object)
                .setMethodInfo(methodInfo)
                .setParameterExpressions(parameterExpressions)
                .build();
    }

    @Override
    public MethodCall.Builder newMethodCallBuilder() {
        return new MethodCallImpl.Builder();
    }

    @Override
    public MethodCall.Builder newMethodCallBuilder(MethodCall methodCall) {
        return new MethodCallImpl.Builder(methodCall);
    }

    @Override
    public TypeExpression newTypeExpression(ParameterizedType parameterizedType, Diamond diamond) {
        return new TypeExpressionImpl(parameterizedType, diamond);
    }

    @Override
    public ConstructorCall.Builder newConstructorCallBuilder() {
        return new ConstructorCallImpl.Builder();
    }

    @Override
    public IfElseStatement.Builder newIfElseBuilder() {
        return new IfElseStatementImpl.Builder();
    }

    @Override
    public ExpressionAsStatement newExpressionAsStatement(Expression expression) {
        return new ExpressionAsStatementImpl(expression);
    }

    @Override
    public ExpressionAsStatement.Builder newExpressionAsStatementBuilder() {
        return new ExpressionAsStatementImpl.Builder();
    }

    @Override
    public ThrowStatement.Builder newThrowBuilder() {
        return new ThrowStatementImpl.Builder();
    }

    @Override
    public AssertStatement.Builder newAssertBuilder() {
        return new AssertStatementImpl.Builder();
    }

    @Override
    public ReturnStatement newReturnStatement(Expression expression) {
        return new ReturnStatementImpl(expression);
    }

    @Override
    public ReturnStatement.Builder newReturnBuilder() {
        return new ReturnStatementImpl.Builder();
    }

    @Override
    public Block.Builder newBlockBuilder() {
        return new BlockImpl.Builder();
    }

    @Override
    public Block emptyBlock() {
        return new BlockImpl();
    }

    @Override
    public VariableExpression newVariableExpression(Variable variable) {
        return new VariableExpressionImpl(variable);
    }

    @Override
    public StringConstant newStringConstant(String string) {
        return new StringConstantImpl(this, string);
    }

    @Override
    public ConstructorCall objectCreation(Expression scope, MethodInfo constructor, ParameterizedType parameterizedType, Diamond diamond, List<Expression> parameterExpressions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeInfo newTypeInfo(TypeInfo typeInfo, String capitalized) {
        return new TypeInfoImpl(typeInfo, capitalized);
    }

    @Override
    public TypeInfo newTypeInfo(CompilationUnit cu, String simpleName) {
        return new TypeInfoImpl(cu, simpleName);
    }

    @Override
    public ParameterizedType newParameterizedType(TypeInfo typeInfo, List<ParameterizedType> newParameters) {
        return new ParameterizedTypeImpl(typeInfo, null, newParameters, 0, null);
    }

    @Override
    public ParameterizedType newParameterizedType(TypeInfo typeInfo, int arrays) {
        return new ParameterizedTypeImpl(typeInfo, null, List.of(), arrays, null);
    }

    @Override
    public ParameterizedType newParameterizedType(TypeParameter typeParameter, int arrays, Wildcard wildCard) {
        return new ParameterizedTypeImpl(null, typeParameter, List.of(), arrays, wildCard);
    }

    @Override
    public ParameterizedType newParameterizedType(TypeInfo typeInfo,
                                                  int arrays,
                                                  Wildcard wildCard,
                                                  List<ParameterizedType> parameters) {
        return new ParameterizedTypeImpl(typeInfo, null, parameters, arrays, wildCard);
    }

    @Override
    public TypeParameter newTypeParameter(int index, String simpleName, Info owner, List<AnnotationExpression> annotations) {
        if (owner instanceof TypeInfo typeInfo) {
            return new TypeParameterImpl(index, simpleName, Either.left(typeInfo), annotations);
        } else if (owner instanceof MethodInfo methodInfo) {
            return new TypeParameterImpl(index, simpleName, Either.right(methodInfo), annotations);
        } else throw new UnsupportedOperationException();
    }


    @Override
    public ParameterizedType parameterizedTypeWildcard() {
        return ParameterizedTypeImpl.WILDCARD_PARAMETERIZED_TYPE;
    }

    @Override
    public ParameterizedType parameterizedTypeReturnTypeOfConstructor() {
        return ParameterizedTypeImpl.RETURN_TYPE_OF_CONSTRUCTOR;
    }

    @Override
    public ParameterizedType parameterizedTypeNullConstant() {
        return ParameterizedTypeImpl.NULL_CONSTANT;
    }

    @Override
    public Diamond diamondYes() {
        return DiamondEnum.YES;
    }

    @Override
    public ElementarySpace elementarySpaceNice() {
        return ElementarySpaceEnum.NICE;
    }

    @Override
    public ElementarySpace elementarySpaceRelaxedNone() {
        return ElementarySpaceEnum.RELAXED_NONE;
    }

    @Override
    public Diamond diamondShowAll() {
        return DiamondEnum.SHOW_ALL;
    }

    @Override
    public Diamond diamondNo() {
        return DiamondEnum.NO;
    }

    @Override
    public InlineConditional newInlineConditional(Expression condition, Expression ifTrue, Expression ifFalse) {
        return new InlineConditionalImpl(List.of(), null, condition, ifTrue, ifFalse,
                commonType(ifTrue.parameterizedType(), ifFalse.parameterizedType()));
    }

    @Override
    public InlineConditional.Builder newInlineConditionalBuilder() {
        return new InlineConditionalImpl.Builder();
    }

    @Override
    public SwitchExpression newSwitchExpression(VariableExpression selector, List<SwitchEntry> switchEntries, ParameterizedType parameterizedType, List<Expression> expressions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SwitchEntry newStatementsSwitchEntry(VariableExpression selector, List<Expression> labels, List<Statement> statements) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CharConstant newChar(char c) {
        return new CharConstantImpl(this, c);
    }

    @Override
    public This newThis(TypeInfo typeInfo, TypeInfo explicitlyWriteType, boolean writeSuper) {
        return new ThisImpl(typeInfo, explicitlyWriteType, writeSuper);
    }

    @Override
    public DependentVariable newDependentVariable(Expression array, Expression index) {
        return DependentVariableImpl.create(array, index);
    }

    @Override
    public Expression newMultiExpressions(List<Expression> expressions) {
        return new CommaExpressionImpl(List.of(), null, expressions);
    }

    @Override
    public BooleanConstant newBoolean(boolean value) {
        return new BooleanConstantImpl(this, value);
    }

    @Override
    public IntConstant intZero() {
        return zero;
    }

    @Override
    public IntConstant intOne() {
        return one;
    }

    @Override
    public IntConstant intMinusOne() {
        return minusOne;
    }

    @Override
    public Instance newInstanceForTooComplex(ParameterizedType parameterizedType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ParameterizedType commonType(ParameterizedType pt1, ParameterizedType pt2) {
        return new CommonType(this).commonType(pt1, pt2);
    }

    @Override
    public Precedence precedenceUnary() {
        return PrecedenceEnum.UNARY;
    }

    @Override
    public Precedence precedenceEquality() {
        return PrecedenceEnum.EQUALITY;
    }

    @Override
    public Expression nullValue(TypeInfo typeInfo) {
        if (typeInfo != null) {
            if (typeInfo.isBoolean()) return newBoolean(false);
            if (typeInfo.isInt()) return zero;
            if (typeInfo.isLong()) return newLong(0L);
            if (typeInfo.isShort()) return newShort((short) 0);
            if (typeInfo.isByte()) return newByte((byte) 0);
            if (typeInfo.isFloat()) return newFloat(0);
            if (typeInfo.isDouble()) return newDouble(0);
            if (typeInfo.isChar()) return newChar('\0');
        }
        return nullConstant();
    }

    @Override
    public Precedence precedenceGreaterThan() {
        return PrecedenceEnum.EQUALITY;
    }

    @Override
    public Precedence precedenceAnd() {
        return PrecedenceEnum.AND;
    }

    @Override
    public Precedence precedenceArrayAccess() {
        return PrecedenceEnum.ARRAY_ACCESS;
    }

    @Override
    public Precedence precedenceOr() {
        return PrecedenceEnum.OR;
    }

    @Override
    public Precedence precedenceAssignment() {
        return PrecedenceEnum.ASSIGNMENT;
    }

    @Override
    public Precedence precedenceMultiplicative() {
        return PrecedenceEnum.MULTIPLICATIVE;
    }

    @Override
    public Precedence precedenceAdditive() {
        return PrecedenceEnum.ADDITIVE;
    }

    @Override
    public IntConstant newInt(int i) {
        return new IntConstantImpl(this, i);
    }

    @Override
    public LongConstant newLong(long l) {
        return new LongConstantImpl(this, l);
    }

    @Override
    public ShortConstant newShort(short s) {
        return new ShortConstantImpl(this, s);
    }

    @Override
    public ByteConstant newByte(byte b) {
        return new ByteConstantImpl(this, b);
    }

    @Override
    public FloatConstant newFloat(float f) {
        return new FloatConstantImpl(this, f);
    }

    @Override
    public DoubleConstant newDouble(double d) {
        return new DoubleConstantImpl(this, d);
    }

    @Override
    public Numeric intOrDouble(double v) {
        if (IntUtil.isMathematicalInteger(v)) {
            long l = Math.round(v);
            if (l > Integer.MAX_VALUE || l < Integer.MIN_VALUE) {
                return newLong(l);
            }
            return newInt((int) l);
        }
        return newDouble(v);
    }

    @Override
    public Expression nullConstant() {
        return new NullConstantImpl(parameterizedTypeNullConstant());
    }

    @Override
    public LocalVariable newLocalVariable(String name, ParameterizedType parameterizedType) {
        return new LocalVariableImpl(name, parameterizedType, null);
    }

    @Override
    public LocalVariable newLocalVariable(String name,
                                          ParameterizedType parameterizedType,
                                          Expression assignmentExpression) {
        return new LocalVariableImpl(name, parameterizedType, assignmentExpression);
    }

    @Override
    public EmptyExpression newEmptyExpression() {
        return new EmptyExpressionImpl(this, EmptyExpressionImpl.EMPTY_EXPRESSION);
    }

    @Override
    public EmptyExpression newEmptyExpression(String msg) {
        return new EmptyExpressionImpl(this, msg);
    }

    @Override
    public LocalVariableCreation newLocalVariableCreation(LocalVariable localVariable) {
        return new LocalVariableCreationImpl(localVariable);
    }

    @Override
    public LocalVariableCreation.Builder newLocalVariableCreationBuilder() {
        return new LocalVariableCreationImpl.Builder();
    }

    @Override
    public TranslationMap.Builder newTranslationMapBuilder() {
        return new TranslationMapImpl.Builder();
    }

    @Override
    public TranslationMap.Builder newTranslationMapBuilder(TranslationMap startingPoint) {
        return new TranslationMapImpl.Builder(startingPoint);
    }

    @Override
    public TryStatement.Builder newTryBuilder() {
        return new TryStatementImpl.Builder();
    }

    @Override
    public TryStatement.CatchClause.Builder newCatchClauseBuilder() {
        return new TryStatementImpl.CatchClauseImpl.Builder();
    }

    @Override
    public Lambda.Builder newLambdaBuilder() {
        return new LambdaImpl.Builder();
    }

    @Override
    public Lambda.OutputVariant lambdaOutputVariantEmpty() {
        return LambdaImpl.OutputVariantImpl.EMPTY;
    }

    @Override
    public Lambda.OutputVariant lambdaOutputVariantTyped() {
        return LambdaImpl.OutputVariantImpl.TYPED;
    }

    @Override
    public Lambda.OutputVariant lambdaOutputVariantVar() {
        return LambdaImpl.OutputVariantImpl.VAR;
    }

    @Override
    public FieldReference newFieldReference(FieldInfo fieldInfo) {
        return new FieldReferenceImpl(fieldInfo);
    }

    @Override
    public FieldReference newFieldReference(FieldInfo fieldInfo, Expression scope, ParameterizedType concreteReturnType) {
        return new FieldReferenceImpl(fieldInfo, scope, null, concreteReturnType);
    }

    @Override
    public MethodInfo newConstructor(TypeInfo owner) {
        return new MethodInfoImpl(owner);
    }

    @Override
    public MethodInfo newConstructor(TypeInfo owner, MethodInfo.MethodType methodType) {
        return new MethodInfoImpl(owner, methodType);
    }

    @Override
    public MethodInfo newMethod(TypeInfo owner, String name, MethodInfo.MethodType methodType) {
        return new MethodInfoImpl(methodType, name, owner);
    }

    @Override
    public MethodInfo.MethodType methodTypeMethod() {
        return MethodInfoImpl.MethodTypeEnum.METHOD;
    }

    @Override
    public MethodInfo.MethodType methodTypeStaticMethod() {
        return MethodInfoImpl.MethodTypeEnum.STATIC_METHOD;
    }

    @Override
    public Comment newSingleLineComment(String comment) {
        return new SingleLineComment(comment);
    }

    @Override
    public Comment newMultilineComment(String comment) {
        return new MultiLineComment(comment);
    }

    @Override
    public Source newParserSource(Element parent, String index, int beginLine, int beginPos, int endLine, int endPos) {
        return new SourceImpl(parent, index, beginLine, beginPos, endLine, endPos);
    }

    @Override
    public CompilationUnit.Builder newCompilationUnitBuilder() {
        return new CompilationUnitImpl.Builder();
    }

    @Override
    public Wildcard wildcardExtends() {
        return WildcardEnum.EXTENDS;
    }

    @Override
    public Wildcard wildcardSuper() {
        return WildcardEnum.SUPER;
    }

    @Override
    public AnnotationExpression.Builder newAnnotationExpressionBuilder() {
        return new AnnotationExpressionImpl.Builder();
    }

    @Override
    public MethodInfo.MethodType methodTypeAbstractMethod() {
        return MethodInfoImpl.MethodTypeEnum.ABSTRACT_METHOD;
    }

    @Override
    public MethodInfo.MethodType methodTypeCompactConstructor() {
        return MethodInfoImpl.MethodTypeEnum.COMPACT_CONSTRUCTOR;
    }

    @Override
    public MethodInfo.MethodType methodTypeDefaultMethod() {
        return MethodInfoImpl.MethodTypeEnum.DEFAULT_METHOD;
    }

    @Override
    public EnclosedExpression newEnclosedExpression(Expression inner) {
        return new EnclosedExpressionImpl(inner);
    }

    @Override
    public FieldModifier fieldModifierFinal() {
        return FieldModifierEnum.FINAL;
    }

    @Override
    public FieldModifier fieldModifierPublic() {
        return FieldModifierEnum.PUBLIC;
    }

    @Override
    public FieldModifier fieldModifierStatic() {
        return FieldModifierEnum.STATIC;
    }

    @Override
    public FieldModifier fieldModifierTransient() {
        return FieldModifierEnum.TRANSIENT;
    }

    @Override
    public FieldModifier fieldModifierPrivate() {
        return FieldModifierEnum.PRIVATE;
    }

    @Override
    public FieldModifier fieldModifierProtected() {
        return FieldModifierEnum.PROTECTED;
    }

    @Override
    public FieldModifier fieldModifierVolatile() {
        return FieldModifierEnum.VOLATILE;
    }

    @Override
    public Access accessPackage() {
        return InspectionImpl.AccessEnum.PACKAGE;
    }

    @Override
    public Access accessPrivate() {
        return InspectionImpl.AccessEnum.PRIVATE;
    }

    @Override
    public Access accessProtected() {
        return InspectionImpl.AccessEnum.PROTECTED;
    }

    @Override
    public Access accessPublic() {
        return InspectionImpl.AccessEnum.PUBLIC;
    }

    @Override
    public FieldInfo newFieldInfo(String name, boolean isStatic, ParameterizedType parameterizedType, TypeInfo owner) {
        return new FieldInfoImpl(name, isStatic, parameterizedType, owner);
    }

    @Override
    public TypeNature typeNatureAnnotation() {
        return TypeNatureEnum.ANNOTATION;
    }

    @Override
    public TypeNature typeNatureClass() {
        return TypeNatureEnum.CLASS;
    }

    @Override
    public TypeNature typeNatureEnum() {
        return TypeNatureEnum.ENUM;
    }

    @Override
    public TypeNature typeNatureInterface() {
        return TypeNatureEnum.INTERFACE;
    }

    @Override
    public TypeNature typeNatureRecord() {
        return TypeNatureEnum.RECORD;
    }

    @Override
    public TypeModifier typeModifierAbstract() {
        return TypeModifierEnum.ABSTRACT;
    }

    @Override
    public TypeModifier typeModifierFinal() {
        return TypeModifierEnum.FINAL;
    }

    @Override
    public TypeModifier typeModifierNonSealed() {
        return TypeModifierEnum.NON_SEALED;
    }

    @Override
    public TypeModifier typeModifierPrivate() {
        return TypeModifierEnum.PRIVATE;
    }

    @Override
    public TypeModifier typeModifierProtected() {
        return TypeModifierEnum.PROTECTED;
    }

    @Override
    public TypeModifier typeModifierPublic() {
        return TypeModifierEnum.PUBLIC;
    }

    @Override
    public TypeModifier typeModifierSealed() {
        return TypeModifierEnum.SEALED;
    }

    @Override
    public TypeModifier typeModifierStatic() {
        return TypeModifierEnum.STATIC;
    }

    @Override
    public MethodModifier methodModifierAbstract() {
        return MethodModifierEnum.ABSTRACT;
    }

    @Override
    public MethodModifier methodModifierDefault() {
        return MethodModifierEnum.DEFAULT;
    }

    @Override
    public MethodModifier methodModifierFinal() {
        return MethodModifierEnum.FINAL;
    }

    @Override
    public MethodModifier methodModifierPrivate() {
        return MethodModifierEnum.PRIVATE;
    }

    @Override
    public MethodModifier methodModifierProtected() {
        return MethodModifierEnum.PROTECTED;
    }

    @Override
    public MethodModifier methodModifierPublic() {
        return MethodModifierEnum.PUBLIC;
    }

    @Override
    public MethodModifier methodModifierStatic() {
        return MethodModifierEnum.STATIC;
    }

    @Override
    public MethodModifier methodModifierSynchronized() {
        return MethodModifierEnum.SYNCHRONIZED;
    }

    @Override
    public MethodInfo.MethodType methodTypeConstructor() {
        return MethodInfoImpl.MethodTypeEnum.CONSTRUCTOR;
    }

    @Override
    public ForEachStatement.Builder newForEachBuilder() {
        return new ForEachStatementImpl.Builder();
    }

    @Override
    public WhileStatement.Builder newWhileBuilder() {
        return new WhileStatementImpl.Builder();
    }

    @Override
    public YieldStatement.Builder newYieldBuilder() {
        return new YieldStatementImpl.Builder();
    }

    @Override
    public DoStatement.Builder newDoBuilder() {
        return new DoStatementImpl.Builder();
    }

    @Override
    public VariableExpression.Builder newVariableExpressionBuilder() {
        return new VariableExpressionImpl.Builder();
    }

    @Override
    public SynchronizedStatement.Builder newSynchronizedBuilder() {
        return new SynchronizedStatementImpl.Builder();
    }

    @Override
    public MethodInfo.MethodType methodTypeSyntheticConstructor() {
        return MethodInfoImpl.MethodTypeEnum.SYNTHETIC_CONSTRUCTOR;
    }

    @Override
    public MethodReference.Builder newMethodReferenceBuilder() {
        return new MethodReferenceImpl.Builder();
    }

    @Override
    public ImportStatement newImportStatement(String importString, boolean isStatic) {
        return new ImportStatementImpl(importString, isStatic);
    }

    @Override
    public TypeInfo newAnonymousType(TypeInfo enclosingType, int index) {
        return new TypeInfoImpl(enclosingType, index);
    }

    @Override
    public CommaExpression.Builder newCommaBuilder() {
        return new CommaExpressionImpl.Builder();
    }

    @Override
    public MethodInfo newArrayCreationConstructor(ParameterizedType type) {
        MethodInfo mi = newMethod(type.typeInfo(), "<init>", methodTypeSyntheticConstructor());
        mi.builder().setReturnType(type).addMethodModifier(methodModifierPublic()).computeAccess();
        for (int i = 0; i < type.arrays(); i++) {
            mi.builder().addParameter("dim" + i, intParameterizedType());
        }
        mi.builder().commitParameters();
        mi.builder().commit();
        return mi;
    }

    @Override
    public OutputBuilder newOutputBuilder() {
        return new OutputBuilderImpl();
    }

    @Override
    public OutputElement symbolComma() {
        return SymbolEnum.COMMA;
    }

    @Override
    public OutputElement symbolColon() {
        return SymbolEnum.COLON;
    }

    @Override
    public Qualification qualificationFullyQualifiedNames() {
        return QualificationImpl.FULLY_QUALIFIED_NAMES;
    }

    @Override
    public Qualification qualificationDoNotQualifyImplicit() {
        return new QualificationImpl(true, TypeNameImpl.Required.FQN);
    }

    @Override
    public Split splitNever() {
        return SplitEnum.NEVER;
    }

    @Override
    public OutputElement symbolLeftParenthesis() {
        return SymbolEnum.LEFT_PARENTHESIS;
    }

    @Override
    public OutputElement symbolRightParenthesis() {
        return SymbolEnum.RIGHT_PARENTHESIS;
    }

    @Override
    public OutputElement newText(String text) {
        return new TextImpl(text);
    }

    @Override
    public Collector<OutputBuilder, OutputBuilder, OutputBuilder> outputBuilderJoining(OutputElement outputElement) {
        return OutputBuilderImpl.joining(outputElement);
    }

    @Override
    public VariableExpression.Suffix newVariableFieldSuffix(int statementTime, String latestAssignment) {
        return new VariableExpressionImpl.VariableFieldSuffix(statementTime, latestAssignment);
    }

    @Override
    public ExplicitConstructorInvocation.Builder newExplicitConstructorInvocationBuilder() {
        return new ExplicitConstructorInvocationImpl.Builder();
    }

    @Override
    public BreakStatement.Builder newBreakBuilder() {
        return new BreakStatementImpl.Builder();
    }

    @Override
    public ContinueStatement.Builder newContinueBuilder() {
        return new ContinueStatementImpl.Builder();
    }

    @Override
    public ForStatement.Builder newForBuilder() {
        return new ForStatementImpl.Builder();
    }

    @Override
    public DescendMode descendModeNo() {
        return DescendModeEnum.NO;
    }

    @Override
    public DescendMode descendModeYes() {
        return DescendModeEnum.YES;
    }

    @Override
    public Expression newStringConcat(Expression l, Expression r) {
        return new StringConcatImpl(this, l, r);
    }

    @Override
    public int isAssignableFromCovariantErasure(ParameterizedType target, ParameterizedType from) {
        return new IsAssignableFrom(this, target, from)
                .execute(false, IsAssignableFrom.Mode.COVARIANT_ERASURE);
    }

    @Override
    public int isNotAssignable() {
        return IsAssignableFrom.NOT_ASSIGNABLE;
    }

    @Override
    public Precedence precedenceTop() {
        return PrecedenceEnum.TOP;
    }

    @Override
    public Precedence precedenceBottom() {
        return PrecedenceEnum.BOTTOM;
    }

    @Override
    public Source newCompiledClassSource(CompilationUnit compilationUnit) {
        return new SourceImpl(compilationUnit, "", 0, 0, 0, 0);
    }

    @Override
    public ClassExpression newClassExpression(TypeInfo typeInfo) {
        ParameterizedType pt = newParameterizedType(typeInfo, 0);
        ParameterizedType classPt = newParameterizedType(classTypeInfo(), List.of(pt));
        return new ClassExpressionImpl(pt, classPt);
    }

    @Override
    public SwitchStatementOldStyle.Builder newSwitchStatementOldStyleBuilder() {
        return new SwitchStatementOldStyleImpl.Builder();
    }

    @Override
    public SwitchStatementOldStyle.SwitchLabel newSwitchLabelOldStyle(Expression literal, int pos,
                                                                      LocalVariable patternVariable,
                                                                      Expression whenExpression) {
        return new SwitchStatementOldStyleImpl.SwitchLabelImpl(literal, pos, patternVariable, whenExpression);
    }

    @Override
    public SwitchStatementNewStyle.Builder newSwitchStatementNewStyleBuilder() {
        return new SwitchStatementNewStyleImpl.BuilderImpl();
    }

    @Override
    public SwitchStatementNewStyle.EntryBuilder newSwitchStatementNewStyleEntryBuilder() {
        return new SwitchStatementNewStyleImpl.EntryBuilderImpl();
    }
}

