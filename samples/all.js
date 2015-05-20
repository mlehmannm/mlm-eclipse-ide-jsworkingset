// all.js
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

// adapt
var adaptedProjects = workingSet.adaptElements(projects)

// update
workingSet.setElements(adaptedProjects);
workingSet.setLabel("All (" + adaptedProjects.length + " of " + projects.length + ")");
