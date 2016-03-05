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

public class StartupTest {


	@Test
	public void testEarlyStartup() {

		new Startup().earlyStartup();

	}


}
