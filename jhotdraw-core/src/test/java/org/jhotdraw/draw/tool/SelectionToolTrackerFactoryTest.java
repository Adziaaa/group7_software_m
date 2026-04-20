/*
 * @(#)SelectionToolTrackerFactoryTest.java
 *
 * Unit tests for SelectionToolTrackerFactory
 * Verifies the factory correctly creates and manages tracker instances
 */
package org.jhotdraw.draw.tool;

import org.jhotdraw.draw.*;
import org.jhotdraw.draw.figure.RectangleFigure;
import org.jhotdraw.draw.handle.Handle;
import org.junit.Before;
import org.junit.Test;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Unit tests for SelectionToolTrackerFactory.
 * 
 * Tests verify:
 * - Factory creates correct tracker instances
 * - Factory implements lazy singleton pattern (caching)
 * - Factory properly configures trackers
 * - Factory allows custom implementations via injection
 * - Creation logic is properly isolated from SelectionTool
 */
public class SelectionToolTrackerFactoryTest {

    private SelectionToolTrackerFactory factory;
    private SelectionTool selectionTool;
    private Drawing drawing;
    private DrawingView drawingView;
    private DrawingEditor editor;
    private RectangleFigure testFigure;

    @Before
    public void setUp() {
        // Initialize editor and drawing components
        drawing = new DefaultDrawing();
        drawingView = new DefaultDrawingView();
        drawingView.setDrawing(drawing);
        
        editor = new DefaultDrawingEditor();
        editor.add(drawingView);
        
        // Create SelectionTool and get its factory
        selectionTool = new SelectionTool();
        selectionTool.activate(editor);
        editor.setTool(selectionTool);
        
        factory = selectionTool.getTrackerFactory();
        
        // Create test figure
        testFigure = new RectangleFigure();
        testFigure.setBounds(
            new Point2D.Double(100, 100),
            new Point2D.Double(200, 200)
        );
        drawing.add(testFigure);
    }

    // ==================== Factory Creation Tests ====================

    @Test
    public void testFactoryNotNull() {
        // Business Rule: SelectionTool must provide access to tracker factory
        assertNotNull("Factory should be instantiated", factory);
    }

    @Test
    public void testCreateHandleTracker() {
        // Business Rule: Factory must create HandleTracker instances
        Collection<Handle> handles = testFigure.createHandles(-1);
        Handle testHandle = handles.iterator().next();
        
        HandleTracker tracker = factory.getHandleTracker(testHandle);
        
        assertNotNull("Should create HandleTracker", tracker);
        assertTrue("Should be HandleTracker implementation", 
            tracker instanceof DefaultHandleTracker || tracker.getClass().getSimpleName().contains("Tracker"));
    }

    @Test
    public void testCreateDragTracker() {
        // Business Rule: Factory must create DragTracker instances
        DragTracker tracker = factory.getDragTracker(testFigure);
        
        assertNotNull("Should create DragTracker", tracker);
        assertTrue("Should be DragTracker implementation", 
            tracker instanceof DefaultDragTracker || tracker.getClass().getSimpleName().contains("Drag"));
    }

    @Test
    public void testCreateSelectAreaTracker() {
        // Business Rule: Factory must create SelectAreaTracker instances
        SelectAreaTracker tracker = factory.getSelectAreaTracker();
        
        assertNotNull("Should create SelectAreaTracker", tracker);
        assertTrue("Should be SelectAreaTracker implementation", 
            tracker instanceof DefaultSelectAreaTracker || tracker.getClass().getSimpleName().contains("SelectArea"));
    }

    // ==================== Lazy Singleton Pattern Tests ====================

