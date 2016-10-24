package com.ibm.commerce.hvm.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;

import com.ibm.commerce.hvm.app.process.ProcessData;

public class HVMUtils {

	public HVMUtils() {
		// TODO Auto-generated constructor stub
	}
	
	//no paramater for use in method invoking
	static Class<?> noparams[] = {};
	//String parameter for use in method invoking
	static Class<?>[] paramString = new Class<?>[] { String[].class };
	//int parameter for use in method invoking
	static Class<?>[] paramInt = new Class<?>[] { Integer[].class };
	
	/**
	 * @param obj - the object which has the method
	 * @param methodName - name of the method to be invoked in obj
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static String invokeGetStringMethod(Object obj, String methodName) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		String s = "";
		if(obj != null && methodName != null){
			Method method = obj.getClass().getDeclaredMethod(methodName, noparams); //getClass() here apparently returns the sub-class of Object i.e., the Class of parameter obj
			s = (String) method.invoke(obj, null);
		}
		return s;
	}
	
    public static String camelize(String str, String separater, boolean lowerCaseFirst) {
    	if(str == null || str.trim().length() < 1)
    		return str;
    	str = str.trim().toLowerCase();
        StringBuffer buff = new StringBuffer();
        String[] tokens = str.split(separater);
        for (int i=0; i<tokens.length; i++) {
        	if(i == 0 && lowerCaseFirst){
                buff.append(tokens[i]);
        		
        	}else{
	            buff.append(StringUtils.capitalize(tokens[i]));
        	}
        }
        return buff.toString();
    }

	
	public static void main(String args[]){
		/*
		//testing invokeGetStringMethod()
		ProcessData pd = new ProcessData();
		pd.setBehavior("Test Behavior");
		try {
			System.out.println(HVMUtils.invokeGetStringMethod(pd, "getBehavior"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		//testing camelize
		String getBehavior = "get_behavior_score";
		System.out.println(HVMUtils.camelize(getBehavior, "_", true));
		getBehavior = "get-behavior-score";
		System.out.println(HVMUtils.camelize(getBehavior, "-", false));
		getBehavior = "get";
		System.out.println(HVMUtils.camelize(getBehavior, "-", true));
		
	}

}
