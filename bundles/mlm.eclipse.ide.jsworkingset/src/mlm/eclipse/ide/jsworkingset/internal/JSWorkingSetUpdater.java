/****************************************************************************************
 * Copyright (c) 2015 Marco Lehmann-Mörz.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Lehmann-Mörz - initial API and implementation and/or initial documentation
 ***************************************************************************************/


package mlm.eclipse.ide.jsworkingset.internal;


import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetUpdater;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;


/**
 *
 * Updater for the JavaScript-based working set.
 *
 * @author Marco Lehmann-Mörz
 *
 * @since mlm.eclipse.ide.jsworkingset 1.0
 *
 */

public class JSWorkingSetUpdater implements IWorkingSetUpdater {


	private static class WorkingSetData {


		IWorkingSet workingSet;


		IFile scriptFile;


		CompiledScript compiledScript;


	}


	/**
	 *
	 * The script engine manager to create script engines.
	 *
	 */

	private ScriptEngineManager mScriptEngineManager;


	/**
	 *
	 * Mapping from working set to working set data.
	 *
	 */

	private Map<IWorkingSet, WorkingSetData> mWorkingSets;


	/**
	 *
	 * The resource change listener that listens for changes in the workspace.
	 *
	 */

	private final IResourceChangeListener mResourceChangeListener = new IResourceChangeListener() {


		@Override
		public void resourceChanged( final IResourceChangeEvent pEvent ) {

			handleResourceChanged(pEvent);

		}


	};


	/**
	 *
	 * The property change listener that listens for changes in the preference store.
	 *
	 */

	private final IPropertyChangeListener mPreferenceStorePropertyChangeListener = new IPropertyChangeListener() {


		@Override
		public void propertyChange( final PropertyChangeEvent pEvent ) {

			handlePreferenceStorePropertyChange(pEvent);

		}


	};


	/**
	 *
	 * The property change listener that listens for changes in the preference store.
	 *
	 */

	private final IPropertyChangeListener mWorkingSetManagerPropertyChangeListener = new IPropertyChangeListener() {


		@Override
		public void propertyChange( final PropertyChangeEvent pEvent ) {

			handleWorkingSetManagerPropertyChange(pEvent);

		}


	};


	/**
	 *
	 * Constructs a new <code>JSWorkingSetUpdater</code>.
	 *
	 * @since mlm.eclipse.ide.jsworkingset 1.0
	 *
	 */

	public JSWorkingSetUpdater() {

		mWorkingSets = new HashMap<>();

		mScriptEngineManager = new ScriptEngineManager();

		readGlobalScripts();

		ResourcesPlugin.getWorkspace().addResourceChangeListener(mResourceChangeListener, IResourceChangeEvent.POST_CHANGE);

		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(mPreferenceStorePropertyChangeListener);

		PlatformUI.getWorkbench().getWorkingSetManager().addPropertyChangeListener(mWorkingSetManagerPropertyChangeListener);

	}


	private void readGlobalScripts() {

		final Bindings globalBindings = mScriptEngineManager.getBindings();
		globalBindings.put("log", Activator.getDefault().getLog()); //$NON-NLS-1$

		final Bundle bundle = Activator.getDefault().getBundle();
		final Enumeration<URL> scriptUrls = bundle.findEntries("resources", "*.js", false); //$NON-NLS-1$ //$NON-NLS-2$
		while (scriptUrls.hasMoreElements()) {

			final URL scriptUrl = scriptUrls.nextElement();

			try (Reader reader = new InputStreamReader(scriptUrl.openStream(), StandardCharsets.UTF_8)) {

				final ScriptEngine engine = Activator.newScriptEngine(mScriptEngineManager);
				engine.put(ScriptEngine.FILENAME, FileLocator.resolve(scriptUrl).toString());
				engine.eval(reader);

				final Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
				final Bindings module = (Bindings) bindings.get("module");
				final Bindings moduleExports = (Bindings) module.get("exports");
				for (final Entry<String, Object> entry : moduleExports.entrySet()) {

					globalBindings.put(entry.getKey(), entry.getValue());

				}

			} catch (IOException | ScriptException ex) {

				Activator.log(IStatus.ERROR, "Failed to eval script!", ex); //$NON-NLS-1$

			}

		}

	}


