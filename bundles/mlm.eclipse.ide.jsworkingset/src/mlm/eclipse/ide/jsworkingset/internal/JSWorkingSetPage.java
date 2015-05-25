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


import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetPage;
import org.eclipse.ui.dialogs.ResourceListSelectionDialog;


/**
 *
 * Page to create/edit a JavaScript-based working set.
 *
 * @author Marco Lehmann-Mörz
 *
 * @since mlm.eclipse.ide.jsworkingset 1.0
 *
 */

public class JSWorkingSetPage extends WizardPage implements IWorkingSetPage {


	/**
	 *
	 * The working set.
	 *
	 */

	private IWorkingSet mWorkingSet;


	/**
	 *
	 * The label for the working set.
	 *
	 */

	private Text mWorkingSetLabel;


	/**
	 *
	 * The name for the working set.
	 *
	 */

	private Text mWorkingSetName;


	/**
	 *
	 * The script (location) for the working set.
	 *
	 */

	private Text mWorkingSetScript;


	/**
	 *
	 * Constructs a new <code>JSWorkingSetPage</code>.
	 *
	 * @since mlm.eclipse.ide.jsworkingset 1.0
	 *
	 */

	public JSWorkingSetPage() {

		super(JSWorkingSetPage.class.getSimpleName());

		setTitle("JavaScript-based Working Set");
		setMessage("Enter a working set label and name and select the script that makes up the content of the working set.");

	}


	@Override
	public void createControl( final Composite pParent ) {

		final Composite composite = new Composite(pParent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults() //
		        .numColumns(3) //
		        .create());
		setControl(composite);

		Label label = new Label(composite, SWT.WRAP);
		label.setText("Label:");

		final String workingSetLabel = mWorkingSet != null ? mWorkingSet.getLabel() : ""; //$NON-NLS-1$
		mWorkingSetLabel = new Text(composite, SWT.BORDER | SWT.SINGLE);
		mWorkingSetLabel.setText(workingSetLabel);
		mWorkingSetLabel.setLayoutData(GridDataFactory.swtDefaults() //
		        .align(SWT.FILL, SWT.CENTER) //
		        .grab(true, false) //
		        .span(2, 1) //
		        .create());
		mWorkingSetLabel.addModifyListener(new ModifyListener() {


			@Override
			public void modifyText( final ModifyEvent pEvent ) {

				// validatePage();

			}


		});
		mWorkingSetLabel.setFocus();

		label = new Label(composite, SWT.WRAP);
		label.setText("Name:");

		final String workingSetName = JSWorkingSetPrefs.getName(mWorkingSet);
		mWorkingSetName = new Text(composite, SWT.BORDER | SWT.SINGLE);
		mWorkingSetName.setText(workingSetName != null ? workingSetName : "");
		mWorkingSetName.setLayoutData(GridDataFactory.swtDefaults() //
		        .align(SWT.FILL, SWT.CENTER) //
		        .grab(true, false) //
		        .span(2, 1) //
		        .create());
		mWorkingSetName.addModifyListener(new ModifyListener() {


			@Override
			public void modifyText( final ModifyEvent pEvent ) {

				// validatePage();

			}


		});
		mWorkingSetName.setFocus();

		label = new Label(composite, SWT.WRAP);
		label.setText("Script:");

		final String workingSetScript = JSWorkingSetPrefs.getScript(mWorkingSet);
		mWorkingSetScript = new Text(composite, SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE);
		mWorkingSetScript.setText(workingSetScript != null ? workingSetScript : ""); //$NON-NLS-1$
		mWorkingSetScript.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		mWorkingSetScript.setLayoutData(GridDataFactory.swtDefaults() //
		        .align(SWT.FILL, SWT.CENTER) //
		        .grab(true, false) //
		        .create());

		final Button button = new Button(composite, SWT.PUSH);
		button.setText("..."); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {


			@Override
			public void widgetSelected( final SelectionEvent pEvent ) {

				final Shell shell = button.getShell();
				final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				final ResourceListSelectionDialog dialog = new ResourceListSelectionDialog(shell, root, IResource.FILE);
				dialog.setTitle("Select a JavaScript file");
				if (dialog.open() == Window.OK) {

					final Object[] objects = dialog.getResult();
					if (objects.length == 1) {

						final IResource resource = (IResource) objects[0];
						final IPath path = resource.getFullPath();
						mWorkingSetScript.setText(path.toString());

					} else {

						mWorkingSetScript.setText(""); //$NON-NLS-1$

					}

				}

			}

		});

	}


	@Override
	public IWorkingSet getSelection() {

		return mWorkingSet;

	}


	@Override
	public void setSelection( final IWorkingSet pWorkingSet ) {

		mWorkingSet = pWorkingSet;

	}


	@Override
	public void finish() {

		final String workingSetLabel = mWorkingSetLabel.getText().trim();
		final String workingSetName = mWorkingSetName.getText().trim();
		final String workingSetScript = mWorkingSetScript.getText().trim();

		if (mWorkingSet == null) {

			final String name = String.valueOf(System.currentTimeMillis());
			final IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
			mWorkingSet = workingSetManager.createWorkingSet(name, new IAdaptable[0]);

		}

		mWorkingSet.setLabel(workingSetLabel);

		JSWorkingSetPrefs.setName(mWorkingSet, workingSetName.trim());
		JSWorkingSetPrefs.setScript(mWorkingSet, workingSetScript.trim());

	}


}
