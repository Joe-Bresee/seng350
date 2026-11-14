import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public final class ScreenshotCapture {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    private ScreenshotCapture() {
    }

    public static byte[] capture(String busId, int sequence) throws IOException {
        BufferedImage image;
        try {
            if (GraphicsEnvironment.isHeadless()) {
                image = renderHeadlessFrame(busId, sequence);
            } else {
                Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                image = new Robot().createScreenCapture(new Rectangle(screen));
                image = resize(image, Math.min(640, image.getWidth()), Math.min(480, image.getHeight()));
            }
        } catch (AWTException | HeadlessException exception) {
            image = renderHeadlessFrame(busId, sequence);
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "jpg", output)) {
            throw new IOException("No JPEG writer is available");
        }
        return output.toByteArray();
    }

    private static BufferedImage renderHeadlessFrame(String busId, int sequence) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, WIDTH, HEIGHT);
            graphics.setColor(new Color(28, 64, 112));
            graphics.fillRect(0, 0, WIDTH, 96);
            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
            graphics.drawString("SENG 350 bus " + busId, 32, 58);
            graphics.setColor(new Color(32, 32, 32));
            graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
            graphics.drawString("Headless frame " + sequence, 32, 150);
            graphics.drawString("Rendered off-screen for linux202", 32, 190);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private static BufferedImage resize(BufferedImage source, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return image;
    }
}