// team.js

var team = {};

team.isShared = function(pProject) {

	var RepositoryProvider = org.eclipse.team.core.RepositoryProvider;
	
	return RepositoryProvider.getProvider(pProject) !== null;

}

var module = {
	name : 'team',
	exports : {
		team : team,
	},
};
