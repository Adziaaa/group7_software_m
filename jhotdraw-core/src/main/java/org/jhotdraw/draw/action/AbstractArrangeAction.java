/*
 * @(#)AbstractArrangeAction.java
 *
 * Copyright (c) 2003-2008 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.action;

import org.jhotdraw.draw.figure.Figure;
import java.util.*;
import javax.swing.undo.*;
import org.jhotdraw.draw.*;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * Base class for the "arrange" actions that change the stacking order (z-order)
 * of the selected figures, such as {@link BringToFrontAction} and
 * {@link SendToBackAction}.
 * <p>
 * This class captures the algorithm shared by all arrange actions as a
 * <em>template method</em> ({@link #actionPerformed}): it reads the selected
 * figures, applies the arrange operation, and registers a reversible
 * {@code UndoableEdit}. Subclasses only need to supply the two directions of
 * the operation through the primitive operations {@link #arrange} and
 * {@link #reverseArrange}.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public abstract class AbstractArrangeAction extends AbstractSelectedAction {

    private static final long serialVersionUID = 1L;
    /**
     * The label/action id used for the presentation name of the undoable edit.
     */
    private final String labelId;

    /**
     * Creates a new instance.
     *
     * @param editor the drawing editor this action belongs to
     * @param labelId the resource id used to configure this action and to name
     * its undoable edit
     */
    protected AbstractArrangeAction(DrawingEditor editor, String labelId) {
        super(editor);
        this.labelId = labelId;
        ResourceBundleUtil labels
                = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
        labels.configureAction(this, labelId);
        updateEnabledState();
    }

    /**
     * Applies the arrange operation in the forward direction.
     */
    protected abstract void arrange(DrawingView view, Collection<Figure> figures);

    /**
     * Applies the inverse arrange operation. This is used to undo the effect of
     * {@link #arrange}.
     */
    protected abstract void reverseArrange(DrawingView view, Collection<Figure> figures);

    @Override
    public final void actionPerformed(java.awt.event.ActionEvent e) {
        final DrawingView view = getView();
        final LinkedList<Figure> figures = new LinkedList<>(view.getSelectedFigures());
        arrange(view, figures);
        fireUndoableEditHappened(new AbstractUndoableEdit() {
            private static final long serialVersionUID = 1L;

            @Override
            public String getPresentationName() {
                ResourceBundleUtil labels
                        = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
                return labels.getTextProperty(labelId);
            }

            @Override
            public void redo() throws CannotRedoException {
                super.redo();
                arrange(view, figures);
            }

            @Override
            public void undo() throws CannotUndoException {
                super.undo();
                reverseArrange(view, figures);
            }
        });
    }
}
