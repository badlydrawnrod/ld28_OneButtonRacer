package ld28;

import java.util.List;

import ldtk.Camera;
import ldtk.Kernel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class WorldRenderer {

	private World world;
	private Camera gameCam;
	private TrackRenderer trackRenderer;

	public WorldRenderer(World world, Camera gameCam) {
		this.world = world;
		this.gameCam = gameCam;
		trackRenderer = new TrackRenderer(world.track());
	}

	public void setup() {
	}

	public void draw() {
		gameCam.activate();
		trackRenderer.draw();
	}
}


class TrackRenderer {

	private static final int VERTS_PER_QUAD = 20;
	private static final int QUADS_PER_PIECE = 4;
	private static final float TRACK_WIDTH = 60;

	private int quadIndex;
	private float[] verts;
	private Texture texture;
	
	public TrackRenderer(TrackBuilder track) {
		generateVerts(track.pieces());
		texture = Kernel.images.get("textures/track").region().getTexture();
	}
	
	private void generateVerts(List<TrackPiece> pieces) {
		verts = new float[pieces.size() * QUADS_PER_PIECE * VERTS_PER_QUAD];
		quadIndex = 0;
		
		Vector2 tl = new Vector2();
		Vector2 bl = new Vector2();
		Vector2 br = new Vector2();
		Vector2 tr = new Vector2();
		float leftBorder = -TRACK_WIDTH / 2;
		float rightBorder = TRACK_WIDTH / 2;
		for (TrackPiece piece : pieces) {
			float leftLength = piece.length(leftBorder);
			float rightLength = piece.length(rightBorder);
			float leftStep = leftLength / QUADS_PER_PIECE;
			float rightStep = rightLength / QUADS_PER_PIECE;
			for (int i = 0; i < QUADS_PER_PIECE; i++) {
				tl.set(piece.positionAt(leftStep * i, leftBorder));
				bl.set(piece.positionAt(rightStep * i, rightBorder));
				tr.set(piece.positionAt(leftStep * (i + 1), leftBorder));
				br.set(piece.positionAt(rightStep * (i + 1), rightBorder));
				addQuad(tl, bl, br, tr);
			}
		}
	}

	private void addQuad(Vector2 tl, Vector2 bl, Vector2 br, Vector2 tr) {
//		System.out.println("Adding quad: " + tl + ", " + bl + ", " + br + ", " + tr);
		float colorBits = Color.WHITE.toFloatBits();
		int i = quadIndex * VERTS_PER_QUAD;

		// Top left.
		verts[i + 0] = tl.x;			// x
		verts[i + 1] = tl.y;			// y
		verts[i + 2] = colorBits;		// colour
		verts[i + 3] = 0;				// u
		verts[i + 4] = 0;				// v
		
		// Bottom left.
		verts[i + 5] = bl.x;			// x
		verts[i + 6] = bl.y;			// y
		verts[i + 7] = colorBits;		// colour
		verts[i + 8] = 0;				// u
		verts[i + 9] = 1;				// v
		
		// Bottom right.
		verts[i + 10] = br.x;			// x
		verts[i + 11] = br.y;			// y
		verts[i + 12] = colorBits;		// colour
		verts[i + 13] = 1;				// u
		verts[i + 14] = 1;				// v
		
		// Top right.
		verts[i + 15] = tr.x;			// x
		verts[i + 16] = tr.y;			// y
		verts[i + 17] = colorBits;		// colour
		verts[i + 18] = 1;				// u
		verts[i + 19] = 0;				// v
		
		quadIndex++;
	}
	
	public void draw() {
		Kernel.batch.draw(texture, verts, 0, verts.length);
	}
}
