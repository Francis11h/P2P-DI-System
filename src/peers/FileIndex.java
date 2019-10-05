package peers;

import java.io.Serializable;

public class FileIndex implements Serializable {

    String fileName;
    String fileType;
    String title;
    String hostname;
    int port;
    int ttl;

    public FileIndex(String fileName, String fileType, String title, String hostname, int port, int ttl) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.title = title;
        this.hostname = hostname;
        this.port = port;
        this.ttl = ttl;
    }

    @Override
    public boolean equals(Object o) {

        FileIndex tmp = (FileIndex)o;

        String s1 = fileName + fileType;
        String s2 = tmp.fileName + tmp.fileType;

        System.out.println(s1.equals(s2));
        return s1.equals(s2);

    }

    @Override
    public int hashCode() {
        return fileName.hashCode() & fileType.hashCode();
    }
}
