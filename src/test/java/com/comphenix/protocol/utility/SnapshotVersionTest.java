package com.comphenix.protocol.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.junit.jupiter.api.Test;

public class SnapshotVersionTest {

	@Test
	public void testDates() {
		SnapshotVersion a = new SnapshotVersion("12w50b");
		SnapshotVersion b = new SnapshotVersion("13w05a");

		this.expect(a.getSnapshotDate(), 12, 50);
		this.expect(b.getSnapshotDate(), 13, 5);

		// Test equality
		assertEquals(a, new SnapshotVersion("12w50b"));
	}

	@Test
	public void testDateParsingProblem() {
		// This date is not valid
		assertThrows(IllegalArgumentException.class, () -> new SnapshotVersion("12w80a"));
	}

	@Test
	public void testMissingWeekVersion() {
		assertThrows(IllegalArgumentException.class, () -> new SnapshotVersion("13w05"));
	}

	private void expect(Date date, int year, int week) {
		Calendar calendar = Calendar.getInstance(Locale.US);
		calendar.setTime(date);
		assertEquals(year, calendar.get(Calendar.YEAR) % 100);
		assertEquals(week, calendar.get(Calendar.WEEK_OF_YEAR));
	}
}
