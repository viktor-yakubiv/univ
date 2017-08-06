import org.junit.Test;

import static org.junit.Assert.*;

public class CanvasTest {

    private Canvas newCanvas(int width, int height) {
        return new Canvas(width, height);
    }

    @Test
    public void testGetWidth() throws Exception {
        Canvas c = newCanvas(10, 10);
        assertEquals(10, c.getWidth());
    }

    @Test
    public void testGetHeight() throws Exception {

    }

    @Test
    public void testResize() throws Exception {
        Canvas c = new Canvas();
        c.resize(10, 10);

        assertEquals(10, c.getWidth());
    }

    @Test
    public void testSetWidth() throws Exception {

    }

    @Test
    public void testSetHeight() throws Exception {

    }

    @Test
    public void testGet() throws Exception {

    }

    @Test
    public void testSet() throws Exception {

    }
}