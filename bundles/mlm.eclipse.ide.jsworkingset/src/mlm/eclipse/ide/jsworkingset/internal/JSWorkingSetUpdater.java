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
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

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
import org.eclipse.ui.IWorkingSetManager;
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


	/**
	 *
	 * Working set data.
	 *
	 */

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
	 * The property change listener that listens for changes to the preference store.
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
	 * The property change listener that listens for changes to the working set manager.
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
	 * Lambda to reset the given working set data.
	 *
	 */

	private final Consumer<WorkingSetData> mResetWorkingSetData = wsd -> resetWorkingSetData(wsd);


	/**
	 *
	 * Lambda to update the given working set data.
	 *
	 */

	private final Consumer<WorkingSetData> mUpdateWorkingSetData = wsd -> updateWorkingSetData(wsd);


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

		final WorkingSetData workingSetData = new WorkingSetData();
		workingSetData.workingSet = pWorkingSet;

		mWorkingSets.put(pWorkingSet, workingSetData);

		runAsUpdateJob(workingSetData, mUpdateWorkingSetData);

	}


	@Override
	public boolean remove( final IWorkingSet pWorkingSet ) {

		JSWorkingSetPrefs.setScript(pWorkingSet, null);
		JSWorkingSetPrefs.setName(pWorkingSet, null);

		final WorkingSetData workingSetData = mWorkingSets.remove(pWorkingSet);

		resetWorkingSetData(workingSetData);

		return workingSetData != null;

	}


	private void handleWorkingSetManagerPropertyChange( final PropertyChangeEvent pEvent ) {

		if (IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE.equals(pEvent.getProperty())) {

			// final IWorkingSet changedWS = (IWorkingSet) pEvent.getNewValue();

			// TODO
			System.err.println(pEvent);

		}

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

				runAsUpdateJob(workingSetData, mResetWorkingSetData.andThen(mUpdateWorkingSetData));

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

			final int projectKind = affectedChildren[0].getKind() & ~IResourceDelta.CHANGED;
			final int projectFlags = affectedChildren[0].getFlags() & ~IResourceDelta.MARKERS;
			if (projectKind != 0 || projectFlags != 0) {

				if (Activator.DEBUG) {

					final String projects = Arrays.stream(affectedChildren) //
					        .map(e -> e.getResource().getName()) //
					        .sorted(Collator.getInstance()) //
					        .collect(joining(", ")) //$NON-NLS-1$
					        ;
					final String message = String.format("Changes detected in projects '%s'.", projects); //$NON-NLS-1$
					Activator.log(IStatus.INFO, message);

				}

				final long startTime = System.currentTimeMillis();

				for (final WorkingSetData workingSetData : mWorkingSets.values()) {

					runAsUpdateJob(workingSetData, mUpdateWorkingSetData);

				}

				if (Activator.DEBUG) {

					final long endTime = System.currentTimeMillis();
					final Long elapsed = Long.valueOf(endTime - startTime);
					final Integer noOfWorkingSets = Integer.valueOf(mWorkingSets.size());
					final String message = String.format("%d working set(s) updated in %d ms.", noOfWorkingSets, elapsed); //$NON-NLS-1$
					Activator.log(IStatus.INFO, message);

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

						runAsUpdateJob(workingSetData, mResetWorkingSetData.andThen(mUpdateWorkingSetData));

					} else if ((scriptFlags & ~IResourceDelta.MARKERS) != 0) {

						if (Activator.DEBUG) {

							final String message = String.format("Change detected in script '%s'.", scriptPath); //$NON-NLS-1$
							Activator.log(IStatus.INFO, message);

						}

						runAsUpdateJob(workingSetData, mResetWorkingSetData.andThen(mUpdateWorkingSetData));

					}

				}

			}

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


	private void updateWorkingSetData( final WorkingSetData pWorkingSetData ) {

		if (pWorkingSetData == null) {

			return;

		}

		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		if (pWorkingSetData.scriptFile == null && pWorkingSetData.compiledScript == null) {

			final String scriptPathStr = JSWorkingSetPrefs.getScript(pWorkingSetData.workingSet);
			if (scriptPathStr == null) {

				final String name = JSWorkingSetPrefs.getName(pWorkingSetData.workingSet);
				pWorkingSetData.workingSet.setLabel(name != null ? name : "working set");
				pWorkingSetData.workingSet.setElements(new IAdaptable[0]);

				return;

			}

			final Path scriptPath = new Path(scriptPathStr);
			final IFile scriptFile = workspaceRoot.getFile(scriptPath);
			if (!scriptFile.isAccessible()) {

				final String name = JSWorkingSetPrefs.getName(pWorkingSetData.workingSet);
				pWorkingSetData.workingSet.setLabel(name != null ? name : "working set");
				pWorkingSetData.workingSet.setElements(new IAdaptable[0]);

				return;

			}

			pWorkingSetData.scriptFile = scriptFile;

			deleteMarkers(pWorkingSetData.scriptFile);

			try {

				final long startTime = System.currentTimeMillis();

				final ScriptEngine engine = Activator.newScriptEngine(mScriptEngineManager);
				engine.put(ScriptEngine.FILENAME, scriptPathStr);

				try (Reader reader = new InputStreamReader(scriptFile.getContents(), scriptFile.getCharset())) {

					final Compilable compilable = (Compilable) engine;
					pWorkingSetData.compiledScript = compilable.compile(reader);

				}

				if (Activator.DEBUG) {

					final long endTime = System.currentTimeMillis();
					final Long elapsed = Long.valueOf(endTime - startTime);
					final String message = String.format("Script '%s' compiled in %d ms.", scriptFile, elapsed); //$NON-NLS-1$
					Activator.log(IStatus.INFO, message);

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

				final String name = JSWorkingSetPrefs.getName(pWorkingSetData.workingSet);
				pWorkingSetData.workingSet.setLabel(name != null ? name : "working set");
				pWorkingSetData.workingSet.setElements(new IAdaptable[0]);

				createMarkers(pWorkingSetData.scriptFile, ex);

			} finally {

				bindings.clear();

			}

		} else {

			final String name = JSWorkingSetPrefs.getName(pWorkingSetData.workingSet);
			pWorkingSetData.workingSet.setLabel(name != null ? name : "working set");
			pWorkingSetData.workingSet.setElements(new IAdaptable[0]);

		}

	}


	private void runAsUpdateJob( final WorkingSetData pWorkingSetData, final Consumer<WorkingSetData> pConsumer ) {

		// TODO http://git.eclipse.org/c/egit/egit.git/commit/?id=76ab31f44a34a3f61f649bf61a3b114f590a2954
		// Job vs. WorkspaceJob

		final String workingSetName = JSWorkingSetPrefs.getName(pWorkingSetData.workingSet);
		final String jobName = String.format("Updating working set '%s'.", workingSetName);
		final Job updateJob = new WorkspaceJob(jobName) {


			@Override
			public IStatus runInWorkspace( final IProgressMonitor pMonitor ) throws CoreException {

				final long startTime = System.currentTimeMillis();

				pConsumer.accept(pWorkingSetData);

				if (Activator.DEBUG) {

					final long endTime = System.currentTimeMillis();
					final Long elapsed = Long.valueOf(endTime - startTime);
					final String label = pWorkingSetData.workingSet.getLabel();
					final String message = String.format("Working set '%s' updated in %d ms.", label, elapsed); //$NON-NLS-1$
					Activator.log(IStatus.INFO, message);

				}

				return Status.OK_STATUS;

			}


			@Override
			public String toString() {

				return JSWorkingSetUpdater.class.getSimpleName() + '[' + workingSetName + ']';

			}


		};
		updateJob.setSystem(true);
		updateJob.setPriority(getJobPriority());
		// TODO updateJob.setRule(ruleFactory.markerRule(pResource));
		updateJob.schedule();

		final String label = pWorkingSetData.workingSet.getLabel();
		final String message = String.format("Job to update working set '%s' has been scheduled.", label); //$NON-NLS-1$
		Activator.log(IStatus.INFO, message);

	}


	private int getJobPriority() {

		String priority = Activator.JOB_PRIORITY;
		if (priority != null) {

			priority = priority.toUpperCase(Locale.ENGLISH);

			switch (priority) {

			case "SHORT": //$NON-NLS-1$
				return Job.SHORT;

			case "LONG": //$NON-NLS-1$
				return Job.LONG;

			case "BUILD": //$NON-NLS-1$
				return Job.BUILD;

			case "DECORATE": //$NON-NLS-1$
				return Job.DECORATE;

			}

		}

		return Job.SHORT;

	}


	private void createMarkers( final IResource pResource, final Exception pException ) {

		if (pResource == null || !pResource.isAccessible()) {

			return;

		}

		int lineNumber;
		String fileName;
		if (pException instanceof ScriptException) {

			final ScriptException ex = (ScriptException) pException;
			lineNumber = ex.getLineNumber();
			fileName = ex.getFileName();

		} else {

			lineNumber = -1;
			fileName = null;

		}

		if (fileName != null && !fileName.equals(pResource.getFullPath().toString())) {

			return;

		}

		try {

			final IMarker marker = pResource.createMarker(Activator.ID_PROBLEM_MARKER);
			marker.setAttribute(IMarker.MESSAGE, pException.getMessage());
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);

			if (lineNumber > -1) {

				marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);

			}

		} catch (final CoreException ex) {

			Activator.log(IStatus.ERROR, "Failed to create markers!", ex); //$NON-NLS-1$

		}

	}


	private void deleteMarkers( final IResource pResource ) {

		if (pResource == null || !pResource.isAccessible()) {

			return;

		}

		try {

			pResource.deleteMarkers(Activator.ID_MARKER, false, IResource.DEPTH_ZERO);
			pResource.deleteMarkers(Activator.ID_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);

		} catch (final CoreException ex) {

			Activator.log(IStatus.ERROR, "Failed to delete markers!", ex); //$NON-NLS-1$

		}

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
