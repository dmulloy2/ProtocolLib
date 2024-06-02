package com.comphenix.protocol.timing;

/**
 * Represents an online algortihm for computing the mean and standard deviation without storing every value.
 *
 * @author Kristian
 */
public class StatisticsStream {
    // This algorithm is due to Donald Knuth, as described in:
    //     Donald E. Knuth (1998). The Art of Computer Programming, volume 2:
    //     Seminumerical Algorithms, 3rd edn., p. 232. Boston: Addison-Wesley.

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
     *
     * @param other - copy of the stream.
     */
    public StatisticsStream(StatisticsStream other) {
        this.count = other.count;
        this.mean = other.mean;
        this.m2 = other.m2;
        this.minimum = other.minimum;
        this.maximum = other.maximum;
    }

    /**
     * Observe a value.
     *
     * @param value - the observed value.
     */
    public synchronized void observe(double value) {
        double delta = value - this.mean;

        // As per Knuth
        this.count++;
        this.mean += delta / this.count;
        this.m2 += delta * (value - this.mean);

        // Update extremes
        if (value < this.minimum) {
            this.minimum = value;
        }
        if (value > this.maximum) {
            this.maximum = value;
        }
    }

    /**
     * Retrieve the average of all the observations.
     *
     * @return The average.
     */
    public double getMean() {
        this.checkCount();
        return this.mean;
    }

    /**
     * Retrieve the variance of all the observations.
     *
     * @return The variance.
     */
    public double getVariance() {
        this.checkCount();
        return this.m2 / (this.count - 1);
    }

    /**
     * Retrieve the standard deviation of all the observations.
     *
     * @return The STDV.
     */
    public double getStandardDeviation() {
        return Math.sqrt(this.getVariance());
    }

    /**
     * Retrieve the minimum observation yet observed.
     *
     * @return The minimum observation.
     */
    public double getMinimum() {
        this.checkCount();
        return this.minimum;
    }

    /**
     * Retrieve the maximum observation yet observed.
     *
     * @return The maximum observation.
     */
    public double getMaximum() {
        this.checkCount();
        return this.maximum;
    }

    /**
     * Combine the two statistics.
     *
     * @param other - the other statistics.
     * @return Combined statistics
     */
    public StatisticsStream add(StatisticsStream other) {
        // Special cases
        if (this.count == 0) {
            return other;
        } else if (other.count == 0) {
            return this;
        }

        StatisticsStream stream = new StatisticsStream();
        double delta = other.mean - this.mean;
        double n = this.count + other.count;

        stream.count = (int) n;
        stream.mean = this.mean + delta * (other.count / n);
        stream.m2 = this.m2 + other.m2 + ((delta * delta) * (this.count * other.count) / n);
        stream.minimum = Math.min(this.minimum, other.minimum);
        stream.maximum = Math.max(this.maximum, other.maximum);
        return stream;
    }

    /**
     * Retrieve the number of observations.
     *
     * @return Number of observations.
     */
    public int getCount() {
        return this.count;
    }

    private void checkCount() {
        if (this.count == 0) {
            throw new IllegalStateException("No observations in stream.");
        }
    }

    @Override
    public String toString() {
        if (this.count == 0) {
            return "StatisticsStream [Nothing recorded]";
        }

        return String.format("StatisticsStream [Average: %.3f, SD: %.3f, Min: %.3f, Max: %.3f, Count: %s]",
                this.getMean(), this.getStandardDeviation(),
                this.getMinimum(), this.getMaximum(), this.getCount());
    }
}
