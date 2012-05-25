package org.esa.beam.opendap;

import org.esa.beam.framework.datamodel.ProductData;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

/**
 * @author Thomas Storm
 */
public class FilterBuilderTest {

    @Test
    public void testBuilder() throws Exception {
        final FilterBuilder builder = new FilterBuilder();

        final OpenDapInterfaceImpl openDapInterface = new OpenDapInterfaceImpl(null);
        final Filter filter = builder
                .baseUrl(new URL("http://some.url/"))
                .minLat(-89)
                .maxLat(89)
                .minLon(-179)
                .maxLon(179)
                .fileNamePattern("sst.*")
                .openDapInterface(openDapInterface)
                .startDate(ProductData.UTC.parse("2012-01-01", "yyyy-MM-dd"))
                .endDate(ProductData.UTC.parse("2012-01-02", "yyyy-MM-dd"))
                .build();

        assertEquals("http://some.url/", filter.config.baseUrl.toString());
        assertEquals(-89.0, filter.config.minLat, 1E-7);
        assertEquals(89.0, filter.config.maxLat, 1E-7);
        assertEquals(-179.0, filter.config.minLon, 1E-7);
        assertEquals(179.0, filter.config.maxLon, 1E-7);
        assertTrue("sst_somewhere.nc".matches(filter.config.fileNamePattern));
        assertTrue("sst_somewhere_else.nc".matches(filter.config.fileNamePattern));
        assertSame(openDapInterface, filter.config.openDapInterface);
        assertEquals(ProductData.UTC.parse("2012-01-01", "yyyy-MM-dd").getAsDate().getTime(), filter.config.startDate.getAsDate().getTime());
        assertEquals(ProductData.UTC.parse("2012-01-02", "yyyy-MM-dd").getAsDate().getTime(), filter.config.endDate.getAsDate().getTime());
    }

    @Test
    public void testBuilder_Defaults() throws Exception {
        final OpenDapInterfaceImpl openDapInterface = new OpenDapInterfaceImpl(null);
        final Filter filter = new FilterBuilder()
                .baseUrl(new URL("http://some.url/"))
                .openDapInterface(openDapInterface)
                .build();
        assertEquals(filter.config.fileNamePattern, ".*");
        assertEquals(filter.config.minLat, -90.0, 1E-7);
        assertEquals(filter.config.maxLat, 90.0, 1E-7);
        assertEquals(filter.config.minLon, -180.0, 1E-7);
        assertEquals(filter.config.maxLon, 180.0, 1E-7);
        assertNull(filter.config.startDate);
        assertNull(filter.config.endDate);
    }

    @Test
    public void testBuilder_ValidationFail() throws Exception {
        try {
            new FilterBuilder()
                    .baseUrl(new URL("http://some.url/"))
                    .minLat(-91)
                    .openDapInterface(new OpenDapInterfaceImpl(null))
                    .build();
            fail();
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("minLat"));
        }
        try {
            new FilterBuilder()
                    .baseUrl(new URL("http://some.url/"))
                    .maxLat(91)
                    .openDapInterface(new OpenDapInterfaceImpl(null))
                    .build();
            fail();
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("maxLat"));
        }
        try {
            new FilterBuilder()
                    .baseUrl(new URL("http://some.url/"))
                    .minLon(-181)
                    .openDapInterface(new OpenDapInterfaceImpl(null))
                    .build();
            fail();
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("minLon"));
        }
        try {
            new FilterBuilder()
                    .baseUrl(new URL("http://some.url/"))
                    .maxLon(181)
                    .openDapInterface(new OpenDapInterfaceImpl(null))
                    .build();
            fail();
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("maxLon"));
        }
        try {
            new FilterBuilder()
                    .baseUrl(new URL("http://some.url/"))
                    .minLon(100)
                    .maxLon(90)
                    .openDapInterface(new OpenDapInterfaceImpl(null))
                    .build();
            fail();
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("maxLon")
                    && expected.getMessage().contains("minLon"));
        }
        try {
            new FilterBuilder()
                    .baseUrl(null)
                    .openDapInterface(new OpenDapInterfaceImpl(null))
                    .build();
            fail();
        } catch (IllegalStateException expected) {
            final String message = expected.getMessage().toLowerCase();
            assertTrue(message.contains("url") && message.contains("null"));
        }
        try {
            new FilterBuilder()
                    .baseUrl(new URL("http://some.url/"))
                    .openDapInterface(null)
                    .build();
            fail();
        } catch (IllegalStateException expected) {
            final String message = expected.getMessage().toLowerCase();
            assertTrue(message.contains("opendap") && message.contains("null"));
        }
        try {
            new FilterBuilder()
                    .baseUrl(new URL("http://some.url/"))
                    .openDapInterface(new OpenDapInterfaceImpl(null))
                    .startDate(ProductData.UTC.parse("2010-01-01", "yyyy-MM-dd"))
                    .endDate(ProductData.UTC.parse("2009-01-01", "yyyy-MM-dd"))
                    .build();
            fail();
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("endDate")
                    && expected.getMessage().contains("before")
                    && expected.getMessage().contains("startDate"));
        }
        try {
            new FilterBuilder()
                    .baseUrl(new URL("http://some.url/"))
                    .openDapInterface(new OpenDapInterfaceImpl(null))
                    .fileNamePattern("brokenPattern: [")
                    .build();
            fail();
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().toLowerCase().contains("pattern"));
        }
    }
}
