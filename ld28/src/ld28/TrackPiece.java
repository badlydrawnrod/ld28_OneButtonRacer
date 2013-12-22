package ld28;

import com.badlogic.gdx.math.Vector2;

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
		return positionAt(0, lane);
	}
	
	public Vector2 positionAtEnd(float lane) {
		return positionAt(length(lane), lane);
	}
	
	public Vector2 positionAtStart() {
		return startPos;
	}
	
	public Vector2 positionAtEnd() {
		return endPos;
	}
}