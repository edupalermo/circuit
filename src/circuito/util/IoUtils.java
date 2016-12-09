package circuito.util;

import java.io.*;

public class IoUtils {


    public static <T> T readObject(File file, Class<T> clazz) {
        T answer = null;

        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            answer = clazz.cast(ois.readObject());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            closeQuitely(ois);
        }
        return answer;
    }

    public static <T> T writeObject(File file, Object object) {
        T answer = null;

        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            closeQuitely(oos);
        }
        return answer;
    }


    public static void closeQuitely(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeQuitely(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
