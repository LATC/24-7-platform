Runtime Documentation
=====================

![Runtime Overview](https://github.com/LATC/24-7-platform/latc-platform/runtime/doc/runtime.png "Runtime Overview")

Input
-----

The Runtime has four following inputs :
* Link specification : The link specification is in the XML format which is obtained from Console.
* Blacklist file : The list of specification title which are failed or you do not wish it to run due to special reason. Every title is separated by new line.
* Void template (voidtmpl) file
The template VOID file for easy modifying in the future. It contains the key of parameters are punctuated by \*\*.
	* \*\*source\*\* : dataset source parameter.
	* \*\*newprefix\*\* : new prefix has to be added
	* \*\*target\*\* : dataset target parameter.
	* \*\*linksetname\*\* : Linkset name parameter.
	* \*\*linktype\*\* : linkset predicate parameter.
	* \*\*triples\*\* : the number of triples generated parameter.
	* \*\*datadump\*\* : the path of linkset parameter.
	* \*\*linksetcreatedtime\*\* : the time linkset is generated parameter.
	* \*\*specauthor\*\* : the author of specification file parameter.
	* \*\*speccreatedtime\*\* : the time specification file created parameter.
	* \*\*specretrievedtime\*\* : the time specification file retrieved parameter.
	* \*\*specURL\*\* : the URL of specification parameter.
	* \*\*consolehost\*\* : the URL of console host parameter.
* Configuration could be provided as file or command line.
	* REQUIRED
		* HADOOP_PATH : path of hadoop, is required to run SILK and HDFS
   		* LATC_CONSOLE_HOST : the URL of console that provides SILK Specification file
   		* RESULTS_HOST : the URL of host for storing the linkset generated
   		* API_KEY : the URL of host for storing the linkset generated
   	* OPTIONAL
		* HDFS_USER : the user who has privilege access to read and write on Hadoop Distributed FileSystem. Default : running username in the shell
		*LINKS_FILE_STORE : the name of file for storing links generated. The links is produced in N-Triples format. Default : link.nt
		* RESULT_LOCAL_DIR : the path of result directory. Default : results
		* SPEC_FILE : the specification file name. Default : spec.xml
		* VOID_FILE : the void file name that contains declaration of dataset and linkset.

Process
-------

![Runtime Process](https://github.com/LATC/24-7-platform/latc-platform/runtime/doc/flowprocesslatc.jpg "Runtime Process")

Runtime initialises the parameter from the command line or configuration file. The first task is checking whether the console host is live or not.   If it is not down, runtime loads the blacklist file for getting the specification file that must be ignore during the running process and then it fetches all specification file in the console server and classifies which SILK spec is not in the black list in one group.   After obtaining SILK parameter from parsing each file process, it tests SPARQL endpoint and HDFS server. It one of them can not be accessed, the process is terminated. If so, it executes SILK afterwards.  All generating LinkSet are merged in one local file and the VOID that described the linkset is written afterwards. Lastly, it submit report to console regarding the running process and VOID file to MDS.

## Output

The output of runtime are linkset, VOID and report. The linkset and VOID are located at result host. The VOID declares  linkset and dataset. The void:Linkset is taken from concatenation of source dataset and target dataset. For example, let dbpedia and imdb as source and target dataset, the linkset should be  dbpedia2imdb.  The example of VOID is available in here.

There are two kinds of report, success and failed. The success report submit how many links are generated as well as the URL of result host. The failed report mentions the reason why job could not generate links such as SPARQL End Point down or Time Out, HDSF problem and Invalid XML.  
