package ld28;

import java.util.List;

import ld28.World.PlayerLoseEvent;
import ld28.World.PlayerWinEvent;
import ldtk.Kernel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;

class Car {
	private static final String TAG = "Car";
	
	protected static final float MAX_SLOT = 2;
	private static final float HALF_WIDTH = 12;
	private static final float HALF_HEIGHT = 6;

	protected static final float LANE_WIDTH = 16.0f;
	
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
		this.lane = currentSlot * LANE_WIDTH;
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
		App.broker.subscribe(PlayerLoseEvent.class, new Subscriber() {
			@Override
			public void onEvent(Event event) {
				// Prevent ourselves from receiving this event type again.
				App.broker.unsubscribe(PlayerWinEvent.class, this);
				Gdx.app.log(TAG, "PlayerLoseEvent");
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
			float newLane = LANE_WIDTH * currentSlot;
			TrackPiece piece = piece();
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