	@Override
	public boolean contains( final IWorkingSet pWorkingSet ) {

		return mWorkingSets.containsKey(pWorkingSet);

	}


	@Override
	public void add( final IWorkingSet pWorkingSet ) {

		final WorkingSetData wsd = new WorkingSetData();
		wsd.workingSet = pWorkingSet;

		mWorkingSets.put(pWorkingSet, wsd);

		updateWorkingSetData(wsd);

	}


	@Override
	public boolean remove( final IWorkingSet pWorkingSet ) {

		JSWorkingSetPrefs.setScript(pWorkingSet, null);

		final WorkingSetData workingSetData = mWorkingSets.remove(pWorkingSet);

		resetWorkingSetData(workingSetData);

		return workingSetData != null;

	}

	private void handleWorkingSetManagerPropertyChange( final PropertyChangeEvent pEvent ) {

		System.err.println(pEvent);
		// TODO register for working set changes (name)

	}


	private void handlePreferenceStorePropertyChange( final PropertyChangeEvent pEvent ) {

		if (mWorkingSets.isEmpty()) {

			return;

		}

		final String property = pEvent.getProperty();
		if (!JSWorkingSetPrefs.isImportantProperty(property)) {

			return;

		}

		final String workingSetName = JSWorkingSetPrefs.extractWorkingSetNameFromProperty(property);

		for (final WorkingSetData workingSetData : mWorkingSets.values()) {

			if (workingSetName.equals(workingSetData.workingSet.getName())) {

				resetWorkingSetData(workingSetData);
				updateWorkingSetData(workingSetData);

			}

		}

	}


	private void handleResourceChanged( final IResourceChangeEvent pEvent ) {

		if (mWorkingSets.isEmpty()) {

			return;

		}

		// check for updates to projects
		final IResourceDelta delta = pEvent.getDelta();
		final int kind = IResourceDelta.ADDED | IResourceDelta.CHANGED | IResourceDelta.REMOVED;
		final int memberFlags = IResource.PROJECT;
		final IResourceDelta[] affectedChildren = delta.getAffectedChildren(kind, memberFlags);
		if (affectedChildren != null && affectedChildren.length > 0) {

			final int projectFlags = affectedChildren[0].getFlags();
			if ((projectFlags & ~IResourceDelta.MARKERS) != IResourceDelta.NO_CHANGE) {

				if (Activator.DEBUG) {

					final String projects = Arrays.stream(affectedChildren) //
					        .map(e -> e.getResource().getName()) //
					        .sorted(Collator.getInstance()) //
					        .collect(joining(", ")) //$NON-NLS-1$
					;
					final String message = String.format("Changes detected in projects '%s'.", projects); //$NON-NLS-1$
					Activator.log(IStatus.INFO, message);

				}

				for (final WorkingSetData workingSetData : mWorkingSets.values()) {

					updateWorkingSetData(workingSetData);

				}

			}

		}

		// check for updates to scripts
		for (final WorkingSetData workingSetData : mWorkingSets.values()) {

			if (workingSetData.scriptFile != null) {

				final IPath scriptPath = workingSetData.scriptFile.getFullPath();
				final IResourceDelta scriptDelta = delta.findMember(scriptPath);
				if (scriptDelta != null) {

					final int scriptFlags = scriptDelta.getFlags();
					if ((scriptFlags & IResourceDelta.MOVED_TO) != 0) {

						final IPath oldScriptPath = workingSetData.scriptFile.getFullPath();
						final IPath newScriptPath = scriptDelta.getMovedToPath();

						if (Activator.DEBUG) {

							final String message = String.format("Script moved from '%s' to '%s'.", oldScriptPath, newScriptPath); //$NON-NLS-1$
							Activator.log(IStatus.INFO, message);

						}

						JSWorkingSetPrefs.setScript(workingSetData.workingSet, newScriptPath.toString());

						resetWorkingSetData(workingSetData);
						updateWorkingSetData(workingSetData);

					} else if ((scriptFlags & ~IResourceDelta.MARKERS) != IResourceDelta.NO_CHANGE) {

						if (Activator.DEBUG) {

							final String message = String.format("Change detected in script '%s'.", scriptPath); //$NON-NLS-1$
							Activator.log(IStatus.INFO, message);

						}

						resetWorkingSetData(workingSetData);
						updateWorkingSetData(workingSetData);

					}

				}

			}

		}

	}


