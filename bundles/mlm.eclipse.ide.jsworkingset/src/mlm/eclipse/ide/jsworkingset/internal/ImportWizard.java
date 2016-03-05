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


import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;


/**
 *
 * Wizard to import JavaScript-based working sets from a JavaScript-file.
 *
 * @author Marco Lehmann-Mörz
 *
 * @since mlm.eclipse.ide.jsworkingset 1.0
 *
 */

public class ImportWizard extends Wizard implements IImportWizard {


	/**
	 *
	 * Constructs a new <code>ImportWizard</code>.
	 *
	 * @since mlm.eclipse.ide.jsworkingset 1.0
	 *
	 */

	public ImportWizard() {

		super();

	}


	@Override
	public void init( final IWorkbench pWorkbench, final IStructuredSelection pSelection ) {

		System.err.println(pSelection);
		// TODO Auto-generated method stub

	}


	@Override
	public void addPages() {

		addPage(new ImportWizardPage());

	}


	@Override
	public boolean performFinish() {

		// TODO Auto-generated method stub
		return false;

	}


}
