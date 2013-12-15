package ld28;

import java.util.List;

import ldtk.Camera;
import ldtk.Image;
import ldtk.Kernel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class WorldRenderer {

	private World world;
	private Camera gameCam;
	private TrackRenderer trackRenderer;
	private CarRenderer carRenderer;

	public WorldRenderer(World world, Camera gameCam) {
		this.world = world;
		this.gameCam = gameCam;
		trackRenderer = new TrackRenderer(world.track());
		carRenderer = new CarRenderer(world.cars());
	}

	public void setup() {
	}

	public void draw() {
		gameCam.activate();
		for (int i = 0; i < TrackBuilder.NUM_LAYERS; i++) {
			trackRenderer.draw(i);
			carRenderer.draw(i);
		}
	}
}


class TrackRenderer {

	private static final int VERTS_PER_QUAD = 20;
	private static final int QUADS_PER_PIECE = 6;
	private static final float TRACK_WIDTH = 72;

	private int[] quadIndex;
	private float[][] vertices;
	private Texture texture;
	
	public TrackRenderer(TrackBuilder track) {
		generateVerts(track);
		texture = Kernel.images.get("textures/track").region().getTexture();
	}
	
	private void generateVerts(TrackBuilder track) {
		vertices = new float[TrackBuilder.NUM_LAYERS][];
		for (int i = 0; i < vertices.length; i++) {
			vertices[i] = new float[track.piecesOnLayer(i) * QUADS_PER_PIECE * VERTS_PER_QUAD];
		}
		quadIndex = new int[TrackBuilder.NUM_LAYERS];
		
		Vector2 tl = new Vector2();
		Vector2 bl = new Vector2();
		Vector2 br = new Vector2();
		Vector2 tr = new Vector2();
		float leftBorder = -TRACK_WIDTH / 2;
		float rightBorder = TRACK_WIDTH / 2;
		for (TrackPiece piece : track.pieces()) {
			float leftLength = piece.length(leftBorder);
			float rightLength = piece.length(rightBorder);
			float leftStep = leftLength / QUADS_PER_PIECE;
			float rightStep = rightLength / QUADS_PER_PIECE;
			for (int i = 0; i < QUADS_PER_PIECE; i++) {
				tl.set(piece.positionAt(leftStep * i, leftBorder));
				bl.set(piece.positionAt(rightStep * i, rightBorder));
				tr.set(piece.positionAt(leftStep * (i + 1), leftBorder));
				br.set(piece.positionAt(rightStep * (i + 1), rightBorder));
				addQuad(piece.layer(), tl, bl, br, tr);
			}
		}
	}

	private void addQuad(int layer, Vector2 tl, Vector2 bl, Vector2 br, Vector2 tr) {
		float colorBits = Color.WHITE.toFloatBits();
		float[] verts = vertices[layer];
		int i = quadIndex[layer] * VERTS_PER_QUAD;

		// Top left.
		verts[i + 0] = tl.x;			// x
		verts[i + 1] = tl.y;			// y
		verts[i + 2] = colorBits;		// colour
		verts[i + 3] = 0;				// u
		verts[i + 4] = 0;				// v
		
		// Bottom left.
		verts[i + 5] = bl.x;			// x
		verts[i + 6] = bl.y;			// y
		verts[i + 7] = colorBits;		// colour
		verts[i + 8] = 0;				// u
		verts[i + 9] = 1;				// v
		
		// Bottom right.
		verts[i + 10] = br.x;			// x
		verts[i + 11] = br.y;			// y
		verts[i + 12] = colorBits;		// colour
		verts[i + 13] = 1;				// u
		verts[i + 14] = 1;				// v
		
		// Top right.
		verts[i + 15] = tr.x;			// x
		verts[i + 16] = tr.y;			// y
		verts[i + 17] = colorBits;		// colour
		verts[i + 18] = 1;				// u
		verts[i + 19] = 0;				// v
		
		quadIndex[layer]++;
	}
	
	public void draw(int layer) {
		Kernel.batch.draw(texture, vertices[layer], 0, vertices[layer].length);
	}
}


class CarRenderer {
	private Image carImage;
	private Image redCarImage;
	private Image blueCarImage;
	private Image redArrowImage;
	private Image blueArrowImage;
	private List<Car> cars;

	public CarRenderer(List<Car> cars) {
		this.cars = cars;
		carImage = Kernel.images.get("atlases/ld28/dullmagentacar");
		redCarImage = Kernel.images.get("atlases/ld28/redcar");
		blueCarImage = Kernel.images.get("atlases/ld28/bluecar");
		redArrowImage = Kernel.images.get("atlases/ld28/redarrow");
		blueArrowImage = Kernel.images.get("atlases/ld28/bluearrow");
	}
	
	public void draw(int layer) {
		for (int i = 0, n = cars.size(); i < n; i++) {
			Car car = cars.get(i);
			if (car.layer() == layer) { 
				if (car instanceof PlayerCar) {
					PlayerCar playerCar = (PlayerCar) car;
					float arrowX = playerCar.x();
					float arrowY = playerCar.y();
					float arrowAngle = (playerCar.direction() > 0)
							? car.angle() - MathUtils.PI / 2
							: car.angle() + MathUtils.PI / 2;
					float arrowDist = 16 + 4 * MathUtils.sin(Kernel.time.time * 10);
					arrowX += arrowDist * MathUtils.cos(arrowAngle);
					arrowY += arrowDist * MathUtils.sin(arrowAngle);
					arrowAngle *= MathUtils.radDeg;
					
					switch (playerCar.playerNumber()) {
					case 1:
						redCarImage.draw(car.x(), car.y(), MathUtils.radDeg * car.angle());
						redArrowImage.draw(arrowX, arrowY, arrowAngle);
						break;
					case 2:
						blueCarImage.draw(car.x(), car.y(), MathUtils.radDeg * car.angle());
						blueArrowImage.draw(arrowX, arrowY, arrowAngle);
					}
				}
				else {
					carImage.draw(car.x(), car.y(), MathUtils.radDeg * car.angle());
				}
			}
		}
	}
}
