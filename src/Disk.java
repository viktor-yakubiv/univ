import java.io.*;
import java.util.Arrays;

/**
 * Implementation of disk
 */
public class Disk implements Serializable {
    public final int cylindersCount;
    public final int surfacesCount;
    public final int sectionsCount;
    public final int sectionLength;


    private byte data[][][][];


    public Disk(int cylindersCount, int surfacesCount, int sectionsCount, int sectionLength) {
        this.cylindersCount = cylindersCount;
        this.surfacesCount = surfacesCount;
        this.sectionsCount = sectionsCount;
        this.sectionLength = sectionLength;

        // this.length = cylindersCount * surfacesCount * sectionsCount;

        this.data = new byte[cylindersCount][surfacesCount][sectionsCount][sectionLength];
    }


    public int getSize() {
        return (cylindersCount * surfacesCount * sectionsCount);
    }

    public int getLength() {
        return (getSize() * sectionLength);
    }


    private int[] transformIndex(int index) {
        int[] indexes = new int[3];

        indexes[0] = index / (cylindersCount * surfacesCount * sectionsCount);
        index %= (surfacesCount * sectionsCount);

        indexes[1] = index / (surfacesCount * sectionsCount);
        index %= sectionsCount;

        indexes[2] = index;

        return indexes;
    }


    public void read(int i, byte[] b) {
        int indexes[] = transformIndex(i);
        System.arraycopy(data[indexes[0]][indexes[1]][indexes[2]], 0, b, 0, sectionLength);
    }

    public void write(int i, byte[] b) {
        int indexes[] = transformIndex(i);
        System.arraycopy(b, 0, data[indexes[0]][indexes[1]][indexes[2]], 0, sectionLength);
    }


    @Override
    public String toString() {
        return "Disk{" +
                "cylindersCount=" + cylindersCount +
                ", surfacesCount=" + surfacesCount +
                ", sectionsCount=" + sectionsCount +
                ", sectionLength=" + sectionLength +
                ", data=" + Arrays.toString(data) +
                '}';
    }


    public static Disk load(String filename) throws IOException, ClassNotFoundException {
        ObjectInputStream objInput = new ObjectInputStream(new FileInputStream(filename));
        return (Disk) objInput.readObject();
    }

    public void dump(String filename) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.close();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Disk disk = new Disk(2, 4, 8, 64);
        byte[] buf = new byte[disk.sectionLength];
        buf[3] = 126;
        buf[42] = 16;
        disk.write(2, buf);
        disk.dump("out/disk.bin");
        disk = Disk.load("out/disk.bin");
        System.out.println(disk.data[0][2][1][63]);
    }
}
