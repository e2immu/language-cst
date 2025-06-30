package org.e2immu.language.cst.impl.element;

import org.e2immu.language.cst.api.element.*;
import org.e2immu.language.cst.api.info.InfoMap;
import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.Qualification;
import org.e2immu.language.cst.api.variable.DescendMode;
import org.e2immu.language.cst.api.variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ModuleInfoImpl extends ElementImpl implements ModuleInfo {
    private final String name;
    private final List<Comment> comments;
    private final Source source;
    private final List<ModuleInfo.Requires> requires;

    public ModuleInfoImpl(List<Comment> comments, Source source, String name,
                          List<ModuleInfo.Requires> requires) {
        this.name = name;
        this.comments = comments;
        this.source = source;
        this.requires = requires;
    }

    private record RequiresImpl(String name, boolean isStatic, boolean isTransitive) implements Requires {
    }

    public static class BuilderImpl extends ElementImpl.Builder<ModuleInfo.Builder> implements ModuleInfo.Builder {
        private String name;
        private final List<Requires> requiresList = new ArrayList<>();

        @Override
        public ModuleInfo.Builder setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public ModuleInfo build() {
            return new ModuleInfoImpl(comments, source, name, List.copyOf(requiresList));
        }

        @Override
        public void addRequires(String name, boolean isStatic, boolean isTransitive) {
            requiresList.add(new RequiresImpl(name, isStatic, isTransitive));
        }
    }

    @Override
    public int complexity() {
        return 0;
    }

    @Override
    public List<Comment> comments() {
        return comments;
    }

    @Override
    public Element rewire(InfoMap infoMap) {
        return null;
    }

    @Override
    public Source source() {
        return source;
    }

    @Override
    public List<Requires> requires() {
        return requires;
    }

    @Override
    public void visit(Predicate<Element> predicate) {

    }

    @Override
    public void visit(Visitor visitor) {

    }

    @Override
    public OutputBuilder print(Qualification qualification) {
        return null;
    }

    @Override
    public Stream<Variable> variables(DescendMode descendMode) {
        return Stream.empty();
    }

    @Override
    public Stream<Element.TypeReference> typesReferenced() {
        return Stream.empty();
    }

    @Override
    public String name() {
        return name;
    }
}
