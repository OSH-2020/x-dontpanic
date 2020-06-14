package fileDetector;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 文件工具类
 */
public class FileUtil {

    /**
     * 清空文件夹
     *
     * @param folderPath 文件夹路径
     */
    public static void clearFolder(String folderPath) {
        clearFolder(new File(folderPath));
    }

    /**
     * 清空文件夹
     *
     * @param folder 文件夹
     */
    public static void clearFolder(File folder) {
        File[] files = folder.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                file.delete();
            } else {
                clearFolder(file);
                file.delete();
            }
        }
    }

    /**
     * 广度优先遍历文件夹及其子文件夹，获得该文件夹下所有的文件
     *
     * @param folder 顶层文件夹
     * @return 所有的文件
     */
    public static List<File> getAllFiles(File folder) {
        List<File> files = new ArrayList<>();

        LinkedList<File> queue = new LinkedList<>();
        queue.add(folder);

        while (!queue.isEmpty()) {
            File dir = queue.remove();
            File[] fs = dir.listFiles();
            for (File f : fs) {
                if (f.isDirectory()) {
                    queue.add(f);
                } else {
                    files.add(f);
                }
            }
        }

        return files;
    }
}
