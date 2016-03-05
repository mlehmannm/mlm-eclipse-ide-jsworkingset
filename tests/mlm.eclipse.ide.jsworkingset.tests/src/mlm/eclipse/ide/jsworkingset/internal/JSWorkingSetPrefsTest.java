/****************************************************************************************
 * Copyright (c) 2015, 2016 Marco Lehmann-Mörz.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Lehmann-Mörz - initial API and implementation and/or initial documentation
 ***************************************************************************************/


package mlm.eclipse.ide.jsworkingset.internal;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.ui.IWorkingSet;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * Test(s) for class {@link JSWorkingSetPrefs}.
 *
 * @author Marco Lehmann-Mörz
 *
 * @since mlm.eclipse.ide.jsworkingset.tests 1.0
 *
 */

@SuppressWarnings("nls")
public class JSWorkingSetPrefsTest {


	@Test(expected = Error.class)
	public void testConstructor() throws Throwable {

		try {

			final Constructor<JSWorkingSetPrefs> constructor = JSWorkingSetPrefs.class.getDeclaredConstructor();
			constructor.setAccessible(true);
			constructor.newInstance();

		} catch (final InvocationTargetException ex) {

			throw ex.getCause();

		}

	}


	@Test
	public void testGetSetLabel() {

		Assert.assertNull(JSWorkingSetPrefs.getName(null));

		final IWorkingSet ws = mock(IWorkingSet.class);
		when(ws.getName()).thenReturn(String.valueOf("testGetSetLabel"));

		Assert.assertNull(JSWorkingSetPrefs.getName(ws));

		JSWorkingSetPrefs.setName(ws, "label");
		Assert.assertSame("label", JSWorkingSetPrefs.getName(ws));

		JSWorkingSetPrefs.setName(ws, null);
		Assert.assertNull(JSWorkingSetPrefs.getName(ws));

	}


	@Test
	public void testGetSetScript() {

		Assert.assertNull(JSWorkingSetPrefs.getScript(null));

		final IWorkingSet ws = mock(IWorkingSet.class);
		when(ws.getName()).thenReturn(String.valueOf("testGetSetScript"));

		Assert.assertNull(JSWorkingSetPrefs.getScript(ws));

		JSWorkingSetPrefs.setScript(ws, "script");
		Assert.assertSame("script", JSWorkingSetPrefs.getScript(ws));

		JSWorkingSetPrefs.setScript(ws, null);
		Assert.assertNull(JSWorkingSetPrefs.getScript(ws));

	}


}
