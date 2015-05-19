// open.js

var Arrays = java.util.Arrays;
var IAdaptableArr = Java.type("org.eclipse.core.runtime.IAdaptable[]");
var ResourcesPlugin = org.eclipse.core.resources.ResourcesPlugin;

// projects
var workspace = ResourcesPlugin.getWorkspace();
var projects = workspace.getRoot().getProjects();

// filtered projects
var filteredProjects = Arrays.stream(projects)
	.filter(function(p) p.isOpen()) // open projects
	.toArray(function(size) new IAdaptableArr(size)) // to sized array
	;

// adapted projects
var adaptedProjects = workingSet.adaptElements(filteredProjects)
workingSet.setElements(adaptedProjects);

// TODO add (x of y) as decoration?
workingSet.setLabel("Open (" + adaptedProjects.length + " of " + projects.length + ")");
