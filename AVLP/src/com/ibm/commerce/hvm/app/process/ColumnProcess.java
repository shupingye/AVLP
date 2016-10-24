/**
 * 
 */
package com.ibm.commerce.hvm.app.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.commerce.hvm.service.VendorListProcessService;

/**
 * @author shupingye
 *
 */
public class ColumnProcess {
	
	static Logger logger = Logger.getLogger(ColumnProcess.class);
	
	private String inputHeader;
	private String outputHeader;
	private List<String> valueList;
	private boolean valueRestricted = false;
	private boolean derived = false;
	private boolean formatted = false;
	//private List<String> derivingValueList;
	private Map<String, String> valueMap;
	private boolean valueLookupWithContains;
	private InputProfile profile = null;
	
	/**
	 * 
	 */
	public ColumnProcess(InputProfile profile) {
		this.profile = profile;
		this.valueList = new ArrayList<String>();
	}

	/**
	 * @return the inputHeader
	 */
	public String getInputHeader() {
		return inputHeader;
	}

	/**
	 * @param inputHeader the inputHeader to set
	 */
	public void setInputHeader(String inputHeader) {
		this.inputHeader = inputHeader;
	}

	/**
	 * @return the outputHeader
	 */
	public String getOutputHeader() {
		return outputHeader;
	}

	/**
	 * @param outputHeader the outputHeader to set
	 */
	public void setOutputHeader(String outputHeader) {
		this.outputHeader = outputHeader;
	}

	/**
	 * @return the valueList
	 */
	public List<String> getValueList() {
		return valueList;
	}

	/**
	 * @param valueList the valueList to set
	 */
	public void setValueList(List<String> valueList) {
		this.valueList = valueList;
	}

	/**
	 * @return the valueRestricted
	 */
	public boolean isValueRestricted() {
		return valueRestricted;
	}

	/**
	 * @param valueRestricted the valueRestricted to set
	 */
	public void setValueRestricted(boolean valueRestricted) {
		this.valueRestricted = valueRestricted;
		createValueMap(profile);
	}
	
	/**
	 * @return the valueLookupWithContains
	 */
	public boolean isValueLookupWithContains() {
		return valueLookupWithContains;
	}

	/**
	 * @param valueLookupWithContains the valueLookupWithContains to set
	 */
	public void setValueLookupWithContains(boolean valueLookupWithContains) {
		this.valueLookupWithContains = valueLookupWithContains;
	}

	public void validate(ProcessData processData) {
		// check value format
		String format = profile.getProperty(InputProfile.FORMAT_COLUMN_VALUES + outputHeader.toLowerCase());
		if(format != null){
			if(format.equals(InputProfile.PHONE_FORMAT)){
				formatNAPhoneNumber(valueList);
			}
		}
		
		// check for duplicate if applicable
		String dupCheck = profile.getProperty(InputProfile.DUP_CHECK + outputHeader.toLowerCase());
		if(dupCheck != null && dupCheck.equalsIgnoreCase("Yes")){
			dupCheck(outputHeader, valueList, processData);
		}
		
		// check for blanks
		for(String v : valueList){
			if(v == null || v.trim().length() < 1){
				String msg = String.format("Output column '%1$s' contains blank(s)", getOutputHeader());
				processData.addWarnMssage(msg);
				break;
			}
		}
		
	}

	/**
	 * @return the derived
	 */
	public boolean isDerived() {
		return derived;
	}

	/**
	 * @param derived the derived to set
	 */
	public void setDerived(boolean derived) {
		this.derived = derived;
	}

	/**
	 * @return the valueMap
	 */
	public Map<String, String> getValueMap() {
		return valueMap;
	}

	/**
	 * @param valueMap the valueMap to set
	 */
	public void setValueMap(Map<String, String> valueMap) {
		this.valueMap = valueMap;
	}
	
