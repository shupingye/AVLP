/**
 * 
 */
package com.ibm.commerce.hvm.app.process;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.commerce.hvm.util.HVMUtils;

/**
 * @author shupingye
 * 
 */
public class VendorListProcess {
	
	static Logger logger = Logger.getLogger(VendorListProcess.class);

	ProcessData processData;
	
	private int rowCount = 0;
	private int numInputHeaders = 0;
	private Set<String> inputHeaderSet = new HashSet<String>();
	private Set<String> outputHeaderSet = new HashSet<String>();
	private InputProfile profile;
	private List<ColumnProcess> columnProcessList;
	//private Map<String, ColumnProcess> inputOutputColumnMap;
	
	
	public VendorListProcess() {
		super();
	}
	
	/**
	 * @throws IOException 
	 * 
	 */
	public VendorListProcess(ProcessData processData) throws IOException {
		super();
		this.processData = processData;
		if(processData.getProfileName() == null ||
			processData.getProfileName().trim().length() < 1 ||
			processData.getProfileName().equalsIgnoreCase(InputProfile.INPUT_PROFILE_DEFAULT)){
			
			processData.setProfileName(InputProfile.INPUT_PROFILE_DEFAULT);
		}
		
		profile = new InputProfile();
		int i = processData.getProfileName().lastIndexOf('/');
		if(i < 0)
			profile.loadProfileInPath(processData.getProfileName());
		else
			profile.loadProfile(processData.getProfileName().substring(0, i), processData.getProfileName().substring(i+1));
	}
	
	public ProcessData process(){
		try{
			buildColumnProcessFromInput();
			mapInputColumnListToOutputColumnList();
			appendDerivedColumnsToColumnProcessList();
			appendInputParameterColumnsToColumnProcessList();
			appendOtherColumnsToColumnProcessList();
			prepareValueRestrictedColumns();
			validateOutput(); //required column check, dup check, format check, etc
			writeOutputColumnsToOutputInOrder();
		}catch(HVMException he){
			if(processData.getStatus() == ProcessData.STATUS_ERR_FATAL)
				return processData;
		}catch(Exception e){
			logger.error(e.getMessage());
			e.printStackTrace();
			processData.setStatus(ProcessData.STATUS_ERR_FATAL);
			processData.addErrMssage("Exception: " + e.getMessage());
			return processData;
		}
		if(processData.getStatus() == ProcessData.STATUS_NONE)
			processData.setStatus(ProcessData.STATUS_OK);
		return processData;
	}
	
	public void buildColumnProcessFromInput() throws HVMException {
		List<String> inputStrList = processData.getInPrimary();
		if(inputStrList == null || inputStrList.isEmpty()){
			processData.setStatus(ProcessData.STATUS_ERR_FATAL);
			String errMsg = ProcessData.MSG_ERR_INVALID_INPUT + " - Input List null or empty";
			processData.addErrMssage(errMsg);
			logger.fatal(errMsg);
			throw new HVMException(errMsg);
		}
        buildColumnProcessListByheader(inputStrList.get(0));
        for(int i = 1; i<inputStrList.size(); i++){
            addValuesToColumnProcessList(inputStrList.get(i));
            rowCount += 1;
        }
	}

	private void buildColumnProcessListByheader(String headerLine) throws HVMException {
		columnProcessList = new ArrayList<ColumnProcess>();
		String[] headers = StringUtils.splitPreserveAllTokens(headerLine, '\t');
		numInputHeaders = headers.length;
		if(InputProfile.MIN_NUM_COLUMNS > numInputHeaders){
			processData.setStatus(ProcessData.STATUS_ERR_FATAL);
			String errMsg = ProcessData.MSG_ERR_INVALID_INPUT + 
				String.format(" - input columns (%1$s) is less than the minimum required", numInputHeaders);
			processData.addErrMssage(errMsg);
			logger.fatal(errMsg);
			throw new HVMException(errMsg);
		}
		
		for(String header : headers){
			inputHeaderSet.add(header.toLowerCase()); // for later use
			ColumnProcess cp = new ColumnProcess(profile);
			cp.setInputHeader(header);
			columnProcessList.add(cp);
		}
		checkInputColumnConflict();
	}
	
