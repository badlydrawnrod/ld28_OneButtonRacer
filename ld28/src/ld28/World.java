package ld28;

import java.util.ArrayList;
import java.util.List;

import ldtk.Kernel;
import ldtk.Sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;


public class World {

	private static final float SMALL_STRAIGHT_SIZE = 120;
	private static final float SMALL_CURVE_RADIUS = 120;
	private static final float LARGE_STRAIGHT_SIZE = 192;
	private static final float LARGE_CURVE_RADIUS = 192;
//	private String oval = "ssLLsLLssllll+llllssLLsLL-ss";
//	private String oval = "ssLLsLLssLLsLL";
	private String oval = "ssllLLssssrrrrRR+ssll-";
	private TrackBuilder track;
	private List<Car> cars;
	private PlayerCar player1;
	private PlayerCar player2;
	private Sound overtakingSound;

	public World() {
		track = generateTrack(oval);
		cars = new ArrayList<Car>();
		String trackLenStr = oval.replace("+", "");
		trackLenStr = trackLenStr.replace("+", "");
		int numCars = (int)(trackLenStr.length() * 0.8f);
		
		// Spawn the computer cars, avoiding piece 0 so that the players don't get screwed.
		final int margin = 2;
		for (int i = 0; i < numCars; i++) {
			int pieceIndex = MathUtils.random(margin, track.pieces().size() - 1 - margin);
			cars.add(new Car(track, pieceIndex, MathUtils.random(-2, 2), MathUtils.random(300, 400)));
		}
		
		player1 = new PlayerCar(1, Keys.A, track, 0, -1, 500);
		cars.add(player1);
		player2 = new PlayerCar(2, Keys.L, track, 0,  1, 500);
		cars.add(player2);
		
		overtakingSound = Kernel.sounds.get("sounds/overtake");
	}
	
	public TrackBuilder track() {
		return track;
	}
	
	public List<Car> cars() {
		return cars;
	}

	private TrackBuilder generateTrack(String trackDef) {
		TrackBuilder trackBuilder = new TrackBuilder(0, 0, 0);
		for (int i = 0, n = trackDef.length(); i < n; i++) {
			char c = trackDef.charAt(i);
			switch (c) {
			case '+':
				trackBuilder.up();
				break;
			case '-':
				trackBuilder.down();
				break;
			case 's':
				trackBuilder.addStraight(SMALL_STRAIGHT_SIZE);
				break;
			case 'S':
				trackBuilder.addStraight(LARGE_STRAIGHT_SIZE);
				break;
			case 'l':
				trackBuilder.addTurn(MathUtils.PI / 4.0f, SMALL_CURVE_RADIUS);
				break;
			case 'r':
				trackBuilder.addTurn(-MathUtils.PI / 4.0f, SMALL_CURVE_RADIUS);
				break;
			case 'L':
				trackBuilder.addTurn(MathUtils.PI / 4.0f, LARGE_CURVE_RADIUS);
				break;
			case 'R':
				trackBuilder.addTurn(-MathUtils.PI / 4.0f, LARGE_CURVE_RADIUS);
				break;
			default:
				throw new RuntimeException("Unknown track definition: " + c);
			}
		}
		trackBuilder.build();
		return trackBuilder;
	}

	public void update() {
		updateCars();
		updateCollisions();
		checkForOvertaking();
	}

	private void updateCars() {
		for (int i = cars.size() - 1; i >= 0; i--) {
			Car car = cars.get(i);
			car.update();
		}
	}

	private void updateCollisions() {
		for (int i = 0, n = cars.size(); i < n; i++) {
			Car car = cars.get(i);
			for (int j = i + 1; j < n; j++) {
				Car other = cars.get(j);
				switch (car.hit(other)) {
				case 1:
					other.onRanInto(car);
					car.onWasRunInto(other);
					break;
				case -1:
					car.onRanInto(other);
					car.onWasRunInto(other);
					break;
				}
			}
		}
	}
	
	private void checkForOvertaking() {
		if (player2 == null) {
			return;
		}
		
		if (player1.pieceIndex == player2.pieceIndex) {
			if (Math.abs(player1.distance - player2.distance) < 5.0f) {
				overtakingSound.play();
			}
		}
	}
}

class TrackBuilder {
	public static final float TRACK_WIDTH = 72;
	public static final int QUADS_PER_PIECE = 6;
	public static final int NUM_LAYERS = 5;
	
	private int layer;
	private Vector2 pos;
	private float angle;

	private List<TrackPiece> pieces;
	private int[] piecesByLayer;
	
	public TrackBuilder() {
		this(0, 0, 0);
	}
	
