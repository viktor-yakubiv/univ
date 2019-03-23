import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

public class FileSystem {

    /*
    public final static int sizeLength = 2;
    public final static int bitmapLength

    private final Disk disk;

    private final int bitmapLength;
    private final int diskMetaLength;

    private BitSet bitmap;
    private ArrayList<File> oft;

    public FileSystem(Disk disk) {
        this.disk = disk;

        // Read bitmap
        bitmapLength = disk.getLength() / disk.sectionLength / 8;
        byte[] bitmapData = new byte[disk.getLength() / 8];
        for (int i = 0; i <  bitmapLength; ++i) {
            byte[] section = new byte[disk.sectionLength];
            disk.read(i, section);
            System.arraycopy(section, 0, bitmapData, i * disk.sectionLength, disk.sectionLength);
        }
        bitmap = BitSet.valueOf(bitmapData);

        // Set meta data block length
        diskMetaLength = bitmapLength + (disk.getLength() / 25 * 4);

        // Read first file
        open("");
    }

    private void read(int start, int count, byte[] b) {
        for (int i = 0; i < count; i++) {
            byte[] section = new byte[disk.sectionLength];
            disk.read(start + i, section);
            System.arraycopy(section, 0, b, i * disk.sectionLength, disk.sectionLength);
        }
    }

    public int open(String filename) {
        int fdIndex = 0;
        if (filename != "") {
            for (int )
        }
    }

    private Descriptor readDescriptor(int index) {
        byte[] data = new byte[4 * disk.sectionLength];
        read(bitmapLength + index * 4, 4, data);

        Arrays.cop
        long length = Integer.v
    }
/*    // max
    public final static int fdMaxCount = 5;

    private class FileInfo {
        int length;
        int first;

        public FileInfo(int length, int first) {
            this.length = length;
            this.first = first;
        }
    }

    private class FilePart {
        int index;
        int length;
        int next;

        public FilePart(int index, int length, int next) {
            this.index = index;
            this.length = length;
            this.next = next;
        }
    }

    BitSet bitmap;
    byte[] storage;

    private HashMap<Integer, FileInfo> openedFiles = new HashMap<>();

    // File to represent FileSystem as file on disk
    private final File file;
    
    // Accessor to real FileSystem file
    private final RandomAccessFile disk;
    
    
    // Current position on disk
    private long pos = 0;


    public FileSystem(String filename, boolean openExistent) throws IOException {
        // create filesystem file
        file = new File(filename);

        // check exceptions
        if (file.isDirectory()) throw new IOException("Filesystem drive cannot be a directory");
        if (file.exists()) {
            if (!openExistent) throw new IOException("Cannot create new drive in this scope. Try to open existent");
        } else {
            if (openExistent) throw new IOException("Drive does not exists. Please create drive before");
            if (file.createNewFile()) throw new IOException("Cannot create drive");
        }

        disk = new RandomAccessFile(file, "rw");
    }

    public FileSystem create(String filename) throws IOException {
        return new FileSystem(filename, false);
    }

    public FileSystem open(String filename) throws IOException {
        return new FileSystem(filename, true);
    }

    public void close() throws IOException {
        disk.close();
    }

    public boolean destroy() {
        try {
            disk.close();
        } catch (IOException ignored) {}
        return file.delete();
    }


    public int read(int index, byte[] mem, int len) throws IOException {
        return disk.read(mem, 0, len);
    }

    public void write(int index, byte[] mem, int len) throws IOException {
        disk.write(mem, 0, len);
    }

    public void seek(long pos) throws IOException {
        disk.seek(pos);
    }

    public String[] list() {
        return null;
    }*/


    private Disk disk;
    private BitSet bitmap;
    private int bitmapSize;

    public FileSystem(Disk disk) {
        this.disk = disk;
        bitmapSize = disk.getSize() / disk.sectionLength / 8 + 1;
    }


    public static void main(String[] args) {
        Disk disk = new Disk(4, 2, 8, 64);
        FileSystem fs = new FileSystem(disk);
        fs.test();
    }