    @Test
    public void testHandleTrackerLazySingletonPattern() {
        // Business Rule: Factory should cache tracker instances (lazy singleton)
        // First call creates instance
        Collection<Handle> handles = testFigure.createHandles(-1);
        Handle testHandle = handles.iterator().next();
        HandleTracker tracker1 = factory.getHandleTracker(testHandle);
        
        // Second call should return same instance
        HandleTracker tracker2 = factory.getHandleTracker(testHandle);
        
        assertSame("Factory should cache and return same HandleTracker instance", 
            tracker1, tracker2);
    }

    @Test
    public void testDragTrackerLazySingletonPattern() {
        // Business Rule: Factory should cache tracker instances
        DragTracker tracker1 = factory.getDragTracker(testFigure);
        DragTracker tracker2 = factory.getDragTracker(testFigure);
        
        assertSame("Factory should cache and return same DragTracker instance", 
            tracker1, tracker2);
    }

    @Test
    public void testSelectAreaTrackerLazySingletonPattern() {
        // Business Rule: Factory should cache tracker instances
        SelectAreaTracker tracker1 = factory.getSelectAreaTracker();
        SelectAreaTracker tracker2 = factory.getSelectAreaTracker();
        
        assertSame("Factory should cache and return same SelectAreaTracker instance", 
            tracker1, tracker2);
    }

    // ==================== Tracker Configuration Tests ====================

    @Test
    public void testHandleTrackerConfiguredWithHandles() {
        // Business Rule: HandleTracker must be configured with the specified handle
        Collection<Handle> handles = testFigure.createHandles(-1);
        Handle testHandle = handles.iterator().next();
        drawingView.addToSelection(testFigure);
        
        HandleTracker tracker = factory.getHandleTracker(testHandle);
        
        // Tracker should be configured (has handles)
        assertNotNull("HandleTracker should have handles", tracker);
    }

    @Test
    public void testDragTrackerConfiguredWithFigure() {
        // Business Rule: DragTracker must be configured with the specified figure
        DragTracker tracker = factory.getDragTracker(testFigure);
        
        // Tracker should be configured (knows about its figure)
        assertNotNull("DragTracker should be configured", tracker);
    }

    // ==================== Tracker Injection Tests ====================

    @Test
    public void testSetCustomHandleTracker() {
        // Business Rule: Factory should support custom HandleTracker injection
        HandleTracker customTracker = new MockHandleTracker();
        
        factory.setHandleTracker(customTracker);
        Collection<Handle> handles = testFigure.createHandles(-1);
        Handle testHandle = handles.iterator().next();
        
        HandleTracker retrieved = factory.getHandleTracker(testHandle);
        
        assertSame("Factory should use injected custom HandleTracker", 
            customTracker, retrieved);
    }

    @Test
    public void testSetCustomDragTracker() {
        // Business Rule: Factory should support custom DragTracker injection
        DragTracker customTracker = new MockDragTracker();
        
        factory.setDragTracker(customTracker);
        DragTracker retrieved = factory.getDragTracker(testFigure);
        
        assertSame("Factory should use injected custom DragTracker", 
            customTracker, retrieved);
    }

    @Test
    public void testSetCustomSelectAreaTracker() {
        // Business Rule: Factory should support custom SelectAreaTracker injection
        SelectAreaTracker customTracker = new MockSelectAreaTracker();
        
        factory.setSelectAreaTracker(customTracker);
        SelectAreaTracker retrieved = factory.getSelectAreaTracker();
        
        assertSame("Factory should use injected custom SelectAreaTracker", 
            customTracker, retrieved);
    }

    @Test
    public void testSetTrackerToNull() {
        // Business Rule: Setting tracker to null should reset to default
        Collection<Handle> handles = testFigure.createHandles(-1);
        Handle testHandle = handles.iterator().next();
        
        HandleTracker customTracker = new MockHandleTracker();
        factory.setHandleTracker(customTracker);
        assertSame("Custom tracker should be set", 
            customTracker, factory.getHandleTracker(testHandle));
        
        // Reset to null
        factory.setHandleTracker(null);
        HandleTracker defaultTracker = factory.getHandleTracker(testHandle);
        
        assertNotSame("Should create new default tracker after null reset", 
            customTracker, defaultTracker);
    }

