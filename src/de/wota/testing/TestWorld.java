package de.wota.testing;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import de.wota.Player;
import de.wota.ai.DemoAntAI;
import de.wota.ai.DummyHillAI;
import de.wota.gameobjects.*;
import de.wota.gameobjects.caste.*;
import de.wota.utility.Vector;

public class TestWorld {

	public static GameWorld testWorld() throws InstantiationException, IllegalAccessException {
		GameWorld world = new GameWorld();
		for (int i = 0; i < 2; i++) {
			Player player = new Player(new Vector(100 + i * 200, 100 + i * 200), DummyQueenAI.class);
			for (int j = 0; j < 0; j++) {
				AntObject antObject = new AntObject(new Vector(j * 10, 20 + i * 20), Caste.Gatherer, MoveAI.class, player);
				world.addAntObject(antObject, player);
			}
			world.players.add(player);
		}
		return world;
	}

}