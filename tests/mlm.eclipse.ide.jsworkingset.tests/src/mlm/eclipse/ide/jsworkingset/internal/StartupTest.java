/*
 * Copyright (c) 2015 Marco Lehmann-Mörz. All rights reserved.
 */


package mlm.eclipse.ide.jsworkingset.internal;


import org.junit.Test;


/**
 *
 * Test(s) for class {@link Startup}.
 *
 * @author Marco Lehmann-Mörz
 *
 * @since mlm.eclipse.ide.jsworkingset.tests 1.0
 *
 */

@SuppressWarnings("all")
public class StartupTest {


	@Test
	public void testEarlyStartup() {

		new Startup().earlyStartup();

	}


}
