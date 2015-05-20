// regex.js
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

var re = /org\.tigris\.subversion\..+/i;
regex.configureWorkingSetByRegex(workingSet, re, function(pWorkingSet, pProjects, pAdaptedProjects) {
	pWorkingSet.setLabel("org.tigris.subversion.* (" + pAdaptedProjects.length + " of " + pProjects.length + ")");
});
