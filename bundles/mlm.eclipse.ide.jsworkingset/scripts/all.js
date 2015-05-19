// all.js

var ResourcesPlugin = org.eclipse.core.resources.ResourcesPlugin;

// projects
var workspace = ResourcesPlugin.getWorkspace();
var projects = workspace.getRoot().getProjects();

// adapted projects
var adaptedProjects = workingSet.adaptElements(projects)
workingSet.setElements(adaptedProjects);

// TODO add (x of y) as decoration?
workingSet.setLabel("All (" + adaptedProjects.length + " of " + projects.length + ")");
