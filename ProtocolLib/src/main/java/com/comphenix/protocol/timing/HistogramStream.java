package com.comphenix.protocol.timing;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Represents an online algortihm of computing histograms over time.
 * @author Kristian
 */
public class HistogramStream extends OnlineComputation {
	/**
	 * Each bin in the histogram, indexed by time.
	 */
	protected List<StatisticsStream> bins;
	
	/**
	 * The current statistics stream we are updating.
	 */
	protected StatisticsStream current;

	/**
	 * The maximum number of observations in each bin.
	 */
	protected int binWidth;
	
	/**
	 * The number of total observations.
	 */
	protected int count;
	
	/**
	 * Construct a new histogram stream which splits up every observation in different bins, ordered by time. 
	 * @param binWidth - maximum number of observations in each bin.
	 */
	public HistogramStream(int binWidth) {
		this(new ArrayList<StatisticsStream>(), new StatisticsStream(), binWidth);
	}
	
	/**
	 * Construct a new copy of the given histogram.
	 * @param other - the histogram to copy.
	 */
	public HistogramStream(HistogramStream other) {
		// Deep cloning
		for (StatisticsStream stream : other.bins) {
			StatisticsStream copy = stream.copy();
			
			// Update current
			if (stream == other.current)
				this.current = copy;
			this.bins.add(copy);
		}
		this.binWidth = other.binWidth;
	}
	
	/**
	 * Construct a new histogram stream.
	 * @param bins - list of bins.
	 * @param current - the current selected bin. This will be added to the list if it is not already present.
	 * @param binWidth - the desired number of observations in each bin.
	 */
	protected HistogramStream(List<StatisticsStream> bins, StatisticsStream current, int binWidth) {
		if (binWidth < 1)
			throw new IllegalArgumentException("binWidth cannot be less than 1");
		this.bins =  Preconditions.checkNotNull(bins, "bins cannot be NULL");
		this.current = Preconditions.checkNotNull(current, "current cannot be NULL");
		this.binWidth = binWidth;
		
		if (!this.bins.contains(current)) {
			this.bins.add(current);
		}
	}
	
	@Override
	public HistogramStream copy() {
		return new HistogramStream(this);
	}
	
	/**
	 * Retrieve an immutable view of every bin in the histogram.
	 * @return Every bin in the histogram.
	 */
	public ImmutableList<StatisticsStream> getBins() {
		return ImmutableList.copyOf(bins);
	}
	
	@Override
	public void observe(double value) {
		checkOverflow();
		count++;
		current.observe(value);
	}
	
	/**
	 * See if the current bin has overflowed. If so, construct a new bin and set it as the current.
	 */
	protected void checkOverflow() {
		if (current.getCount() >= binWidth) {
			bins.add(current = new StatisticsStream());
		}
	}
	
	/**
	 * Retrieve the total statistics of every bin in the histogram.
	 * <p>
	 * This method is not thread safe.
	 * @return The total statistics.
	 */
	public StatisticsStream getTotal() {
		StatisticsStream sum = null;
		
		for (StatisticsStream stream : bins) {
			sum = sum != null ? stream.add(sum) : stream;
		}
		return sum;
	}
		
	@Override
	public int getCount() {
		return count;
	}
}