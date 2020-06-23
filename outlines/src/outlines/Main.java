package outlines;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

class Pixel {
	public int x;
	public int y;
	public Color color;
	
	public Pixel(int x, int y, Color color) {
		this.x = x;
		this.y = y;
		this.color = color;
	}
}

public class Main extends Application {

	public static void main(String[] args) {
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		// UI setup.
		HBox root = new HBox();
		ImageView before = new ImageView();
		ImageView after = new ImageView();
		ImageView combined = new ImageView();
		root.getChildren().add(before);
		root.getChildren().add(after);
		root.getChildren().add(combined);
		
		// Load image.
		Image img = new Image("file:res/logo.png");
		before.setImage(img);
		
		// Draw outlines of img to a WritableImage using sobel.
		WritableImage outlined = sobel(img, Color.BLACK);
		after.setImage(outlined);
		
		// Combine images.
		WritableImage cImg = combine(img, outlined);
		combined.setImage(cImg);
		
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}
	
	private WritableImage sobel(Image img, Color color) {
		PixelReader pr = img.getPixelReader();
		Pixel[][] grayscale = new Pixel[(int) img.getWidth()][(int) img.getHeight()];
		Pixel[][] result = new Pixel[(int) img.getWidth()][(int) img.getHeight()];

		// Create a gray scale image by replacing transparent pixels with white
		// and all other pixels with black.
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				if (pr.getColor(x, y).getOpacity() > 0.05) {
					grayscale[x][y] = new Pixel(x, y, Color.WHITE);
				} else {
					grayscale[x][y] = new Pixel(x, y, Color.BLACK);
				}
			}
		}

		// Sobel edge detection and assign a color value to each pixel.
		for (int x = 1; x < grayscale.length - 1; x++) {
			for (int y = 1; y < grayscale[0].length - 1; y++) {
				double val1 = grayscale[x - 1][y - 1].color.getRed() * -1;
				val1 += grayscale[x - 1][y].color.getRed() * -2;
				val1 += grayscale[x - 1][y + 1].color.getRed() * -1;
				val1 += grayscale[x + 1][y - 1].color.getRed();
				val1 += grayscale[x + 1][y].color.getRed() * 2;
				val1 += grayscale[x + 1][y + 1].color.getRed();

				double val2 = grayscale[x - 1][y - 1].color.getRed() * -1;
				val2 += grayscale[x][y - 1].color.getRed() * -2;
				val2 += grayscale[x + 1][y - 1].color.getRed() * -1;
				val2 += grayscale[x - 1][y + 1].color.getRed() * 1;
				val2 += grayscale[x][y + 1].color.getRed() * 2;
				val2 += grayscale[x + 1][y + 1].color.getRed() * 1;

				double val = Math.sqrt(val1 * val1 + val2 * val2);
				if (val > 1) {
					val = 1;
				}
				result[x][y] = new Pixel(x, y, new Color(color.getRed(), color.getGreen(), color.getBlue(), val));
			}
		}

		// Create new WritableImage to write to.
		WritableImage bfimg = new WritableImage(result.length, result[0].length);
		PixelWriter g = bfimg.getPixelWriter();
		
		// Write pixels from buffer to image.
		for (int x = 0; x < result.length; x++) {
			for (int y = 0; y < result[0].length; y++) {
				Color c = null;
				if (result[x][y] == null) {
					c = new Color(0, 0, 0, 0);
				} else {
					double red = result[x][y].color.getRed();
					double green = result[x][y].color.getGreen();
					double blue = result[x][y].color.getBlue();
					double op = result[x][y].color.getOpacity();
					c = new Color(red, green, blue, op);
				}
				g.setColor(x, y, c);
			}
		}

		return bfimg;
	}
	
	private WritableImage combine(Image one, Image two) {
		PixelReader pr1 = one.getPixelReader();
		PixelReader pr2 = two.getPixelReader();
		WritableImage combinedImg = new WritableImage(pr1, (int) one.getWidth(), (int) one.getHeight());
		PixelWriter g = combinedImg.getPixelWriter();
		
		for (int x = 0; x < one.getWidth(); x++) {
			for (int y = 0; y < one.getHeight(); y++) {
				Color c = pr2.getColor(x, y);
				if (c.getOpacity() > 0) {
					g.setColor(x, y, c);
				}
			}
		}
		
		return combinedImg;
	}
}
