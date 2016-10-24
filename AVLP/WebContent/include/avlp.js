	$(document).ready(function() {
	    $("#user_input").validate({
	        rules: {
	        	resp_code:{
	                required: true,
	                minlength: 32
	            },
	            submit_name:{
	                required: true,
	                minlength: 5
	            },
	            submit_date:{
	                required: true,
	                minlength: 10
	            },
	            file:{
	                required: true,
	                extension: "txt",
	            }  
	        },
	        messages: {
	        	resp_code:{
		            required: 'Please enter a valid response code'
		        } ,
		        submit_name:{
		            required: 'Please enter a valid name'  
		        } ,
		        submit_date:{
		            required: 'Please enter a valid date'
		        } ,
	        	file:{
	               required: 'Please select a tub-delimited list-upload file (.txt)'  
	            } ,
	        },    
	        submitHandler: function(form) {
	            form.submit();
	        }
	    });
	});


	
	// for date picker
	$( function() {
		$( "#submit_date" ).datepicker();
	});
	
	
	/*
	$('#resp_msg').hide(); 
	$('#user_input').submit(function(){
	    $.ajax({
	      url: $('#form').attr('action'),
	      type: post,
	      data : $('#form').serialize(),
	      success: function(){
	    	  $('#resp_msg').hide();
	      }
	    });
	    return false;
	});
	*/
	
	// Ajax for form submission
    	$('user_input').click(function() {
    	    // send the form to your PHP file (using ajax, no page reload!!)
    	    $.ajax({
    	        type: 'POST', 
    	        url:  $('#user_input').attr('action'),
    	        data:  $('#user_input').serialize(),
    	        dataType: 'multipart/form-data', 
    	        //enctype='multipart/form-data',
    	        success: function(data) {
    	    		alert('Result');
    	        	$resp_msg = data;
    	        },
    	        beforeSend:function(){
    	            $("#resp_msg").text("Loading...")
    	        }
   	    });
    	return false;
     });
  
