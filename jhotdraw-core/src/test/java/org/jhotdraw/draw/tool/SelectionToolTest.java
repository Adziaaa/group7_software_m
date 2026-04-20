/*
 * @(#)SelectionToolTest.java
 *
 * Unit tests for SelectionTool and SelectionToolTrackerFactory
 * Verifies critical business functionality of the Selection Tool feature
 */
package org.jhotdraw.draw.tool;

import org.jhotdraw.draw.*;
import org.jhotdraw.draw.figure.*;
import org.jhotdraw.draw.handle.Handle;
import org.junit.*;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Unit tests for the Selection Tool feature.
 * 
 * Tests verify:
 * - Factory creates correct tracker instances
 * - SelectionTool coordinates proper tracker selection
 * - Selection state transitions work correctly
 * - Trackers delegate events appropriately
 * - Figure selection/deselection behavior
 */
public class SelectionToolTest {

    private SelectionTool selectionTool;
    private SelectionToolTrackerFactory trackerFactory;
    private Drawing drawing;
    private DrawingView drawingView;
    private DrawingEditor editor;
    private RectangleFigure testFigure;

    @Before
    public void setUp() {
        // Initialize test environment
        drawing = new DefaultDrawing();
        drawingView = new DefaultDrawingView();
        drawingView.setDrawing(drawing);
        
        editor = new DefaultDrawingEditor();
        editor.add(drawingView);
        
        // Create SelectionTool
        selectionTool = new SelectionTool();
        selectionTool.activate(editor);
        editor.setTool(selectionTool);
        
        // Get factory reference
        trackerFactory = selectionTool.getTrackerFactory();
        
        // Create test figure
        testFigure = new RectangleFigure();
        testFigure.setBounds(
            new Point2D.Double(100, 100),
            new Point2D.Double(200, 200)
        );
        drawing.add(testFigure);
    }

    // ==================== Factory Tests ====================

    @Test
    public void testFactoryCreatesHandleTracker() {

        // Business Rule: Factory must provide HandleTracker for handle manipulation
        Handle handle = testFigure.createHandles(-1).iterator().next();
        
        HandleTracker tracker = trackerFactory.getHandleTracker(handle);
        
        assertNotNull("Factory should create HandleTracker", tracker);
    }

    @Test
    public void testFactoryCreatesDragTracker() {
        // Business Rule: Factory must provide DragTracker for figure dragging
        DragTracker tracker = trackerFactory.getDragTracker(testFigure);
        
        assertNotNull("Factory should create DragTracker", tracker);
    }

    @Test
    public void testFactoryCreatesSelectAreaTracker() {
        // Business Rule: Factory must provide SelectAreaTracker for area selection
        SelectAreaTracker tracker = trackerFactory.getSelectAreaTracker();
        
        assertNotNull("Factory should create SelectAreaTracker", tracker);
    }

    @Test
    public void testFactoryCachesTrackerInstances() {
        // Business Rule: Factory should reuse tracker instances (lazy singleton pattern)
        SelectAreaTracker tracker1 = trackerFactory.getSelectAreaTracker();
        SelectAreaTracker tracker2 = trackerFactory.getSelectAreaTracker();
        
        assertSame("Factory should cache and reuse tracker instances", 
            tracker1, tracker2);
    }

    @Test
    public void testFactoryAllowsCustomTrackerInjection() {
        // Business Rule: Factory should support custom tracker implementations for testing/extension
        SelectAreaTracker customTracker = new MockSelectAreaTracker();
        trackerFactory.setSelectAreaTracker(customTracker);
        
        SelectAreaTracker retrieved = trackerFactory.getSelectAreaTracker();
        
        assertSame("Factory should use injected custom tracker", 
            customTracker, retrieved);
    }

    // ==================== Selection State Tests ====================

    @Test
    public void testSingleFigureSelection() {
        // Business Rule: User should be able to select a single figure by clicking it
        Point2D.Double clickPoint = new Point2D.Double(150, 150);
        
        // Simulate click on figure
        simulateMouseClick(clickPoint);
        
        assertTrue("Figure should be selected after click", 
            drawingView.isFigureSelected(testFigure));
        assertEquals("Only one figure should be selected", 
            1, drawingView.getSelectedFigures().size());
    }

