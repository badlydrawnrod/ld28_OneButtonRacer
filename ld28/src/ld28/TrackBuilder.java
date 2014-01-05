package ld28;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

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