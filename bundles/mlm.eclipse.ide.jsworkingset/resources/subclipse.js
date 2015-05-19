// subclipse.js

var subclipse = {};

subclipse.isManagedBySubclipse = function(pProject) {

	var SVNWorkspaceRoot = org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
	// java.lang.System.err.println(pProject.getName() + " => isManagedBySubclipse: " + SVNWorkspaceRoot.isManagedBySubclipse(pProject));
	return SVNWorkspaceRoot.isManagedBySubclipse(pProject);

}

subclipse.repositoryRoot = function(pProject) {

	var SVNWorkspaceRoot = org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
	var repositoryRoot = SVNWorkspaceRoot.getSVNResourceFor(pProject).getRepository().getRepositoryRoot();
	// java.lang.System.err.println(pProject.getName() + " => repoRoot: " + repositoryRoot.toString());
	return repositoryRoot.toString();

}

subclipse.url = function(pProject) {

	var SVNWorkspaceRoot = org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
	var url = SVNWorkspaceRoot.getSVNResourceFor(pProject).getUrl();
	// java.lang.System.err.println(pProject.getName() + " => url: " + url.toString());
	return url.toString();

}

var module = {
	name : 'subclipse',
	exports : {
		subclipse : subclipse,
	},
};
