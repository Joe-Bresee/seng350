import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Graphics2D;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

public final class ScreenshotCapture {
    private ScreenshotCapture() {
    }

    public static byte[] capture() {
        if (GraphicsEnvironment.isHeadless()) {
            return null;
        }
        try {
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                java.awt.image.BufferedImage captured = new Robot().createScreenCapture(
                    new Rectangle(screen));
                int width = Math.min(640, captured.getWidth());
                int height = Math.min(480, captured.getHeight());
                java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                    width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = image.createGraphics();
                graphics.drawImage(captured, 0, 0, width, height, null);
                graphics.dispose();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", output);
            return output.toByteArray();
        } catch (Exception exception) {
            return null;
        }
    }
}