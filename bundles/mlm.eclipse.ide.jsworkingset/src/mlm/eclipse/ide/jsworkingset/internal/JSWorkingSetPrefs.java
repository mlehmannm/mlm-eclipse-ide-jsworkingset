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


import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkingSet;


/**
 *
 * Helper class to read/write additional information for the working set in the preferences.
 *
 * @author Marco Lehmann-Mörz
 *
 * @since mlm.eclipse.ide.jsworkingset 1.0
 *
 */

public final class JSWorkingSetPrefs {


	/**
	 *
	 * Common key prefix to access the preferences.
	 *
	 */

	public static final String PREF_KEY__PREFIX = "ws."; //$NON-NLS-1$


	/**
	 *
	 * Key prefix to access the name in the preferences.
	 *
	 */

	public static final String PREF_KEY__NAME_PREFIX = PREF_KEY__PREFIX + "name."; //$NON-NLS-1$


	/**
	 *
	 * Key prefix to access the script in the preferences.
	 *
	 */

	public static final String PREF_KEY__SCRIPT_PREFIX = PREF_KEY__PREFIX + "script."; //$NON-NLS-1$


	/**
	 *
	 * Private default constructor to prevent this class from being instantiated.
	 *
	 * @since mlm.eclipse.ide.jsworkingset 1.0
	 *
	 */

	private JSWorkingSetPrefs() {

		throw new Error();

	}


	/**
	 *
	 * Returns the original name of the working set.
	 *
	 * @param pWorkingSet
	 *            the working set (may be <code>null</code>)
	 *
	 * @return the name or <code>null</code>
	 *
	 * @since mlm.eclipse.ide.jsworkingset 1.0
	 *
	 */

	public static String getName( final IWorkingSet pWorkingSet ) {

		return getString(pWorkingSet, PREF_KEY__NAME_PREFIX);

	}


	/**
	 *
	 * Sets the original name for the working set.
	 *
	 * @param pWorkingSet
	 *            the working set (may be <code>null</code>)
	 * @param pName
	 *            the original name of the working set
	 *
	 * @since mlm.eclipse.ide.jsworkingset 1.0
	 *
	 */

	public static void setName( final IWorkingSet pWorkingSet, final String pName ) {

		setString(pWorkingSet, PREF_KEY__NAME_PREFIX, pName);

	}


	/**
	 *
	 * Returns the script of the working set.
	 *
	 * @param pWorkingSet
	 *            the working set (may be <code>null</code>)
	 *
	 * @return the name or <code>null</code>
	 *
	 * @since mlm.eclipse.ide.jsworkingset 1.0
	 *
	 */

	public static String getScript( final IWorkingSet pWorkingSet ) {

		return getString(pWorkingSet, PREF_KEY__SCRIPT_PREFIX);

	}


	/**
	 *
	 * Sets the script for the working set.
	 *
	 * @param pWorkingSet
	 *            the working set (may be <code>null</code>)
	 * @param pScript
	 *            the workspace-relative path to the script (may be <code>null</code>)
	 *
	 * @since mlm.eclipse.ide.jsworkingset 1.0
	 *
	 */

	public static void setScript( final IWorkingSet pWorkingSet, final String pScript ) {

		setString(pWorkingSet, PREF_KEY__SCRIPT_PREFIX, pScript);

	}


	private static String getString( final IWorkingSet pWorkingSet, final String pKeyPrefix ) {

		if (pWorkingSet == null) {

			return null;

		}

		final IPreferencesService prefService = Platform.getPreferencesService();
		final String qualifier = Activator.ID_PLUGIN;
		final String key = pKeyPrefix + pWorkingSet.getName();
		return prefService.getString(qualifier, key, null, null);

	}


	private static void setString( final IWorkingSet pWorkingSet, final String pKeyPrefix, final String pValue ) {

		if (pWorkingSet == null) {

			return;

		}

		try {

			final IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();

			if (pValue != null) {

				// set
				prefStore.setValue(pKeyPrefix + pWorkingSet.getName(), pValue);

			} else {

				// remove
				prefStore.setValue(pKeyPrefix + pWorkingSet.getName(), IPreferenceStore.STRING_DEFAULT_DEFAULT);

			}

		} catch (final Exception ex) {

			// log
			Activator.log(IStatus.ERROR, "Failed to save to preferences!", ex); //$NON-NLS-1$

		}

	}


}
