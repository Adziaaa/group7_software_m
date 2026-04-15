/*
 * @(#)SelectionToolTrackerFactory.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.tool;

import org.jhotdraw.draw.figure.Figure;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.draw.DrawingView;

/**
 * Factory for creating and managing tracker instances used by SelectionTool.
 * <p>
 * This class encapsulates all creation knowledge for tracker objects,
 * separating creation concerns from the selection coordination logic of
 * SelectionTool.
 * <p>
 * Trackers are cached as singletons and reused with configuration as needed.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class SelectionToolTrackerFactory {

    private SelectionTool selectionTool;
    private HandleTracker handleTracker;
    private SelectAreaTracker selectAreaTracker;
    private DragTracker dragTracker;

    /**
     * Creates a new factory for the specified SelectionTool.
     *
     * @param selectionTool The SelectionTool that will use this factory.
     */
    public SelectionToolTrackerFactory(SelectionTool selectionTool) {
        this.selectionTool = selectionTool;
    }

    /**
     * Gets a HandleTracker for the specified handle.
     * <p>
     * The tracker is cached and reused. It is configured with the specified
     * handle and compatible handles.
     *
     * @param handle The handle to track.
     * @return A HandleTracker configured for the specified handle.
     */
    public HandleTracker getHandleTracker(Handle handle) {
        if (handleTracker == null) {
            handleTracker = new DefaultHandleTracker();
        }
        DrawingView view = selectionTool.getView();
        if (view != null) {
            handleTracker.setHandles(handle, view.getCompatibleHandles(handle));
        }
        return handleTracker;
    }

    /**
     * Gets a DragTracker for the specified figure.
     * <p>
     * The tracker is cached and reused. It is configured with the specified
     * figure as the dragged figure.
     *
     * @param figure The figure to drag.
     * @return A DragTracker configured for the specified figure.
     */
    public DragTracker getDragTracker(Figure figure) {
        if (dragTracker == null) {
            dragTracker = new DefaultDragTracker();
        }
        dragTracker.setDraggedFigure(figure);
        return dragTracker;
    }

    /**
     * Gets a SelectAreaTracker for area-based selection.
     * <p>
     * The tracker is cached and reused.
     *
     * @return A SelectAreaTracker.
     */
    public SelectAreaTracker getSelectAreaTracker() {
        if (selectAreaTracker == null) {
            selectAreaTracker = new DefaultSelectAreaTracker();
        }
        return selectAreaTracker;
    }

    /**
     * Sets a custom HandleTracker. If null, the factory will use DefaultHandleTracker.
     *
     * @param newValue The custom HandleTracker, or null for default.
     */
    public void setHandleTracker(HandleTracker newValue) {
        handleTracker = newValue;
    }

    /**
     * Sets a custom SelectAreaTracker. If null, the factory will use DefaultSelectAreaTracker.
     *
     * @param newValue The custom SelectAreaTracker, or null for default.
     */
    public void setSelectAreaTracker(SelectAreaTracker newValue) {
        selectAreaTracker = newValue;
    }

    /**
     * Sets a custom DragTracker. If null, the factory will use DefaultDragTracker.
     *
     * @param newValue The custom DragTracker, or null for default.
     */
    public void setDragTracker(DragTracker newValue) {
        dragTracker = newValue;
    }
}
