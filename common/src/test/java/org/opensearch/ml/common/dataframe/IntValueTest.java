/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 *
 */

package org.opensearch.ml.common.dataframe;

import org.junit.Test;
import org.opensearch.common.Strings;
import org.opensearch.common.xcontent.XContentBuilder;
import org.opensearch.common.xcontent.XContentFactory;
import org.opensearch.common.xcontent.XContentType;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class IntValueTest {

    @Test
    public void intValue() {
        IntValue intValue = new IntValue(2);
        assertEquals(ColumnType.INTEGER, intValue.columnType());
        assertEquals(2, intValue.getValue());
        assertEquals(2, intValue.intValue());
    }

    @Test
    public void testToXContent() throws IOException {
        IntValue intValue = new IntValue(2);
        XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON);
        intValue.toXContent(builder);

        assertNotNull(builder);
        String jsonStr = Strings.toString(builder);
        assertEquals("{\"column_type\":\"INTEGER\",\"value\":2}", jsonStr);
    }
}