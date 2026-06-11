/*
 * @(#)UndoRedoManager.java
 *
 * Copyright (c) 1996-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.undo;

import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.undo.*;
import org.jhotdraw.util.*;

/**
 * Same as javax.swing.UndoManager but provides actions for undo and
 * redo operations.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class UndoRedoManager extends UndoManager { //javax.swing.undo.UndoManager {

    private static final long serialVersionUID = 1L;
    protected PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);
    private static final boolean DEBUG = false;
    /**
     * The resource bundle used for internationalisation.
     */
    private static ResourceBundleUtil labels;
    /**
     * This flag is set to true when at
     * least one significant UndoableEdit
     * has been added to the manager since the
     * last call to discardAllEdits.
     */
    private boolean hasSignificantEdits = false;
    /**
     * This flag is set to true when an undo or redo
     * operation is in progress. The UndoRedoManager
     * ignores all incoming UndoableEdit events while
     * this flag is true.
     */
    private boolean undoOrRedoInProgress;
    /**
     * Sending this UndoableEdit event to the UndoRedoManager
     * disables the Undo and Redo functions of the manager.
     */
    public static final UndoableEdit DISCARD_ALL_EDITS = new AbstractUndoableEdit() {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean canUndo() {
            return false;
        }

        @Override
        public boolean canRedo() {
            return false;
        }
    };

    /**
     * A single parameterised action for both undo and redo. The label key
     * and the operation to run are supplied at construction, removing the
     * duplication between the former UndoAction and RedoAction classes.
     */
    private class UndoRedoAction extends AbstractAction {
        private final Runnable operation;

        UndoRedoAction(String labelKey, Runnable operation) {
            this.operation = operation;
            labels.configureAction(this, labelKey);
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            try {
                operation.run();
            } catch (CannotUndoException | CannotRedoException e) {
                System.err.println("Cannot perform operation: " + e);
            }
        }
    }
    /**
     * The undo action instance.
     */
    private UndoRedoAction undoAction;
    /**
     * The redo action instance.
     */
    private UndoRedoAction redoAction;

    public static ResourceBundleUtil getLabels() {
        if (labels == null) {
            labels = ResourceBundleUtil.getBundle("org.jhotdraw.undo.Labels");
        }
        return labels;
    }

    /**
     * Creates new UndoRedoManager
     */
    public UndoRedoManager() {
        getLabels();
        undoAction = new UndoRedoAction("edit.undo", this::undo);
        redoAction = new UndoRedoAction("edit.redo", this::redo);
    }

    public void setLocale(Locale l) {
        labels = ResourceBundleUtil.getBundle("org.jhotdraw.undo.Labels", l);
    }

    /**
     * Discards all edits.
     */
    @Override
    public void discardAllEdits() {
        super.discardAllEdits();
        updateActions();
        setHasSignificantEdits(false);
    }

    public void setHasSignificantEdits(boolean newValue) {
        boolean oldValue = hasSignificantEdits;
        hasSignificantEdits = newValue;
        firePropertyChange("hasSignificantEdits", oldValue, newValue);
    }

    /**
     * Returns true if at least one significant UndoableEdit
     * has been added since the last call to discardAllEdits.
     */
    public boolean hasSignificantEdits() {
        return hasSignificantEdits;
    }

    /**
     * If inProgress, inserts anEdit at indexOfNextAdd, and removes
     * any old edits that were at indexOfNextAdd or later. The die
     * method is called on each edit that is removed is sent, in the
     * reverse of the order the edits were added. Updates
     * indexOfNextAdd.
     *
     * <p>
     * If not inProgress, acts as a CompoundEdit</p>
     *
     * <p>
     * Regardless of inProgress, if undoOrRedoInProgress,
     * calls die on each edit that is sent.</p>
     *
     *
     * @see CompoundEdit#end
     * @see CompoundEdit#addEdit
     */
    @Override
    public boolean addEdit(UndoableEdit anEdit) {
        if (DEBUG) {
            System.out.println("UndoRedoManager@" + hashCode() + ".add " + anEdit);
        }
        if (undoOrRedoInProgress) {
            anEdit.die();
            return true;
        }
        boolean success = super.addEdit(anEdit);
        updateActions();
        if (success && anEdit.isSignificant() && editToBeUndone() == anEdit) {
            setHasSignificantEdits(true);
        }
        return success;
    }

    /**
     * Gets the undo action for use as an Undo menu item.
     */
    public Action getUndoAction() {
        return undoAction;
    }

    /**
     * Gets the redo action for use as a Redo menu item.
     */
    public Action getRedoAction() {
        return redoAction;
    }

    /**
     * Updates the properties of the UndoAction
     * and of the RedoAction.
     */
    private void updateActions() {
    configureActionState(undoAction, canUndo(),
            getUndoPresentationName(), "edit.undo.text");
    configureActionState(redoAction, canRedo(),
            getRedoPresentationName(), "edit.redo.text");
}

    /**
     * Enables or disables the given action and sets its name and short
     * description. Centralises the logic previously duplicated for the
     * undo and redo actions.
     *
     * @param action           the action to configure
     * @param available        whether the operation is currently possible
     * @param presentationName the label to use when the operation is available
     * @param disabledLabelKey the resource key for the label when disabled
     */
    private void configureActionState(Action action, boolean available,
            String presentationName, String disabledLabelKey) {
        String label = available ? presentationName : labels.getString(disabledLabelKey);
        action.setEnabled(available);
        action.putValue(Action.NAME, label);
        action.putValue(Action.SHORT_DESCRIPTION, label);
    }

        /**
     * Runs an undo/redo operation while suppressing incoming UndoableEdit
     * events, and refreshes the action state afterwards. Centralises the
     * guard logic previously duplicated in undo(), redo() and undoOrRedo().
     */
    private void runTracked(Runnable operation) {
        undoOrRedoInProgress = true;
        try {
            operation.run();
        } finally {
            undoOrRedoInProgress = false;
            updateActions();
        }
    }

    @Override
    public void undo() throws CannotUndoException {
        runTracked(super::undo);
    }

    @Override
    public void redo() throws CannotUndoException {
        runTracked(super::redo);
    }

    @Override
    public void undoOrRedo() throws CannotUndoException, CannotRedoException {
        runTracked(super::undoOrRedo);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(propertyName, listener);
    }

    protected void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        propertySupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected void firePropertyChange(String propertyName, int oldValue, int newValue) {
        propertySupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertySupport.firePropertyChange(propertyName, oldValue, newValue);
    }
}
