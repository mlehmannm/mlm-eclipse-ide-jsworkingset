/*
 * Copyright (c) 2015 Marco Lehmann-Mörz. All rights reserved.
 */


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

@SuppressWarnings("all")
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

		Assert.assertNull(JSWorkingSetPrefs.getLabel(null));

		final IWorkingSet ws = mock(IWorkingSet.class);
		when(ws.getName()).thenReturn(String.valueOf("testGetSetLabel"));

		Assert.assertNull(JSWorkingSetPrefs.getLabel(ws));

		JSWorkingSetPrefs.setLabel(ws, "label");
		Assert.assertSame("label", JSWorkingSetPrefs.getLabel(ws));

		JSWorkingSetPrefs.setLabel(ws, null);
		Assert.assertNull(JSWorkingSetPrefs.getLabel(ws));

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
