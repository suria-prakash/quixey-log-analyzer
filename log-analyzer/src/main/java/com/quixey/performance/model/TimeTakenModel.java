package com.quixey.performance.model;

import java.io.Serializable;

public class TimeTakenModel implements Serializable {

	private static final long serialVersionUID = -1842957989099854358L;

	int numSamples;
	long totalTimeTaken;
	long minTimeTaken;
	long maxTimeTaken;

	public TimeTakenModel() {
		// TODO Auto-generated constructor stub
	}

	public TimeTakenModel(int numSamples, long totalTimeTaken, long minTimeTaken, long maxTimeTaken) {
		this.numSamples = numSamples;
		this.totalTimeTaken = totalTimeTaken;
		this.minTimeTaken = minTimeTaken;
		this.maxTimeTaken = maxTimeTaken;
	}

	// public void writeObject(ObjectOutputStream out) throws IOException {
	// //out.defaultWriteObject();
	//// out.writeObject("number of requests = " + numSamples + "\n total time
	// taken = " + totalTimeTaken + " \n"
	//// + "average time taken for each request = " + (float) (numSamples /
	// totalTimeTaken));
	// out.writeObject(this.numSamples);
	// out.flush();
	// }

	/**
	 * @return the numSamples
	 */
	public int getNumSamples() {
		return numSamples;
	}

	/**
	 * @param numSamples
	 *            the numSamples to set
	 */
	public void setNumSamples(int numSamples) {
		this.numSamples = numSamples;
	}

	/**
	 * @return the totalTimeTaken
	 */
	public long getTotalTimeTaken() {
		return totalTimeTaken;
	}

	/**
	 * @param totalTimeTaken
	 *            the totalTimeTaken to set
	 */
	public void setTotalTimeTaken(long totalTimeTaken) {
		this.totalTimeTaken = totalTimeTaken;
	}

	/**
	 * @return the minTimeTaken
	 */
	public long getMinTimeTaken() {
		return minTimeTaken;
	}

	/**
	 * @param minTimeTaken
	 *            the minTimeTaken to set
	 */
	public void setMinTimeTaken(long minTimeTaken) {
		this.minTimeTaken = minTimeTaken;
	}

	/**
	 * @return the maxTimeTaken
	 */
	public long getMaxTimeTaken() {
		return maxTimeTaken;
	}

	/**
	 * @param maxTimeTaken
	 *            the maxTimeTaken to set
	 */
	public void setMaxTimeTaken(long maxTimeTaken) {
		this.maxTimeTaken = maxTimeTaken;
	}

}