	private void addValuesToColumnProcessList(String valueLine) throws HVMException {
		valueLine = replaceTabsInQuotes(valueLine);
		String[] values = StringUtils.splitPreserveAllTokens(valueLine, '\t');
		if(values.length != numInputHeaders){
			processData.setStatus(ProcessData.STATUS_ERR_FATAL);
			String msg = String.format("Bad data: cell count at row %1$s is different from that of header", rowCount + 1);
			logger.fatal(msg);
			processData.addErrMssage(msg);
			throw new HVMException(msg);
		}
			
		for(int i=0; i<values.length; i++){
			columnProcessList.get(i).addValue(values[i]);
		}
	}

	
	public void mapInputColumnListToOutputColumnList(){
		String ioMapString = profile.getProperty(InputProfile.IN_OUT_COLUMN_MAPPING);
		String[] ioMapArr = StringUtils.splitPreserveAllTokens(ioMapString, InputProfile.MAP_SEP);
		Map<String, String> ioMap = new HashMap<String, String>();
		for(String ioStr : ioMapArr){
			String[] ioPair = StringUtils.splitPreserveAllTokens(ioStr, InputProfile.MAP_OP);
			ioMap.put(ioPair[0].toLowerCase(), ioPair[2]);
		}
		for(ColumnProcess cp : columnProcessList){
			String inputHeader = cp.getInputHeader().toLowerCase();
			String outputHeader = ioMap.get(inputHeader);
			if(outputHeader != null && outputHeader.trim().length() > 0 ){
				cp.setOutputHeader(outputHeader);
				outputHeaderSet.add(outputHeader.toLowerCase());
			}
		}
	}

	public void appendDerivedColumnsToColumnProcessList(){
		String columnListString = profile.getProperty(InputProfile.DERIVED_COLUMNS);
		String[] columnPairs = StringUtils.splitPreserveAllTokens(columnListString, InputProfile.MAP_SEP);
		for(String columnPair : columnPairs){
			String[] kv = StringUtils.splitPreserveAllTokens(columnPair, InputProfile.MAP_OP);
			//skip if derived header exists in input
			if(outputHeaderSet.contains(kv[2].toLowerCase()))
				continue;
			outputHeaderSet.add(kv[2]);
			ColumnProcess cp = new ColumnProcess(profile);
			cp.setOutputHeader(kv[2].toLowerCase()); 
			for(ColumnProcess parentCp : columnProcessList){
				if(parentCp.getOutputHeader() != null && parentCp.getOutputHeader().equalsIgnoreCase(kv[0])){
					cp.setValueList(parentCp.getValueList());
					//System.err.println("derived CP " + cp.getOutputHeader() + " valueList size = " + cp.getValueList().size());
					cp.setDerived(true);
					cp.setValueRestricted(true);
				}
			}
			columnProcessList.add(cp);
		}
		
	}
	
	public void appendInputParameterColumnsToColumnProcessList(){
		String[] columns = getTokensByProperty(InputProfile.INPUT_PARAMETER_COLUMNS, InputProfile.MAP_SEP);
		for(String columnStr : columns){
			String[] kv = StringUtils.splitPreserveAllTokens(columnStr, InputProfile.MAP_OP);
			//Note: when there is no value on the right side of the separator, StringUtils.splitPreserveAllTokens() produces an array consisting only the left side
			//element. In this case array length will be 1. So we check for that.
			String kv0 = getInputParameterValue(kv[0]);
			String kv2 = getInputParameterValue(kv[2]);
			if(outputHeaderSet.contains(kv0.toLowerCase()))
				continue;
			outputHeaderSet.add(kv0.toLowerCase());
			generateOutputColumnByColumnHeaderAndCoverAllValue(kv0, kv2);
		}
	}
	
