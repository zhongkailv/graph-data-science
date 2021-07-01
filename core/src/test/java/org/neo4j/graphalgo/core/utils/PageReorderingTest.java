/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.graphalgo.core.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.core.utils.paged.HugeLongArray;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PageReorderingTest {

    static Stream<Arguments> offsets() {
        return Stream.of(
            Arguments.of(new long[]{ 16, 18, 22, 0, 3, 6, 24, 28, 30, 8, 13, 15}, new int[]{1, 3, 0, 2}),
            Arguments.of(new long[]{ 16, 18, 22, 0, 3, 6, 8, 13, 15, 24, 28, 30}, new int[]{1, 2, 0, 3}),
            Arguments.of(new long[]{ 16, 18, 22, 24, 28, 30, 0, 3, 6, 8, 13, 15}, new int[]{2, 3, 0, 1}),
            Arguments.of(new long[]{ 16, 18, 22, 24, 28, 30, 8, 13, 15, 0, 3, 6}, new int[]{3, 2, 0, 1}),
            Arguments.of(new long[]{ 16, 18, 22, 8, 13, 15, 0, 3, 6, 24, 28, 30}, new int[]{2, 1, 0, 3}),
            Arguments.of(new long[]{ 16, 18, 22, 8, 13, 15, 24, 28, 30, 0, 3, 6}, new int[]{3, 1, 0, 2}),
            Arguments.of(new long[]{ 0, 3, 6, 16, 18, 22, 24, 28, 30, 8, 13, 15}, new int[]{0, 3, 1, 2}),
            Arguments.of(new long[]{ 0, 3, 6, 16, 18, 22, 8, 13, 15, 24, 28, 30}, new int[]{0, 2, 1, 3}),
            Arguments.of(new long[]{ 0, 3, 6, 24, 28, 30, 16, 18, 22, 8, 13, 15}, new int[]{0, 3, 2, 1}),
            Arguments.of(new long[]{ 0, 3, 6, 24, 28, 30, 8, 13, 15, 16, 18, 22}, new int[]{0, 2, 3, 1}),
            Arguments.of(new long[]{ 0, 3, 6, 8, 13, 15, 16, 18, 22, 24, 28, 30}, new int[]{0, 1, 2, 3}),
            Arguments.of(new long[]{ 0, 3, 6, 8, 13, 15, 24, 28, 30, 16, 18, 22}, new int[]{0, 1, 3, 2}),
            Arguments.of(new long[]{ 24, 28, 30, 16, 18, 22, 0, 3, 6, 8, 13, 15}, new int[]{2, 3, 1, 0}),
            Arguments.of(new long[]{ 24, 28, 30, 16, 18, 22, 8, 13, 15, 0, 3, 6}, new int[]{3, 2, 1, 0}),
            Arguments.of(new long[]{ 24, 28, 30, 0, 3, 6, 16, 18, 22, 8, 13, 15}, new int[]{1, 3, 2, 0}),
            Arguments.of(new long[]{ 24, 28, 30, 0, 3, 6, 8, 13, 15, 16, 18, 22}, new int[]{1, 2, 3, 0}),
            Arguments.of(new long[]{ 24, 28, 30, 8, 13, 15, 16, 18, 22, 0, 3, 6}, new int[]{3, 1, 2, 0}),
            Arguments.of(new long[]{ 24, 28, 30, 8, 13, 15, 0, 3, 6, 16, 18, 22}, new int[]{2, 1, 3, 0}),
            Arguments.of(new long[]{ 8, 13, 15, 16, 18, 22, 0, 3, 6, 24, 28, 30}, new int[]{2, 0, 1, 3}),
            Arguments.of(new long[]{ 8, 13, 15, 16, 18, 22, 24, 28, 30, 0, 3, 6}, new int[]{3, 0, 1, 2}),
            Arguments.of(new long[]{ 8, 13, 15, 0, 3, 6, 16, 18, 22, 24, 28, 30}, new int[]{1, 0, 2, 3}),
            Arguments.of(new long[]{ 8, 13, 15, 0, 3, 6, 24, 28, 30, 16, 18, 22}, new int[]{1, 0, 3, 2}),
            Arguments.of(new long[]{ 8, 13, 15, 24, 28, 30, 16, 18, 22, 0, 3, 6}, new int[]{3, 0, 2, 1}),
            Arguments.of(new long[]{ 8, 13, 15, 24, 28, 30, 0, 3, 6, 16, 18, 22}, new int[]{2, 0, 3, 1})
        );
    }

    @ParameterizedTest
    @MethodSource("offsets")
    void testOrdering(long[] offsets, int[] expectedOrdering) {
        var pageCount = 4;
        var pageShift = 3;
        var hugeOffsets = HugeLongArray.of(offsets);

        var ordering = PageReordering.ordering(hugeOffsets, pageCount, pageShift);

        assertThat(ordering.ordering()).isEqualTo(expectedOrdering);
        assertThat(ordering.pageOffsets()).isEqualTo(new long[] {0, 3, 6, 9, 12});
    }

    static Stream<int[]> orderings() {
        return Stream.of(
            new int[]{0, 1, 2, 3},
            new int[]{0, 1, 3, 2},
            new int[]{0, 2, 1, 3},
            new int[]{0, 2, 3, 1},
            new int[]{0, 3, 1, 2},
            new int[]{0, 3, 2, 1},
            new int[]{1, 0, 2, 3},
            new int[]{1, 0, 3, 2},
            new int[]{1, 2, 0, 3},
            new int[]{1, 2, 3, 0},
            new int[]{1, 3, 0, 2},
            new int[]{1, 3, 2, 0},
            new int[]{2, 0, 1, 3},
            new int[]{2, 0, 3, 1},
            new int[]{2, 1, 0, 3},
            new int[]{2, 1, 3, 0},
            new int[]{2, 3, 0, 1},
            new int[]{2, 3, 1, 0},
            new int[]{3, 0, 1, 2},
            new int[]{3, 0, 2, 1},
            new int[]{3, 1, 0, 2},
            new int[]{3, 1, 2, 0},
            new int[]{3, 2, 0, 1},
            new int[]{3, 2, 1, 0}
        );
    }

    @ParameterizedTest
    @MethodSource("orderings")
    void testReordering(int[] ordering) {
        var page0 = new String[] {"blue"};
        var page1 = new String[] {"red"};
        var page2 = new String[] {"green"};
        var page3 = new String[] {"yellow"};

        var expectedPages = new String[][]{page0, page1, page2, page3};
        var inputPages = new String[ordering.length][];

        for (int i = 0; i < ordering.length; i++) {
            int order = ordering[i];
            inputPages[order] = expectedPages[i];
        }

        var swaps = PageReordering.reorder(inputPages, ordering);
        assertThat(inputPages).isEqualTo(expectedPages);
        assertThat(swaps).isEqualTo(ordering);
    }

    @ParameterizedTest
    @MethodSource("offsets")
    void testSortOffsets(long[] offsets, int[] expectedOrdering) {
        var orderedOffsets = PageReordering.sortOffsets(
            HugeLongArray.of(offsets),
            ImmutablePageOrdering
                .builder()
                .ordering(expectedOrdering)
                .pageOffsets(0, 3, 6, 9, 12)
                .build(),
            AllocationTracker.empty()
        );

        Arrays.sort(offsets);
        assertThat(orderedOffsets.toArray()).isEqualTo(offsets);
    }

}