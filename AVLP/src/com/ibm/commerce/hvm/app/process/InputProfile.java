/**
 * 
 */
package com.ibm.commerce.hvm.app.process;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author shupingye
 *
 */
public class InputProfile extends Properties {

	/*
	 * Input Profile Structure
	 * 
	 * - Output column list (one, optional) - Input column list (one, optional)
	 * - Input to output column mapping (one, required) - Derived column and
	 * mapping (multiple, required) - Value restricted column and mapping
	 * (multiple, required) - duplicate check column list (one, required) -
	 * Output column order (one, required) - Score model mapping (one, required)
	 * 
	 */

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = Logger.getLogger(InputProfile.class);
	
	// folder holding all input profiles
	// public static final String INPUT_PROFILE_PATH =
	// "D:/Work/MKTWorking/ListUpload/input_profile/";
	public static final String INPUT_PROFILE_PATH = "";
	public static final String INPUT_PROFILE_DEFAULT = "hvm_common_profile";
	public static final String INPUT_FILE_PATH_DEFAULT = "D:/Work/MKTWorking/ListUpload/in/";
	public static final String OUTPUT_FILE_PATH_DEFAULT = "D:/Work/MKTWorking/ListUpload/out/";
	public static final String OUPUT_FILE_PREFIX_DEFAULT = "Event_List_Upload_";
	// public static final String OUPUT_FILE_SUFFIX = SubmitterName +
	// CurrentDate;

	// minimum number columns
	public static final int MIN_NUM_COLUMNS = 5;
	// mapping operator
	public static final String MAP_OP = ">>";
	// map separator
	public static final String MAP_SEP = "|";
	// map double separator
	public static final String MAP_DOUBLE_SEP = "||";
	// header list of input parameter columns, used to generate output columns
	public static final String INPUT_PARAMETER_COLUMNS = "input_parameter_columns";
	// header list of input columns. Just to show the entire list of headers.
	public static final String INPUT_FILE_COLUMNS = "input_file_columns";
	// header list of output columns. Output columns are usually fixed across
	// all profiles.
	public static final String OUTPUT_FILE_COLUMNS = "output_columns";
	// column mapping property name
	public static final String IN_OUT_COLUMN_MAPPING = "input_output_column_mapping";
	// Other columns
	public static final String OTHER_COLUMNS = "other_columns";
	// derived columns
	public static final String DERIVED_COLUMNS = "derived_columns";
	// value derived column property has the form:
	// "value_restricted_column.<colomn name>=<derived column name>"
	// a value derived column is usually also a value restricted column
	public static final String DERIVED_COLUMN_PREFIX = "derived_column.";
	// value restricted columns - list of value restricted columns
	public static final String VALUE_RESTRICTED_COLUMNS = "value_restricted_columns";
	// value restricted column property takes the form:
	// "value_restricted_column.<output_colomn header>={value map}"
	public static final String VALUE_RESTRICT_COLUMN_PREFIX = "value_restricted_column.";
	public static final String FORMAT_COLUMN_VALUES = "format_column_values.";
	public static final String PHONE_FORMAT = "NA_PHONE_FORMAT";
	// duplicate check column
	public static final String DUP_CHECK = "dup_check_column.";
	public static final String INPUT_VAR_INDICATOR = "$input_";
	
	//output validation
	public static final String REQUIRED_OUTPUT_COLUMNS = "required_output_columns";
	public static final String REQUIRED_OUTPUT_CHOICE_COLUMNS = "required_output_choice_columns"; // note MAP_DOUBLE_SEP used here
	public static final String INPUT_COLUMN_CONFLICTS = "input_column_conflicts";

	private String profileName;

	// Call this constructor and then loadProfile (see below) if profile
	// (properties file) is placed in Java classpath
	public InputProfile() {
		super();
	}

	public InputProfile loadDefaultProfile() throws IOException {
		return loadProfileInPath(INPUT_PROFILE_DEFAULT);
	}

	public InputProfile loadProfileInPath(String profileName) throws IOException {
		if (!profileName.endsWith(".properties")) {
			profileName += ".properties";
		}
		setProfileName(profileName);
		ClassLoader classLoader = getClass().getClassLoader();
		// find the properties file in the class path by profileName
		InputStream is = classLoader.getResourceAsStream(profileName);
		load(is);
		//list(System.out);
		is.close();
		return this;
	}

	/**
	 * 
	 * @param profilePath
	 * @param profileName
	 * @return
	 * @throws IOException
	 */
	public InputProfile loadProfile(String profilePath, String profileName) throws IOException {
		this.profileName = profilePath + "/" + profileName;
		Reader reader = new FileReader(profileName);
		load(reader);
		//list(System.out);
		reader.close();
		return this;
	}

	/**
	 * @return the profileName
	 */
	public String getProfileName() {
		return profileName;
	}

	/**
	 * @param profileName
	 *            the profileName to set
	 */
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

}
