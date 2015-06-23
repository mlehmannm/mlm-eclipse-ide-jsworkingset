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


import java.util.Hashtable;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 *
 * The activator class controls the plug-in life cycle.
 *
 * @author Marco Lehmann-Mörz
 *
 * @since mlm.eclipse.ide.jsworkingset 1.0
 *
 */

public final class Activator extends AbstractUIPlugin implements DebugOptionsListener {


	/**
	 *
	 * The id of the plug-in (value <code>"mlm.eclipse.ide.jsworkingset"</code>).
	 *
	 */

	public static final String ID_PLUGIN = "mlm.eclipse.ide.jsworkingset"; //$NON-NLS-1$


	/**
	 *
	 * The id of the working set.
	 *
	 */

	public static final String ID_WORKING_SET = ID_PLUGIN + ".JSWorkingSet"; //$NON-NLS-1$


	/**
	 *
	 * The id of the marker.
	 *
	 */

	public static final String ID_MARKER = ID_PLUGIN + ".marker"; //$NON-NLS-1$


	/**
	 *
	 * The id of the problem marker.
	 *
	 */

	public static final String ID_PROBLEM_MARKER = ID_PLUGIN + ".problemmarker"; //$NON-NLS-1$


	/**
	 *
	 * Holds the trace.
	 *
	 */

	public static DebugTrace sTrace;


	/**
	 *
	 * Global debug flag.
	 *
	 */

	public static boolean DEBUG = false;


	/**
	 *
	 * The shared instance.
	 *
	 */

	private static Activator sSingleton;


	/**
	 *
	 * Constructs a new <code>Activator</code>.
	 *
	 * @since mlm.eclipse.ide.jsworkingset 1.0
	 *
	 */

	public Activator() {

		super();

	}


	@Override
	public void start( final BundleContext pContext ) throws Exception {

		super.start(pContext);

		sSingleton = this;

		final Hashtable<String, Object> props = new Hashtable<>(4);
		props.put(DebugOptions.LISTENER_SYMBOLICNAME, ID_PLUGIN);
		pContext.registerService(DebugOptionsListener.class, this, props);

	}


	@Override
	public void optionsChanged( final DebugOptions pOptions ) {

		if (sTrace == null) {

			sTrace = pOptions.newDebugTrace(ID_PLUGIN);

		}

		DEBUG = pOptions.getBooleanOption(ID_PLUGIN + "/debug", false); //$NON-NLS-1$

	}


	@Override
	public void stop( final BundleContext pContext ) throws Exception {

		sSingleton = null;

		super.stop(pContext);

	}


	/**
	 *
	 * Returns the shared instance.
	 *
	 * @return the shared instance
	 *
	 * @since mlm.eclipse.ide.jsworkingset 1.0
	 *
	 */

	public static final Activator getDefault() {

		return sSingleton;

	}


	/**
	 *
	 * Factory method to create a new script engine.
	 * <p>
	 * This is necessary, because the class loader must be changed to access all classes.
	 * </p>
	 *
	 * @param pManager
	 *            the script engine manager
	 *
	 * @return the new script engine
	 *
	 * @since mlm.eclipse.ide.jsworkingset 1.0
	 *
	 */

	public static final ScriptEngine newScriptEngine( final ScriptEngineManager pManager ) {

		final Thread currentThread = Thread.currentThread();
		final ClassLoader contextClassLoader = currentThread.getContextClassLoader();

		try {

			currentThread.setContextClassLoader(Activator.class.getClassLoader());

			return pManager.getEngineByName("nashorn"); //$NON-NLS-1$

		} finally {

			currentThread.setContextClassLoader(contextClassLoader);

		}

	}


	/**
	 *
	 * Convenience method to retrieve images located in this bundle.
	 *
	 * @param pImageFilePath
	 *            bundle relative path to the image
	 * @return the image descriptor
	 *
	 * @see AbstractUIPlugin#imageDescriptorFromPlugin(String, String)
	 *
	 * @since mlm.eclipse.ide.jsworkingset 1.0
	 *
	 */

	public static final ImageDescriptor getImageDescriptor( final String pImageFilePath ) {

		return AbstractUIPlugin.imageDescriptorFromPlugin(ID_PLUGIN, pImageFilePath);

	}


	/**
	 *
	 * Convenience method to log.
	 *
	 * @param pSeverity
	 *            the severity
	 * @param pMessage
	 *            the message
	 *
	 * @since mlm.eclipse.ide.jsworkingset 1.0
	 *
	 */

	public static final void log( final int pSeverity, final String pMessage ) {

		getDefault().getLog().log(new Status(pSeverity, ID_PLUGIN, pMessage));

	}


	/**
	 *
	 * Convenience method to log.
	 *
	 * @param pSeverity
	 *            the severity
	 * @param pMessage
	 *            the message
	 * @param pThrow
	 *            the throwable
	 *
	 * @since mlm.eclipse.ide.jsworkingset 1.0
	 *
	 */

	public static final void log( final int pSeverity, final String pMessage, final Throwable pThrow ) {

		getDefault().getLog().log(new Status(pSeverity, ID_PLUGIN, pMessage, pThrow));

	}


}
