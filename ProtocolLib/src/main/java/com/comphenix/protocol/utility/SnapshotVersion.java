package com.comphenix.protocol.utility;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

/**
 * Used to parse a snapshot version.
 * @author Kristian
 */
public class SnapshotVersion implements Comparable<SnapshotVersion>, Serializable {
	// Increment when the class changes
	private static final long serialVersionUID = 1L;

	private static final Pattern SNAPSHOT_PATTERN = Pattern.compile("(\\d{2}w\\d{2})([a-z])");

	private final Date snapshotDate;
	private final int snapshotWeekVersion;

	private transient String rawString;
	
	public SnapshotVersion(String version) {
		Matcher matcher = SNAPSHOT_PATTERN.matcher(version.trim());
		
		if (matcher.matches()) {
			try {
				this.snapshotDate = getDateFormat().parse(matcher.group(1));
				this.snapshotWeekVersion = matcher.group(2).charAt(0) - 'a';
				this.rawString = version;
			} catch (ParseException e) {
				throw new IllegalArgumentException("Date implied by snapshot version is invalid.", e);
			}
		} else {
			throw new IllegalArgumentException("Cannot parse " + version + " as a snapshot version.");
		}
	}
	
	/**
	 * Retrieve the snapshot date parser.
	 * <p>
	 * We have to create a new instance of SimpleDateFormat every time as it is not thread safe.
	 * @return The date formatter.
	 */
	private static SimpleDateFormat getDateFormat() {
		SimpleDateFormat format = new SimpleDateFormat("yy'w'ww", Locale.US);
		format.setLenient(false);
		return format;
	}
	
	/**
	 * Retrieve the snapshot version within a week, starting at zero.
	 * @return The weekly version
	 */
	public int getSnapshotWeekVersion() {
		return snapshotWeekVersion;
	}
	
	/**
	 * Retrieve the week this snapshot was released.
	 * @return The week.
	 */
	public Date getSnapshotDate() {
		return snapshotDate;
	}
	
	/**
	 * Retrieve the raw snapshot string (yy'w'ww[a-z]).
	 * @return The snapshot string.
	 */
	public String getSnapshotString() {
		if (rawString == null) {
			// It's essential that we use the same locale
			Calendar current = Calendar.getInstance(Locale.US);
			current.setTime(snapshotDate);
			rawString = String.format("%02dw%02d%s",
				current.get(Calendar.YEAR) % 100, 
				current.get(Calendar.WEEK_OF_YEAR),
				(char) ('a' + snapshotWeekVersion));
		}
		return rawString;
	}

	@Override
	public int compareTo(SnapshotVersion o) {
		if (o == null)
			return 1;
	
		return ComparisonChain.start().
					compare(snapshotDate, o.getSnapshotDate()).
					compare(snapshotWeekVersion, o.getSnapshotWeekVersion()).
					result();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof SnapshotVersion) {
			SnapshotVersion other = (SnapshotVersion) obj;
			return Objects.equal(snapshotDate, other.getSnapshotDate()) &&
				   snapshotWeekVersion == other.getSnapshotWeekVersion();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(snapshotDate, snapshotWeekVersion);
	}
	
	@Override
	public String toString() {
		return getSnapshotString();
	}
}
