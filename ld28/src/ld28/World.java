package ld28;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import ldtk.Kernel;
import ldtk.Sound;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.MathUtils;


public class World {
	
	enum GameState { START_LEVEL, PLAY_LEVEL, END_LEVEL, WON_GAME, LOST_GAME };

	static class PlayerWinEvent implements Event {
		public final int winner;
		
		public PlayerWinEvent(int player) {
			this.winner = player;
		}
	}

	static class PlayerLoseEvent implements Event {
	}
	
	private static final float SMALL_STRAIGHT_SIZE = 120;
	private static final float SMALL_CURVE_RADIUS = 120;
	private static final float LARGE_STRAIGHT_SIZE = 192;
	private static final float LARGE_CURVE_RADIUS = 192;
	private static final float END_LEVEL_TIMEOUT = 2.0f;
	private static final float GAME_OVER_TIMEOUT = 2.0f;
	
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
	private float startingTime;
	private long player1Score;
	private long player2Score;
	private boolean isTwoPlayer;
	private int level;
	private GameState gameState;
	private float stateTime;

	public void init(boolean isTwoPlayer) {
		this.isTwoPlayer = isTwoPlayer;
		overtakingSound = Kernel.sounds.get("sounds/overtake");
		startSound = Kernel.sounds.get("sounds/startrace");
		level = -1;
		changeState(GameState.START_LEVEL);
	}

	private void changeState(GameState newState) {
		gameState = newState;
		stateTime = Kernel.time.time;
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

	public void update() {
		switch (gameState) {
		case START_LEVEL:
			startNewLevel();
			break;
		case PLAY_LEVEL:
			updateCars();
			updateCollisions();
			checkForOvertaking();
			checkForWinCondition();
			checkForLoseCondition();
			updateScores();
			break;
		case END_LEVEL:
			updateCars();
			if (Kernel.time.time >= stateTime + END_LEVEL_TIMEOUT) {
				tearDownLevel();
				changeState(GameState.START_LEVEL);
			}
			break;
		case WON_GAME:
			updateCars();
			break;
		case LOST_GAME:
			updateCars();
			break;
		}
	}

	public boolean isStarting() {
		return Kernel.time.time < startingTime;
	}
	
	private void startNewLevel() {
		if (level + 1 >= levels.length) {
			changeState(GameState.WON_GAME);
			return;
		}

		level++;
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
		startingTime = Kernel.time.time + 2.0f;
		startSound.play();
		changeState(GameState.PLAY_LEVEL);
	}

	private void tearDownLevel() {
		App.broker.unsubscribeAll(PlayerWinEvent.class);
		App.broker.unsubscribeAll(PlayerLoseEvent.class);
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
			changeState(GameState.END_LEVEL);
			App.broker.publish(new PlayerWinEvent(1));
		}
		else if (player2 != null && player2.lap() > laps[level]) {
			changeState(GameState.END_LEVEL);
			App.broker.publish(new PlayerWinEvent(2));
		}
	}
	
	private void checkForLoseCondition() {
		boolean isGameOver = player1.health() < 0 && (player2 == null || player2.health() < 0);
		if (isGameOver) {
			changeState(GameState.LOST_GAME);
			App.broker.publish(new PlayerLoseEvent());
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
		return gameState == GameState.LOST_GAME;
	}

	public boolean isGameWon() {
		return gameState == GameState.WON_GAME;
	}
	
	public boolean canQuit() {
		return (gameState == GameState.LOST_GAME || gameState == GameState.WON_GAME) &&
				Kernel.time.time > stateTime + GAME_OVER_TIMEOUT;
	}
}
