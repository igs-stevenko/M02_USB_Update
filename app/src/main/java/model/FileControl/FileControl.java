package model.FileControl;

import android.os.Environment;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class FileControl {

    public static int RemoveFolder(String folderPath) {

        int rtn = 0;
        File folder = new File(folderPath);
        rtn = deleteFolder(folder);
        return rtn;
    }

    public static void RemoveFile(String FilePath) {

        File file = new File(FilePath);
        if (file.exists() == true) {
            file.delete();
        }
    }


    private static int deleteFolder(File folder) {

        boolean deletionStatus = true;
        int rtn = 0;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 递归删除子文件夹的内容
                    rtn = deleteFolder(file);
                    if(rtn < 0){
                        Log.d(TAGS, "deleteFolder failed");
                        return rtn;
                    }
                }
                // 删除文件或空文件夹
                deletionStatus = file.delete();
                if(deletionStatus != true){
                    Log.d(TAGS, "file.delete failed");
                    return -1;
                }
            }
            return 0;
        }
        else{
            Log.d(TAGS, "file is null");
            return -1;
        }
    }

    public static int CopyAllDir(String source, String dest) throws IOException {

        File sourceFile = new File(source);
        File destFile = new File(dest);

        return CopyDir(sourceFile, destFile);
    }

    private static int CopyDir(File soruce, File dest) {

        int rtn = 0;

        /* 檢查來源是否為資料夾 */
        if (!soruce.isDirectory()) {
            return -1;
        }
        /* 檢查目標資料夾是否存在，若不存在則先創建 */
        if (!dest.exists()) {
            dest.mkdir();
            dest.setReadable(true, false);
            dest.setWritable(true, false);
            dest.setExecutable(true, false);
        }

        File[] files = soruce.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File newDestinationDirectory = new File(dest, file.getName());
                    rtn = CopyDir(file, newDestinationDirectory);
                    if(rtn != 0){
                        rtn = -3;
                        break;
                    }
                } else {
                    Path sourcePath = file.toPath();
                    Path destinationPath = new File(dest, file.getName()).toPath();
                    rtn = CopyFile(sourcePath.toString(), destinationPath.toString());
                    if(rtn != 0) {
                        rtn = -2;
                        break;
                    }
                    //Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                    File targetfile = new File(destinationPath.toString());
                    targetfile.setReadable(true, false);
                    targetfile.setWritable(true, false);
                    targetfile.setExecutable(true, false);
                }
            }
        }

        return rtn;
    }

    public static int CopyFile(String source, String dest) {

        int rtn = 0;
        File sourceFile = new File(source);
        File destFile = new File(dest);

        Sleep(10);

        Log.d(TAGS, "File Name = " + source);

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(sourceFile);
            fos = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {

                fos.write(buffer, 0, bytesRead);
            }
            fos.flush();
            fos.getFD().sync();
            fis.close();
            fos.close();

        } catch (IOException e) {
            Log.d(TAGS, "CopyFile ERROR : " + source);
            rtn = -1;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                Log.d(TAGS, "IOException closing file stream failed");
                rtn = -2;
            }
        }

        return rtn;
    }

    public static String ReadStringFromFile(String filePath) {

        File file = new File(filePath);
        if (file.exists() == false)
            return "";

        StringBuilder content = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return content.toString().replace("\n", "");
    }


    public static int WriteStringToFile(String buffer, String filePath) {

        int rtn = 0;

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), filePath);
        file.setReadable(true);
        file.setWritable(true);

        try {
            FileWriter filewrite = new FileWriter(filePath);
            BufferedWriter writer = new BufferedWriter(filewrite);
            writer.write(buffer);
            writer.flush();

        } catch (IOException e) {
           rtn = -1;
        }

        return rtn;
    }

    public static String CalculateMD5(String filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(filePath);
            DigestInputStream dis = new DigestInputStream(fis, md);

            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) {
                // 读取文件并更新 MD5 散列
            }

            byte[] digest = md.digest();

            // 转换字节数组为十六进制字符串
            StringBuilder md5StringBuilder = new StringBuilder();
            for (byte b : digest) {
                md5StringBuilder.append(String.format("%02x", b));
            }

            fis.close();

            return md5StringBuilder.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int Unzip(String zipFilePath, String destDirectory) throws IOException {

        int rtn = 0;

        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipInputStream.getNextEntry();

            while (entry != null) {
                String entryName = entry.getName();
                File entryFile = new File(destDirectory + File.separator + entryName);

                /*新增*/
                // 去掉第一層資料夾 (Resource/)
                int firstSlash = entryName.indexOf('/');
                if (firstSlash != -1) {
                    entryName = entryName.substring(firstSlash + 1);
                }

                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    File parent = entryFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }

                    FileOutputStream fos = new FileOutputStream(entryFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    /*
                    fos.flush();
                    fos.getFD().sync();
                    */
                }
                zipInputStream.closeEntry();
                entry = zipInputStream.getNextEntry();
            }
        } catch (IOException e) {
            Log.d(TAGS, "Unzip IOException");
            rtn = -1;
        }

        return rtn;
    }
    public static int UnzipWithoutFirstName(String zipFilePath, String destDirectory) throws IOException {

        int rtn = 0;

        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipInputStream.getNextEntry();

            while (entry != null) {

                String entryName = entry.getName();

                /*新增*/
                // 去掉第一層資料夾 (Resource/)
                int firstSlash = entryName.indexOf('/');
                if (firstSlash != -1) {
                    entryName = entryName.substring(firstSlash + 1);
                }

                File entryFile = new File(destDirectory + File.separator + entryName);

                Log.d(TAGS, "entryFile = " + entryFile.toString());

                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    File parent = entryFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }

                    FileOutputStream fos = new FileOutputStream(entryFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    /*
                    fos.flush();
                    fos.getFD().sync();
                    */
                }
                zipInputStream.closeEntry();
                entry = zipInputStream.getNextEntry();
            }
        } catch (IOException e) {
            Log.d(TAGS, "Unzip IOException");
            rtn = -1;
        }

        return rtn;
    }
    public static void CreateDir(String DirName) {
        File folder = new File(DirName);
        if (!folder.exists()) { // 检查文件夹是否已经存在
            boolean success = folder.mkdir(); // 尝试创建文件夹
        }
    }

    public static long GetFileSize(String FileName) {

        File file = new File(FileName);

        return file.length();

    }

    public static boolean IsFileExist(String FilePath) {

        File file = new File(FilePath);

        return file.exists();
    }

    private static void Sleep(int mTime) {
        try {
            Thread.sleep(mTime);
        }catch(Exception e) {

        }
    }

    static String TAGS = "## [KO] FileControl";

}