    private void test() {
        Descriptor d = new Descriptor(0);
        d.length = 5;
        d.pointers.add((short) 18);
        d.write();
        try {
            disk.dump("out/disk.obj");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            disk = Disk.load("out/disk.obj");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        d = new Descriptor(0, true);
        System.out.println(d.length);
        System.out.println(d.pointers.size() + " " + d.pointers.get(0));
    }

    /**
     * Reads block sequence from disk
     *
     * @param start index of first block to read
     * @param count amount of blocks to read
     * @return sequence of memory read from disk
     */
    private byte[] readBlockSq(int start, int count) {
        byte[] result = new byte[count * disk.sectionLength];

        byte[] buffer = new byte[disk.sectionLength];
        for (int i = 0; i < count; ++i) {
            disk.read(start + i, buffer);
            System.arraycopy(buffer, 0, result, i * disk.sectionLength, disk.sectionLength);
        }

        return result;
    }

    /**
     * Writes block sequence to disk.
     * Writes as many as possible to write from data array size
     *
     * @param start index of first block to write
     * @param data memory area to write
     */
    private void writeBlockSq(int start, byte[] data) {
        byte[] buffer = new byte[disk.sectionLength];
        int count = data.length / disk.sectionLength + ((data.length % disk.sectionLength > 0) ? 1 : 0);
        for (int i = 0; i < count; ++i) {
            System.arraycopy(data, i * disk.sectionLength, buffer, 0,
                    Math.min(disk.sectionLength, data.length - i * disk.sectionLength));
            disk.write(start + i, buffer);
        }
    }


    private class Descriptor {
        final static int size = 2;

        // Descriptor unique index
        final private int id;

        // Data
        int length;
        ArrayList<Short> pointers = new ArrayList<>();


        Descriptor(int id) {
            this(id, false);
        }

        Descriptor(int id, boolean read) {
            this.id = id;
            if (read) read();
        }


        private int getPointersCount() {
            return (length / disk.sectionLength + ((length % disk.sectionLength > 0) ? 1 : 0));
        }


        /**
         * Reads descriptor from disk
         */
        void read() {
            byte[] buf = readBlockSq(bitmapSize + id, Descriptor.size);

            ByteArrayInputStream is = new ByteArrayInputStream(buf);
            DataInputStream reader = new DataInputStream(is);

            // Read file size
            try {
                this.length = reader.readInt();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Read pointers
            for (int i = 0; i < getPointersCount(); ++i) {
                try {
                    pointers.add(reader.readShort());
                } catch (IOException e) {
                    break;
                }
            }
        }

        /**
         * Writes descriptor to disk
         */
        void write() {
            ByteArrayOutputStream os = new ByteArrayOutputStream(Descriptor.size * disk.sectionLength);
            DataOutputStream writer = new DataOutputStream(os);

            // Write length to buffer
            try {
                writer.writeInt(length);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Write pointers to buffer
            pointers.forEach((p) -> {
                try {
                    writer.writeShort(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // Flush buffer
            writeBlockSq(bitmapSize + id, os.toByteArray());
        }
    }


    class DirectoryNode {
        public final static int size = 2;

        int id;
        String name;

        DirectoryNode(int id, String name) {
            this.id = id;
            this.name = name;
        }

        /*static DirectoryNode valueOf(byte[] b) {
            return null;
        }*/
    }


    class Directory extends File implements Iterable<DirectoryNode> {
        private final FileSystem fs;

        Directory(FileSystem fs) {
            this.fs = fs;
        }

        Descriptor lookUp(String filename) {
            // look up directory and return file descriptor of

            return null;
        }

        List<DirectoryNode> list() {
            return null;
        }

        @Override
        public Iterator<DirectoryNode> iterator() {
            return null;
        }
    }

    class File {
        // Length of buffer in blocks
        private final static int bufferLength = 4;

        // Size of each block in bytes
        // TODO: Delete and add correct constant
        private final static int blockSize = 64;


        Descriptor descriptor;

        // File buffer with its size
        private byte[] buffer = new byte[bufferLength * blockSize];
        private int bufferSize = 0;

        // Current position in linear file representation
        private int bufferPosition = 0;

        // Current shift in buffer
        private int bufferShift = 0;

        // Modify flag
        private boolean modified = false;


        /**
         * Reads one block of file on disk space
         */
        private void readBuffer() {
            flushBuffer();
            bufferShift = 0;
            bufferPosition = (int) Math.min(bufferPosition + buffer.length, descriptor.length);

            // TODO: read next blocks
            while (true) {

            }
        }

        private void flushBuffer() {
            if (!modified) return;

            for (int i = 0; i < bufferLength; ++i) {
                // TODO: write
            }
            bufferPosition += bufferShift;
            bufferShift = 0;

            modified = false;
        }

        /**
         * Reads file and return bytes of data.
         *
         * @param len count of bytes to read
         * @return result of reading
         */
        public byte[] read(int len) {
            int readLen = (int) Math.min(len, descriptor.length - bufferPosition - bufferShift);
            if (readLen == 0) return null;

            byte[] data = new byte[readLen];

            int dataPos = 0;
            while (dataPos < data.length) {
                if (bufferShift >= buffer.length) readBuffer();

                readLen = Math.min(data.length - dataPos, buffer.length - bufferShift);
                System.arraycopy(buffer, bufferShift, data, dataPos, readLen);
                dataPos += readLen;
                bufferShift += readLen;
            }

            return data;
        }

        /**
         * Writes specified data to file
         *
         * @param data bytes to write into file
         */
        public void write(byte[] data) {
            int dataPos = 0;
            while (dataPos < data.length) {
                if (bufferShift == buffer.length) flushBuffer();

                int writeLen = Math.min(data.length - dataPos, buffer.length - bufferShift);
                System.arraycopy(data, dataPos, buffer, bufferShift, writeLen);
                dataPos += writeLen;
                bufferShift += writeLen;
                modified = true;
            }
        }
    }
}