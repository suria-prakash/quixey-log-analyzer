package com.quixey.performance.model;

public class TraceLogModel {

	private String typeAndSequence;
	private long startTime;
	private long stopTime;
	private String requestURI;
	private String httpStatusCode;
	private String exceptionMessage;
	private long requestProcessingTime;

	/**
	 * @return the typeAndSequence
	 */
	public String getTypeAndSequence() {
		return typeAndSequence;
	}

	/**
	 * @param typeAndSequence
	 *            the typeAndSequence to set
	 */
	public void setTypeAndSequence(String typeAndSequence) {
		this.typeAndSequence = typeAndSequence;
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            the startTime to set
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the stopTime
	 */
	public long getStopTime() {
		return stopTime;
	}

	/**
	 * @param stopTime
	 *            the stopTime to set
	 */
	public void setStopTime(long stopTime) {
		this.stopTime = stopTime;
	}

	/**
	 * @return the requestURI
	 */
	public String getRequestURI() {
		return requestURI;
	}

	/**
	 * @param requestURI
	 *            the requestURI to set
	 */
	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	/**
	 * @return the httpStatusCode
	 */
	public String getHttpStatusCode() {
		return httpStatusCode;
	}

	/**
	 * @param httpStatusCode
	 *            the httpStatusCode to set
	 */
	public void setHttpStatusCode(String httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}

	/**
	 * @return the exceptionMessage
	 */
	public String getExceptionMessage() {
		return exceptionMessage;
	}

	/**
	 * @param exceptionMessage
	 *            the exceptionMessage to set
	 */
	public void setExceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}

	/**
	 * @return the requestProcessingTime
	 */
	public long getRequestProcessingTime() {
		return requestProcessingTime;
	}

	/**
	 * @param requestProcessingTime
	 *            the requestProcessingTime to set
	 */
	public void setRequestProcessingTime(long requestProcessingTime) {
		this.requestProcessingTime = requestProcessingTime;
	}

}

