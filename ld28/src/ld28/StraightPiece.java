package ld28;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

class StraightPiece extends TrackPiece {
	private float angle;
	private float length;
	
	public void set(int layer, Vector2 pos, float angle, float length) {
		setLayer(layer);
		setStartPos(pos.x, pos.y);
		this.angle = angle;
		this.length = length;
		float endX = startPos.x + MathUtils.cos(angle) * length;
		float endY = startPos.y + MathUtils.sin(angle) * length;
		setEndPos(endX, endY);
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