import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileSystem {
    private final File fsFile;
    private final RandomAccessFile fsDisk;

    private FileSystem(String filename, boolean openExistent) throws IOException {
        // create filesystem file
        fsFile = new File(filename);

        // check exceptions
        if (fsFile.isDirectory()) throw new IOException("Filesystem drive cannot be a directory");
        if (fsFile.exists()) {
            if (!openExistent) throw new IOException("Cannot create new drive in this scope. Try to open existent");
        } else {
            if (openExistent) throw new IOException("Drive does not exists. Please create drive before");
            if (fsFile.createNewFile()) throw new IOException("Cannot create drive");
        }

        fsDisk = new RandomAccessFile(fsFile, "rw");
    }

    public static FileSystem create(String filename) throws IOException {
        return new FileSystem(filename, false);
    }

    public static FileSystem open(String filename) throws IOException {
        return new FileSystem(filename, true);
    }

    public void close() throws IOException {
        fsDisk.close();
    }

    public boolean destroy() {
        try {
            fsDisk.close();
        } catch (IOException ignored) {}
        return fsFile.delete();
    }


    public int read(byte[] mem, int len) throws IOException {
        return fsDisk.read(mem, 0, len);
    }

    public void write(byte[] mem, int len) throws IOException {
        fsDisk.write(mem, 0, len);
    }

    public void seek(long pos) throws IOException {
        fsDisk.seek(pos);
    }

    public String[] directory() {
        return null;
    }
}
