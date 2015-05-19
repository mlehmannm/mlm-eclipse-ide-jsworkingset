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

public final class Activator extends AbstractUIPlugin {


	/**
	 *
	 * The id of the plug-in (value <code>"mlm.eclipse.ide.jsworkingset"</code>).
	 *
	 */

	public static final String ID_PLUGIN = "mlm.eclipse.ide.jsworkingset"; //$NON-NLS-1$


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

	public static Activator getDefault() {

		return sSingleton;

	}


}
