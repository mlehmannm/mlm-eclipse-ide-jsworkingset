// subclipse.js

var subclipse = {};

subclipse.isManagedBySubclipse = function(pProject) {

	var SVNWorkspaceRoot = org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;

	return SVNWorkspaceRoot.isManagedBySubclipse(pProject);

}

subclipse.repositoryRoot = function(pProject) {

	var SVNWorkspaceRoot = org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;

	var repository = SVNWorkspaceRoot.getSVNResourceFor(pProject).getRepository();
	if (repository === null) {

		return null;

	}

	var repositoryRoot = repository.getRepositoryRoot();
	if (repositoryRoot === null) {

		return null;

	}

	return repositoryRoot.toString();

}

subclipse.url = function(pProject) {

	var SVNWorkspaceRoot = org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;

	var url = SVNWorkspaceRoot.getSVNResourceFor(pProject).getUrl();
	if (url === null) {

		return null;

	}

	return url.toString();

}

var module = {
	name : 'subclipse',
	exports : {
		subclipse : subclipse,
	},
};
