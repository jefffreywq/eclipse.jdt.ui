package org.eclipse.jdt.internal.ui.javaeditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

 
import org.eclipse.core.resources.IFile;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IMenuManager;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.ui.JavaPlugin;


/**
 * Java specific text editor.
 */
public class ClassFileEditor extends JavaEditor {
	
	/**
	 * Default constructor.
	 */
	public ClassFileEditor() {
		super();
		setDocumentProvider(JavaPlugin.getDefault().getClassFileDocumentProvider());
		setEditorContextMenuId("#ClassFileEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#ClassFileRulerContext"); //$NON-NLS-1$
		setOutlinerContextMenuId("#ClassFileOutlinerContext"); //$NON-NLS-1$
	}
	
	/**
	 * @see AbstractTextEditor#createActions
	 */
	protected void createActions() {
		super.createActions();
		
		setAction(ITextEditorActionConstants.SAVE, null);
		setAction(ITextEditorActionConstants.REVERT_TO_SAVED, null);
		
		setAction("AddBreakpoint", new AddBreakpointAction(this)); //$NON-NLS-1$
		setAction("ManageBreakpoints", new BreakpointRulerAction(getVerticalRuler(), this)); //$NON-NLS-1$
		
		/*
		 * 1GF82PL: ITPJUI:ALL - Need to be able to add bookmark to classfile
		 *
		 *  // replace default action with class file specific ones
		 *
		 *	setAction(ITextEditorActionConstants.BOOKMARK, new AddClassFileMarkerAction("AddBookmark.", this, IMarker.BOOKMARK, true)); //$NON-NLS-1$
		 *	setAction(ITextEditorActionConstants.ADD_TASK, new AddClassFileMarkerAction("AddTask.", this, IMarker.TASK, false)); //$NON-NLS-1$
		 *	setAction(ITextEditorActionConstants.RULER_MANAGE_BOOKMARKS, new ClassFileMarkerRulerAction("ManageBookmarks.", getVerticalRuler(), this, IMarker.BOOKMARK, true)); //$NON-NLS-1$
		 *	setAction(ITextEditorActionConstants.RULER_MANAGE_TASKS, new ClassFileMarkerRulerAction("ManageTasks.", getVerticalRuler(), this, IMarker.TASK, true)); //$NON-NLS-1$
		 */
		setAction(ITextEditorActionConstants.BOOKMARK, null);
		setAction(ITextEditorActionConstants.ADD_TASK, null);
		setAction(ITextEditorActionConstants.RULER_MANAGE_BOOKMARKS, null);
		setAction(ITextEditorActionConstants.RULER_MANAGE_TASKS, null);
		
		setAction(ITextEditorActionConstants.RULER_DOUBLE_CLICK, getAction("ManageBreakpoints"));		 //$NON-NLS-1$
	}
	
	/**
	 * @see JavaEditor#getJavaSourceReferenceAt
	 */
	protected ISourceReference getJavaSourceReferenceAt(int position) {
		if (getEditorInput() instanceof IClassFileEditorInput) {
			try {
				
				IClassFileEditorInput input= (IClassFileEditorInput) getEditorInput();
				IJavaElement element= input.getClassFile().getElementAt(position);
				if (element instanceof ISourceReference)
					return (ISourceReference) element;
			} catch (JavaModelException x) {
			}
		}
		return null;
	}
	
	/**
	 * @see EditorPart#init(IEditorSite, IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		
		if (input instanceof IFileEditorInput) {
			IFile file= ((IFileEditorInput) input).getFile();
			IClassFileEditorInput classFileInput= new ExternalClassFileEditorInput(file);
			if (classFileInput.getClassFile() != null)
				input= classFileInput;
		}
		
		if (!(input instanceof IClassFileEditorInput))
			throw new PartInitException(JavaEditorMessages.getString("ClassFileEditor.error.invalid_input_message")); //$NON-NLS-1$
			
		super.init(site, input);
	}
	
	/**
	 * @see IEditorPart#saveState(IMemento)
	 */
	public void saveState(IMemento memento) {
	}
		
	/**
	 * @see AbstractTextEditor#editorContextMenuAboutToShow
	 */
	public void editorContextMenuAboutToShow(IMenuManager menu) {
		
		super.editorContextMenuAboutToShow(menu);
		
		if (getEditorInput() instanceof IClassFileEditorInput) {
			
			IClassFileEditorInput input= (IClassFileEditorInput) getEditorInput();
			IClassFile file= input.getClassFile();
			
			try {
				if (file.getSource() != null)
					addAction(menu, ITextEditorActionConstants.GROUP_ADD, "AddBreakpoint"); //$NON-NLS-1$
			} catch (JavaModelException x) {
				// ignore
			}
		}
	}
	
	/**
	 * @see AbstractTextEditor#rulerContextMenuAboutToShow
	 */
	protected void rulerContextMenuAboutToShow(IMenuManager menu) {
		super.rulerContextMenuAboutToShow(menu);
		
		if (getEditorInput() instanceof IClassFileEditorInput) {
			
			IClassFileEditorInput input= (IClassFileEditorInput) getEditorInput();
			IClassFile file= input.getClassFile();
			
			try {
				if (file.getSource() != null)
					addAction(menu, "ManageBreakpoints"); //$NON-NLS-1$
			} catch (JavaModelException x) {
				// ignore
			}
		}
	}
	
	/**
	 * @see JavaEditor#setOutlinePageInput(JavaOutlinePage, IEditorInput)
	 */
	protected void setOutlinePageInput(JavaOutlinePage page, IEditorInput input) {
		if (page != null && input instanceof IClassFileEditorInput) {
			IClassFileEditorInput cfi= (IClassFileEditorInput) input;
			page.setInput(cfi.getClassFile());
		}
	}
	
	/*
	 * 1GEPKT5: ITPJUI:Linux - Source in editor for external classes is editable
	 * Removed methods isSaveOnClosedNeeded and isDirty.
	 * Added method isEditable.
	 */
	/**
	 * @see AbstractTextEditor#isEditable()
	 */
	public boolean isEditable() {
		return false;
	}
	
	/*
	 * @see AbstractTextEditor#doSetInput(IEditorInput)
	 */
	protected void doSetInput(IEditorInput input) throws CoreException {
		if (input instanceof ExternalClassFileEditorInput) {
			ExternalClassFileEditorInput classFileInput= (ExternalClassFileEditorInput) input;
			IFile file= classFileInput.getFile();
			try {
				file.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException x) {
				JavaPlugin.log(x);
			}
		}
		super.doSetInput(input);
	}
}