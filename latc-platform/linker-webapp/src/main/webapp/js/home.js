$(document).ready(function(){
    function getLatestJobs(){
    		jQuery.ajax({
    		     url: contextPrefix+"latest-jobs",
    		     dataType:"json",
    		     success:function(json){
    		         var entriesNo = 100;
    		    	 var html =['<table class="table"><thead>'];
    				 
    		    	 html.push('<tr>');
    				 html.push('<th>Performed at:</th>');
    				 html.push('<th>Performed by:</th>');
    				 html.push('<th>Created at:</th>');
    				 html.push('<th>Created by:</th>');
      		        
    				 html.push('</tr></thead><tbody>');
     		         
    		    	 if(json.status && json.status =="ok" && json.entries ){
    		        	 //ok 
    		        	 var entries = json.entries.results.bindings;
    		        	 if(entriesNo>entries.length){
    		        		 entriesNo = entries.length;
    		        	 }
    	    		         
    		        	 for(var i=0;i<entriesNo;i++){
    		        		var entry = entries[i];
    		        		
    		        		var performedBy  = entry.performedBy.value;
    		        		var index = performedBy.lastIndexOf("#");
    		        		var performedByName  = performedBy .substring(index+1);
    		        		
    		        		
    		        		var createdBy = entry.createdBy.value;
    		        		var index = createdBy.lastIndexOf("/");
    		        		var createdByName  = createdBy.substring(index+1);
    		        			
    		        		html.push('<tr>');
    		        		html.push('<td>'+(new Date(entry.performedAt.value))+'</td>');
    		        		html.push('<td><a href="'+performedBy+'">'+performedByName+'</a></td>');
    		        		html.push('<td>'+(new Date(entry.createdAt.value))+'</td>');
    		        		html.push('<td><a href="'+createdBy+'">'+createdByName+'</a></td>');
     		        		html.push('</tr>');
    		        	 }
    		         }
    		         html.push("</tbody></table>")
    		         jQuery("#latestJobs").html(html.join(""));
    		     },error:function(a,b,c){
    		    	 if(console){
    		    		 console.log(a);
    		    		 console.log(b);
    		    		 console.log(c);
    		    	 }
    		    	 
    		     }
    		 });	
    }
    
    getLatestJobs();
});