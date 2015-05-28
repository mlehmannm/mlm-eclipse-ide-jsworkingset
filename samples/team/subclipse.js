// subclipse.js
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
var url = "http://subclipse.tigris.org/svn/subclipse";
var filteredProjects = Arrays.stream(projects)
	.filter(function(p) p.isOpen()) // open projects
	.filter(function(p) subclipse.isManagedBySubclipse(p)) // by subclipse-managed
	.filter(function(p) subclipse.url(p).startsWith(url)) // by same url
	.toArray(function(size) new IAdaptableArr(size)) // to sized array
	;

// adapt
var adaptedProjects = workingSet.adaptElements(filteredProjects)

// update
workingSet.setElements(adaptedProjects);
workingSet.setLabel("Subclipse (" + adaptedProjects.length + " of " + projects.length + ")");
