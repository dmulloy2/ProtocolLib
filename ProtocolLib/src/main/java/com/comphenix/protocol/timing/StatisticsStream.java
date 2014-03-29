package com.comphenix.protocol.timing;

/**
 * Represents an online algortihm for computing the mean and standard deviation without storing every value.
 * @author Kristian
 */
public class StatisticsStream extends OnlineComputation {
	// This algorithm is due to Donald Knuth, as described in:
	//     Donald E. Knuth (1998). The Art of Computer Programming, volume 2: 
	//	   Seminumerical Algorithms, 3rd edn., p. 232. Boston: Addison-Wesley.
	
    private int count = 0;
    private double mean = 0;
    private double m2 = 0;
    
    // Also keep track of minimum and maximum observation
    private double minimum = Double.MAX_VALUE;
    private double maximum = 0;
    
    /**
     * Construct a new stream with no observations.
     */
    public StatisticsStream() {
    }
    
    /**
     * Construct a copy of the given stream.
     * @param other - copy of the stream.
     */
    public StatisticsStream(StatisticsStream other) {
		this.count = other.count;
		this.mean = other.mean;
		this.m2 = other.m2;
		this.minimum = other.minimum;
		this.maximum = other.maximum;
	}

	@Override
	public StatisticsStream copy() {
		return new StatisticsStream(this);
	}
    
	/**
     * Observe a value.
     * @param value - the observed value.
     */
    @Override
	public void observe(double value) {
    	double delta = value - mean;
    	
    	// As per Knuth
    	count++;
    	mean += delta / count;
    	m2 += delta * (value - mean); 
    	
    	// Update extremes
    	if (value < minimum)
    		minimum = value;
    	if (value > maximum)
    		maximum = value;
    }
 
    /**
     * Retrieve the average of all the observations.
     * @return The average.
     */
    public double getMean() {
    	checkCount();
		return mean;
	}
 
    /**
     * Retrieve the variance of all the observations.
     * @return The variance.
     */
    public double getVariance() {
    	checkCount();
    	return m2 / (count - 1);
    }
    
    /**
     * Retrieve the standard deviation of all the observations.
     * @return The STDV.
     */
    public double getStandardDeviation() {
    	return Math.sqrt(getVariance());
    }
    
    /**
     * Retrieve the minimum observation yet observed.
     * @return The minimum observation.
     */
    public double getMinimum() {
    	checkCount();
    	return minimum;
	}
    
    /**
     * Retrieve the maximum observation yet observed.
     * @return The maximum observation.
     */
    public double getMaximum() {
    	checkCount();
		return maximum;
	}
    
    /**
     * Combine the two statistics.
     * @param other - the other statistics.
     */
    public StatisticsStream add(StatisticsStream other) {
    	// Special cases
    	if (count == 0)
    		return other;
    	else if (other.count == 0)
    		return this;
    	
    	StatisticsStream stream = new StatisticsStream();
    	double delta = other.mean - mean;
    	double n = count + other.count;
    	
    	stream.count = (int) n;
    	stream.mean = mean + delta * (other.count / n);
    	stream.m2 = m2 + other.m2 + ((delta * delta) * (count * other.count) / n);
    	stream.minimum = Math.min(minimum, other.minimum);
    	stream.maximum = Math.max(maximum, other.maximum);
    	return stream;
    }
    
    /**
     * Retrieve the number of observations.
     * @return Number of observations.
     */
    @Override
	public int getCount() {
		return count;
	}
    
    private void checkCount() {
    	if (count == 0) {
    		throw new IllegalStateException("No observations in stream.");
    	}
    }
    
    @Override
    public String toString() {
    	if (count == 0)
    		return "StatisticsStream [Nothing recorded]";
    	
		return String.format("StatisticsStream [Average: %.3f, SD: %.3f, Min: %.3f, Max: %.3f, Count: %s]", 
			getMean(), getStandardDeviation(), 
			getMinimum(), getMaximum(), getCount());
    }
}