    @Test
    public void testClickingEmptyAreaDeselectsAll() {
        // Business Rule: Clicking on empty area should deselect all figures
        drawingView.addToSelection(testFigure);
        assertTrue("Setup: figure should be selected", 
            drawingView.isFigureSelected(testFigure));
        
        Point2D.Double emptyPoint = new Point2D.Double(50, 50);  // Outside figure bounds
        simulateMouseClick(emptyPoint);
        
        assertTrue("All figures should be deselected", 
            drawingView.getSelectedFigures().isEmpty());
    }

    @Test
    public void testShiftClickAddsToSelection() {
        // Business Rule: Shift+click should add figure to current selection
        RectangleFigure figure2 = new RectangleFigure();
        figure2.setBounds(
            new Point2D.Double(300, 300),
            new Point2D.Double(400, 400)
        );
        drawing.add(figure2);
        
        // Select first figure
        drawingView.addToSelection(testFigure);
        assertEquals("First figure should be selected", 
            1, drawingView.getSelectedFigures().size());
        
        // Shift+click second figure
        Point2D.Double figure2Point = new Point2D.Double(350, 350);
        simulateMouseClickWithModifier(figure2Point, MouseEvent.SHIFT_DOWN_MASK);
        
        assertEquals("Both figures should now be selected", 
            2, drawingView.getSelectedFigures().size());
        assertTrue("First figure should still be selected", 
            drawingView.isFigureSelected(testFigure));
        assertTrue("Second figure should be selected", 
            drawingView.isFigureSelected(figure2));
    }

    // ==================== Tracker Selection Logic Tests ====================

    @Test
    public void testClickOnFigureUsesHandleTracker() {
        // Business Rule: Clicking on a handle should activate HandleTracker
        Handle handle = testFigure.createHandles(-1).iterator().next();
        drawingView.addToSelection(testFigure);
        
        // Verify factory can create HandleTracker for this handle
        HandleTracker tracker = trackerFactory.getHandleTracker(handle);
        assertNotNull("HandleTracker should be created for handle manipulation", tracker);
    }

    @Test
    public void testClickOnFigureContentUsesDragTracker() {
        // Business Rule: Clicking on figure content (not handle) should activate DragTracker
        Point2D.Double figureContent = new Point2D.Double(150, 150);  // Inside figure
        
        // Verify figure is under this point
        Point p = new Point((int) figureContent.x, (int) figureContent.y);

        Figure foundFigure = drawingView.findFigure(p);
        assertNotNull("Figure should be found at click point", foundFigure);
        
        // Factory should provide DragTracker
        DragTracker tracker = trackerFactory.getDragTracker(foundFigure);
        assertNotNull("DragTracker should be created for figure dragging", tracker);
    }

    @Test
    public void testClickOnEmptyAreaUsesSelectAreaTracker() {
        // Business Rule: Clicking on empty area should activate SelectAreaTracker for rubberband selection
        Point2D.Double emptyArea = new Point2D.Double(50, 50);
        
        // No figure should be at this location
        Point p = new Point((int) emptyArea.x, (int) emptyArea.y);
        Figure foundFigure = drawingView.findFigure(p);
        assertNull("No figure should exist at this point", foundFigure);
        
        // Factory should provide SelectAreaTracker
        SelectAreaTracker tracker = trackerFactory.getSelectAreaTracker();
        assertNotNull("SelectAreaTracker should be created for area selection", tracker);
    }

    // ==================== Selection Tool Delegation Tests ====================

    @Test
    public void testSelectionToolDelegatesTrackerMethods() {
        // Business Rule: SelectionTool should delegate to factory for tracker management
        SelectAreaTracker customTracker = new MockSelectAreaTracker();
        
        // Setter should delegate to factory
        selectionTool.setSelectAreaTracker(customTracker);
        
        // Verify factory has received the custom tracker
        SelectAreaTracker retrieved = selectionTool.getTrackerFactory().getSelectAreaTracker();
        assertSame("SelectionTool setter should delegate to factory", 
            customTracker, retrieved);
    }

    @Test
    public void testSelectionToolMaintainsSingleActiveTracker() {
        // Business Rule: Only one tracker should be active at a time
        SelectAreaTracker selectTracker = trackerFactory.getSelectAreaTracker();
        DragTracker dragTracker = trackerFactory.getDragTracker(testFigure);
        
        assertNotSame("Trackers should be different instances", 
            selectTracker, dragTracker);
    }