	public String getValue(int rowIndex){
		String value = valueList.get(rowIndex);
		if(!valueRestricted)
			return value;
		if(isValueLookupWithContains()){
			value = lookupValueOfContainingKey(value.toLowerCase(), valueMap);
		}else{//exact match
			value = valueMap.get(value.toLowerCase());
		}
		return value == null? "" : value;
	}
	
	public String lookupValueOfContainingKey(String containingKey, Map<String, String> vMap){
		if(containingKey == null || vMap == null || vMap.isEmpty()){
			return "";
		}
		containingKey = containingKey.toLowerCase();
		String v = "";
		Set<String> keySet = vMap.keySet();
		Iterator<String> itr = keySet.iterator();
		while(itr.hasNext()){
			String key = itr.next();
			if(containingKey.contains(key.toLowerCase())){
				v = vMap.get(key);
			}
		}
		return v;
	}
	
	public void addValue(String value){
		//System.out.print("add value " + value + " for input header " + getInputHeader());
		//remove quotes. Note that StringUtils.replace does not work for this
		value = value.trim();
		if(value.startsWith("\"")){
			value = value.substring(1, value.length()-1);
		}
		valueList.add(value);
		//System.out.println(". Done");
	}
	
	public void createValueMap(InputProfile profile){
		String mapString = profile.getProperty(InputProfile.VALUE_RESTRICT_COLUMN_PREFIX + getOutputHeader().toLowerCase());
		if(mapString == null){
			logger.error("Critical error - cannot find the mapping property for " + InputProfile.VALUE_RESTRICT_COLUMN_PREFIX + getOutputHeader());
		}else{
			String[] mapPairs = StringUtils.splitPreserveAllTokens(mapString, InputProfile.MAP_SEP);
			valueMap = new HashMap<String, String>();
			for(String mapPair : mapPairs){
				String[] pairSplit = StringUtils.splitPreserveAllTokens(mapPair, InputProfile.MAP_OP);
				String k = pairSplit[0];
				String v = pairSplit[2];
				valueMap.put(k.toLowerCase(), v);
			}
			logger.debug("Value map built for " + InputProfile.VALUE_RESTRICT_COLUMN_PREFIX + getOutputHeader());
			logger.debug(valueMap.toString());
		}
	}
	
	public void formatNAPhoneNumber(List<String> phoneNumberList){
		String regex = "^\\(?([0-9]{3})\\)?[-.\\s]?([0-9]{3})[-.\\s]?([0-9]{4})$";
		Pattern pattern = Pattern.compile(regex);
		int i = 0;
		for(String phoneNunmber : phoneNumberList)
		{
			//remove all spaces in phone number
			phoneNunmber = StringUtils.remove(phoneNunmber, ' ');
			//remove all "." in phone number
			phoneNunmber = StringUtils.remove(phoneNunmber, '.');
			//remove all "," in phone number
			phoneNunmber = StringUtils.remove(phoneNunmber, ',');
			//remove all "-" in phone number
			phoneNunmber = StringUtils.remove(phoneNunmber, '-');
			if(phoneNunmber.length() == 11 && phoneNunmber.startsWith("1")) {// remove starting "1" for NA numbers
				phoneNunmber = phoneNunmber.substring(1);
			}
		    Matcher matcher = pattern.matcher(phoneNunmber);
		    //If phone number is correct then format it to (123)-456-7890, else do nothing
		    if(matcher.matches())
		    {
		    	phoneNunmber = matcher.replaceFirst("($1)$2-$3");
		    	phoneNumberList.set(i, phoneNunmber);
		    }
		    i += 1;
		}
	}
	
	public void dupCheck(String header, List<String> valueList, ProcessData processData){
		for(int i=0; i<valueList.size(); i++) {
			String currVal = valueList.get(i);
			for(int j=i; j<valueList.size(); j++){
				if(i == j){
					continue;
				}
				if(currVal.equalsIgnoreCase(valueList.get(j))){ //dup found
					String msg = String.format("Output column '%1$s' has duplicate value '%4$s' at rows %2$s and %3$s", header, i, j, currVal);
					processData.addWarnMssage(msg);
					logger.info(msg);
				}
			}
		}
	}
}
