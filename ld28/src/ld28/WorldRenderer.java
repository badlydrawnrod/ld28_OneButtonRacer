package ld28;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ldtk.Camera;
import ldtk.Image;
import ldtk.Kernel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.FloatArray;

public class WorldRenderer {

	private World world;
	private Camera gameCam;
	private TrackRenderer trackRenderer;
	private CarRenderer carRenderer;

	public WorldRenderer(World world, Camera gameCam) {
		this.world = world;
		this.gameCam = gameCam;
	}

	public void onLevelStart() {
		trackRenderer = new TrackRenderer(world.track());
		carRenderer = new CarRenderer(world.cars(), trackRenderer.obscuring(), trackRenderer.polysByPiece());
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

	private Map<TrackPiece, List<Polygon>> polysByPiece;
	private Map<TrackPiece, List<TrackPiece>> obscuring;
	private final float[] startFinishVertices;
	private final FloatArray faVertices;
	private final int[] faStarts;
	private final int[] faIndex;
	private final Texture trackTexture;
	private final Texture startFinishTexture;
	
	public TrackRenderer(TrackBuilder track) {
		trackTexture = Kernel.images.get("textures/track").region().getTexture();
		startFinishTexture = Kernel.images.get("textures/startfinish").region().getTexture();
		polysByPiece = new HashMap<TrackPiece, List<Polygon>>();
		obscuring = new HashMap<TrackPiece, List<TrackPiece>>();
		faVertices = new FloatArray(TrackBuilder.MAX_TRACK_PIECES * QUADS_PER_PIECE * VERTS_PER_QUAD);
		faStarts = new int[TrackBuilder.NUM_LAYERS];
		faIndex = new int[TrackBuilder.NUM_LAYERS];
		startFinishVertices = new float[VERTS_PER_QUAD];
		generateVerts(track);
		generateObscuring(track);
	}
	
	public Map<TrackPiece, List<TrackPiece>> obscuring() {
		return obscuring;
	}
	
	public Map<TrackPiece, List<Polygon>> polysByPiece() {
		return polysByPiece;
	}

	private void generateVerts(TrackBuilder track) {
		faVertices.clear();
		int start = 0;
		for (int i = 0; i < TrackBuilder.NUM_LAYERS; i++) {
			faStarts[i] = start;
			faIndex[i] = start;
			start += track.piecesOnLayer(i) * QUADS_PER_PIECE * VERTS_PER_QUAD;
		}
		faVertices.size = start;
		
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
			List<Polygon> polys = new ArrayList<Polygon>();
			for (int i = 0; i < QUADS_PER_PIECE; i++) {
				tl.set(piece.positionAt(leftStep * i, leftBorder));
				bl.set(piece.positionAt(rightStep * i, rightBorder));
				tr.set(piece.positionAt(leftStep * (i + 1), leftBorder));
				br.set(piece.positionAt(rightStep * (i + 1), rightBorder));
				addQuad(piece.layer(), tl, bl, br, tr);
				polys.add(new Polygon(new float[] { tl.x, tl.y, bl.x, bl.y, br.x, br.y, tr.x, tr.y }));
				if (first) {
					first = false;
					Vector2 tr2 = new Vector2(piece.positionAt(leftStep / 2, leftBorder));
					Vector2 br2 = new Vector2(piece.positionAt(rightStep / 2, rightBorder));
					addStartFinish(tl, bl, br2, tr2);
				}
			}
			polysByPiece.put(piece, polys);
		}
	}

	private void generateObscuring(TrackBuilder track) {
		List<TrackPiece> pieces = track.pieces();
		int numPieces = pieces.size();
		for (int i = 0; i < numPieces; i++) {
			TrackPiece piece = pieces.get(i);
			Polygon[] piecePolys = polysByPiece.get(piece).toArray(new Polygon[0]);
			int layer = piece.layer();
			for (int j = 0; j < numPieces; j++) {
				// A piece can't obscure itself or its neighbours. 
				if (Math.abs(i - j) < 2) {
					continue;
				}
				TrackPiece other = pieces.get(j);
				if (layer >= other.layer()) {
					continue;
				}
				Polygon[] otherPolys = polysByPiece.get(other).toArray(new Polygon[0]);
				if (Polys.hitAny(piecePolys, otherPolys)) {
					List<TrackPiece> obscuringPieces = obscuring.containsKey(piece)
							? obscuring.get(piece)
							: new ArrayList<TrackPiece>();
					obscuringPieces.add(other);
					obscuring.put(piece, obscuringPieces);
				}
			}
		}
	}

