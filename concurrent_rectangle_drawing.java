import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.*;

public class concurrent_rectangle_drawing {

    // Parameters
    public static int n = 1;
    public static int width;
    public static int height;
    public static volatile int k;

    static BufferedImage outputimage;
    static LinkedList<Rectangle> rectangles = new LinkedList<Rectangle>();

    static int BLACK = (0xff << 24);

    // Lock
    static final ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        try {

            // example of reading/parsing an argument
            if (args.length > 3) {
                width = Integer.parseInt(args[0]);
                height = Integer.parseInt(args[1]);
                n = Integer.parseInt(args[2]);
                k = Integer.parseInt(args[3]);
            }

            // once we know what size we want we can create an empty image
            outputimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            // ------------------------------------
            // Your code would go here
            Thread[] threads = new Thread[n];
            
            long time_start = System.currentTimeMillis();
            
            for (int id = 0; id < n; id++) {
                threads[id] = new Thread(new Pencil());
                threads[id].start();
            }
            for (int id = 0; id < n; id++) {
                threads[id].join();
            }

            long time_end = System.currentTimeMillis();

            long time = time_end - time_start;

            System.out.println("The program took: " + time + " ms");
            // The easiest mechanisms for getting and setting pixels are the
            // BufferedImage.setRGB(x,y,value) and getRGB(x,y) functions.
            // Note that setRGB is synchronized (on the BufferedImage object).
            // Consult the javadocs for other methods.

            // The getRGB/setRGB functions return/expect the pixel value in ARGB format, one
            // byte per channel. For example,
            // int p = img.getRGB(x,y);
            // With the 32-bit pixel value you can extract individual colour channels by
            // shifting and masking:
            // int red = ((p>>16)&0xff);
            // int green = ((p>>8)&0xff);
            // int blue = (p&0xff);
            // If you want the alpha channel value it's stored in the uppermost 8 bits of
            // the 32-bit pixel value
            // int alpha = ((p>>24)&0xff);
            // Note that an alpha of 0 is transparent, and an alpha of 0xff is fully opaque.

            // ------------------------------------

            // Write out the image
            File outputfile = new File("outputimage.png");
            ImageIO.write(outputimage, "png", outputfile);

        } catch (Exception e) {
            System.out.println("ERROR " + e);
            e.printStackTrace();
        }
    }

    static class Pencil implements Runnable {
        @Override
        public void run() {

            while (true) {

                /* lock */
                lock.lock();

                if (k == 0) {
                    lock.unlock();
                    return;
                }

                boolean isOverlap = true;
                int x1;
                int y1;
                int x2;
                int y2;
                int x_max = 0;
                int x_min = 0;
                int y_max = 0;
                int y_min = 0;
                Rectangle rectangle_to_draw = new Rectangle();
                while (isOverlap) {
                    // Choose a random place to start
                    x1 = ThreadLocalRandom.current().nextInt(width);
                    y1 = ThreadLocalRandom.current().nextInt(height);
                    x2 = ThreadLocalRandom.current().nextInt(width);
                    y2 = ThreadLocalRandom.current().nextInt(height);

                    if (x1 >= x2) {
                        x_max = x1;
                        x_min = x2;
                    } else {
                        x_max = x2;
                        x_min = x1;
                    }
                    if (y1 >= y2) {
                        y_max = y1;
                        y_min = y2;
                    } else {
                        y_max = y2;
                        y_min = y1;
                    }

                    // System.out.println("x = "+x+"\ty = "+y+"\trec_width =
                    // "+rec_width+"\trec_height = "+rec_height);

                    // Check if the chosen position overlaps other in-process rectangles
                    isOverlap = false;
                    for (Rectangle rect : rectangles) {
                        if (!(y_max < rect.vertical_range[0] || y_min > rect.vertical_range[1]
                                || x_max < rect.horizontal_range[0] || x_min > rect.horizontal_range[1])) {
                            isOverlap = true;
                            break;
                        }
                    }
                }
                // Reduce the number of rectangles to draw
                k = k - 1;

                // Add a rectangle to the likedlist to 'lock' pixels it takes
                int[] horizontal_range = { x_min, x_max };
                int[] vertical_range = { y_min, y_max };
                rectangle_to_draw.horizontal_range = horizontal_range;
                rectangle_to_draw.vertical_range = vertical_range;
                rectangles.add(rectangle_to_draw);

                // Unlock
                lock.unlock();

                // Select a random color
                int r = ThreadLocalRandom.current().nextInt(256);
                int g = ThreadLocalRandom.current().nextInt(256);
                int b = ThreadLocalRandom.current().nextInt(256);
                rectangle_to_draw.color = BLACK | (r << 16) | (g << 8) | b;

                // Start drawing
                rectangle_to_draw.draw();

                // Remove this rectangle from our linked list when drawing process ends
                lock.lock();
                rectangles.remove(rectangle_to_draw);
                lock.unlock();
            }

        }

    }

    static class Rectangle {
        int color;
        int[] vertical_range;
        int[] horizontal_range;

        void draw() {

            //System.out.printf("draw ceiling\n");
            // draw the ceiling
            for (int i = horizontal_range[0]; i <= horizontal_range[1]; i++) {
                //System.out.printf("ceiling: vert[0] = " + vertical_range[0] + "\ti = " + i + "\n");
                outputimage.setRGB(vertical_range[0], i, BLACK);
            }
            //System.out.printf("draw middle\n");
            // draw the middle part
            for (int i = vertical_range[0] + 1; i <= vertical_range[1] - 1; i++) {
                //System.out.printf("middle.left: i = " + i + "\n" + "vert[1] = " + vertical_range[1]);
                outputimage.setRGB(i, horizontal_range[0], BLACK);
                for (int j = horizontal_range[0] + 1; j <= horizontal_range[1] - 1; j++) {
                    //System.out.println("middle: j = " + j + "\ti = " + i + "\t" + "parallel[1] = " + horizontal_range[1]);
                    outputimage.setRGB(i, j, color);
                }
                //System.out.println("middle.right: i = " + i + "\t" + "parallel[1] = " + horizontal_range[1]);
                outputimage.setRGB(i, horizontal_range[1], BLACK);
            }

            // draw the floor
            for (int i = horizontal_range[0]; i <= horizontal_range[1]; i++) {
                //System.out.printf("floor: vert[1] = " + vertical_range[0] + "\ti = " + i + "\n");
                outputimage.setRGB(vertical_range[1], i, BLACK);
            }
        }
    }
}
