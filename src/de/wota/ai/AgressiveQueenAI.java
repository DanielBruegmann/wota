package de.wota.ai;

import de.wota.gamemaster.AIInformation;
import de.wota.gameobjects.Caste;
import de.wota.gameobjects.QueenAI;
import de.wota.utility.SeededRandomizer;

@AIInformation(creator = "WotA-Team", name = "AgressiveQueen")
public class AgressiveQueenAI extends QueenAI {
	
	@Override
	public void tick() {
		//System.out.println("Player: " + self.playerID + " has Queen has " + self.health);
		//createAnt(Caste.Gatherer, MoveAI.class);
		if (false)//(SeededRandomizer.nextInt(2) == 0)
			createAnt(Caste.Gatherer, SolitaryAI.class);
		else
			createAnt(Caste.Gatherer, SoldierAI.class);
	}
}