	private void addStartFinish(Vector2 tl, Vector2 bl, Vector2 br, Vector2 tr) {
		float colorBits = Color.WHITE.toFloatBits();
		
		// Top left.
		startFinishVertices[0] = tl.x;			// x
		startFinishVertices[1] = tl.y;			// y
		startFinishVertices[2] = colorBits;		// colour
		startFinishVertices[3] = 0;				// u
		startFinishVertices[4] = 0;				// v
		
		// Bottom left.
		startFinishVertices[5] = bl.x;			// x
		startFinishVertices[6] = bl.y;			// y
		startFinishVertices[7] = colorBits;		// colour
		startFinishVertices[8] = 0;				// u
		startFinishVertices[9] = 1;				// v
		
		// Bottom right.
		startFinishVertices[10] = br.x ;		// x
		startFinishVertices[11] = br.y;			// y
		startFinishVertices[12] = colorBits;	// colour
		startFinishVertices[13] = 1;			// u
		startFinishVertices[14] = 1;			// v
		
		// Top right.
		startFinishVertices[15] = tr.x;			// x
		startFinishVertices[16] = tr.y;			// y
		startFinishVertices[17] = colorBits;	// colour
		startFinishVertices[18] = 1;			// u
		startFinishVertices[19] = 0;			// v
	}
	
	private void addQuad(int layer, Vector2 tl, Vector2 bl, Vector2 br, Vector2 tr) {
		float colorBits = Color.WHITE.toFloatBits();
		int index = faIndex[layer];
		
		// Top left.
		faVertices.set(index + 0, tl.x);			// x
		faVertices.set(index + 1, tl.y);			// y
		faVertices.set(index + 2, colorBits);		// colour
		faVertices.set(index + 3, 0);				// u
		faVertices.set(index + 4, 0);				// v
		
		// Bottom left.
		faVertices.set(index + 5, bl.x);			// x
		faVertices.set(index + 6, bl.y);			// y
		faVertices.set(index + 7, colorBits);		// colour
		faVertices.set(index + 8, 0);				// u
		faVertices.set(index + 9, 1);				// v
		
		// Bottom right.
		faVertices.set(index + 10, br.x);			// x
		faVertices.set(index + 11, br.y);			// y
		faVertices.set(index + 12, colorBits);		// colour
		faVertices.set(index + 13, 1);				// u
		faVertices.set(index + 14, 1);				// v
		
		// Top right.
		faVertices.set(index + 15, tr.x);			// x
		faVertices.set(index + 16, tr.y);			// y
		faVertices.set(index + 17, colorBits);		// colour
		faVertices.set(index + 18, 1);				// u
		faVertices.set(index + 19, 0);				// v
		
		faIndex[layer] += VERTS_PER_QUAD;
	}
	
	public void draw(int layer) {
		int start = faStarts[layer];
		int count = faIndex[layer] - start;
		Kernel.batch.draw(trackTexture, faVertices.items, start, count);
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
	private Map<TrackPiece, List<Polygon>> polysByPiece;
	private Map<TrackPiece, List<TrackPiece>> obscuring;

	public CarRenderer(List<Car> cars, Map<TrackPiece, List<TrackPiece>> obscuring, Map<TrackPiece, List<Polygon>> polysByPiece) {
		this.cars = cars;
		this.obscuring = obscuring;
		this.polysByPiece = polysByPiece;
		carImage = Kernel.images.get("atlases/ld28/dronecar");
		obscuredCarImage = Kernel.images.get("atlases/ld28/transparentcar");
		redCarImage = Kernel.images.get("atlases/ld28/redcar");
		blueCarImage = Kernel.images.get("atlases/ld28/bluecar");
		redArrowImage = Kernel.images.get("atlases/ld28/redarrow");
		blueArrowImage = Kernel.images.get("atlases/ld28/bluearrow");
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
			TrackPiece piece = car.piece();
			List<TrackPiece> obscuringPieces = obscuring.get(piece);
			if (obscuringPieces != null) {
				for (TrackPiece obscuringPiece : obscuringPieces) {
					Polygon[] piecePolys = polysByPiece.get(obscuringPiece).toArray(new Polygon[0]);
					if (Polys.hitAny(car.poly(), piecePolys)) {
						obscuredCarImage.draw(car.x(), car.y(), MathUtils.radDeg * car.angle());
						break;
					}
				}
			}
		}
	}
}
