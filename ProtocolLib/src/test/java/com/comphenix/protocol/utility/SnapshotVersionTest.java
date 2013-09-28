package com.comphenix.protocol.utility;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

public class SnapshotVersionTest {
	@Test
	public void testDates() {
		SnapshotVersion a = new SnapshotVersion("12w50b");
		SnapshotVersion b = new SnapshotVersion("13w05a");
	
		expect(a.getSnapshotDate(), 12, 50);
		expect(b.getSnapshotDate(), 13, 5);
		
		// Test equality
		assertEquals(a, new SnapshotVersion("12w50b"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testDateParsingProblem() {
		// This date is not valid
		new SnapshotVersion("12w80a");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testMissingWeekVersion() {
		new SnapshotVersion("13w05");
	}
	
	private void expect(Date date, int year, int week) {
		Calendar calendar = Calendar.getInstance(Locale.US);
		calendar.setTime(date);
		assertEquals(year, calendar.get(Calendar.YEAR) % 100);
		assertEquals(week, calendar.get(Calendar.WEEK_OF_YEAR));
	}
}
