package org.jhotdraw.action.edit;

import static org.junit.Assert.assertTrue;

import java.awt.datatransfer.Clipboard;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import org.junit.Test;

/**
 * Unit tests for the important business behavior of {@link CopyAction}.
 * <p>
 * The tests verify that copying a valid component delegates to the component's
 * transfer handler using {@link TransferHandler#COPY}, and that calling the
 * action with a null component does not throw an exception.
 * <p>
 * This documents the refactored action flow that now reuses the shared
 * {@code actionPerformed(...)} implementation in
 * {@link AbstractSelectionAction}.
 */
public class CopyActionTest {

    private boolean exportCalled = false;

    private class MockTransferHandler extends TransferHandler {
        @Override
        public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
            exportCalled = true;
            assert (action == TransferHandler.COPY);
        }
    }

    private static class TestableCopyAction extends CopyAction {
        public void testExecute(JComponent c) {
            execute(c);
        }
    }

    @Test
    public void testCopyOperation() {
        JPanel panel = new JPanel();
        panel.setTransferHandler(new MockTransferHandler());

        TestableCopyAction action = new TestableCopyAction();
        action.testExecute(panel);

        assertTrue(exportCalled);
    }

    @Test
    public void testNullComponent() {
        TestableCopyAction action = new TestableCopyAction();

        action.testExecute(null);

        assertTrue(true);
    }
}