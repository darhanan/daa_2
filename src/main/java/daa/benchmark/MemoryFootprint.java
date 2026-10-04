package daa.benchmark;

import daa.structures.DynamicArray;
import daa.structures.MinHeap;
import daa.structures.MyLinkedList;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.vm.VM;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Bonus task A: measures with JOL (Java Object Layout) how many bytes n elements really take.
 * {@link GraphLayout} walks the whole object graph reachable from the structure, so the
 * numbers include the backing array, every node object, headers and alignment padding.
 *
 * <p>Usage: {@code java daa.benchmark.MemoryFootprint [outputDir]} (default {@code results}).
 */
public final class MemoryFootprint {
    static final int[] SIZES = {100, 1_000, 10_000, 100_000, 1_000_000};

    private MemoryFootprint() {
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Path outDir = Path.of(args.length > 0 ? args[0] : "results");
        Files.createDirectories(outDir);

        System.out.println(VM.current().details());
        System.out.println(ClassLayout.parseClass(Class.forName("daa.structures.MyLinkedList$Node")).toPrintable());
        System.out.println(ClassLayout.parseInstance(new int[4]).toPrintable());

        StringBuilder csv = new StringBuilder("structure,n,bytes,mb,bytes_per_element\n");
        for (int n : SIZES) {
            Dataset d = Dataset.of(n);
            DynamicArray array = new DynamicArray();
            MyLinkedList list = new MyLinkedList();
            MinHeap heap = new MinHeap();
            for (int v : d.values) {
                array.add(v);
                list.add(v);
                heap.insert(v);
            }
            DynamicArray exactArray = new DynamicArray(n);
            for (int v : d.values) {
                exactArray.add(v);
            }
            append(csv, "DynamicArray", n, GraphLayout.parseInstance(array).totalSize());
            append(csv, "DynamicArray(exact capacity)", n, GraphLayout.parseInstance(exactArray).totalSize());
            append(csv, "MyLinkedList", n, GraphLayout.parseInstance(list).totalSize());
            append(csv, "MinHeap", n, GraphLayout.parseInstance(heap).totalSize());
        }
        Files.writeString(outDir.resolve("memory.csv"), csv);
        System.out.print(csv);
        System.out.println("Wrote " + outDir.resolve("memory.csv"));
    }

    private static void append(StringBuilder csv, String structure, int n, long bytes) {
        csv.append(String.format(Locale.ROOT, "%s,%d,%d,%.4f,%.2f%n",
                structure, n, bytes, bytes / (1024.0 * 1024.0), (double) bytes / n));
    }
}