	private void updateWorkingSetData( final WorkingSetData pWorkingSetData ) {

		final long startTime = System.currentTimeMillis();

		updateWorkingSetData0(pWorkingSetData);

		if (Activator.DEBUG) {

			final long endTime = System.currentTimeMillis();

			final long elapsed = endTime - startTime;

			final String label = pWorkingSetData.workingSet.getLabel();
			final String message = String.format("Working set '%s' updated in %d ms.", label, elapsed); //$NON-NLS-1$
			Activator.log(IStatus.INFO, message);

		}

	}


	private void updateWorkingSetData0( final WorkingSetData pWorkingSetData ) {

		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		if (pWorkingSetData.scriptFile == null && pWorkingSetData.compiledScript == null) {

			final String scriptPathStr = JSWorkingSetPrefs.getScript(pWorkingSetData.workingSet);
			if (scriptPathStr == null) {

				// TODO change name/icon?
				final String name = JSWorkingSetPrefs.getName(pWorkingSetData.workingSet);
				pWorkingSetData.workingSet.setLabel(name != null ? name : "working set");
				pWorkingSetData.workingSet.setElements(new IAdaptable[0]);

				return;

			}

			final Path scriptPath = new Path(scriptPathStr);
			final IFile scriptFile = workspaceRoot.getFile(scriptPath);
			if (!scriptFile.isAccessible()) {

				// TODO change name/icon?
				final String name = JSWorkingSetPrefs.getName(pWorkingSetData.workingSet);
				pWorkingSetData.workingSet.setLabel(name != null ? name : "working set");
				pWorkingSetData.workingSet.setElements(new IAdaptable[0]);

				return;

			}

			pWorkingSetData.scriptFile = scriptFile;

			deleteMarkers(pWorkingSetData.scriptFile);

			try {

				final ScriptEngine engine = Activator.newScriptEngine(mScriptEngineManager);
				engine.put(ScriptEngine.FILENAME, scriptPathStr);

				try (Reader reader = new InputStreamReader(scriptFile.getContents(), scriptFile.getCharset())) {

					final Compilable compilable = (Compilable) engine;
					pWorkingSetData.compiledScript = compilable.compile(reader);

				}

			} catch (final CoreException | IOException ex) {

				Activator.log(IStatus.ERROR, "Failed to access/load script!", ex); //$NON-NLS-1$

				createMarkers(pWorkingSetData.scriptFile, ex);

			} catch (final ScriptException ex) {

				Activator.log(IStatus.ERROR, "Failed to compile script!", ex); //$NON-NLS-1$

				createMarkers(pWorkingSetData.scriptFile, ex);

			}

		}

		if (pWorkingSetData.compiledScript != null) {

			final SimpleBindings bindings = new SimpleBindings();

			try {

				bindings.put(ScriptEngine.FILENAME, pWorkingSetData.scriptFile.toString());
				bindings.put("workingSet", pWorkingSetData.workingSet); //$NON-NLS-1$
				bindings.computeIfAbsent("projects", ( s ) -> workspaceRoot.getProjects()); //$NON-NLS-1$

				pWorkingSetData.compiledScript.eval(bindings);

			} catch (final ScriptException ex) {

				Activator.log(IStatus.ERROR, "Failed to eval script!", ex); //$NON-NLS-1$

				// TODO change name/icon?
				final String name = JSWorkingSetPrefs.getName(pWorkingSetData.workingSet);
				pWorkingSetData.workingSet.setLabel(name != null ? name : "working set");
				pWorkingSetData.workingSet.setElements(new IAdaptable[0]);

				createMarkers(pWorkingSetData.scriptFile, ex);

			} finally {

				bindings.clear();

			}

		} else {

			// TODO change name/icon?
			final String name = JSWorkingSetPrefs.getName(pWorkingSetData.workingSet);
			pWorkingSetData.workingSet.setLabel(name != null ? name : "working set");
			pWorkingSetData.workingSet.setElements(new IAdaptable[0]);

		}

	}


