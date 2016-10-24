/**
 * 
 */
package com.ibm.commerce.hvm.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ibm.commerce.hvm.app.process.ColumnProcess;
import com.ibm.commerce.hvm.app.process.InputProfile;

/**
 * @author shupingye
 * 
 * Note: HashTable look up is case sensitive (due to hashing) by default. It will make our lives much easier if 
 * we can make it case insensitive. To achieve that, the approach here is to convert all the keys to lower case when
 * they are inserted to a HashTable and do the look up later with that conversion in mind. Note that the conversion
 * is done at runtime and behind the scenes. In the properties file, the keys can be maintained in any case forms.
 * See ColumnProcess.getValue(), createValueMap(), and other methods for details on this implementation.
 * 
 */
public class VendorListProcessApp {

	//inout parameters
	private String inputFileName;
	private String outputFileName;
	private String profileName;
	private String listSubmitter;
	private String listDate;
	private String campaignID;
	private String b2cInterest;
	private String b2bInterest;
	private String caInterest;
	private String scoreModel;
	
	//processing varables
	private int rowCount = 0; // count of value lines, header line excluded
	private InputProfile profile;
	
	/**
	 * 
	 */
	public VendorListProcessApp(String[] args) {
		super();
		/*
		//Command line parameter value (e.g., args[x]) must not contain space - it's deadly and difficult to debug
		//For input parameter requirement and settings, see "input_parameters" in the profile

		profileName = args[0].substring(args[0].indexOf("=")+1); 
		
		//load profile from file system with system path specified - out of Java class path typically
		//Note: INPUT_PROFILE_PATH is a constant, not a property in profile
		//profileName = InputProfile.INPUT_PROFILE_PATH + profileName;
		//profile = new InputProfile(profileName);
		
		//note: input profile name does not include path info. The loadProfile(profileName) method
		// is to find the properties file by name within the current (default) class path
		System.out.println("Loading profile: " + profileName);
		
		// load profile placed in Java classpath - without system path specified
		profile = new InputProfile();
		// The loadProfile(profileName) finds the profile (properties file) by searching in Java class path
		profile.loadProfile(profileName);
		inputFileName = args[1].substring(args[1].indexOf("=")+1);
		inputFileName = InputProfile.INPUT_FILE_PATH + inputFileName;
		SimpleDateFormat formater = new SimpleDateFormat("MM-dd-yyyy hh-mm-ss");
		outputFileName = args[2].substring(args[2].indexOf("=")+1);
		//deal with business parameters. Output columns will be generated on these parameters
		listSubmitter = args[3];
		listDate = args[4];
		campaignID = args[5];
		b2cInterest = args[6];
		caInterest = args[7];
		b2bInterest = args[8];
		scoreModel = args[9];
		
		//create output file name combining input parameters
		String submitterLastName = listSubmitter.substring(listSubmitter.indexOf("=")+1);
		//Note: command line parameter value (e.g., args[x]) must not contain space - it's deadly and difficult to debug
		int lastSpaceIndex = submitterLastName.lastIndexOf(' '); // no space is allowed in commandline args
		int lastUnderscoreIndex = submitterLastName.lastIndexOf('_');
		if(lastSpaceIndex > 0)
			submitterLastName = submitterLastName.substring(lastSpaceIndex + 1);
		else if(lastUnderscoreIndex > 0)
			submitterLastName = submitterLastName.substring(lastUnderscoreIndex + 1);
			
		outputFileName = InputProfile.OUTPUT_FILE_PATH + 
				scoreModel.substring(0, scoreModel.indexOf("=")) + "_" +
				outputFileName + "_" + submitterLastName + "_" +
				formater.format(Calendar.getInstance().getTime()) + ".txt";
		outputFileName = StringUtils.replaceChars(outputFileName, ' ', '_'); //Java hates filenames with white spaces
		
		String inputParameterColumns = 
				listSubmitter + "|" +
				listDate + "|" +
				campaignID + "|" +
				b2cInterest + "|" +
				caInterest + "|" +
				b2bInterest + "|" +
				scoreModel;
		profile.setProperty("input_parameter_columns", inputParameterColumns);
		
		System.out.println(inputFileName);
		System.out.println(outputFileName);
		System.out.println(listSubmitter);
		System.out.println(listDate);
		System.out.println(campaignID);
		System.out.println(b2cInterest);
		System.out.println(caInterest);
		System.out.println(b2bInterest);
		System.out.println(scoreModel);
	}
	
	public void process(){
		buildColumnProcessorListFromInputFile();
		writeOutputColumnsToOutputFileInOrder();
	}
	
	public void buildColumnProcessorListFromInputFile(){
		rowCount = 0;
		BufferedReader br = null;
		try {
	         br = new BufferedReader(new FileReader(inputFileName));
	         String headerLine = br.readLine();
	         System.out.println(headerLine);
	         buildColumnProcessorListByheader(headerLine);
	         String valueLine = null;
	         while ((valueLine = br.readLine()) != null) {
	            //System.out.println(valueLine);
	            addValuesToColumnProcessorList(valueLine);
	            rowCount += 1;
	         }
	         System.out.println("Row count = " + rowCount);
		}catch(Exception e){
			System.err.println("Error loading input file " + inputFileName + ". Error message: " + e.getMessage());
		}finally{
			try{
				if(br != null)
					br.close();
			}catch(IOException ioe){
				System.err.println(ioe.getMessage());
			}
		}
	}
	
	public void writeOutputColumnsToOutputFileInOrder(){
		//First, create a output column only list and order it according to the order in the output_columns property
		List<ColumnProcess> outCPList = new ArrayList<ColumnProcess>();
		String outHeaders = profile.getProperty(InputProfile.OUTPUT_FILE_COLUMNS);
		String[] outHeaderArr = StringUtils.splitPreserveAllTokens(outHeaders, InputProfile.MAP_SEP);
		for(String outHeader : outHeaderArr){
			if(outHeader.equalsIgnoreCase("$score_model"))
				outHeader = scoreModel.substring(0, scoreModel.indexOf("=")); //ex of scoreModel: "Resource_Download=Yes"
			for(ColumnProcess cp : columnProcessorList){
				if(cp.getOutputHeader() != null && cp.getOutputHeader().equalsIgnoreCase(outHeader)){
					outCPList.add(cp);
					break; //for
				}
			}
		}
	
		// writing file
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter(outputFileName));
			String headerLine = "";
			for(ColumnProcess cp : outCPList){
				headerLine += '\t';
				headerLine += cp.getOutputHeader();
			}
			//headerLine.replaceFirst("t", "");
			headerLine = headerLine.substring(1);
			bw.write(headerLine);
			bw.newLine();
			for(int i=0; i<rowCount; i++){
				String valueLine = "";
				for(ColumnProcess cp : outCPList){
					valueLine += '\t';
					valueLine += cp.getValue(i);
				}
				valueLine = valueLine.substring(1);
				//System.out.println(i + " befor valueLine");
				bw.write(valueLine);
				//System.out.println(i + " befor newline");
				bw.newLine();
				//System.out.println(i + " after newline");
			}
		}catch(Exception e){
			System.err.println("Error processing file " + outputFileName + ". Error message: " + e.getMessage());
		}finally{
			try{
				if(bw != null)
					bw.close();
			}catch(IOException ioe){
				System.err.println(ioe.getMessage());
			}
		}
		*/
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		VendorListProcessApp vendorListProcessApp = new VendorListProcessApp(args);
		//venderListProcessApp.process();
	}

}
