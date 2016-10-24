package com.ibm.commerce.hvm.app.process;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class ProcessData {
	
	public static final int STATUS_NONE = -1;
	public static final int STATUS_OK = 0;
	public static final int STATUS_ERR_REQUIRED_COLUMN_MISSING = 1;
	public static final int STATUS_ERR_DUPLICATE_VIOLATION = 2;
	public static final int STATUS_ERR_FATAL = 3;
	
	public static final String MSG_STATUS_OK = "Processing data completed successfully";
	public static final String MSG_ERR_REQUIRED_COLUMN_MISSING = "%1$s is required. No corresponding input column is found per current mapping profile";
	public static final String MSG_ERR_INVALID_INPUT = "Invalid input";
	
	public static final String COLUMN_HEADER_LISTSUBMITTER = "";
	
	
	//input parameters
	private String inputFileName = "";
	private String outputFileName = "";
	private String profileName = "";
	private String listSubmitterName = "";
	private String listSubmittedDate = "";
	private String unicaCampaignId = "";
	private String behavior = "";
	private String interestArea = "";
	/*
	private String b2cInterest;
	private String b2bInterest;
	private String caInterest;
	*/
	
	// primary data
	private List<String> inPrimary;
	private List<String> outPrimary;
	
	//output parameters
	private int status = STATUS_NONE;
	private List<String> errMssages = new LinkedList<String>();
	private List<String> warnMessages = new LinkedList<String>();
	
	public ProcessData() {
		super();
	}

	/**
	 * @return the inputFileName
	 */
	public String getInputFileName() {
		return inputFileName;
	}

	/**
	 * @param inputFileName the inputFileName to set
	 */
	public void setInputFileName(String inputFileName) {
		this.inputFileName = inputFileName;
	}

	/**
	 * @return the outputFileName
	 */
	public String getOutputFileName() {
		return outputFileName;
	}

	/**
	 * @param outputFileName the outputFileName to set
	 */
	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	/**
	 * @return the profileName
	 */
	public String getProfileName() {
		return profileName;
	}

	/**
	 * @param profileName the profileName to set
	 */
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	/**
	 * @return the listSubmitterName
	 */
	public String getListSubmitterName() {
		return listSubmitterName;
	}

	/**
	 * @param listSubmitterName the listSubmitterName to set
	 */
	public void setListSubmitterName(String listSubmitterName) {
		this.listSubmitterName = listSubmitterName;
	}

	/**
	 * @return the listSubmittedDate
	 */
	public String getListSubmittedDate() {
		return listSubmittedDate;
	}

	/**
	 * @param listSubmittedDate the listSubmittedDate to set
	 */
	public void setListSubmittedDate(String listSubmittedDate) {
		this.listSubmittedDate = listSubmittedDate;
	}

	/**
	 * @return the unicaCampaignId
	 */
	public String getUnicaCampaignId() {
		return unicaCampaignId;
	}

	/**
	 * @param unicaCampaignId the unicaCampaignId to set
	 */
	public void setUnicaCampaignId(String unicaCampaignId) {
		this.unicaCampaignId = unicaCampaignId;
	}

	/**
	 * @return the behavior
	 */
	public String getBehavior() {
		return behavior;
	}

	/**
	 * @param behavior the behavior to set
	 */
	public void setBehavior(String behavior) {
		this.behavior = behavior;
	}

	/**
	 * @return the interestArea
	 */
	public String getInterestArea() {
		return interestArea;
	}

	/**
	 * @param interestArea the interestArea to set
	 */
	public void setInterestArea(String interestArea) {
		this.interestArea = interestArea;
	}

	/**
	 * @return the inPrimary
	 */
	public List<String> getInPrimary() {
		return inPrimary;
	}

	/**
	 * @param inPrimary the inPrimary to set
	 */
	public void setInPrimary(List<String> inPrimary) {
		this.inPrimary = inPrimary;
	}

	/**
	 * @return the outPrimary
	 */
	public List<String> getOutPrimary() {
		return outPrimary;
	}

	/**
	 * @param outPrimary the outPrimary to set
	 */
	public void setOutPrimary(List<String> outPrimary) {
		this.outPrimary = outPrimary;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the errMssages
	 */
	public List<String> getErrMssages() {
		return errMssages;
	}

	/**
	 * @param errMssages the errMssages to set
	 */
	public void setErrMssages(List<String> errMssages) {
		this.errMssages = errMssages;
	}
	
	public String getSubmitterLastName(){
		String lastName = getListSubmitterName().trim();
		int i = lastName.lastIndexOf(' ');
		if(i < 0)
			i = lastName.lastIndexOf('_');
		if(i < 0)
			return lastName;
		return lastName.substring(i+1);
	}

	public void addErrMssage(String errMssage) {
		errMssages.add(errMssage);
	}
	
	/**
	 * @return the warnMessages
	 */
	public List<String> getWarnMessages() {
		return warnMessages;
	}

	/**
	 * @param warnMessages the warnMessages to set
	 */
	public void setWarnMessages(List<String> warnMessages) {
		this.warnMessages = warnMessages;
	}

	public void addWarnMssage(String warnMssage) {
		warnMessages.add(warnMssage);
	}
	

}