	public TrackBuilder(float x, float y, float angle) {
		this.pos = new Vector2(x, y);
		this.angle = angle;
		pieces = new ArrayList<TrackPiece>();
		piecesByLayer = new int[NUM_LAYERS];
		layer = 0;
	}
	
	public void build() {
		Vector2 bl = new Vector2();
		Vector2 tr = new Vector2();
		Vector2 cmp = new Vector2();
		float leftBorder = -TRACK_WIDTH / 2;
		float rightBorder = TRACK_WIDTH / 2;
		for (TrackPiece piece : pieces) {
			float leftLength = piece.length(leftBorder);
			float rightLength = piece.length(rightBorder);
			float leftStep = leftLength / QUADS_PER_PIECE;
			float rightStep = rightLength / QUADS_PER_PIECE;
			for (int i = 0; i < QUADS_PER_PIECE; i++) {
				cmp.set(piece.positionAt(leftStep * i, leftBorder));
				bl.set(Math.min(bl.x, cmp.x), Math.min(bl.y, cmp.y));
				tr.set(Math.max(tr.x, cmp.x), Math.max(tr.y, cmp.y));

				cmp.set(piece.positionAt(rightStep * i, rightBorder));
				bl.set(Math.min(bl.x, cmp.x), Math.min(bl.y, cmp.y));
				tr.set(Math.max(tr.x, cmp.x), Math.max(tr.y, cmp.y));

				cmp.set(piece.positionAt(leftStep * (i + 1), leftBorder));
				bl.set(Math.min(bl.x, cmp.x), Math.min(bl.y, cmp.y));
				tr.set(Math.max(tr.x, cmp.x), Math.max(tr.y, cmp.y));

				cmp.set(piece.positionAt(rightStep * (i + 1), rightBorder));
				bl.set(Math.min(bl.x, cmp.x), Math.min(bl.y, cmp.y));
				tr.set(Math.max(tr.x, cmp.x), Math.max(tr.y, cmp.y));
			}
		}
		float centreX = MathUtils.floor((bl.x + tr.x) / 2);
		float centreY = MathUtils.floor((bl.y + tr.y) / 2);
		for (TrackPiece piece : pieces) {
			piece.adjust(centreX, centreY);
		}
	}
	
	public List<TrackPiece> pieces() {
		return pieces;
	}
	
	public int piecesOnLayer(int layer) {
		return piecesByLayer[layer];
	}
	
	public TrackBuilder addStraight(float length) {
		TrackPiece piece = new StraightPiece(layer, pos, angle, length);
		pos.set(piece.positionAtEnd());
		pieces.add(piece);
		piecesByLayer[layer]++;
		return this;
	}
	
	public TrackBuilder addTurn(float angle, float radius) {
		TrackPiece piece = new TurnPiece(layer, pos, this.angle, this.angle + angle, radius);
		pos.set(piece.positionAtEnd());
		this.angle += angle;
		pieces.add(piece);
		piecesByLayer[layer]++;
		return this;
	}
	
	public TrackBuilder up() {
		if (layer < NUM_LAYERS - 1) {
			++layer;
		}
		return this;
	}
	
	public TrackBuilder down() {
		if (layer > 0) {
			--layer;
		}
		return this;
	}
}


abstract class TrackPiece {
	
	protected Vector2 startPos;
	protected Vector2 endPos;
	private final int layer;

	public TrackPiece(int layer) {
		this.layer = layer;
	}

	public int layer() {
		return layer;
	}

	public void adjust(float x, float y) {
		startPos.sub(x, y);
		endPos.sub(x, y);
	}
	
	public abstract float length(float lane);
	public abstract float angleAtStart();
	public abstract float angleAtEnd();
	
	public abstract float angleAt(float length, float lane);
	public abstract Vector2 positionAt(float length, float lane);

	
	public Vector2 positionAtStart(float lane) {
		Vector2 v = new Vector2(positionAtStart());
		float angleAtStart = angleAtStart();
		v.x += lane * MathUtils.cos(angleAtStart + MathUtils.PI / 2);
		v.y += lane * MathUtils.sin(angleAtStart + MathUtils.PI / 2);
		return v;
	}
	
	public Vector2 positionAtEnd(float lane) {
		Vector2 v = new Vector2(positionAtEnd());
		float angleAtEnd = angleAtEnd();
		v.x += lane * MathUtils.cos(angleAtEnd + MathUtils.PI / 2);
		v.y += lane * MathUtils.sin(angleAtEnd + MathUtils.PI / 2);
		return v;
	}
	
