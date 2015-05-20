// regex.js

var regex = {};

regex.configureWorkingSetByRegex = function(pWorkingSet, pRegex, pCallback) {

	// imports
	var Arrays = java.util.Arrays;
	var IAdaptableArr = Java.type("org.eclipse.core.runtime.IAdaptable[]");
	var ResourcesPlugin = org.eclipse.core.resources.ResourcesPlugin;

	// projects
	var workspace = ResourcesPlugin.getWorkspace();
	var projects = workspace.getRoot().getProjects();

	// filtered projects
	var filteredProjects = Arrays.stream(projects)
		.filter(function(p) pRegex.test(p.getName())) // by matching regex
		.toArray(function(size) new IAdaptableArr(size)) // to sized array
		;

	// adapted projects
	var adaptedProjects = pWorkingSet.adaptElements(filteredProjects)
	pWorkingSet.setElements(adaptedProjects);

	// transfer result to caller
	pCallback(pWorkingSet, projects, adaptedProjects);

}

var module = {
	name : 'regex',
	exports : {
		regex : regex,
	},
};
