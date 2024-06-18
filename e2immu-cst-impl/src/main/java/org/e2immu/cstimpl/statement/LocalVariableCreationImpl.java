package org.e2immu.cstimpl.statement;

import org.e2immu.cstapi.element.Comment;
import org.e2immu.cstapi.element.Element;
import org.e2immu.cstapi.element.Source;
import org.e2immu.cstapi.element.Visitor;
import org.e2immu.cstapi.expression.AnnotationExpression;
import org.e2immu.cstapi.info.TypeInfo;
import org.e2immu.cstapi.output.OutputBuilder;
import org.e2immu.cstapi.output.Qualification;
import org.e2immu.cstapi.statement.LocalVariableCreation;
import org.e2immu.cstapi.variable.DescendMode;
import org.e2immu.cstapi.variable.LocalVariable;
import org.e2immu.cstapi.variable.Variable;
import org.e2immu.cstimpl.element.ElementImpl;
import org.e2immu.cstimpl.output.*;
import org.e2immu.cstimpl.type.DiamondEnum;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class LocalVariableCreationImpl extends StatementImpl implements LocalVariableCreation {

    private final LocalVariable localVariable;
    private final List<LocalVariable> otherLocalVariables;
    private final Set<Modifier> modifiers;

    public enum ModifierEnum implements Modifier {
        FiNAL, VAR;

        @Override
        public boolean isFinal() {
            return this == FiNAL;
        }

        @Override
        public boolean isWithoutTypeSpecification() {
            return this == VAR;
        }

    }

    public LocalVariableCreationImpl(LocalVariable localVariable) {
        this.localVariable = localVariable;
        this.modifiers = Set.of();
        this.otherLocalVariables = List.of();
    }

    public LocalVariableCreationImpl(List<Comment> comments,
                                     Source source,
                                     List<AnnotationExpression> annotations,
                                     String label,
                                     LocalVariable localVariable,
                                     List<LocalVariable> otherLocalVariables,
                                     Set<Modifier> modifiers) {
        super(comments, source, annotations, 0, label);
        assert localVariable.assignmentExpression() != null;
        this.localVariable = localVariable;
        assert otherLocalVariables.stream()
                .allMatch(lv -> lv.assignmentExpression() != null
                                && lv.parameterizedType().equals(localVariable.parameterizedType()));
        this.otherLocalVariables = otherLocalVariables;
        this.modifiers = modifiers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocalVariableCreationImpl that)) return false;
        return Objects.equals(localVariable, that.localVariable)
               && Objects.equals(otherLocalVariables, that.otherLocalVariables)
               && Objects.equals(modifiers, that.modifiers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localVariable, otherLocalVariables, modifiers);
    }

    public static class Builder extends StatementImpl.Builder<LocalVariableCreation.Builder>
            implements LocalVariableCreation.Builder {
        private final Set<Modifier> modifiers = new HashSet<>();
        private LocalVariable localVariable;
        private final List<LocalVariable> otherLocalVariables = new ArrayList<>();

        @Override
        public LocalVariableCreation.Builder addModifier(Modifier modifier) {
            modifiers.add(modifier);
            return this;
        }

        @Override
        public LocalVariableCreation.Builder setLocalVariable(LocalVariable localVariable) {
            this.localVariable = localVariable;
            return this;
        }

        @Override
        public LocalVariableCreation.Builder addOtherLocalVariable(LocalVariable localVariable) {
            otherLocalVariables.add(localVariable);
            return this;
        }

        @Override
        public LocalVariableCreation build() {
            return new LocalVariableCreationImpl(comments, source, annotations, label, localVariable,
                    List.copyOf(otherLocalVariables), Set.copyOf(modifiers));
        }
    }

    @Override
    public Set<Modifier> modifiers() {
        return modifiers;
    }

    @Override
    public boolean isVar() {
        return modifiers.stream().anyMatch(Modifier::isWithoutTypeSpecification);
    }

    @Override
    public LocalVariable localVariable() {
        return localVariable;
    }

    @Override
    public List<LocalVariable> otherLocalVariables() {
        return otherLocalVariables;
    }

    @Override
    public Stream<LocalVariable> localVariableStream() {
        return Stream.concat(Stream.of(localVariable), otherLocalVariables.stream());
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        if (predicate.test(this)) {
            localVariable.assignmentExpression().visit(predicate);
            for (LocalVariable lv : otherLocalVariables) {
                lv.assignmentExpression().visit(predicate);
            }
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeStatement(this)) {
            localVariable.visit(visitor);
            for (LocalVariable lv : otherLocalVariables) {
                lv.visit(visitor);
            }
        }
        visitor.afterStatement(this);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        // annotations
        OutputBuilder outputBuilder = outputBuilder(qualification);

        // modifiers, in the correct order!
        boolean isFinal = modifiers.stream().anyMatch(Modifier::isFinal);
        if (isFinal) {
            outputBuilder.add(KeywordImpl.FINAL).add(SpaceEnum.ONE);
        }
        boolean isVar = modifiers.stream().anyMatch(Modifier::isWithoutTypeSpecification);

        // var or type
        if (isVar) {
            outputBuilder.add(KeywordImpl.VAR);
        } else {
            outputBuilder.add(localVariable.parameterizedType()
                    .print(qualification, false, DiamondEnum.SHOW_ALL));
        }

        // declarations
        outputBuilder.add(SpaceEnum.ONE);
        OutputBuilder first = new OutputBuilderImpl().add(new TextImpl(localVariable.simpleName()));
        if (!localVariable.assignmentExpression().isEmpty()) {
            first.add(SymbolEnum.assignment("=")).add(localVariable.assignmentExpression().print(qualification));
        }
        Stream<OutputBuilder> rest = otherLocalVariables.stream().map(d -> {
            OutputBuilder ob = new OutputBuilderImpl().add(new TextImpl(d.simpleName()));
            if (!d.assignmentExpression().isEmpty()) {
                ob.add(SymbolEnum.assignment("="))
                        .add(d.assignmentExpression().print(qualification));
            }
            return ob;
        });
        outputBuilder.add(Stream.concat(Stream.of(first), rest).collect(OutputBuilderImpl.joining(SymbolEnum.COMMA)));
        return outputBuilder;
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return Stream.concat(localVariable.assignmentExpression().variables(descendMode),
                otherLocalVariables.stream().flatMap(lv -> lv.assignmentExpression().variables(descendMode)));
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        TypeInfo typeInfo = localVariable.parameterizedType().typeInfo();
        Element.TypeReference tr = new ElementImpl.TypeReference(typeInfo, true);
        return Stream.concat(Stream.of(tr), Stream.concat(localVariable.assignmentExpression().typesReferenced(),
                otherLocalVariables.stream().flatMap(lv -> lv.assignmentExpression().typesReferenced())));
    }
}