	public Vector2 positionAtStart() {
		return startPos;
	}
	
	public Vector2 positionAtEnd() {
		return endPos;
	}
}


class StraightPiece extends TrackPiece {
	private float angle;
	private float length;
	
	public StraightPiece(int layer, Vector2 pos, float angle, float length) {
		super(layer);
		this.startPos = new Vector2(pos);
		this.angle = angle;
		this.length = length;
		float endX = startPos.x + MathUtils.cos(angle) * length;
		float endY = startPos.y + MathUtils.sin(angle) * length;
		this.endPos = new Vector2(endX, endY);
	}

	@Override
	public float length(float lane) {
		return length;
	}
	
	@Override
	public float angleAtStart() {
		return angle;
	}
	
	@Override
	public float angleAtEnd() {
		return angle;
	}

	@Override
	public float angleAt(float length, float lane) {
		return angle;
	}

	@Override
	public Vector2 positionAt(float length, float lane) {
		Vector2 v = new Vector2(startPos);
		v.x += length * Math.cos(angle) - lane * Math.cos(angle + Math.PI / 2);
		v.y += length * Math.sin(angle) - lane * Math.sin(angle + Math.PI / 2);
		return v;
	}
}


class TurnPiece extends TrackPiece {
	private Vector2 centre;
	private float startAngle;
	private float endAngle;
	private float radius;

	public TurnPiece(int layer, Vector2 pos, float startAngle, float endAngle, float radius) {
		super(layer);
		this.startPos = new Vector2(pos);
		this.startAngle = startAngle;
		this.endAngle = endAngle;
		this.radius = radius;
		
		// Find the centre of the circle that this track piece is turning around.
		centre = new Vector2();
		if (startAngle <= endAngle) {
			float x = pos.x + radius * MathUtils.cos(startAngle + MathUtils.PI/2);
			float y = pos.y + radius * MathUtils.sin(startAngle + MathUtils.PI/2);
			centre.set(x, y);
			float endX = centre.x + radius * MathUtils.cos(endAngle - MathUtils.PI / 2);
			float endY = centre.y + radius * MathUtils.sin(endAngle - MathUtils.PI / 2);
			this.endPos = new Vector2(endX, endY);
		}
		else {
			float x = pos.x + radius * MathUtils.cos(startAngle - MathUtils.PI / 2);
			float y = pos.y + radius * MathUtils.sin(startAngle - MathUtils.PI / 2);
			centre.set(x, y);
			float endX = centre.x + radius * MathUtils.cos(endAngle + MathUtils.PI / 2);
			float endY = centre.y + radius * MathUtils.sin(endAngle + MathUtils.PI / 2);
			this.endPos = new Vector2(endX, endY);
		}
	}

	@Override
	public float length(float lane) {
		return Math.abs(endAngle - startAngle) * (radius + lane);
	}

	@Override
	public void adjust(float x, float y) {
		super.adjust(x, y);
		centre.sub(x, y);
	}
	
	@Override
	public float angleAtStart() {
		return startAngle;
	}

	@Override
	public float angleAtEnd() {
		return endAngle;
	}

	@Override
	public float angleAt(float length, float lane) {
		float angle = length / (radius + lane);
		if (startAngle >= endAngle) {
			return startAngle - angle;			
		}
		else {
			return startAngle + angle;
		}
	}

	@Override
	public Vector2 positionAt(float length, float lane) {
		Vector2 v = new Vector2(centre);
		float angle = angleAt(length, lane);
		if (startAngle >= endAngle) {
			angle += MathUtils.PI / 2;
			float laneRadius = radius - lane;
			v.x += laneRadius * Math.cos(angle);
			v.y += laneRadius * Math.sin(angle);
		}
		else {
			angle -= MathUtils.PI / 2;
			float laneRadius = radius + lane;
			v.x += laneRadius * Math.cos(angle);
			v.y += laneRadius * Math.sin(angle);
		}
		return v;
	}
}


class Car {
	protected static final float MAX_SLOT = 2;
	private static final float HALF_WIDTH = 12;
	private static final float HALF_HEIGHT = 6;
	
	protected TrackBuilder track;
	protected float lane;
	protected float distance;
	private Vector2 position;
	private float angle;
	protected float speed;
	private float maxSpeed;
	private int layer;
	protected int pieceIndex;
	private Polygon poly;
	private float accel;
	protected int currentSlot;
	protected float direction = 1.0f;