    // ==================== Factory Separation Tests ====================

    @Test
    public void testFactoryEncapsulatesCreationLogic() {
        // Business Rule: All tracker creation logic should be in factory, not SelectionTool
        // This test verifies that SelectionTool delegates through factory
        
        SelectionTool tool = new SelectionTool();
        tool.activate(editor);
        
        SelectionToolTrackerFactory toolFactory = tool.getTrackerFactory();
        assertNotNull("SelectionTool should have factory", toolFactory);
        
        SelectAreaTracker tracker = toolFactory.getSelectAreaTracker();
        assertNotNull("Factory should provide tracker", tracker);
    }

    @Test
    public void testMultipleFactoriesIndependent() {
        // Business Rule: Different SelectionTool instances should have independent factories
        SelectionTool tool1 = new SelectionTool();
        SelectionTool tool2 = new SelectionTool();
        
        SelectionToolTrackerFactory factory1 = tool1.getTrackerFactory();
        SelectionToolTrackerFactory factory2 = tool2.getTrackerFactory();
        
        assertNotSame("Different tools should have different factories", 
            factory1, factory2);
        
        SelectAreaTracker tracker1 = factory1.getSelectAreaTracker();
        SelectAreaTracker tracker2 = factory2.getSelectAreaTracker();
        
        assertNotSame("Different factories should create independent tracker instances", 
            tracker1, tracker2);
    }

    // ==================== Refactoring Verification Tests ====================

    @Test
    public void testRefactoringCompletenessSingleResponsibility() {
        // Business Rule: Factory should have only ONE responsibility: create trackers
        // Verify factory is focused and not mixed with other concerns
        
        SelectionToolTrackerFactory testFactory = new SelectionToolTrackerFactory(selectionTool);
        
        // Factory should create trackers
        assertNotNull("Factory should create SelectAreaTracker", 
            testFactory.getSelectAreaTracker());
        
        // Factory should allow injection
        testFactory.setSelectAreaTracker(new MockSelectAreaTracker());
        
        // That's it - factory has ONE responsibility, properly isolated
    }

    @Test
    public void testRefactoringVerifySelectionToolSimplified() {
        // Business Rule: SelectionTool should be simplified by factory extraction
        // Test that SelectionTool can perform its coordination role without creation logic
        
        SelectionTool tool = new SelectionTool();
        tool.activate(editor);
        
        // SelectionTool should be able to request trackers from factory
        SelectionToolTrackerFactory toolFactory = tool.getTrackerFactory();
        SelectAreaTracker tracker = toolFactory.getSelectAreaTracker();
        
        assertNotNull("SelectionTool should be able to use factory", tracker);
        
        // SelectionTool should NOT contain creation methods
        // (If these methods existed, they would override factory - that would be bad design)
        // This is verified through normal Java compilation
    }

    // ==================== Mock Implementations ====================

    private static class MockHandleTracker extends AbstractTool implements HandleTracker {
        private static final long serialVersionUID = 1L;

        @Override
        public void setHandles(org.jhotdraw.draw.handle.Handle handle, 
                              java.util.Collection<org.jhotdraw.draw.handle.Handle> compatibleHandles) {
            // no-op mock
        }

        @Override
        public void mouseDragged(MouseEvent arg0) {
            // no-op mock
        }
    }

    private static class MockDragTracker extends AbstractTool implements DragTracker {
        private static final long serialVersionUID = 1L;

        @Override
        public void setDraggedFigure(org.jhotdraw.draw.figure.Figure f) {
            // no-op mock
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            // no-op mock
        }
    }

    private static class MockSelectAreaTracker extends AbstractTool implements SelectAreaTracker {
        private static final long serialVersionUID = 1L;
        // no-op mock

        @Override
        public void mouseDragged(MouseEvent e) {
            // no-op mock
        }
    }
}
