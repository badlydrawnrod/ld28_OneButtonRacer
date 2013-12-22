package ld28;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class TestTurnPiece {
	private static final float EPSILON = 0.00001f;

	@Test
	public void testCentreWhenTurningAnticlockwise() {
		Vector2 pos = new Vector2(0, 0);
		float startAngle = 0.0f;
		float endAngle = MathUtils.PI / 2;
		float radius = 100;
		TurnPiece turnPiece = new TurnPiece(0, pos, startAngle, endAngle, radius);
		assertEquals(radius * MathUtils.PI / 2, turnPiece.length(0), EPSILON);
		
		assertEquals(MathUtils.PI / 8, turnPiece.angleAt(turnPiece.length(0) / 4, 0), EPSILON);
		
		float expectedX = 100;
		float expectedY = 100;
		float actualX = turnPiece.positionAtEnd(0).x;
		float actualY = turnPiece.positionAtEnd(0).y;
		assertEquals(expectedX, actualX, EPSILON);
		assertEquals(expectedY, actualY, EPSILON);
	}
	
	@Test
	public void testCentreWhenTurningClockwise() {
		Vector2 pos = new Vector2(0, 0);
		float startAngle = 0.0f;
		float endAngle = -MathUtils.PI / 2;
		float radius = 100;
		TurnPiece turnPiece = new TurnPiece(0, pos, startAngle, endAngle, radius);
		assertEquals(radius * MathUtils.PI / 2, turnPiece.length(0), EPSILON);
		
		assertEquals(-MathUtils.PI / 8, turnPiece.angleAt(turnPiece.length(0) / 4, 0), EPSILON);
		
		float expectedX = 100;
		float expectedY = -100;
		float actualX = turnPiece.positionAtEnd(0).x;
		float actualY = turnPiece.positionAtEnd(0).y;
		assertEquals(expectedX, actualX, EPSILON);
		assertEquals(expectedY, actualY, EPSILON);
	}
	
	@Test
	public void testLeftLaneWhenTurningAntiClockwise() {
		Vector2 pos = new Vector2(0, 0);
		float startAngle = 0.0f;
		float endAngle = MathUtils.PI / 2;
		float radius = 100;
		TurnPiece turnPiece = new TurnPiece(0, pos, startAngle, endAngle, radius);
		float lane = -10.0f;
		assertEquals((radius + lane) * MathUtils.PI / 2, turnPiece.length(lane), EPSILON);
		
		assertEquals(MathUtils.PI / 8, turnPiece.angleAt(turnPiece.length(lane) / 4, lane), EPSILON);
		
		float expectedX = 90;
		float expectedY = 100;
		float actualX = turnPiece.positionAtEnd(lane).x;
		float actualY = turnPiece.positionAtEnd(lane).y;
		assertEquals(expectedX, actualX, EPSILON);
		assertEquals(expectedY, actualY, EPSILON);
	}
	
	@Test
	public void testLeftLaneWhenTurningClockwise() {
		Vector2 pos = new Vector2(0, 0);
		float startAngle = 0.0f;
		float endAngle = -MathUtils.PI / 2;
		float radius = 100;
		TurnPiece turnPiece = new TurnPiece(0, pos, startAngle, endAngle, radius);
		float lane = -10.0f;
		assertEquals((radius - lane) * MathUtils.PI / 2, turnPiece.length(lane), EPSILON);
		
		assertEquals(-MathUtils.PI / 8, turnPiece.angleAt(turnPiece.length(lane) / 4, lane), EPSILON);
		
		float expectedX = 110;
		float expectedY = -100;
		float actualX = turnPiece.positionAtEnd(lane).x;
		float actualY = turnPiece.positionAtEnd(lane).y;
		assertEquals(expectedX, actualX, EPSILON);
		assertEquals(expectedY, actualY, EPSILON);
	}
	
	@Test
	public void testRightLaneWhenTurningAntiClockwise() {
		Vector2 pos = new Vector2(0, 0);
		float startAngle = 0.0f;
		float endAngle = MathUtils.PI / 2;
		float radius = 100;
		TurnPiece turnPiece = new TurnPiece(0, pos, startAngle, endAngle, radius);
		float lane = 10.0f;
		assertEquals((radius + lane) * MathUtils.PI / 2, turnPiece.length(lane), EPSILON);
		
		assertEquals(MathUtils.PI / 8, turnPiece.angleAt(turnPiece.length(lane) / 4, lane), EPSILON);
		
		float expectedX = 110;
		float expectedY = 100;
		float actualX = turnPiece.positionAtEnd(lane).x;
		float actualY = turnPiece.positionAtEnd(lane).y;
		assertEquals(expectedX, actualX, EPSILON);
		assertEquals(expectedY, actualY, EPSILON);
	}

	@Test
	public void testRightLaneWhenTurningClockwise() {
		Vector2 pos = new Vector2(0, 0);
		float startAngle = 0.0f;
		float endAngle = -MathUtils.PI / 2;
		float radius = 100;
		TurnPiece turnPiece = new TurnPiece(0, pos, startAngle, endAngle, radius);
		float lane = 10.0f;
		assertEquals((radius - lane) * MathUtils.PI / 2, turnPiece.length(lane), EPSILON);
		
		assertEquals(-MathUtils.PI / 8, turnPiece.angleAt(turnPiece.length(lane) / 4, lane), EPSILON);
		
		float expectedX = 90;
		float expectedY = -100;
		float actualX = turnPiece.positionAtEnd(lane).x;
		float actualY = turnPiece.positionAtEnd(lane).y;
		assertEquals(expectedX, actualX, EPSILON);
		assertEquals(expectedY, actualY, EPSILON);
	}
}
