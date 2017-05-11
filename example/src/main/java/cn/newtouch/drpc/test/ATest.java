package cn.newtouch.drpc.test;

/**
 * Created by Administrator on 2017/5/10.
 */
public class ATest {

    public static void main(String[] args) throws InterruptedException {
        long time = System.currentTimeMillis();
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int x = 0;
                    while (true) {
                        x += 1;
                        String[][] ints = new String[100][100];
                        for (int j = 0; j < 100; j++) {
                            ints[j] = new String[100];
                            for (int k = 0; k < 100; k++) {
                                ints[j][k] = "j = " + j + "\nk = " + k+ "\nj * k = " + j * k;
                            }
                        }
                        if (x == 5000000) {
                            break;
                        }
                    }
                }
            });
            thread.start();
            threads[i] = thread;
        }
        for (int i = 0; i < 10; i++) {
            threads[i].join();
        }
        System.out.println(System.currentTimeMillis() - time);
    }
}