	private String getInputParameterValue(String inVal){
		String outV = inVal;
		if(inVal.startsWith(InputProfile.INPUT_VAR_INDICATOR)){
			int i = inVal.indexOf('_');
			inVal = "get_" + inVal.substring(i+1);
			inVal = HVMUtils.camelize(inVal, "_", true);
			try {
				outV = HVMUtils.invokeGetStringMethod(processData, inVal);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return outV;
	}
	
	public void appendOtherColumnsToColumnProcessList(){
		String[] columns = getTokensByProperty(InputProfile.OTHER_COLUMNS, InputProfile.MAP_SEP);
		for(String columnStr : columns){
			String[] kv = StringUtils.splitPreserveAllTokens(columnStr, InputProfile.MAP_OP);
			if(outputHeaderSet.contains(kv[0].toLowerCase()))
				continue;
			outputHeaderSet.add(kv[0].toLowerCase());
			generateOutputColumnByColumnHeaderAndCoverAllValue(kv[0], kv[2]);
		}
	}
	
	private void generateOutputColumnByColumnHeaderAndCoverAllValue(String ColumnHeader, String coverAllValue){
		ColumnProcess cp = new ColumnProcess(profile);
		cp.setOutputHeader(ColumnHeader);
		for(int i=0; i<rowCount; i++){
			cp.addValue(coverAllValue);
		}
		columnProcessList.add(cp);
	}
	
	public void prepareValueRestrictedColumns(){
		String[] vrcArr = getTokensByProperty(InputProfile.VALUE_RESTRICTED_COLUMNS, InputProfile.MAP_SEP);
		Map<String, String> vrcMap = new HashMap<String, String>();
		for(String vrc : vrcArr){
			String[] vrcKV = StringUtils.splitPreserveAllTokens(vrc, InputProfile.MAP_OP);
			vrcMap.put(vrcKV[0].toLowerCase(), vrcKV[2]);
		}
		for(ColumnProcess cp : columnProcessList){
			String outHeader = cp.getOutputHeader();
			if(outHeader != null && vrcMap.containsKey(outHeader.toLowerCase())){
				cp.setValueRestricted(true);
				cp.createValueMap(profile);
				cp.setValueLookupWithContains(vrcMap.get(outHeader.toLowerCase()).equalsIgnoreCase("Yes") ? true : false);
			}
		}
	}
	
	public void validateOutput(){ // required column check, dub check, format etc
		// validate required columns
		String[] requiredColumns = getTokensByProperty(InputProfile.REQUIRED_OUTPUT_COLUMNS, InputProfile.MAP_SEP);
		for(String rc : requiredColumns){
			if(!outputHeaderSet.contains(rc.toLowerCase())){
				String msg = String.format("Missing required output column '%1$s", rc);
				processData.addErrMssage(msg);
				logger.error("Validation error: " + msg);
			}
		}
		
		// validate required choice columns
		// park this check since, as of now,  columns of choice are all validated at user input
		/*
		String[] requiredChoiceColumnCat = getTokensByProperty(InputProfile.REQUIRED_OUTPUT_CHOICE_COLUMNS, InputProfile.MAP_DOUBLE_SEP);
		logger.debug(requiredChoiceColumnCat);
		for(String rccc : requiredChoiceColumnCat){
			StringUtils.
			for(String rc : requiredColumns){
				if(!outputHeaderSet.contains(rc.toLowerCase())){
					String msg = String.format("Missing required output column '%1$s", rc);
					processData.addErrMssage(msg);
					logger.error("Validation error: " + msg);
				}
			}
		}
		*/
		
		// validate column values in individual columns
		for(ColumnProcess cp : columnProcessList){
			if(cp.getOutputHeader() != null && cp.getOutputHeader().trim().length() > 0){
				cp.validate(processData);
			}
		}
	}

	public void checkInputColumnConflict(){
		String conflicts = profile.getProperty(InputProfile.INPUT_COLUMN_CONFLICTS);
		String[] conflictsArr = StringUtils.splitPreserveAllTokens(conflicts, InputProfile.MAP_SEP);
		for(String ccPair : conflictsArr){
			String[] ccArr = StringUtils.splitPreserveAllTokens(ccPair, InputProfile.MAP_OP);
			if(inputHeaderSet.contains(ccArr[0].toLowerCase()) && inputHeaderSet.contains(ccArr[2].toLowerCase())){
				String msg = String.format("Input contains conflict column headers: '%1$s' and '%2$s'", ccArr[0], ccArr[2]);
				processData.addErrMssage(msg);
				logger.error("Validation error: " + msg);
			}
		}
	}
	
	public void writeOutputColumnsToOutputInOrder(){
		List<ColumnProcess> outCPList = new ArrayList<ColumnProcess>();
		String outHeaders = profile.getProperty(InputProfile.OUTPUT_FILE_COLUMNS);
		String[] outHeaderArr = StringUtils.splitPreserveAllTokens(outHeaders, InputProfile.MAP_SEP);
		for(String outHeader : outHeaderArr){
			for(ColumnProcess cp : columnProcessList){
				if(cp.getOutputHeader() != null && cp.getOutputHeader().equalsIgnoreCase(outHeader)){
					outCPList.add(cp);
					break; //for
				}
			}
		}
	
		List<String> outList = new LinkedList<String>();
		String headerLine = "";
		for(ColumnProcess cp : outCPList){
			headerLine += '\t';
			headerLine += cp.getOutputHeader();
		}
		//headerLine.replaceFirst("t", "");
		headerLine = headerLine.substring(1);
		//headerLine += "\n";
		outList.add(headerLine);
		for(int i=0; i<rowCount; i++){
			String valueLine = "";
			for(ColumnProcess cp : outCPList){
				valueLine += '\t';
				valueLine += cp.getValue(i);
			}
			//valueLine.replaceFirst("t", "");
			valueLine = valueLine.substring(1);
			//valueLine += "\n";
			outList.add(valueLine);
		}
		processData.setOutPrimary(outList);
	}
	
	private String[] getTokensByProperty(String prop, String sep){
		String vProp = profile.getProperty(prop);
		if(vProp == null)
			return null;
		return StringUtils.splitPreserveAllTokens(vProp, sep);
	}
	
	private String replaceTabsInQuotes(String inValueLine){
		logger.debug("inValueLine: " + inValueLine);
		String outValueLine = "";
		if(inValueLine == null)
			return outValueLine;
		/*
		inValueLine = StringUtils.remove(inValueLine, '\n');
		inValueLine = StringUtils.remove(inValueLine, '\r');
		*/
		boolean checkQuote = true;
		while(checkQuote){
			int iQuote1 = inValueLine.indexOf('"');
			if(iQuote1 < 0){
				outValueLine += inValueLine;
				checkQuote = false;
			}else{
				outValueLine += inValueLine.substring(0, iQuote1);
				int iQuote2 = inValueLine.indexOf('"', iQuote1+1);
				logger.warn(String.format("String in quotes '%4$s': quote 1 index = %1$s, quote 2 index = %2$s, inValueLine length = %3$s", iQuote1, iQuote2, inValueLine.length(), inValueLine));
				
				String strInQuotes = inValueLine.substring(iQuote1, iQuote2+1);
				logger.debug("String in quotes found: " + strInQuotes);
				strInQuotes = StringUtils.replaceChars(strInQuotes, '\t', ' ');
				logger.debug("String in quotes replaced: " + strInQuotes);
				outValueLine += strInQuotes;
				if(inValueLine.length() == iQuote2+1){ // closing quote is at the end of the input string
					inValueLine = "";
				}else{
					inValueLine = inValueLine.substring(iQuote2+1);
				}
			}
		}
		logger.debug("otValueLine: " + outValueLine);
		return outValueLine;
	}
}
