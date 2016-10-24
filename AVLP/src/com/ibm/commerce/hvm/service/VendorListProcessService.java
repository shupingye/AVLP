package com.ibm.commerce.hvm.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.commerce.hvm.app.process.ProcessData;
import com.ibm.commerce.hvm.app.process.VendorListProcess;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path("/files")
public class VendorListProcessService {
	static Logger logger = Logger.getLogger(VendorListProcessService.class);
	
	private static final String api_version = "1.01A rev.1";
	//private static final String JIMF_DIR = "/data";

	@Path("/version")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String returnVersion() {
		return "<p>Version: " + api_version + "</p>";
	}
	
	@GET
	@Path("/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadFilebyQuery(@QueryParam("filename") String fileName) {
		return download(fileName);
	}
	
	@GET
	@Path("/download/{filename}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadFilebyPath(@PathParam("filename")  String fileName) {
 		return download(fileName);
	}

	private Response download(String fileName) {		 
		Response response = null;
		
	    String content = "<?xml version='1.0' encoding='UTF-8'?>\n<books>\n	<book>\n" +
	    		"<title>Some Book</title>\n	</book>\n	<book>\n" +
	    		"<title>Another book</title>\n	</book>\n</books>";
	    
		ResponseBuilder builder = Response.ok(content.getBytes(StandardCharsets.UTF_8));
		builder.header("Content-Disposition", "attachment; filename=" + "test_download.txt");
		response = builder.build();

		return response;
	}
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	//@Produces("text/html")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response uploadFile(
			@FormDataParam("profile") String profile,
			@FormDataParam("interest_area") String interestArea,
			@FormDataParam("behavior") String behavior,
			@FormDataParam("resp_code") String respCode,
			@FormDataParam("submit_name") String submitName,
			@FormDataParam("submit_date") String submitDate,
			@FormDataParam("validate") String validate,
			@FormDataParam("generate") String generate,
			@FormDataParam("file") InputStream inputStream,
			@FormDataParam("file") FormDataContentDisposition fdcd) {
 
		logger.info("Validate = " + validate + " Generate = " + generate);
		logger.info(profile);
		logger.info(interestArea);
		logger.info(behavior);
		logger.info(respCode);
		logger.info(submitName);
		logger.info(submitDate);
		
		String action = validate == null ? generate : validate;
		String fileName = fdcd.getFileName();
		if(fileName.indexOf(':') > 0){
			fileName = fileName.substring(fileName.indexOf(':') + 1);
		}
		fileName = StringUtils.replaceChars(fileName, ' ', '_');
		logger.debug(fileName);
		
		ProcessData processData = new ProcessData();
		processData.setInputFileName(fileName);
		processData.setInterestArea(interestArea);
		processData.setListSubmittedDate(submitDate);
		processData.setUnicaCampaignId(respCode);
		processData.setListSubmitterName(submitName);
		processData.setProfileName(profile);
		processData.setBehavior(behavior);
		
	    Response response = null;
		//FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
		//java.nio.file.Path data = fs.getPath(JIMF_DIR);
		try {
			//Files.createDirectory(data);
			//java.nio.file.Path uploadFile = data.resolve(fileName); // /data/<fileName>
			//Files.copy(inputStream, uploadFile, StandardCopyOption.REPLACE_EXISTING);
			//Files.write(uploadFile, ImmutableList.of(content), StandardCharsets.UTF_8);
			//Files.write(uploadFile, content.getBytes());
			//List<String> sl = Files.readAllLines(uploadFile);
			List<String> sl = getStringsFromInputStream(inputStream);
			for(String s : sl){
				logger.debug(s);
			}
			processData.setInPrimary(sl);
			
			VendorListProcess vlp = new VendorListProcess(processData);
			processData = vlp.process();
		
			String outFileName = 
					behavior + "_" +
					fileName.substring(0, fileName.lastIndexOf('.')) + "_" +
					processData.getSubmitterLastName() + "_" +
					getCurrentDateTimeString().replace(' ', '_') +
					".txt";
			//java.nio.file.Path downloadFile = data.resolve(outFileName); // /data/<fileName>
			//Files.write(downloadFile, processData.getOutPrimary());
			//ResponseBuilder builder = Response.ok(Files.readAllBytes(downloadFile));
			if(action.equals(validate) || processData.getStatus() != ProcessData.STATUS_OK){
				String vMsg = buildValidationMessage(processData);
				
				response = Response.
						status(200).
						entity(vMsg).
						//type("text/plain").
						type("text/html").
						build();
			} else {
				byte[] outBytes = stringListToByteArray(processData.getOutPrimary());
				ResponseBuilder builder = Response.ok(outBytes);
				logger.debug("Output file: " + outFileName);
				builder.header("Content-Disposition", "attachment; filename=" + outFileName);
				response = builder.build();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response = Response.status(500).
				      entity("Processing cannot be done at this time. Please try later.").
				      type("text/plain").	//overrides @produces?
				      build();
		}
		return response;
		//return Response.status(200).entity(status.getBytes()).build();
	}
	
	private String buildValidationMessage(ProcessData processData){
		String vm = "";
		if(processData == null || processData.getStatus()  == ProcessData.STATUS_NONE){
			vm = "Validation failed";
		}else{
			vm = "-- VALIDATION STATUS --\n\n";
			vm += String.format("Records in/out:\t%1$s/%2$s\n\n", processData.getInPrimary().size() - 1, processData.getOutPrimary().size() - 1);
			vm += String.format("ERRORS: (%1$s)\n", processData.getErrMssages().size());
			//vm += "-----------\n";
			for(String e : processData.getErrMssages()){
				vm += e;
				vm += "\n";
			}
			vm += "\n";
			vm += String.format("WARNINGS: (%1$s)\n", processData.getWarnMessages().size());
			//vm += "-----------\n";
			for(String w : processData.getWarnMessages()){
				vm += w;
				vm += "\n";
			}
		}
		return createPage(vm);
	}
	
	private String makeItAnPopup(String msg){
		msg = "<script>alert(\"" + msg;
		msg += "\");";
		msg += " window.location=\"index.html\";";
		msg += "</script>";
		return msg;
	}
	
	public static List<String> getStringsFromInputStream(InputStream is) throws IOException {
		List<String> ls = new LinkedList<String>();

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		if(br != null) {
			String line = null;
			while ((line = br.readLine()) != null) {
				if(line.trim().length() > 1){
					String nextPart = null;
					// fix the problem with embedded line break (embed with quotes) in a line originated from Excel
					while(lineHasOddQuotes(line) && (nextPart = br.readLine()) != null){ 
						line += nextPart;
					}
					ls.add(line);
				}
			}
			br.close();
		}
		return ls;
	}
	
	public static boolean lineHasOddQuotes(String line){
		boolean odd = false;
		int qCount = StringUtils.countMatches(line, "\"");
		if((qCount & 1) != 0) 
			odd = true;
		return odd;
	}
	
	public static byte[] stringListToByteArray(List<String> ls) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);
		for (String s : ls) {
		    out.writeBytes(s);
		    out.writeBytes("\n");
		}
		return baos.toByteArray();
	}
	
	public static String getCurrentDateTimeString(){
		SimpleDateFormat formater = new SimpleDateFormat("MM-dd-yyyy hh-mm-ss");
		return formater.format(Calendar.getInstance().getTime());
	}
	
	private String createPage(String msg){
		String page = "";
		page += "<!DOCTYPE html>";
		page += "<html>";
		page += "<head>";
		page += "<meta charset=\"utf-8\" />";
		page += "<title>Process complete</title>";
		page += "<link rel=\"stylesheet\" href=\"include/bootstrap.min.css\">";
		//page += "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">";
		page += "<link href=\"include/avlp_styles.css\" rel=\"stylesheet\">";
		page += "</head>";
		page += "<body>";
		page += "	<div class=\"container\">";
		page += "		<div class=\"header\">";
		page += "			<h1 class=\"custom\">Process complete</h1>";
		//page += "			<h4 class=\"custom\">---- based on Excel list processing template by Mary Ann Connerley</h4>";
		page += "		</div>";
		page += "		<p>&nbsp;</p>";
		page += "<pre style=\"font-size:11pt\">\n\n" + msg + "\n</pre>";
		page += "<br> <br> <br> <br> <br> <br>";
		page += "		<div class=\"footer\">HVM Data Procession - 2016</div>";
		page += "	</div>";
		page += "</body>";
		page += "</html>";
		return page;
	}
}
