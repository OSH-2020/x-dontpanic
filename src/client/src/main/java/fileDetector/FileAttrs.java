package fileDetector;

/**
 * 文件属性
 */
public class FileAttrs {

    public String name; // 文件名
    public String path; // 文件路径
    public String attr;
    public int noa;

    public FileAttrs(String name, String path,
                     String attr, int noa) {
        this.name = name;
        this.path = path;
        this.attr = attr;
        this.noa = noa;
    }

}
