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

var re = /mlm\.eclipse\.jsworkingset\..+/i;
regex.configureWorkingSetByRegex(workingSet, re, function(pWorkingSet, pProjects, pAdaptedProjects) {
	pWorkingSet.setLabel("mlm.eclipse.jsworkingset.* (" + pAdaptedProjects.length + " of " + pProjects.length + ")");
});
