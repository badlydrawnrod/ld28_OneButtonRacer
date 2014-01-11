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

	public WorldRenderer() {
		trackRenderer = new TrackRenderer();
		carRenderer = new CarRenderer();
	}

	public void init(World world, Camera camera) {
		this.world = world;
		this.gameCam = camera;
		trackRenderer.init();
		carRenderer.init();
	}

	public void onLevelStart() {
		trackRenderer.changeTrack(world.track());
		carRenderer.changeTrack(world.cars());
	}
	
	public void draw() {
		gameCam.activate();
		for (int i = 0; i < TrackBuilder.NUM_LAYERS; i++) {
			trackRenderer.draw(i);
			carRenderer.draw(i);
		}
		carRenderer.drawObscured();
	}
}


class TrackRenderer {

	private static final int VERTS_PER_QUAD = 20;
	private static final int QUADS_PER_PIECE = 6;
	private static final float TRACK_WIDTH = 88;
	private static final float WHITE_BITS = Color.WHITE.toFloatBits();

	private final float[] startFinishVertices;
	private final float[] vertices;
	private final int[] layerStarts;
	private final int[] layerIndexes;
	private Texture trackTexture;
	private Texture startFinishTexture;

	
	public TrackRenderer() {
		vertices = new float[TrackBuilder.MAX_TRACK_PIECES * QUADS_PER_PIECE * VERTS_PER_QUAD];
		layerStarts = new int[TrackBuilder.NUM_LAYERS];
		layerIndexes = new int[TrackBuilder.NUM_LAYERS];
		startFinishVertices = new float[VERTS_PER_QUAD];
	}
	
	public void init() {
		trackTexture = Kernel.images.get("textures/track").region().getTexture();
		startFinishTexture = Kernel.images.get("textures/startfinish").region().getTexture();
	}

	public void changeTrack(TrackBuilder track) {
		generateVerts(track);
	}
	
	private void generateVerts(TrackBuilder track) {
		int start = 0;
		for (int i = 0; i < TrackBuilder.NUM_LAYERS; i++) {
			layerStarts[i] = start;
			layerIndexes[i] = start;
			start += track.piecesOnLayer(i) * QUADS_PER_PIECE * VERTS_PER_QUAD;
		}
		
		Vector2 tl = new Vector2();
		Vector2 bl = new Vector2();
		Vector2 br = new Vector2();
		Vector2 tr = new Vector2();
		float leftBorder = -TRACK_WIDTH / 2;
		float rightBorder = TRACK_WIDTH / 2;
		boolean first = true;
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
				if (first) {
					first = false;
					Vector2 tr2 = new Vector2(piece.positionAt(leftStep / 2, leftBorder));
					Vector2 br2 = new Vector2(piece.positionAt(rightStep / 2, rightBorder));
					addStartFinish(tl, bl, br2, tr2);
				}
			}
		}
	}
	
	private void addStartFinish(Vector2 tl, Vector2 bl, Vector2 br, Vector2 tr) {
		// Top left.
		startFinishVertices[0] = tl.x;			// x
		startFinishVertices[1] = tl.y;			// y
		startFinishVertices[2] = WHITE_BITS;	// colour
		startFinishVertices[3] = 0;				// u
		startFinishVertices[4] = 0;				// v
		
		// Bottom left.
		startFinishVertices[5] = bl.x;			// x
		startFinishVertices[6] = bl.y;			// y
		startFinishVertices[7] = WHITE_BITS;	// colour
		startFinishVertices[8] = 0;				// u
		startFinishVertices[9] = 1;				// v
		
		// Bottom right.
		startFinishVertices[10] = br.x ;		// x
		startFinishVertices[11] = br.y;			// y
		startFinishVertices[12] = WHITE_BITS;	// colour
		startFinishVertices[13] = 1;			// u
		startFinishVertices[14] = 1;			// v
		
		// Top right.
		startFinishVertices[15] = tr.x;			// x
		startFinishVertices[16] = tr.y;			// y
		startFinishVertices[17] = WHITE_BITS;	// colour
		startFinishVertices[18] = 1;			// u
		startFinishVertices[19] = 0;			// v
	}
	
	private void addQuad(int layer, Vector2 tl, Vector2 bl, Vector2 br, Vector2 tr) {
		int index = layerIndexes[layer];
		
		// Top left.
		vertices[index + 0] = tl.x;			// x
		vertices[index + 1] = tl.y;			// y
		vertices[index + 2] = WHITE_BITS;	// colour
		vertices[index + 3] = 0;			// u
		vertices[index + 4] = 0;			// v
		
		// Bottom left.
		vertices[index + 5] = bl.x;			// x
		vertices[index + 6] = bl.y;			// y
		vertices[index + 7] = WHITE_BITS;	// colour
		vertices[index + 8] = 0;			// u
		vertices[index + 9] = 1;			// v
		
		// Bottom right.
		vertices[index + 10] = br.x;		// x
		vertices[index + 11] = br.y;		// y
		vertices[index + 12] = WHITE_BITS;	// colour
		vertices[index + 13] = 1;			// u
		vertices[index + 14] = 1;			// v
		
		// Top right.
		vertices[index + 15] = tr.x;		// x
		vertices[index + 16] = tr.y;		// y
		vertices[index + 17] = WHITE_BITS;	// colour
		vertices[index + 18] = 1;			// u
		vertices[index + 19] = 0;			// v
		
		layerIndexes[layer] += VERTS_PER_QUAD;
	}
	
	public void draw(int layer) {
		int start = layerStarts[layer];
		int count = layerIndexes[layer] - start;
		Kernel.batch.draw(trackTexture, vertices, start, count);
		if (layer == 0) {
			Kernel.batch.draw(startFinishTexture, startFinishVertices, 0, startFinishVertices.length);
		}
	}
}


class CarRenderer {
	private Image carImage;
	private Image obscuredCarImage;
	private Image redCarImage;
	private Image blueCarImage;
	private Image redArrowImage;
	private Image blueArrowImage;
	private List<Car> cars;

	public void init() {
		carImage = Kernel.images.get("atlases/ld28/dronecar");
		obscuredCarImage = Kernel.images.get("atlases/ld28/transparentcar");
		redCarImage = Kernel.images.get("atlases/ld28/redcar");
		blueCarImage = Kernel.images.get("atlases/ld28/bluecar");
		redArrowImage = Kernel.images.get("atlases/ld28/redarrow");
		blueArrowImage = Kernel.images.get("atlases/ld28/bluearrow");
	}
	
	public void changeTrack(List<Car> cars) {
		this.cars = cars;
	}
	
	public void draw(int layer) {
		for (int i = 0, n = cars.size(); i < n; i++) {
			Car car = cars.get(i);
			if (car.layer() == layer || car.adjoiningLayer() == layer) { 
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
	
	public void drawObscured() {
		for (int i = 0, n = cars.size(); i < n; i++) {
			Car car = cars.get(i);
			obscuredCarImage.draw(car.x(), car.y(), MathUtils.radDeg * car.angle());
		}
	}
}
