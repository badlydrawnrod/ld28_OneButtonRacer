package ld28;

import java.util.ArrayList;
import java.util.BitSet;
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
	private static String[] levels = {
		"sssssLLsLLsssssLLsLL",							// 1
		"ssLLsLLssllll+llllssLLsLL-ss",					// 2
		"srrllllrrsssllllssssssssllll",					// 3
		"ssllLLssss+rrrrRRss-ll",						// 4
		"ssssLLsLLssllsll+llsllssssLLsLL-ss",			// 5
		"sssllllsssllrrsrrsssrrssssss+rrrrssssllsll-s",	// 6
	};
	private static String[] levelNames = {
		"Ludum Racetrack",
		"Game Loop",
		"Twisted Track",
		"Infinite Loop",
		"Kernel Speedway",
		"The Magic Garden",
	};
	private static int[] laps = { 5, 5, 5, 8, 5, 5 };
	private TrackBuilder track;
	private List<Car> cars;
	private PlayerCar player1;
	private PlayerCar player2;
	private Sound overtakingSound;
	private Sound startSound;
	private boolean isStartSoundPlaying;
	private float startingTime;
	private long player1Score;
	private long player2Score;
	private boolean isTwoPlayer;
	private int level;
	private float wonTime;
	private boolean isWon;
	private boolean isGameOver;
	private boolean isGameWon;
	private float gameOverTime;

	public World(boolean isTwoPlayer) {
		this.isTwoPlayer = isTwoPlayer;
		overtakingSound = Kernel.sounds.get("sounds/overtake");
		startSound = Kernel.sounds.get("sounds/startrace");
		level = -1;
	}

	public int level() {
		return level;
	}
	
	public TrackBuilder track() {
		return track;
	}
	
	public List<Car> cars() {
		return cars;
	}
	
	public String levelName() {
		return levelNames[level];
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

	public boolean isStarting() {
		return Kernel.time.time < startingTime;
	}
	
	public void update() {
		if ((level == -1) || (isWon && Kernel.time.time >= wonTime)) {
			startNewLevel();
		}
		
		if (isStarting()) {
			if (!isStartSoundPlaying) {
				startSound.play();
				isStartSoundPlaying = true;
			}
		}
		else if (!isGameOver) {
			updateCars();
			if (!isWon) {
				updateCollisions();
				checkForOvertaking();
				checkForWinCondition();
				checkForLoseCondition();
				updateScores();
			}
		}
	}

	private void startNewLevel() {
		if (isGameWon) return;
		if (level + 1>= levels.length) {
			isGameWon = true;
			gameOverTime = Kernel.time.time + 2;
			return;
		}

		// TODO: this should be in the code to tear down the old level.
		App.broker.unsubscribeAll(PlayerWinEvent.class);
		
		level++;
		isWon = false;
		String trackDef = levels[level];
		track = generateTrack(trackDef);
		cars = new ArrayList<Car>();
		String trackLenStr = trackDef.replace("+", "");
		trackLenStr = trackLenStr.replace("+", "");
		int numCars = (int)(trackLenStr.length() * 0.8f);
		
		// Spawn the computer cars, avoiding piece 0 so that the players don't get screwed.
		// Computer cars aren't allowed to spawn on each other.
		BitSet[] occupied = new BitSet[5];
		for (int i = 0; i < occupied.length; i++) {
			occupied[i] = new BitSet();
		}
		final int margin = 1;
		for (int i = 0; i < numCars; i++) {
			int attempts = 20;
			int pieceIndex;
			int lane;
			do {
				pieceIndex = MathUtils.random(margin + 1, track.pieces().size() - 1);
				lane = MathUtils.random(-2, 2);
			} while (occupied[lane + 2].get(pieceIndex) && attempts-- > 0);
			cars.add(new Car(track, pieceIndex, lane, MathUtils.random(300, 400)));
		}
		
		int mult = MathUtils.randomBoolean() ? 1 : -1;
		player1 = new PlayerCar(1, Keys.A, track, 0, -1 * mult, 500);
		cars.add(player1);
		if (isTwoPlayer) {
			player2 = new PlayerCar(2, Keys.L, track, 0,  1 * mult, 500);
			cars.add(player2);
		}
		isStartSoundPlaying = false;
		
		startingTime = Kernel.time.time + 2.0f;
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

	private void checkForWinCondition() {
		if (player1.lap() > laps[level]) {
			isWon = true;
			wonTime = Kernel.time.time + 5;
			App.broker.publish(new PlayerWinEvent(1));
		}
		else if (player2 != null && player2.lap() > laps[level]) {
			isWon = true;
			wonTime = Kernel.time.time + 5;
			App.broker.publish(new PlayerWinEvent(2));
		}
	}
	
	private void checkForLoseCondition() {
		isGameOver = player1.health() < 0 && (player2 == null || player2.health() < 0);
		if (isGameOver) {
			gameOverTime = Kernel.time.time + 2; 
		}
		if (player1.health() < 0) {
			cars.remove(player1);
		}
		if (player2 != null && player2.health() < 0) {
			cars.remove(player2);
		}
	}
	
	private void updateScores() {
		player1Score += player1.speed * Kernel.time.delta;
		if (isTwoPlayer) {
			player2Score += player2.speed * Kernel.time.delta;
		}
	}
	
	public long player1Score() {
		return player1Score;
	}
	
	public long player2Score() {
		return player2Score;
	}
	
	public int player1Lap() {
		return player1.lap();
	}
	
	public int player2Lap() {
		return player2.lap();
	}
	
	public int laps() {
		return laps[level];
	}

	public float player1Health() {
		return player1.health();
	}

	public float player2Health() {
		return player2.health();
	}

	public boolean isGameOver() {
		return isGameOver;
	}

	public boolean isGameWon() {
		return isGameWon;
	}
	
	public boolean canQuit() {
		return (isGameOver || isGameWon) && Kernel.time.time > gameOverTime;
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


class Car {
	private static final String TAG = "Car";
	
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
	private int adjoiningLayer;
	protected int pieceIndex;
	private Polygon poly;
	private float accel;
	protected int currentSlot;
	protected float direction = 1.0f;
	protected boolean isRaceOver;

	public Car(TrackBuilder track, int pieceIndex, int currentSlot, float speed) {
		this.currentSlot = currentSlot;
		this.track = track;
		this.lane = currentSlot * 16;
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
		App.broker.subscribe(PlayerWinEvent.class, new Subscriber() {
			@Override
			public void onEvent(Event event) {
				// Prevent ourselves from receiving this event type again.
				App.broker.unsubscribe(PlayerWinEvent.class, this);
				Gdx.app.log(TAG, "PlayerWinEvent");
				isRaceOver = true;
				maxSpeed = 200;
			}
		});
	}
	
	public void update() {
		if (!isRaceOver) {
			speed = Math.min(maxSpeed, speed + Kernel.time.delta * accel);
		}
		else {
			speed = speed + (maxSpeed - speed) * Kernel.time.delta;
		}
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
		calculateAdjoiningLayer(pieces, piece);
		poly.setPosition(position.x, position.y);
		poly.setRotation(MathUtils.radDeg * angle);
	}

	private void calculateAdjoiningLayer(List<TrackPiece> pieces, TrackPiece piece) {
		if (distance < HALF_WIDTH) {
			int n = pieceIndex - 1;
			if (n < 0) {
				n = pieces.size() - 1;
			}
			TrackPiece adjoiningPiece = pieces.get(n);
			adjoiningLayer = adjoiningPiece.layer();
		}
		else if (distance > piece.length(lane) - HALF_WIDTH) {
			int n = (pieceIndex + 1) % pieces.size();
			TrackPiece adjoiningPiece = pieces.get(n);
			adjoiningLayer = adjoiningPiece.layer();
		}
		else {
			adjoiningLayer = layer;
		}
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
	
	public Polygon poly() {
		return poly;
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
		if (!isRaceOver && other.maxSpeed < maxSpeed) {
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

	public int adjoiningLayer() {
		return adjoiningLayer;
	}
	
	public TrackPiece piece() {
		return track.pieces().get(pieceIndex);
	}
}


class PlayerCar extends Car {
	private int key;
	private boolean isKeyPressed;
	private int playerNumber;
	private Sound lapCompleteSound;
	private Sound crashSound;
	private int lap;
	private float health;
	
	public PlayerCar(int playerNumber, int key, TrackBuilder track, int pieceIndex, int currentSlot, float speed) {
		super(track, pieceIndex, currentSlot, speed);
		this.playerNumber = playerNumber;
		this.key = key;
		this.lapCompleteSound = Kernel.sounds.get("sounds/lapcomplete");
		this.crashSound = Kernel.sounds.get("sounds/crash");
		lap = 1;
		health = 1.0f;
	}
	
	public int lap() {
		return lap;
	}
	
	public float health() {
		return health;
	}

	public void update() {
		if (!isRaceOver) {
			boolean wasKeyPressed = isKeyPressed;
			isKeyPressed = Gdx.input.isKeyPressed(key);
			boolean justTouched = Gdx.input.justTouched();
			if ((wasKeyPressed && !isKeyPressed) || justTouched) {
				currentSlot += direction;
				if (currentSlot == MAX_SLOT || currentSlot == -MAX_SLOT) {
					direction = -direction;
				}
				float newLane = 16 * currentSlot;	// TODO: magic!
				TrackPiece piece = track.pieces().get(pieceIndex);
				distance *= piece.length(newLane) / piece.length(lane);
				lane = newLane;
			}
		}
		int lastPieceIndex = pieceIndex;
		super.update();
		if (pieceIndex < lastPieceIndex) {
			lap++;
			lapCompleteSound.play();
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
		if (!isRaceOver) {
			health -= 0.05f;
		}
	}
	
	@Override
	public void onRanInto(Car other) {
		speed *= 0.5f;
		crashSound.play();
		if (!isRaceOver) {
			health -= 0.1f;
		}
	}
}


class PlayerWinEvent implements Event {
	public final int winner;
	
	public PlayerWinEvent(int player) {
		this.winner = player;
	}
}
