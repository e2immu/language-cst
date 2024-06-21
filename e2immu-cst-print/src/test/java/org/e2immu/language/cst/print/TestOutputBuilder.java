/*
 * e2immu: a static code analyser for effective and eventual immutability
 * Copyright 2020-2021, Bart Naudts, https://www.e2immu.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details. You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.e2immu.language.cst.print;

import org.e2immu.language.cst.api.output.OutputBuilder;
import org.e2immu.language.cst.api.output.element.Guide;
import org.e2immu.language.cst.impl.output.GuideImpl;
import org.e2immu.language.cst.impl.output.OutputBuilderImpl;
import org.e2immu.language.cst.impl.output.SymbolEnum;
import org.e2immu.language.cst.impl.output.TextImpl;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestOutputBuilder {

    @Test
    public void test() {
        OutputBuilder[] outputBuilders = { new OutputBuilderImpl().add(new TextImpl("a")),
                new OutputBuilderImpl().add(new TextImpl("b")),
                new OutputBuilderImpl().add(new TextImpl("c")), };
        OutputBuilder all = Arrays.stream(outputBuilders).collect(OutputBuilderImpl.joining(SymbolEnum.COMMA));
        System.out.println(all.list());
        assertEquals(9, all.list().size());
        assertTrue(all.list().get(0) instanceof GuideImpl guide && guide.position() == GuideImpl.Position.START);
        assertTrue(all.list().get(3) instanceof GuideImpl guide && guide.position() == GuideImpl.Position.MID);
        assertTrue(all.list().get(6) instanceof GuideImpl guide && guide.position() == GuideImpl.Position.MID);
        assertTrue(all.list().get(8) instanceof GuideImpl guide && guide.position() == GuideImpl.Position.END);
    }
}
