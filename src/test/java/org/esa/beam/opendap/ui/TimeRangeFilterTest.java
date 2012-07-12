/*
 * Copyright (C) 2011 Brockmann Consult GmbH (info@brockmann-consult.de)
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.beam.opendap.ui;

import org.esa.beam.opendap.OpendapLeaf;
import org.esa.beam.opendap.utils.TimeStampExtractor;
import org.junit.Test;

import java.util.GregorianCalendar;

import static org.junit.Assert.*;

/**
 * @author Thomas Storm
 * @author Tonio Fincke
 */
public class TimeRangeFilterTest {

    @Test
    public void testAccept_UserEndFileStart() throws Exception {
        TimeRangeFilter filter = new TimeRangeFilter();
        filter.endDate = new GregorianCalendar(2010, 0, 2, 12, 37, 15).getTime();

        filter.timeStampExtractor = new TimeStampExtractor("yyyyMMdd:hhmmss", "*${date}*");

        assertTrue(filter.accept(new OpendapLeaf("sth__20080101:192345.nc")));
        assertFalse(filter.accept(new OpendapLeaf("sth__20111231:192345.nc")));
    }

    @Test
    public void testAccept_UserBothFileStart() throws Exception {
        TimeRangeFilter filter = new TimeRangeFilter();
        filter.startDate = new GregorianCalendar(2010, 0, 1, 12, 37, 15).getTime();
        filter.endDate = new GregorianCalendar(2010, 0, 2, 12, 37, 15).getTime();

        filter.timeStampExtractor = new TimeStampExtractor("yyyyMMdd:hhmmss", "*${date}*");

        assertTrue(filter.accept(new OpendapLeaf("sth__20100101:192345.nc")));
        assertFalse(filter.accept(new OpendapLeaf("sth__20091231:192345.nc")));
        assertTrue(filter.accept(new OpendapLeaf("does_not_match_naming_pattern")));
    }

    @Test
    public void testAccept_UserStartFileStart() throws Exception {
        TimeRangeFilter filter = new TimeRangeFilter();
        filter.startDate = new GregorianCalendar(2010, 0, 1, 12, 37, 15).getTime();
        filter.endDate = null;
        filter.timeStampExtractor = new TimeStampExtractor("yyyyMMdd:hhmmss", "*${date}*");

        assertTrue(filter.accept(new OpendapLeaf("sth__20100101:192345.nc")));
        assertFalse(filter.accept(new OpendapLeaf("sth__20091231:192345.nc")));
        assertTrue(filter.accept(new OpendapLeaf("does_not_match_naming_pattern")));
    }

    @Test
    public void testAccept_UserBothFileBoth() throws Exception {
        TimeRangeFilter filter = new TimeRangeFilter();
        filter.startDate = new GregorianCalendar(2010, 0, 1, 12, 37, 15).getTime();
        filter.endDate = new GregorianCalendar(2010, 0, 2, 12, 37, 15).getTime();
        filter.timeStampExtractor = new TimeStampExtractor("yyyyMMdd:hhmmss", "*${date}*${date}*");

        assertTrue(filter.accept(new OpendapLeaf("sth__20100101:192345___20100102:012345__.nc")));
        assertFalse(filter.accept(new OpendapLeaf("sth__20091231:192345___20100102:012345__.nc")));
        assertFalse(filter.accept(new OpendapLeaf("sth__20100101:192345___20100103:012345__.nc")));
        assertFalse(filter.accept(new OpendapLeaf("sth__20091231:192345___20100103:012345__.nc")));
        assertFalse(filter.accept(new OpendapLeaf("sth__20091231:192345___20091231:233012__.nc")));
        assertFalse(filter.accept(new OpendapLeaf("sth__20100103:004523___20100103:012345__.nc")));
        assertTrue(filter.accept(new OpendapLeaf("sth__20100101:192345_does_not_match_naming_pattern.nc")));
        assertTrue(filter.accept(new OpendapLeaf("sth__20100104:192345_does_not_match_naming_pattern.nc")));
        assertTrue(filter.accept(new OpendapLeaf("does_not_match_naming_pattern")));
    }
}