	private void resetWorkingSetData( final WorkingSetData pWorkingSetData ) {

		if (pWorkingSetData == null) {

			return;

		}

		deleteMarkers(pWorkingSetData.scriptFile);

		pWorkingSetData.scriptFile = null;
		pWorkingSetData.compiledScript = null;

	}


	private void createMarkers( final IResource pResource, final ScriptException pException ) {

		if (pResource == null || !pResource.isAccessible()) {

			return;

		}

		if (!pResource.getFullPath().toString().equals(pException.getFileName())) {

			return;

		}

		final IResourceRuleFactory ruleFactory = pResource.getWorkspace().getRuleFactory();
		final WorkspaceJob markerJob = new WorkspaceJob("create-markers") {


			@Override
			public IStatus runInWorkspace( final IProgressMonitor pMonitor ) throws CoreException {

				final IMarker marker = pResource.createMarker(Activator.ID_PROBLEM_MARKER);
				marker.setAttribute(IMarker.MESSAGE, pException.getMessage());
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				marker.setAttribute(IMarker.LINE_NUMBER, pException.getLineNumber());

				return Status.OK_STATUS;

			}


		};
		markerJob.setSystem(true);
		markerJob.setPriority(Job.SHORT);
		markerJob.setRule(ruleFactory.markerRule(pResource));
		markerJob.schedule();

	}


	private void createMarkers( final IResource pResource, final Exception pException ) {

		if (pResource == null || !pResource.isAccessible()) {

			return;

		}

		final IResourceRuleFactory ruleFactory = pResource.getWorkspace().getRuleFactory();
		final WorkspaceJob markerJob = new WorkspaceJob("create-markers") {


			@Override
			public IStatus runInWorkspace( final IProgressMonitor pMonitor ) throws CoreException {

				final IMarker marker = pResource.createMarker(Activator.ID_PROBLEM_MARKER);
				marker.setAttribute(IMarker.MESSAGE, pException.getMessage());
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);

				return Status.OK_STATUS;

			}


		};
		markerJob.setSystem(true);
		markerJob.setPriority(Job.SHORT);
		markerJob.setRule(ruleFactory.markerRule(pResource));
		markerJob.schedule();

	}


	private void deleteMarkers( final IResource pResource ) {

		if (pResource == null || !pResource.isAccessible()) {

			return;

		}

		final IResourceRuleFactory ruleFactory = pResource.getWorkspace().getRuleFactory();
		final WorkspaceJob markerJob = new WorkspaceJob("delete-markers") {


			@Override
			public IStatus runInWorkspace( final IProgressMonitor pMonitor ) throws CoreException {

				pResource.deleteMarkers(Activator.ID_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);

				return Status.OK_STATUS;

			}


		};
		markerJob.setSystem(true);
		markerJob.setPriority(Job.SHORT);
		markerJob.setRule(ruleFactory.markerRule(pResource));
		markerJob.schedule();

	}


	@Override
	public void dispose() {

		PlatformUI.getWorkbench().getWorkingSetManager().removePropertyChangeListener(mWorkingSetManagerPropertyChangeListener);

		Activator.getDefault().getPreferenceStore().removePropertyChangeListener(mPreferenceStorePropertyChangeListener);

		ResourcesPlugin.getWorkspace().removeResourceChangeListener(mResourceChangeListener);

		mWorkingSets.clear();
		mWorkingSets = null;

		mScriptEngineManager = null;

	}


}
