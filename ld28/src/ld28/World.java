package ld28;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;


public class World {

	private String oval = "ssLLsLLssllllllllssLLsLLss";
	private TrackBuilder track;
	
	public World() {
		track = generateTrack(oval);
	}
	
	public TrackBuilder track() {
		return track;
	}

	private TrackBuilder generateTrack(String trackDef) {
		TrackBuilder trackBuilder = new TrackBuilder(0, -200, 0);
		for (int i = 0, n = trackDef.length(); i < n; i++) {
			char c = trackDef.charAt(i);
			switch (c) {
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
	}
}

class TrackBuilder {
	private Vector2 pos;
	private float angle;

	private List<TrackPiece> pieces;
	
	public TrackBuilder() {
		this(0, 0, 0);
	}
	
	public TrackBuilder(float x, float y, float angle) {
		this.pos = new Vector2(x, y);
		this.angle = angle;
		pieces = new ArrayList<TrackPiece>();
	}
	
	public List<TrackPiece> pieces() {
		return pieces;
	}
	
	public TrackBuilder addStraight(float length) {
		TrackPiece piece = new StraightPiece(pos, angle, length);
		pos.set(piece.positionAtEnd());
		pieces.add(piece);
		return this;
	}
	
	public TrackBuilder addTurn(float angle, float radius) {
		TrackPiece piece = new TurnPiece(pos, this.angle, this.angle + angle, radius);
		pos.set(piece.positionAtEnd());
		this.angle += angle;
		pieces.add(piece);
		return this;
	}
}


abstract class TrackPiece {
	protected Vector2 startPos;
	protected Vector2 endPos;

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
	
	public StraightPiece(Vector2 pos, float angle, float length) {
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

	public TurnPiece(Vector2 pos, float startAngle, float endAngle, float radius) {
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
