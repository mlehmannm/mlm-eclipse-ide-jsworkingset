// pde.js

var pde = {};

pde.isFeature = function(pProject) {

	var PDECore = org.eclipse.pde.internal.core.PDECore;
	var featureModel = PDECore.getDefault().getFeatureModelManager().getFeatureModel(pProject);

	return featureModel !== null;

}

pde.isPlugin = function(pProject) {

	var PluginRegistry = org.eclipse.pde.core.plugin.PluginRegistry;
	var pluginModelBase = PluginRegistry.findModel(pProject);
	if (pluginModelBase === null) {

		return false;

	}

	return !pluginModelBase.isFragmentModel();

}

pde.isFragment = function(pProject) {

	var PluginRegistry = org.eclipse.pde.core.plugin.PluginRegistry;
	var pluginModelBase = PluginRegistry.findModel(pProject);
	if (pluginModelBase === null) {

		return false;

	}

	return pluginModelBase.isFragmentModel();

}

var module = {
	name : 'pde',
	exports : {
		pde : pde,
	},
};
