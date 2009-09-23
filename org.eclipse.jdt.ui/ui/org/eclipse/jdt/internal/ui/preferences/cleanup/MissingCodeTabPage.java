/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.preferences.cleanup;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;

import org.eclipse.jdt.internal.ui.fix.AbstractCleanUp;
import org.eclipse.jdt.internal.ui.fix.Java50CleanUp;
import org.eclipse.jdt.internal.ui.fix.PotentialProgrammingProblemsCleanUp;
import org.eclipse.jdt.internal.ui.fix.UnimplementedCodeCleanUp;

public final class MissingCodeTabPage extends AbstractCleanUpTabPage {

	public static final String ID= "org.eclipse.jdt.ui.cleanup.tabpage.missing_code"; //$NON-NLS-1$

	public MissingCodeTabPage() {
		super();
	}

	protected AbstractCleanUp[] createPreviewCleanUps(Map values) {
		return new AbstractCleanUp[] { new Java50CleanUp(values), new PotentialProgrammingProblemsCleanUp(values), new UnimplementedCodeCleanUp(values) };
	}

	protected void doCreatePreferences(Composite composite, int numColumns) {

    	Group annotationsGroup= createGroup(numColumns, composite, CleanUpMessages.MissingCodeTabPage_GroupName_Annotations);

		final CheckboxPreference annotationsPref= createCheckboxPref(annotationsGroup, numColumns, CleanUpMessages.MissingCodeTabPage_CheckboxName_AddMissingAnnotations, CleanUpConstants.ADD_MISSING_ANNOTATIONS, CleanUpModifyDialog.FALSE_TRUE);
		
		intent(annotationsGroup);
		final CheckboxPreference overridePref= createCheckboxPref(annotationsGroup, numColumns - 1, CleanUpMessages.MissingCodeTabPage_CheckboxName_AddMissingOverrideAnnotations, CleanUpConstants.ADD_MISSING_ANNOTATIONS_OVERRIDE, CleanUpModifyDialog.FALSE_TRUE);
		intent(annotationsGroup);
		intent(annotationsGroup);
		final CheckboxPreference overrideInterfacePref= createCheckboxPref(annotationsGroup, numColumns - 2, CleanUpMessages.MissingCodeTabPage_CheckboxName_AddMissingOverrideInterfaceAnnotations, CleanUpConstants.ADD_MISSING_ANNOTATIONS_OVERRIDE_FOR_INTERFACE_METHOD_IMPLEMENTATION, CleanUpModifyDialog.FALSE_TRUE);
		
		intent(annotationsGroup);
		final CheckboxPreference deprecatedPref= createCheckboxPref(annotationsGroup, numColumns - 1, CleanUpMessages.MissingCodeTabPage_CheckboxName_AddMissingDeprecatedAnnotations, CleanUpConstants.ADD_MISSING_ANNOTATIONS_DEPRECATED, CleanUpModifyDialog.FALSE_TRUE);
		
		/* This observable preferences framework is over-designed but under-performing.
		 * I didn't find an easy way to make it work for non-trivial (3-level) dependencies, so I just do it by hand for now...
		 */
		// Need to add observer before registering slaves below, since java.util.Observable notifies in wrong order!
		annotationsPref.addObserver(new Observer() {
			public void update(Observable o, Object arg) {
				boolean enabled= overridePref.getEnabled() && overridePref.getChecked();
				overrideInterfacePref.setEnabled(enabled);
			}
		});
		
		registerSlavePreference(annotationsPref, new CheckboxPreference[] {overridePref, deprecatedPref});
		registerSlavePreference(overridePref, new CheckboxPreference[] {overrideInterfacePref});
		
		overrideInterfacePref.setEnabled(overridePref.getEnabled() && overridePref.getChecked());

		if (!isSaveAction()) {
			Group pppGroup= createGroup(numColumns, composite, CleanUpMessages.MissingCodeTabPage_GroupName_PotentialProgrammingProblems);

			final CheckboxPreference addSUIDPref= createCheckboxPref(pppGroup, numColumns, CleanUpMessages.MissingCodeTabPage_CheckboxName_AddSUID, CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID, CleanUpModifyDialog.FALSE_TRUE);
			intent(pppGroup);
			final RadioPreference generatedPref= createRadioPref(pppGroup, 1, CleanUpMessages.MissingCodeTabPage_RadioName_AddGeneratedSUID, CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID_GENERATED, CleanUpModifyDialog.FALSE_TRUE);
			final RadioPreference defaultPref= createRadioPref(pppGroup, 1, CleanUpMessages.MissingCodeTabPage_RadioName_AddDefaultSUID, CleanUpConstants.ADD_MISSING_SERIAL_VERSION_ID_DEFAULT, CleanUpModifyDialog.FALSE_TRUE);
			registerSlavePreference(addSUIDPref, new RadioPreference[] {generatedPref, defaultPref});

			Group udGroup= createGroup(numColumns, composite, CleanUpMessages.MissingCodeTabPage_GroupName_UnimplementedCode);
			CheckboxPreference addMethodPref= createCheckboxPref(udGroup, numColumns, CleanUpMessages.MissingCodeTabPage_CheckboxName_AddMethods, CleanUpConstants.ADD_MISSING_METHODES, CleanUpModifyDialog.FALSE_TRUE);
			registerPreference(addMethodPref);

			createLabel(numColumns, udGroup, CleanUpMessages.MissingCodeTabPage_Label_CodeTemplatePreferencePage);
		}
    }
}
