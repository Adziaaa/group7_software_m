package org.jhotdraw.xml;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class JavaxDOMInputTest {
    private JavaxDOMInput domInput;
    private DOMFactory mockFactory;
    private Document mockDocument;
    private Element mockElement;
    private NodeList mockNodeList;

    @Before
    public void setUp() throws Exception {
        mockFactory = Mockito.mock(DOMFactory.class);
        mockDocument = Mockito.mock(Document.class);
        mockElement = Mockito.mock(Element.class);
        mockNodeList = Mockito.mock(NodeList.class);
        InputStream emptyXml = new ByteArrayInputStream("<root/>".getBytes(StandardCharsets.UTF_8));
        domInput = Mockito.spy(new JavaxDOMInput(mockFactory, emptyXml));
        java.lang.reflect.Field currentField = JavaxDOMInput.class.getDeclaredField("current");
        currentField.setAccessible(true);
        currentField.set(domInput, mockElement);
    }

    @Test
    public void testGetElementCount_Empty() {
        when(mockElement.getChildNodes()).thenReturn(mockNodeList);
        when(mockNodeList.getLength()).thenReturn(0);
        assertEquals(0, domInput.getElementCount());
    }

    @Test
    public void testGetElementCount_WithElements() {
        when(mockElement.getChildNodes()).thenReturn(mockNodeList);
        when(mockNodeList.getLength()).thenReturn(2);
        Node child1 = Mockito.mock(Element.class);
        Node child2 = Mockito.mock(Element.class);
        when(mockNodeList.item(0)).thenReturn(child1);
        when(mockNodeList.item(1)).thenReturn(child2);
        assertEquals(2, domInput.getElementCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOpenElement_IndexOutOfBounds() {
        when(mockElement.getChildNodes()).thenReturn(mockNodeList);
        when(mockNodeList.getLength()).thenReturn(0);
        domInput.openElement(0);
    }

    @Test
    public void testOpenElement_ValidIndex() {
        when(mockElement.getChildNodes()).thenReturn(mockNodeList);
        when(mockNodeList.getLength()).thenReturn(1);
        Element child = Mockito.mock(Element.class);
        when(mockNodeList.item(0)).thenReturn(child);
        domInput.openElement(0);
        // Use reflection to check that current is set to child
        try {
            java.lang.reflect.Field currentField = JavaxDOMInput.class.getDeclaredField("current");
            currentField.setAccessible(true);
            assertSame(child, currentField.get(domInput));
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }
}
