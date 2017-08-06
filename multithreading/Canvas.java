import java.util.ArrayList;
import java.util.Arrays;

/**
 * Canvas field.
 * Realize interface for working with abstract two-dimensional matrix.
 */
public class Canvas {
    /**
     * Data.
     * Two dimensional array
     * represented as array with width*height size
     */
    int[] data;

    /**
     * Amount of rows in matrix.
     */
    int rows;

    /**
     * Calculates real index in data array and returns it.
     *
     * @param row row index in data array
     * @param col column index in data array
     * @return index in data array with specified row and column
     */
    private int idx(int row, int col) {
        return row * getWidth() + col;
    }


    /**
     * Empty constructor
     * Creates zero-size two dimensional matrix.
     */
    public Canvas() {
        rows = 0;
    }

    /**
     * Normal constructor
     * Constructs two dimensional matrix with specified width and height.
     *
     * @param width  amount columns in canvas matrix
     * @param height amount rows in canvas matrix
     */
    public Canvas(int width, int height) {
        this();

        // Generate matrix.
        data = new int[width * height];
        rows = height;
    }


    /**
     * Matrix width getter
     *
     * @return count of columns in canvas matrix
     */
    public int getWidth() {
        return (rows > 0 ? data.length / rows : 0);
    }

    /**
     * Matrix height getter
     *
     * @return count of rows in canvas matrix
     */
    public int getHeight() {
        return rows;
    }

    /**
     * Matrix width setter
     * Alias to resize method.
     *
     * @param width new count of columns
     */
    public void setWidth(int width) {
        resize(width, getHeight());
    }

    /**
     * Matrix height setter
     *
     * @param height new count of rows
     */
    public void setHeight(int height) {
        resize(getWidth(), height);
    }

    /**
     * Matrix resizer.
     * Resize matrix with new width and height.
     *
     * @param width  new count of columns
     * @param height new count of rows
     */
    public void resize(int width, int height) {
        // Saving current sizes.
        int thisWidth = getWidth();
        int thisHeight = getHeight();

        // Calc minimal sizes for data moving.
        int minWidth = Math.min(width, thisWidth);
        int minHeight = Math.min(height, thisHeight);

        // Allocating new data.
        int[] newData = new int[width * height];

        // Moving data
        for (int i = 0; i < minHeight; ++i)
            for (int j = 0; j < minWidth; ++j)
                newData[i * width + j] = get(i, j);

        // Refreshing data
        data = newData;
        rows = height;
    }


    /**
     * Returns value of specified cell in matrix.
     *
     * @param row index of row in matrix
     * @param col index of column in matrix
     * @return value of matrix cell
     */
    public int get(int row, int col) {
        return data[idx(row, col)];
    }

    /**
     * Sets new value to specified cell in matrix.
     *
     * @param row   index of row in matrix
     * @param col   index of column in matrix
     * @param value value to set
     */
    public void set(int row, int col, int value) {
        data[idx(row, col)] = value;
    }


    /* Comparison methods overriding */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Canvas canvas = (Canvas) o;

        return Arrays.equals(data, canvas.data);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }


    /* Output */

    @Override
    public String toString() {
        String str = "";
        for (int i = 0; i < getHeight(); ++i)
            for (int j = 0; j < getWidth(); ++j)
                str += (j == 0 ? " " : "") + get(i, j) + (j == getWidth() - 1 ? "\n" : " ");
        return str;
    }
}
