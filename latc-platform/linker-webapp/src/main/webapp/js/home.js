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
    		    	 html.push('<th>Linkset name and spec:</th>');
    		    	 html.push('<th>Created at:</th>');
    		    	 html.push('<th>Created by:</th>');
    				 html.push('<th>Performed at:</th>');
    				 html.push('<th>Triples,report:</th>');
    				 html.push('<th>Rss feed:</th>');
       		        
    				 html.push('</tr></thead><tbody>');
     		         
    		    	 if(json.status && json.status =="ok" && json.entries && json.entries.results && json.entries.results.bindings ){
    		        	 //ok 
    		        	 var entries = json.entries.results.bindings;
    		        	 if(entriesNo>entries.length){
    		        		 entriesNo = entries.length;
    		        	 }
    	    
    		        	 for(var i=0;i<entriesNo;i++){
    		        		var entry = entries[i];
    		        		
    		        		var linkset  = entry.linkset.value;
    		        		var index = linkset.lastIndexOf("#");
    		        		var linksetName  = linkset.substring(index+1);
    		        		
    		        		
    		        		var generatedTriples = entry.generatedTriples.value;
    		        		var createdBy = entry.createdBy.value;
    		        		var index = createdBy.lastIndexOf("/");
    		        		var createdByName  = createdBy.substring(index+1);
    		        		var createdByLink  = createdBy.substring(0,index+1)+"links.nt";
    		        		var reportLink  = createdBy.substring(0,index+1)+"report.log";
    		        		var specLink  = createdBy.substring(0,index+1)+"spec.xml";
    		        		var performedAt = entry.performedAt.value.replace(/-/g, "/").replace("T"," ").replace(/[+,-]\d\d\d\d$/,""); 
    		        		var createdAt =   entry.createdAt.value.replace(/-/g, "/").replace("T"," ").replace(/[+,-]\d\d\d\d$/,""); 
    		        		var feedLink = contextPrefix+"rss/"+entry.id.value+"/notifications.atom";
    		        		
    		        		html.push('<tr>');
    		        		html.push('<td><a href="'+linkset+'">'+linksetName+'</a>, <a href="'+specLink+'">spec</a></td>');
    		        		html.push('<td>'+createdAt+'</td>');
    		        		html.push('<td>'+createdByName+'</td>');
    		        		html.push('<td>'+performedAt+'</td>');
    		        		html.push('<td><a href="'+createdByLink+'">'+ generatedTriples+ ' triples</a>, <a href="'+reportLink+'">report</a></td>');
    		        		html.push('<td><a class="rssFeed" href="'+feedLink+'"></td>');
    		        		html.push('</tr>');
    		        	 }
    		         }
    		         html.push("</tbody></table>");
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
