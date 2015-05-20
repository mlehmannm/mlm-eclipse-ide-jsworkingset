// open.js
// 
// Global Variables
//
//	working set
//		the current working set to be modified
//	projects
//		array of all (visible) projects in the workspace
//	log
//		the plug-in log
//

// imports
var Arrays = java.util.Arrays;
var IAdaptableArr = Java.type("org.eclipse.core.runtime.IAdaptable[]");

// filter
var filteredProjects = Arrays.stream(projects)
	.filter(function(p) p.isOpen()) // open projects
	.toArray(function(size) new IAdaptableArr(size)) // to sized array
	;

// adapt
var adaptedProjects = workingSet.adaptElements(filteredProjects)

// udpdate
workingSet.setElements(adaptedProjects);
workingSet.setLabel("Open (" + adaptedProjects.length + " of " + projects.length + ")");
