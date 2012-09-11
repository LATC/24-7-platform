# The LATC 24/7 Interlinking Platform
The [LATC](http://latc-project.eu/) 24/7 Interlinking Platform (24/7 Plaform) is a [cloud](http://en.wikipedia.org/wiki/Cloud_computing#Application) offering to generate RDF links between datasets in the [Linked Open Data](http://lod-cloud.net/) cloud.

All the components of the platform are licensed under the [Apache license 2.0](http://www.apache.org/licenses/LICENSE-2.0).
The installation guide is available [there](https://github.com/LATC/24-7-platform/wiki/Deployment-guide) 

## Workflow components
This platform is structured around several components as depicted in the following picture:
![24/7 Platform Overview](https://github.com/LATC/24-7-platform/raw/master/doc/img/24-7-platform-flow.png "24/7 Platform Overview") These components are organised in a workflow, every one of them ensuring a specific role.

### Console
The Console controls the execution of link tasks towards the Runtime and acts as an intermediate towards the Workbench. This component deals with a list of Linking Tasks to be executed and the status of the link runs. The LATC Console also functions as the main access point for an Operator, providing status information about the 24/7 Platform, including health, link runs, errors, quality measures, etc. and control options for link tasks. For this purpose, the Console is split in two parts: a graphical interface for the Operator and a REST API for the Workbench and the Runtime.

The documentation of the console is located [there](https://github.com/LATC/24-7-platform/wiki/Documentation-for-Console) and its source code [there](https://github.com/LATC/24-7-platform/tree/master/latc-platform/console)

### Workbench

The Workbench allows creating link specifications and is typically used by a Link Author. It is a specialised version of the [Silk Workbench](http://www.assembla.com/wiki/show/silk/Silk_Workbench). The Workbench provides both a UI component and a backend component to handle reference linksets. A Link Author constructs one ore more link tasks in the Workbench and typically uses reference linksets to assess the quality of the links produced: to enable this, the Workbench operates a local version of Silk, allowing the Link Author to preview a generated linkset.

The documentation of the workbench is located [there](http://www.assembla.com/wiki/show/silk/Silk_Workbench) and its source code [there](http://www.assembla.com/code/silk/git/nodes/silk2/silk-workbench)


### Metadata Store (MDS)

The Metadata Store (MDS) is the central hub for all dataset (DS) and linkset (LS) metadata in the 24/7 Platform. It is a backend component that manages the following data:

* List of curated datasets (C-DS) from CKAN.
* List of host-based datasets (H-DS) from Sindice.
* Sindice-coverage statistics for datasets.

Metadata for generated linksets including precision, recall and pointer to the reference linkset and vetted status of linksets. Internally, the MDS uses VoID to represent DS/LS metadata and to take the C-DS via CKAN into account. It is assumed that C-DS are maintained entirely via CKAN. Additionally, the MDS acts as the backend for the Data Source Inventory (DSI). The DSI and the MDS communicate via SPARQL queries. 

The documentation of the MDS is located [there](https://github.com/LATC/24-7-platform/wiki/Documentation-for-Meta-Data-Store-%28MDS%29)

### Data Source Inventory (DSI)

The LATC Data Source Inventory (DSI) is a UI component operated by TALIS. It supports the following use cases:

* Allows Link Authors to find datasets to link against.
* Enables a Link Author to study example resources in order to decide how to write a link specification or whether a link specification is feasible.
* Helps a Link Consumer to find interesting LATC-generated linksets.
* Notifies a Link Consumer about re-generated linksets via feeds.
* Provides a feed of vetted linksets.
* Enables any user to explore all available datasets.


### Runtime

The Runtime is a backend component operated by DERI. The Runtime uses a Silk MapReduce version and Hadoop. It takes a list of link tasks and produces linksets along with metadata (in VoID) as well as log information, collectively known as the link run.

The documentation of the runtime is located [there](https://github.com/LATC/24-7-platform/wiki/Documentation-for-Runtime) and its source code [there](https://github.com/LATC/24-7-platform/tree/master/latc-platform/runtime)

## Aditional components

There are some componentns which are not depicted in the drawing of the platform but are used and embedded by other components.

### Linkset Evaluator

The LinkSet Evaluator is a QA module executed by the Runtime which verify if the reference link sets are correctly generated when the task gets executed.

The documentation of the LinkSet evaluator is located [there](https://github.com/LATC/24-7-platform/wiki/Documentation-for-LinksetEvaluator)


