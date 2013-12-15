package ld28;

import java.util.ArrayList;
import java.util.List;

import ldtk.Kernel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;


public class World {

	private String oval = "ssLLsLLssllll+llllss-LLsLLss";
	private TrackBuilder track;
	private List<Car> cars;
	
	public World() {
		track = generateTrack(oval);
		cars = new ArrayList<Car>();
		for (int i = 0; i < 10; i++) {
			int pieceIndex = MathUtils.random(track.pieces().size() - 1);
			cars.add(new Car(track, pieceIndex, MathUtils.random(-2, 2) * 16, MathUtils.random(100, 350)));
		}
		cars.add(new PlayerCar(Keys.SPACE, track, 0, 0, 400));
	}
	
	public TrackBuilder track() {
		return track;
	}
	
	public List<Car> cars() {
		return cars;
	}

	private TrackBuilder generateTrack(String trackDef) {
		TrackBuilder trackBuilder = new TrackBuilder(0, -200, 0);
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
				trackBuilder.addStraight(100);
				break;
			case 'l':
				trackBuilder.addTurn(MathUtils.PI / 4.0f, 100);
				break;
			case 'r':
				trackBuilder.addTurn(-MathUtils.PI / 4.0f, 100);
				break;
			case 'L':
				trackBuilder.addTurn(MathUtils.PI / 4.0f, 120);
				break;
			case 'R':
				trackBuilder.addTurn(-MathUtils.PI / 4.0f, 120);
				break;
			default:
				throw new RuntimeException("Unknown track definition: " + c);
			}
		}
		return trackBuilder;
	}

	public void update() {
		for (int i = cars.size() - 1; i >= 0; i--) {
			Car car = cars.get(i);
			car.update();
		}
	}
}

class TrackBuilder {
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
	protected TrackBuilder track;
	protected float lane;
	protected float distance;
	private Vector2 position;
	private float angle;
	protected float speed;
	private int layer;
	protected int pieceIndex;

	public Car(TrackBuilder track, int pieceIndex, float lane, float speed) {
		this.track = track;
		this.lane = lane;
		this.distance = 0;
		this.speed = speed;
		this.pieceIndex = pieceIndex;
		position = new Vector2();
		updatePosition();
	}
	
	public void update() {
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
}


class PlayerCar extends Car {
	private int key;
	private float direction = 1.0f;
	private float currentSlot;
	private float maxSlot = 2;
	private boolean isKeyPressed;
	
	public PlayerCar(int key, TrackBuilder track, int pieceIndex, float lane, float speed) {
		super(track, pieceIndex, lane, speed);
		this.key = key;
		this.currentSlot = 0;
	}
	
	public void update() {
		boolean wasKeyPressed = isKeyPressed;
		isKeyPressed = Gdx.input.isKeyPressed(key);
		if (wasKeyPressed && !isKeyPressed) {
			if ((currentSlot + direction > maxSlot) || (currentSlot + direction < -maxSlot)) {
				direction = -direction;
			}
			currentSlot += direction;
			float newLane = 16 * currentSlot;	// TODO: magic!
			TrackPiece piece = track.pieces().get(pieceIndex);
			distance *= piece.length(newLane) / piece.length(lane);
			lane = newLane;
		}
		super.update();
	}
}
