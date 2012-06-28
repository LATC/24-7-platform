    function getLatestJobs(username){
    	var url = contextPrefix+"latest-jobs";
    	if(username){
    		url +="?username="+encodeURIComponent(username);
    	}
    	jQuery.ajax({
    		     url:url,
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
     		         
    		    	 if(json.status && json.status =="ok" && json.entries && json.entries.results && json.entries.results.bindings ){
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
    		        		var performedAt = entry.performedAt.value.replace(/-/g, "/").replace("T"," ").replace(/[+,-]\d\d\d\d$/,""); 
    		        		var createdAt =   entry.createdAt.value.replace(/-/g, "/").replace("T"," ").replace(/[+,-]\d\d\d\d$/,""); 
    		        		
    		        		html.push('<tr>');
    		        		html.push('<td>'+performedAt+'</td>');
    		        		html.push('<td><a href="'+performedBy+'">'+performedByName+'</a></td>');
    		        		html.push('<td>'+createdAt+'</td>');
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
