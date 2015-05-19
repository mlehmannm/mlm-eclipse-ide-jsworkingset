// svnkit.js

var Arrays = java.util.Arrays;
var Collectors = java.util.stream.Collectors;
var IAdaptableArr = Java.type("org.eclipse.core.runtime.IAdaptable[]");
var ResourcesPlugin = org.eclipse.core.resources.ResourcesPlugin;
var System = java.lang.System;
var repoRoot = "http://svn.svnkit.com/repos/svnkit";

// projects
var workspace = ResourcesPlugin.getWorkspace();
var projects = workspace.getRoot().getProjects();

// filtered projects
var filteredProjects = Arrays.stream(projects)
	.filter(function(p) p.isOpen()) // open projects
	.filter(function(p) subclipse.isManagedBySubclipse(p)) // by subclipse-managed
	.filter(function(p) subclipse.repositoryRoot(p).startsWith(repoRoot)) // by starts-with same repository root url
	.toArray(function(size) new IAdaptableArr(size)) // to sized array
	;

// adapted projects
var adaptedProjects = workingSet.adaptElements(filteredProjects)
workingSet.setElements(adaptedProjects);

// TODO add (x of y) as decoration?
workingSet.setLabel("SVNKit (" + adaptedProjects.length + " of " + projects.length + ")");
