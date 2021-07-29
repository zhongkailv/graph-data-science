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
package org.neo4j.gds.storageengine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.BaseTest;
import org.neo4j.graphalgo.NodeProjection;
import org.neo4j.graphalgo.PropertyMapping;
import org.neo4j.graphalgo.PropertyMappings;
import org.neo4j.graphalgo.StoreLoaderBuilder;
import org.neo4j.graphalgo.api.GraphStore;
import org.neo4j.graphalgo.compat.Neo4jVersion;
import org.neo4j.graphalgo.config.GraphCreateFromStoreConfig;
import org.neo4j.graphalgo.core.loading.GraphStoreCatalog;
import org.neo4j.graphalgo.extension.Neo4jGraph;
import org.neo4j.graphalgo.junit.annotation.DisableForNeo4jVersion;
import org.neo4j.internal.recordstorage.InMemoryStorageEngineCompanion;
import org.neo4j.token.DelegatingTokenHolder;
import org.neo4j.token.ReadOnlyTokenCreator;
import org.neo4j.token.TokenHolders;
import org.neo4j.token.api.TokenHolder;
import org.neo4j.token.api.TokenNotFoundException;
import org.neo4j.values.storable.LongValue;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryNodeCursorTest extends BaseTest {

    @Neo4jGraph
    static final String DB_CYPHER = "CREATE" +
                                    "  (a:A {prop1: 42})" +
                                    ", (b:B {prop2: 1337})" +
                                    ", (:A)" +
                                    ", (:A)";

    GraphStore graphStore;
    TokenHolders tokenHolders;
    InMemoryNodeCursor nodeCursor;

    @BeforeEach
    void setup() throws Exception {
        this.graphStore = new StoreLoaderBuilder()
            .api(db)
            .addNodeProjection(NodeProjection.of("A", PropertyMappings.of(PropertyMapping.of("prop1"))))
            .addNodeProjection(NodeProjection.of("B", PropertyMappings.of(PropertyMapping.of("prop2"))))
            .build()
            .graphStore();

        GraphStoreCatalog.set(GraphCreateFromStoreConfig.emptyWithName("", db.databaseLayout().getDatabaseName()), graphStore);

        this.tokenHolders = new TokenHolders(
            new DelegatingTokenHolder(new ReadOnlyTokenCreator(), TokenHolder.TYPE_PROPERTY_KEY),
            new DelegatingTokenHolder(new ReadOnlyTokenCreator(), TokenHolder.TYPE_LABEL),
            new DelegatingTokenHolder(new ReadOnlyTokenCreator(), TokenHolder.TYPE_RELATIONSHIP_TYPE)
        );

        InMemoryStorageEngineCompanion.create(db.databaseLayout(), tokenHolders).schemaAndTokensLifecycle().init();
        this.nodeCursor = new InMemoryNodeCursor(graphStore, tokenHolders);
    }

    @Test
    @DisableForNeo4jVersion(Neo4jVersion.V_4_0)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_1)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_2)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_3_drop31)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_3_drop40)
    void shouldScanSingle() {
        nodeCursor.single(0);
        assertThat(nodeCursor.next()).isTrue();
        assertThat(nodeCursor.next()).isFalse();
    }

    @Test
    @DisableForNeo4jVersion(Neo4jVersion.V_4_0)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_1)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_2)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_3_drop31)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_3_drop40)
    void shouldScanRange() {
        nodeCursor.scanRange(1, 2);
        nodeCursor.next();
        assertThat(nodeCursor.getId()).isEqualTo(1);
        assertThat(nodeCursor.next()).isTrue();
        assertThat(nodeCursor.getId()).isEqualTo(2);
        assertThat(nodeCursor.next()).isFalse();
    }

    @Test
    @DisableForNeo4jVersion(Neo4jVersion.V_4_0)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_1)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_2)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_3_drop31)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_3_drop40)
    void shouldScanAll() {
        nodeCursor.scan();
        graphStore.nodes().forEachNode(nodeId -> {
            assertThat(nodeCursor.next()).isTrue();
            assertThat(nodeCursor.getId()).isEqualTo(nodeId);
            return true;
        });
        assertThat(nodeCursor.next()).isFalse();
    }

    @Test
    @DisableForNeo4jVersion(Neo4jVersion.V_4_0)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_1)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_2)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_3_drop31)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_3_drop40)
    void testLabels() {
        graphStore.nodes().forEachNode(nodeId -> {
            nodeCursor.single(nodeId);
            nodeCursor.next();
            var labelTokens = graphStore.nodes().nodeLabels(nodeId).stream()
                .mapToLong(label -> tokenHolders.labelTokens().getIdByName(label.name))
                .toArray();
            assertThat(nodeCursor.labels()).containsExactlyInAnyOrder(labelTokens);
            return true;
        });
    }

    @Test
    @DisableForNeo4jVersion(Neo4jVersion.V_4_0)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_1)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_2)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_3_drop31)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_3_drop40)
    void shouldHaveProperties() {
        nodeCursor.next();
        assertThat(nodeCursor.hasProperties()).isTrue();
        assertThat(nodeCursor.propertiesReference()).isEqualTo(0);
    }

    @Test
    @DisableForNeo4jVersion(Neo4jVersion.V_4_0)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_1)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_2)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_3_drop31)
    @DisableForNeo4jVersion(Neo4jVersion.V_4_3_drop40)
    void shouldTraverseProperties() throws TokenNotFoundException {
        nodeCursor.next();
        var propertyCursor = new InMemoryNodePropertyCursor(graphStore, tokenHolders);
        nodeCursor.properties(propertyCursor);
        assertThat(propertyCursor.next()).isTrue();

        var propertyName = tokenHolders.propertyKeyTokens().getTokenById(propertyCursor.propertyKey()).name();
        var propertyValue = ((LongValue) propertyCursor.propertyValue()).longValue();
        assertThat(propertyValue).isEqualTo(graphStore.nodePropertyValues(propertyName).longValue(nodeCursor.getId()));

        assertThat(propertyCursor.next()).isFalse();
    }
}