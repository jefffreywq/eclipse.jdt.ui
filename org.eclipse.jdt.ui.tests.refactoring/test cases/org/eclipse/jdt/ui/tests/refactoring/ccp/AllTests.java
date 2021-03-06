/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.ui.tests.refactoring.ccp;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite ( ) {
		TestSuite suite= new TestSuite(AllTests.class.getName());
		suite.addTest(DeleteTest.suite());
		suite.addTest(CopyToClipboardActionTest.suite());
		suite.addTest(PasteActionTest.suite());
		suite.addTest(CopyTest.suite());
		suite.addTest(MoveTest.suite());
		suite.addTest(MultiMoveTest.suite());

		//------old reorg tests
		suite.addTest(CopyResourcesToClipboardActionTest.suite());

		return suite;
	}
}
