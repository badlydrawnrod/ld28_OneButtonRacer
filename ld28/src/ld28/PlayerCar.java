package ld28;

import ldtk.Kernel;
import ldtk.Sound;

import com.badlogic.gdx.Gdx;

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
				float newLane = LANE_WIDTH * currentSlot;
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