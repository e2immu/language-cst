package org.e2immu.language.cst.impl.statement;

import org.e2immu.language.cst.api.element.Comment;
import org.e2immu.language.cst.api.element.Element;
import org.e2immu.language.cst.api.element.Source;
import org.e2immu.language.cst.api.element.Visitor;
import org.e2immu.language.cst.api.expression.AnnotationExpression;
import org.e2immu.language.cst.api.expression.Expression;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.statement.Statement;
import org.e2immu.language.cst.api.statement.SwitchEntry;
import org.e2immu.language.cst.api.statement.SwitchStatementNewStyle;
import org.e2immu.language.cst.api.translate.TranslationMap;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.Variable;
import org.e2immu.language.cst.impl.output.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SwitchStatementNewStyleImpl extends StatementImpl implements SwitchStatementNewStyle {
    private final Expression selector;
    private final List<SwitchEntry> entries;

    public SwitchStatementNewStyleImpl(List<Comment> comments, Source source, List<AnnotationExpression> annotations,
                                       String label, Expression selector, List<SwitchEntry> entries) {
        super(comments, source, annotations,
                10 + selector.complexity() + entries.stream().mapToInt(SwitchEntry::complexity).sum(), label);
        this.selector = selector;
        this.entries = entries;
    }

    @Override
    public Expression expression() {
        return selector;
    }

    @Override
    public List<SwitchEntry> entries() {
        return entries;
    }

    @Override
    public boolean hasSubBlocks() {
        return !entries.isEmpty();
    }

    @Override
    public void visit(Predicate<Element> predicate) {
        selector.visit(predicate);
        int i = 0;
        for (SwitchEntry entry : entries) {
            entry.conditions().forEach(e -> e.visit(predicate));
            if (entry.patternVariable() != null) entry.patternVariable().visit(predicate);
            entry.whenExpression().visit(predicate);
            entry.statement().visit(predicate);
            i++;
        }
    }

    @Override
    public void visit(Visitor visitor) {
        if (visitor.beforeStatement(this)) {
            selector.visit(visitor);
            int i = 0;
            for (SwitchEntry entry : entries) {
                entry.conditions().forEach(e -> e.visit(visitor));
                if (entry.patternVariable() != null) entry.patternVariable().visit(visitor);
                entry.whenExpression().visit(visitor);
                visitor.startSubBlock(i);
                entry.statement().visit(visitor);
                visitor.endSubBlock(i);
                i++;
            }
        }
        visitor.afterStatement(this);
    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        OutputBuilder outputBuilder = outputBuilder(qualification).add(KeywordImpl.SWITCH)
                .add(SymbolEnum.LEFT_PARENTHESIS)
                .add(selector.print(qualification))
                .add(SymbolEnum.RIGHT_PARENTHESIS)
                .add(SymbolEnum.LEFT_BRACE);
        GuideImpl.GuideGenerator guideGenerator = GuideImpl.generatorForBlock();
        outputBuilder.add(guideGenerator.start());
        int i = 0;
        for (SwitchEntry entry : entries) {
            if (i > 0) outputBuilder.add(guideGenerator.mid());
            outputBuilder.add(entry.print(qualification));
            i++;
        }
        return outputBuilder.add(guideGenerator.end()).add(SymbolEnum.RIGHT_BRACE);
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return Stream.concat(selector.variables(descendMode),
                entries.stream().flatMap(e -> e.variables(descendMode)));
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return Stream.empty();
    }

    @Override
    public List<Statement> translate(TranslationMap translationMap) {
        List<Statement> direct = translationMap.translateStatement(this);
        if (direct.size() != 1 || direct.get(0) != this) {
            return direct;
        }
        throw new UnsupportedOperationException("NYI");
    }

    public static class BuilderImpl extends StatementImpl.Builder<SwitchStatementNewStyle.Builder>
            implements SwitchStatementNewStyle.Builder {
        private Expression selector;
        private final List<SwitchEntry> entries = new ArrayList<>();

        @Override
        public SwitchStatementNewStyle.Builder setSelector(Expression selector) {
            this.selector = selector;
            return this;
        }

        @Override
        public SwitchStatementNewStyle.Builder addSwitchEntries(Collection<SwitchEntry> switchEntries) {
            this.entries.addAll(switchEntries);
            return this;
        }

        @Override
        public SwitchStatementNewStyle build() {
            return new SwitchStatementNewStyleImpl(comments, source, annotations, label, selector, List.copyOf(entries));
        }
    }

}
