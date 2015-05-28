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


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;


public class ImportWorkingSetsHandler extends AbstractHandler {


	@Override
	public Object execute( final ExecutionEvent pEvent ) throws ExecutionException {

		final long startTime = System.currentTimeMillis();

		execute0(pEvent);

		final long endTime = System.currentTimeMillis();

		final long elapsed = endTime - startTime;

		if (Activator.DEBUG) {

			final String message = String.format("Imported working sets in %d ms.", elapsed); //$NON-NLS-1$
			Activator.log(IStatus.INFO, message);

		}

		return null;

	}


	private void execute0( final ExecutionEvent pEvent ) {

		final ISelection selection = HandlerUtil.getCurrentSelection(pEvent);
		if (selection == null || selection.isEmpty()) {

			return;

		}

		if (!(selection instanceof IStructuredSelection)) {

			return;

		}

		final IAdapterManager adapterManager = Platform.getAdapterManager();
		final IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();

		final ScriptEngineManager manager = new ScriptEngineManager();
		final ScriptEngine engine = Activator.newScriptEngine(manager);

		final IStructuredSelection sselection = (IStructuredSelection) selection;
		final List<?> selElems = sselection.toList();

		for (final Object selElem : selElems) {

			final IFile file = (IFile) adapterManager.getAdapter(selElem, IFile.class);
			if (file != null) {

				final IPath dir = file.getFullPath().removeLastSegments(1);

				engine.put(ScriptEngine.FILENAME, file.getFullPath());

				try (Reader reader = new InputStreamReader(file.getContents())) {

					engine.eval(reader);

					final Bindings workingSetsBindings = (Bindings) engine.get("workingSets");
					for (final Object wsObj : workingSetsBindings.values()) {

						final Bindings workingSetBindings = (Bindings) wsObj;
						final String label = (String) workingSetBindings.get("name");
						final String script = (String) workingSetBindings.get("script");

						IPath scriptPath = new Path(script);
						if (!scriptPath.isAbsolute()) {

							scriptPath = dir.append(scriptPath);

						}

						final String name = String.valueOf(System.currentTimeMillis());
						final IWorkingSet workingSet = workingSetManager.createWorkingSet(name, new IAdaptable[0]);
						workingSet.setId(Activator.ID_WORKING_SET);
						JSWorkingSetPrefs.setName(workingSet, label);
						JSWorkingSetPrefs.setScript(workingSet, scriptPath.toString());
						workingSetManager.addWorkingSet(workingSet);

					}

				} catch (CoreException | IOException | ScriptException ex) {

					Activator.log(IStatus.ERROR, "Failed to access/load/compile script!", ex); //$NON-NLS-1$

				}

			}

		}


	}


}
