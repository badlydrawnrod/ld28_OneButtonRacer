package ld28;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

class TurnPiece extends TrackPiece {
	private Vector2 centre;
	private float startAngle;
	private float endAngle;
	private float radius;
	private boolean isClockwise;

	public TurnPiece(int layer, Vector2 pos, float startAngle, float endAngle, float radius) {
		super(layer);
		this.startPos = new Vector2(pos);
		this.startAngle = normalizeAngle(startAngle);
		this.endAngle = normalizeAngle(endAngle);
		this.isClockwise = normalizeAngle(endAngle - startAngle) < 0;
		this.radius = radius;
		
		// Find the centre of the circle that this track piece is turning around.
		centre = new Vector2();
		if (isClockwise) {
			float x = pos.x + radius * MathUtils.cos(startAngle - MathUtils.PI / 2);
			float y = pos.y + radius * MathUtils.sin(startAngle - MathUtils.PI / 2);
			centre.set(x, y);
			float endX = centre.x + radius * MathUtils.cos(endAngle + MathUtils.PI / 2);
			float endY = centre.y + radius * MathUtils.sin(endAngle + MathUtils.PI / 2);
			this.endPos = new Vector2(endX, endY);
		}
		else {
			float x = pos.x + radius * MathUtils.cos(startAngle + MathUtils.PI / 2);
			float y = pos.y + radius * MathUtils.sin(startAngle + MathUtils.PI / 2);
			centre.set(x, y);
			float endX = centre.x + radius * MathUtils.cos(endAngle - MathUtils.PI / 2);
			float endY = centre.y + radius * MathUtils.sin(endAngle - MathUtils.PI / 2);
			this.endPos = new Vector2(endX, endY);
		}
	}
	
	private float normalizeAngle(float angle) {
		while (angle < -MathUtils.PI) {
			angle += MathUtils.PI2;
		}
		while (angle >= MathUtils.PI) {
			angle -= MathUtils.PI2;
		}
		return angle;
	}
	
	@Override
	public float length(float lane) {
		if (!isClockwise) {
			return normalizeAngle(endAngle - startAngle) * (radius + lane);
		}
		else {
			return normalizeAngle(startAngle - endAngle) * (radius - lane);
		}
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
		if (!isClockwise) {
			float angle = length / (radius + lane);
			return normalizeAngle(startAngle + angle);
		}
		else {
			float angle = length / (radius - lane);
			return normalizeAngle(startAngle - angle);
		}
	}

	@Override
	public Vector2 positionAt(float length, float lane) {
		Vector2 v = new Vector2(centre);
		float angle = angleAt(length, lane);
		if (!isClockwise) {
			angle -= MathUtils.PI / 2;
			float laneRadius = radius + lane;
			v.x += laneRadius * MathUtils.cos(angle);
			v.y += laneRadius * MathUtils.sin(angle);
		}
		else {
			angle += MathUtils.PI / 2;
			float laneRadius = radius - lane;
			v.x += laneRadius * MathUtils.cos(angle);
			v.y += laneRadius * MathUtils.sin(angle);
		}
		return v;
	}
}