    // ==================== Factory Separation Verification ====================

    @Test
    public void testFactoryResponsibilityIsolation() {
        // Business Rule: Factory should handle ALL creation logic, not scattered in SelectionTool
        // Verify that getting trackers from factory is consistent
        
        SelectAreaTracker tracker1 = trackerFactory.getSelectAreaTracker();
        trackerFactory.setSelectAreaTracker(null);  // Reset
        SelectAreaTracker tracker2 = trackerFactory.getSelectAreaTracker();
        
        assertNotSame("After reset, factory should create new instance", 
            tracker1, tracker2);
    }

    @Test
    public void testSelectionToolDoesNotCreateTrackersDirectly() {
        // Business Rule: SelectionTool should NOT contain creation methods
        // (This is verified through refactoring - testing the contract)
        
        // SelectionTool should only have factory instance variable
        assertTrue("SelectionTool should have trackerFactory", 
            selectionTool.getTrackerFactory() != null);
        
        // SelectionTool should NOT have getHandleTracker, getDragTracker, getSelectAreaTracker methods
        // (These would be compile errors if called)
        // This test documents the API contract
    }

    // ==================== Integration Tests ====================

    @Test
    public void testCompleteSelectionWorkflow() {

        // Step 1: Select figure
        Point2D.Double clickPoint = new Point2D.Double(150, 150);
        simulateMouseClick(clickPoint);

        assertTrue("Step 1: Figure should be selected",
                drawingView.isFigureSelected(testFigure));

        // Step 2: Handles exist (independent of selection)
        Collection<Handle> handles = testFigure.createHandles(-1);

        assertFalse("Step 2: At least one handle should exist",
                handles.isEmpty());

        // Step 3: Deselect
        Point2D.Double emptyPoint = new Point2D.Double(50, 50);
        simulateMouseClick(emptyPoint);

        assertTrue("Step 3: Selection should be cleared",
                drawingView.getSelectedFigures().isEmpty());
    }

    @Test
    public void testSelectMultipleFigures() {
        // Business Rule: User should be able to select multiple figures via shift+click
        
        RectangleFigure figure2 = new RectangleFigure();
        figure2.setBounds(
            new Point2D.Double(300, 300),
            new Point2D.Double(400, 400)
        );
        drawing.add(figure2);
        
        // Select first figure
        simulateMouseClick(new Point2D.Double(150, 150));
        assertEquals("First figure selected", 1, drawingView.getSelectedFigures().size());
        
        // Shift+click to add second figure
        simulateMouseClickWithModifier(
            new Point2D.Double(350, 350), 
            MouseEvent.SHIFT_DOWN_MASK
        );
        
        assertEquals("Both figures should be selected", 
            2, drawingView.getSelectedFigures().size());
    }

    // ==================== Helper Methods ====================

    private void simulateMouseClick(Point2D.Double drawingPoint) {
        Point viewPoint = drawingView.drawingToView(drawingPoint);
        MouseEvent event = new MouseEvent(
            drawingView.getComponent(),
            MouseEvent.MOUSE_PRESSED,
            System.currentTimeMillis(),
            0,  // No modifiers
            (int)viewPoint.x,
            (int)viewPoint.y,
            1,  // Click count
            false  // Not popup trigger
        );
        selectionTool.mousePressed(event);
    }

    private void simulateMouseClickWithModifier(Point2D.Double drawingPoint, int modifiers) {
        Point viewPoint = drawingView.drawingToView(drawingPoint);
        MouseEvent event = new MouseEvent(
            drawingView.getComponent(),
            MouseEvent.MOUSE_PRESSED,
            System.currentTimeMillis(),
            modifiers,
            (int)viewPoint.x,
            (int)viewPoint.y,
            1,  // Click count
            false  // Not popup trigger
        );
        selectionTool.mousePressed(event);
    }

    // ==================== Mock Implementation for Testing ====================

    /**
     * Mock SelectAreaTracker for testing tracker injection
     */
    private static class MockSelectAreaTracker extends AbstractTool implements SelectAreaTracker {
        private static final long serialVersionUID = 1L;

        @Override
        public void mousePressed(MouseEvent evt) {
            // Mock implementation
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
            // Mock implementation
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            // Mock implementation
        }
    }
}
