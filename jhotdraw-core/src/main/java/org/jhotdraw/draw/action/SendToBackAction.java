/*
 * @(#)SendToBackAction.java
 *
 * Copyright (c) 2003-2008 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.action;

import org.jhotdraw.draw.figure.Figure;
import java.util.*;
import org.jhotdraw.draw.*;

/**
 * SendToBackAction.
 * <p>
 * Moves the selected figures to the back of the stacking order. The common
 * algorithm (read selection, apply, register an undoable edit) lives in
 * {@link AbstractArrangeAction}; this class only supplies the forward and
 * inverse directions.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class SendToBackAction extends AbstractArrangeAction {

    private static final long serialVersionUID = 1L;
    public static final String ID = "edit.sendToBack";

    /**
     * Creates a new instance.
     */
    public SendToBackAction(DrawingEditor editor) {
        super(editor, ID);
    }

    @Override
    protected void arrange(DrawingView view, Collection<Figure> figures) {
        sendToBack(view, figures);
    }

    @Override
    protected void reverseArrange(DrawingView view, Collection<Figure> figures) {
        BringToFrontAction.bringToFront(view, figures);
    }

    public static void sendToBack(DrawingView view, Collection<Figure> figures) {
        Drawing drawing = view.getDrawing();
        for (Figure figure : figures) { // XXX Shouldn't the figures be sorted here back to front?
            drawing.sendToBack(figure);
        }
    }
}
