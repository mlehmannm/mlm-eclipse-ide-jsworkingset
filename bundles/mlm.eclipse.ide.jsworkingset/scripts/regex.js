// regex.js

var Arrays = java.util.Arrays;
var IAdaptableArr = Java.type("org.eclipse.core.runtime.IAdaptable[]");
var ResourcesPlugin = org.eclipse.core.resources.ResourcesPlugin;
var re = /org\.tigris\.subversion\..+/i;

// projects
var workspace = ResourcesPlugin.getWorkspace();
var projects = workspace.getRoot().getProjects();

// filtered projects
var filteredProjects = Arrays.stream(projects)
	.filter(function(p) re.test(p.getName())) // by matching regex
	.toArray(function(size) new IAdaptableArr(size)) // to sized array
	;

// adapted projects
var adaptedProjects = workingSet.adaptElements(filteredProjects)
workingSet.setElements(adaptedProjects);

// TODO add (x of y) as decoration?
workingSet.setLabel("org.tigris.subversion.* (" + adaptedProjects.length + " of " + projects.length + ")");