	public Car(TrackBuilder track, int pieceIndex, int currentSlot, float speed) {
		this.currentSlot = currentSlot;
		this.track = track;
		this.lane = currentSlot * 16; // TODO: remove Paul Daniels' constant.
		this.distance = 0;
		this.speed = 0;
		this.accel = 100.0f;
		this.maxSpeed = speed;
		this.pieceIndex = pieceIndex;
		float[] verts = new float[] {
			-HALF_WIDTH,  HALF_HEIGHT,
			-HALF_WIDTH, -HALF_HEIGHT,
			 HALF_WIDTH, -HALF_HEIGHT,
			 HALF_WIDTH,  HALF_HEIGHT,
		};
		poly = new Polygon(verts); 
		position = new Vector2();
		updatePosition();
	}
	
	public void update() {
		speed = Math.min(maxSpeed, speed + Kernel.time.delta * accel);
		distance += Kernel.time.delta * speed;
		updatePosition();
	}
	
	private void updatePosition() {
		List<TrackPiece> pieces = track.pieces();
		TrackPiece piece = pieces.get(pieceIndex);
		while (distance >= piece.length(lane)) {
			distance -= piece.length(lane);
			pieceIndex = (pieceIndex + 1) % pieces.size();
			piece = pieces.get(pieceIndex);
		}
		position.set(piece.positionAt(distance, lane));
		angle = piece.angleAt(distance, lane);
		layer = piece.layer();
		poly.setPosition(position.x, position.y);
		poly.setRotation(MathUtils.radDeg * angle);
	}
	
	public float x() {
		return position.x;
	}
	
	public float y() {
		return position.y;
	}
	
	public float angle() {
		return angle;
	}
	
	public int layer() {
		return layer;
	}
	
	public int hit(Car other) {
		// Throw out non-collisions.
		if (!Polys.hit(poly, other.poly) || (Math.abs(pieceIndex - other.pieceIndex) > 1)) {
			return 0;
		}

		// Now we know we had a collision.

		int numPieces = track.pieces().size();
		if (pieceIndex == (other.pieceIndex + 1) % numPieces) {
			// We're ahead of the other car by one piece, so they shunted us.
			return 1;
		}
		else if ((pieceIndex + 1) % numPieces == other.pieceIndex) {
			// We're behind the other car by one piece, so we shunted them.
			return -1;
		}
		
		// We're on the same track piece, so it's all down to distance.
		return (distance >= other.distance) ? 1: -1;
	}
	
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
	public void onWasRunInto(Car other) {
	}
	
	public void onRanInto(Car other) {
		speed *= 0.5f;
		// Change lanes if we're faster than the other car.
		if (other.maxSpeed < maxSpeed) {
			if ((currentSlot == MAX_SLOT && direction > 0) || (currentSlot == -MAX_SLOT && direction < 0)) {
				direction = -direction;
			}
			currentSlot += direction;
			float newLane = 16 * currentSlot;	// TODO: magic!
			TrackPiece piece = track.pieces().get(pieceIndex);
			distance *= piece.length(newLane) / piece.length(lane);
			lane = newLane;
		}
	}
}


class PlayerCar extends Car {
	private int key;
	private boolean isKeyPressed;
	private int playerNumber;
	private Sound lapCompleteSound;
	private Sound crashSound;
	
	public PlayerCar(int playerNumber, int key, TrackBuilder track, int pieceIndex, int currentSlot, float speed) {
		super(track, pieceIndex, currentSlot, speed);
		this.playerNumber = playerNumber;
		this.key = key;
		this.lapCompleteSound = Kernel.sounds.get("sounds/lap_complete");
		this.crashSound = Kernel.sounds.get("sounds/crash");
	}
	
	public void update() {
		boolean wasKeyPressed = isKeyPressed;
		isKeyPressed = Gdx.input.isKeyPressed(key);
		if (wasKeyPressed && !isKeyPressed) {
			currentSlot += direction;
			if (currentSlot == MAX_SLOT || currentSlot == -MAX_SLOT) {
				direction = -direction;
			}
			float newLane = 16 * currentSlot;	// TODO: magic!
			TrackPiece piece = track.pieces().get(pieceIndex);
			distance *= piece.length(newLane) / piece.length(lane);
			lane = newLane;
		}
		int lastPieceIndex = pieceIndex;
		super.update();
		if (pieceIndex < lastPieceIndex) {
			// TODO: create this sound.
//			lapCompleteSound.play();
		}
	}

	public int playerNumber() {
		return playerNumber;
	}

	public float direction() {
		return direction;
	}
	
	@Override
	public void onWasRunInto(Car other) {
		crashSound.play();
	}
	
	@Override
	public void onRanInto(Car other) {
		speed *= 0.5f;
		crashSound.play();
	}
}
