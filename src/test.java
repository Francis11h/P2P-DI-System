import peers.FileIndex;
import java.util.HashSet;

public class test {

    public static void main(String[] args) {

        HashSet<FileIndex> set = new HashSet<>();

        FileIndex f1 = new FileIndex("rfc8445.txt", "pdf", "????", "1", 1, 1);
        FileIndex f2 = new FileIndex("rfc8445.txt", "pdf", "abcd", "2", 2, 2);

        set.add(f1);
        set.add(f2);

        System.out.println(set.size());


    }

}
