/*
 * Copyright (c) 2015 Marco Lehmann-Mörz. All rights reserved.
 */


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
