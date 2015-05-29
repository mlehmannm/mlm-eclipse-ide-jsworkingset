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


import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.dialog.ValidationMessageProvider;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
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


	// TODO history or path completion for script?


	private DataBindingContext mDataBindingContext;


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

	private IObservableValue mWorkingSetLabel;


	/**
	 *
	 * The name for the working set.
	 *
	 */

	private IObservableValue mWorkingSetName;


	/**
	 *
	 * The script (location) for the working set.
	 *
	 */

	private IObservableValue mWorkingSetScript;


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

	}


	@Override
	public void createControl( final Composite pParent ) {

		mDataBindingContext = new DataBindingContext(SWTObservables.getRealm(pParent.getDisplay()));

		final Composite composite = new Composite(pParent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults() //
		        .numColumns(3) //
		        .create());
		setControl(composite);

		// working set label
		{
			final String workingSetLabel = mWorkingSet != null ? mWorkingSet.getLabel() : ""; //$NON-NLS-1$
			mWorkingSetLabel = WritableValue.withValueType(String.class);
			mWorkingSetLabel.setValue(workingSetLabel);

			final Label label = new Label(composite, SWT.WRAP);
			label.setText("Label:");

			final Text text = new Text(composite, SWT.BORDER | SWT.SINGLE);
			text.setLayoutData(GridDataFactory.swtDefaults() //
			        .align(SWT.FILL, SWT.CENTER) //
			        .grab(true, false) //
			        .span(2, 1) //
			        .create());

			final IValidator validator = new IValidator() {


				@Override
				public IStatus validate( final Object pValue ) {

					final String value = (String) pValue;

					if (value == null || value.trim().isEmpty()) {

						return ValidationStatus.ok();

					}

					return ValidationStatus.info("This may likely be overridden by the script.");

				}


			};

			final UpdateValueStrategy targetToModel = new UpdateValueStrategy() //
			        .setAfterConvertValidator(validator) //
			;

			final ISWTObservableValue target = WidgetProperties.text(SWT.Modify) //
			        .observe(text) //
			;

			final Binding binding = mDataBindingContext.bindValue(target, mWorkingSetLabel, targetToModel, null);
			binding.getValidationStatus().setValue(ValidationStatus.ok());

			ControlDecorationSupport.create(binding, SWT.LEFT | SWT.TOP);
		}

		// working set name
		{
			final String workingSetName = JSWorkingSetPrefs.getName(mWorkingSet);
			mWorkingSetName = WritableValue.withValueType(String.class);
			mWorkingSetName.setValue(workingSetName);

			final Label label = new Label(composite, SWT.WRAP);
			label.setText("Name:");

			final Text text = new Text(composite, SWT.BORDER | SWT.SINGLE);
			text.setLayoutData(GridDataFactory.swtDefaults() //
			        .align(SWT.FILL, SWT.CENTER) //
			        .grab(true, false) //
			        .span(2, 1) //
			        .create());
			text.setFocus();

			final IValidator validator = new IValidator() {


				@Override
				public IStatus validate( final Object pValue ) {

					final String value = (String) pValue;

					if (value == null || value.trim().isEmpty()) {

						return ValidationStatus.error("Please provide a name!");

					}

					return ValidationStatus.ok();

				}


			};

			final UpdateValueStrategy targetToModel = new UpdateValueStrategy() //
			        .setAfterConvertValidator(validator) //
			;

			final ISWTObservableValue target = WidgetProperties.text(SWT.Modify) //
			        .observe(text) //
			;

			final Binding binding = mDataBindingContext.bindValue(target, mWorkingSetName, targetToModel, null);

			ControlDecorationSupport.create(binding, SWT.LEFT | SWT.TOP);
		}

		// script
		{
			final String workingSetScript = JSWorkingSetPrefs.getScript(mWorkingSet);
			mWorkingSetScript = WritableValue.withValueType(String.class);
			mWorkingSetScript.setValue(workingSetScript);

			final Label label = new Label(composite, SWT.WRAP);
			label.setText("Script:");

			final Text text = new Text(composite, SWT.BORDER | SWT.SINGLE);
			text.setLayoutData(GridDataFactory.swtDefaults() //
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
							text.setText(path.toString());

						} else {

							text.setText(""); //$NON-NLS-1$

						}

					}

				}


			});

			final IValidator validator = new IValidator() {


				@Override
				public IStatus validate( final Object pValue ) {

					final String value = (String) pValue;

					if (value == null || value.trim().isEmpty()) {

						return ValidationStatus.error("Please provide a script!");

					}

					final IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(value);
					if (member == null || !member.exists()) {

						return ValidationStatus.error("The provided script does not exist in the workspace!");

					}

					return ValidationStatus.ok();

				}


			};

			final UpdateValueStrategy targetToModel = new UpdateValueStrategy() //
			        .setAfterConvertValidator(validator) //
			;

			final ISWTObservableValue target = WidgetProperties.text(SWT.Modify) //
			        .observe(text) //
			;

			final Binding binding = mDataBindingContext.bindValue(target, mWorkingSetScript, targetToModel, null);

			ControlDecorationSupport.create(binding, SWT.LEFT | SWT.TOP);
		}

		final WizardPageSupport wizardPageSupport = WizardPageSupport.create(this, mDataBindingContext);
		wizardPageSupport.setValidationMessageProvider(new ValidationMessageProvider() {


			@Override
			public String getMessage( final ValidationStatusProvider pStatusProvider ) {

				final String message = super.getMessage(pStatusProvider);
				if (message != null) {

					return message;

				}

				return "Enter a working set label and name and select the script that makes up the content of the working set.";

			}


		});

		// provide a clean start
		setErrorMessage(null);
		setMessage("Enter a working set label and name and select the script that makes up the content of the working set.");

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

		final String workingSetLabel = (String) mWorkingSetLabel.getValue();
		final String workingSetName = (String) mWorkingSetName.getValue();
		final String workingSetScript = (String) mWorkingSetScript.getValue();

		if (mWorkingSet == null) {

			final String name = String.valueOf(System.currentTimeMillis());
			final IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
			mWorkingSet = workingSetManager.createWorkingSet(name, new IAdaptable[0]);

		}

		mWorkingSet.setLabel(workingSetLabel);

		JSWorkingSetPrefs.setName(mWorkingSet, workingSetName.trim());
		JSWorkingSetPrefs.setScript(mWorkingSet, workingSetScript.trim());

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
