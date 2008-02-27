/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.ui.search;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.StyledStringBuilder;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledStringBuilder.Styler;

import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;

import org.eclipse.jdt.internal.corext.util.Messages;

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.viewsupport.ColoredJavaElementLabels;

class OccurrencesSearchLabelProvider extends TextSearchLabelProvider implements IStyledLabelProvider {
	
	public OccurrencesSearchLabelProvider(AbstractTextSearchViewPage page) {
		super(page);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		return getLabelWithCounts(element, internalGetText(element)); 
	}
	
	private String getLineNumberLabel(JavaElementLine element) {
		return Messages.format(SearchMessages.OccurrencesSearchLabelProvider_line_number, new Integer(element.getLine()));
	}
	
	private String internalGetText(Object element) {
		JavaElementLine jel= (JavaElementLine) element;
		return getLineNumberLabel(jel) + jel.getLineContents();
	}
	
	private StyledStringBuilder internalGetRichText(Object element) {
		JavaElementLine jel= (JavaElementLine) element;

		String lineNumberString= getLineNumberLabel(jel);

		Styler highlightStyle= ColoredJavaElementLabels.HIGHLIGHT_STYLE;
		
		StyledStringBuilder res= new StyledStringBuilder();
		res.append(lineNumberString, ColoredJavaElementLabels.QUALIFIER_STYLE);
		res.append(jel.getLineContents());
		Match[] matches= getPage().getInput().getMatches(jel);
		for (int i= 0; i < matches.length; i++) {
			OccurrenceMatch curr= (OccurrenceMatch) matches[i];
			int offset= curr.getOriginalOffset() - jel.getLineStartOffset() + lineNumberString.length();
			int length= curr.getOriginalLength();
			
			if (offset >= 0 && (offset + length <= res.length())) {
				if ((curr.getFlags() & IOccurrencesFinder.F_WRITE_OCCURRENCE) != 0) {
					res.setStyle(offset, length, ColoredJavaElementLabels.HIGHLIGHT_WRITE_STYLE);
				} else {
					res.setStyle(offset, length, highlightStyle);
				}
			}
		}
		return res;
	}
	
	public Image getImage(Object element) {
		if (element instanceof JavaElementLine) {
			int flags= ((JavaElementLine) element).getFlags();
			if ((flags & IOccurrencesFinder.F_WRITE_OCCURRENCE) != 0) {
				return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_SEARCH_WRITEACCESS);
			}
			if ((flags & IOccurrencesFinder.F_READ_OCCURRENCE) != 0) {
				return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_SEARCH_READACCESS);
			}
			if ((flags & IOccurrencesFinder.F_EXCEPTION_DECLARATION) != 0) {				
				return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_EXCEPTION);
			}
		}
		return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_SEARCH_OCCURRENCE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider#getStyledText(java.lang.Object)
	 */
	public StyledStringBuilder getStyledText(Object element) {
		return getColoredLabelWithCounts(element, internalGetRichText(element));			
	}
}
