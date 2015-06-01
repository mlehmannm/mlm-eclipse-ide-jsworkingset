/*
 * Copyright (c) 2015 Marco Lehmann-Mörz. All rights reserved.
 */


package mlm.eclipse.ide.jsworkingset.internal;


import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ResourceListSelectionDialog;


/**
 *
 * TODO
 *
 * @author Marco Lehmann-Mörz
 *
 * @since mlm.eclipse.ide.jsworkingset 1.0
 *
 */

public class ImportWizardPage extends WizardPage {


	/**
	 *
	 * The data-binding context to glue everything together.
	 *
	 */

	private DataBindingContext mDataBindingContext;


	/**
	 *
	 * Constructs a new <code>ImportWizardPage</code>.
	 *
	 * @since mlm.eclipse.ide.jsworkingset 1.0
	 *
	 */

	public ImportWizardPage() {

		super(ImportWizardPage.class.getSimpleName());

		setTitle("Import JavaScript-based Working Sets");
		setImageDescriptor(Activator.getImageDescriptor("icons/full/wizban/workset_wiz.png")); //$NON-NLS-1$

	}


	@Override
	public void createControl( final Composite pParent ) {

		mDataBindingContext = new DataBindingContext(SWTObservables.getRealm(pParent.getDisplay()));

		final Composite composite = new Composite(pParent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults() //
		        .numColumns(3) //
		        .create());
		setControl(composite);

		final Label label = new Label(composite, SWT.WRAP);
		label.setText("Script:");

		final Text text = new Text(composite, SWT.BORDER | SWT.SINGLE);
		text.setToolTipText("Enter a workspace file.");
		text.setLayoutData(GridDataFactory.swtDefaults() //
		        .align(SWT.FILL, SWT.CENTER) //
		        .grab(true, false) //
		        .create());

		// TODO history or path completion for script?

		final Button button = new Button(composite, SWT.PUSH);
		button.setText("Browse..."); //$NON-NLS-1$
		button.setLayoutData(GridDataFactory.swtDefaults() //
		        .align(SWT.FILL, SWT.CENTER) //
		        .grab(false, false) //
		        .create());
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
						text.setText(path.toString());

					} else {

						text.setText(""); //$NON-NLS-1$

					}

				}

			}


		});

		// TODO table to display found working sets

	}


	@Override
	public void dispose() {

		if (mDataBindingContext != null) {

			mDataBindingContext.dispose();
			mDataBindingContext = null;

		}

		super.dispose();

	}


}
