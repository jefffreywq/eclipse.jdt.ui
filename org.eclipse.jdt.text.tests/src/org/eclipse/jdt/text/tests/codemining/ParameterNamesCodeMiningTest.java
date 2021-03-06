/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.text.tests.codemining;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;

import org.eclipse.jdt.testplugin.JavaProjectHelper;

import org.eclipse.swt.custom.StyledText;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.text.tests.util.DisplayHelper;

import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import org.eclipse.jdt.ui.PreferenceConstants;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.javaeditor.codemining.JavaMethodParameterCodeMiningProvider;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ParameterNamesCodeMiningTest extends TestCase {

	private IJavaProject fProject;
	private IPackageFragment fPackage;
	private JavaMethodParameterCodeMiningProvider fParameterNameCodeMiningProvider;

	public static Test suite() {
		return new TestSuite(ParameterNamesCodeMiningTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if(!welcomeClosed) {
			closeIntro(PlatformUI.getWorkbench());
		}
		fProject= JavaProjectHelper.createJavaProject(getClass().getName(), "bin");
		JavaProjectHelper.addRTJar(fProject);
		IPackageFragmentRoot root= JavaProjectHelper.addSourceContainer(fProject, "src");
		fPackage= root.getPackageFragment("");
		fParameterNameCodeMiningProvider= new JavaMethodParameterCodeMiningProvider();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		IWorkbenchPage workbenchPage= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		for (IEditorReference ref : workbenchPage.getEditorReferences()) {
			workbenchPage.closeEditor(ref.getEditor(false), false);
		}
	}

	public void testParameterNamesOK() throws Exception {
		String contents= "public class Foo {\n" +
				"	int n = Math.max(1, 2);\n" +
				"}\n";
		ICompilationUnit compilationUnit= fPackage.createCompilationUnit("Foo.java", contents, true, new NullProgressMonitor());
		JavaEditor editor= (JavaEditor) EditorUtility.openInEditor(compilationUnit);
		fParameterNameCodeMiningProvider.setContext(editor);
		ISourceViewer viewer = editor.getViewer();
		assertEquals(2, fParameterNameCodeMiningProvider.provideCodeMinings(viewer, new NullProgressMonitor()).get().size());
	}

	public void testVarargs() throws Exception {
		String contents= "public class Foo {\n" +
				"	String s = String.format(\"%d %d\", 1, 2);\n" +
				"}\n";
		ICompilationUnit compilationUnit= fPackage.createCompilationUnit("Foo.java", contents, true, new NullProgressMonitor());
		JavaEditor editor= (JavaEditor) EditorUtility.openInEditor(compilationUnit);
		fParameterNameCodeMiningProvider.setContext(editor);
		ISourceViewer viewer = editor.getViewer();
		assertEquals(2, fParameterNameCodeMiningProvider.provideCodeMinings(viewer, new NullProgressMonitor()).get().size());
	}

	public void testUnresolvedMethodBinding() throws Exception {
		String contents= "public class Foo {\n" +
		"	public void mehod() {\n" +
		"		List<String> list = Arrays.asList(\"foo\", \"bar\");\n" +
		"		System.out.printf(\"%s %s\", list.get(0), list.get(1));\n" +
		"	}\n" +
		"}";
		ICompilationUnit compilationUnit= fPackage.createCompilationUnit("Foo.java", contents, true, new NullProgressMonitor());
		JavaEditor editor= (JavaEditor) EditorUtility.openInEditor(compilationUnit);
		fParameterNameCodeMiningProvider.setContext(editor);
		ISourceViewer viewer = editor.getViewer();
		// Only code mining on "printf" parameters
		assertEquals(2, fParameterNameCodeMiningProvider.provideCodeMinings(viewer, new NullProgressMonitor()).get().size());
	}

	public void testCollapsedFolding() throws Exception {
		String contents= "/**\n" +
				" * aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
				" * aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
				" * aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
				" * aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
				" * aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
				" * aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
				" * aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
				" * aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
				" */" +
				"public class Foo {\n" +
				"	int n = Math.max(1, 2);\n" +
				"}";
		ICompilationUnit compilationUnit= fPackage.createCompilationUnit("Foo.java", contents, true, new NullProgressMonitor());
		JavaEditor editor= (JavaEditor) EditorUtility.openInEditor(compilationUnit);
		fParameterNameCodeMiningProvider.setContext(editor);
		JavaSourceViewer viewer = (JavaSourceViewer)editor.getViewer();
		viewer.doOperation(ProjectionViewer.COLLAPSE_ALL);
		viewer.setCodeMiningProviders(new ICodeMiningProvider[] {
			fParameterNameCodeMiningProvider
		});
		//
		ILog log= WorkbenchPlugin.getDefault().getLog();
		AtomicReference<IStatus> errorInLog = new AtomicReference<>();
		ILogListener logListener= (status, plugin) -> {
			if (status.getSeverity() == IStatus.ERROR) {
				errorInLog.set(status);
			}
		};
		try {
			log.addLogListener(logListener);
			DisplayHelper.sleep(editor.getViewer().getTextWidget().getDisplay(), 1000);
			Assert.assertNull(errorInLog.get());
		} finally {
			log.removeLogListener(logListener);
		}
	}

	public void testCollapsedFoldingAndToggleHighlight() throws Exception {
		String contents= "/**\n" +
				" *\n" +
				" */" +
				"public class Foo {\n" +
				"	int n = Math.max(1, 2);\n" +
				"}";
		ICompilationUnit compilationUnit= fPackage.createCompilationUnit("Foo.java", contents, true, new NullProgressMonitor());
		JavaEditor editor= (JavaEditor) EditorUtility.openInEditor(compilationUnit);
		fParameterNameCodeMiningProvider.setContext(editor);
		JavaSourceViewer viewer = (JavaSourceViewer)editor.getViewer();
		viewer.setCodeMiningProviders(new ICodeMiningProvider[] {
			fParameterNameCodeMiningProvider
		});
		//
		StyledText widget = viewer.getTextWidget();
		int charWidth = widget.getTextBounds(0, 1).width;
		assertTrue("Code mining not available on expected chars", new DisplayHelper() {
			@Override
			protected boolean condition() {
				return Arrays.stream(widget.getStyleRanges(widget.getText().indexOf(", 2"), 3)).anyMatch(style ->
						style.metrics != null && style.metrics.width > charWidth);
			}
		}.waitForCondition(widget.getDisplay(), 2000));
		//
		viewer.doOperation(ProjectionViewer.COLLAPSE_ALL);
		assertTrue("Code mining not available on expected chars after collapsing", new DisplayHelper() {
			@Override
			protected boolean condition() {
				return Arrays.stream(widget.getStyleRanges(widget.getText().indexOf(", 2"), 3)).anyMatch(style ->
						style.metrics != null && style.metrics.width > charWidth);
			}
		}.waitForCondition(widget.getDisplay(), 2000));
		//
		viewer.setSelectedRange(viewer.getDocument().get().indexOf("max") + 1, 0);
		IPreferenceStore preferenceStore= JavaPlugin.getDefault().getPreferenceStore();
		boolean initial = preferenceStore.getBoolean(PreferenceConstants.EDITOR_MARK_OCCURRENCES);
		try {
			preferenceStore.setValue(PreferenceConstants.EDITOR_MARK_OCCURRENCES, true);
			assertTrue("Occurence annotation not added", new org.eclipse.jdt.text.tests.performance.DisplayHelper() {
				@Override
				protected boolean condition() {
					AtomicInteger annotationCount = new AtomicInteger();
					viewer.getAnnotationModel().getAnnotationIterator().forEachRemaining(annotation -> {
						if (annotation.getType().contains("occurrence")) {
							annotationCount.incrementAndGet();
						}
					});
					return annotationCount.get() != 0;
				}
			}.waitForCondition(widget.getDisplay(), 2000));
			assertTrue("Code mining space at undesired offset after collapsing", new DisplayHelper() {
				@Override
				protected boolean condition() {
					return Arrays.stream(widget.getStyleRanges())
							.filter(range -> range.metrics != null && range.metrics.width > charWidth)
							.allMatch(style -> {
								char c = widget.getText().charAt(style.start + 1);
								return c == '1' || c == '2';
							});
				}
			}.waitForCondition(widget.getDisplay(), 2000));
		} finally {
			preferenceStore.setValue(PreferenceConstants.EDITOR_MARK_OCCURRENCES, initial);
		}
	}

	private static boolean welcomeClosed;
	private static void closeIntro(final IWorkbench wb) {
		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
		if (window != null) {
			IIntroManager im = wb.getIntroManager();
			IIntroPart intro = im.getIntro();
			if (intro != null) {
				welcomeClosed = im.closeIntro(intro);
			}
		}
	}
}
