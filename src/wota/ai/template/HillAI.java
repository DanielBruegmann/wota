/**
 * 
 */
package wota.ai.template;

import wota.gamemaster.AIInformation;
import wota.gameobjects.Caste;

/**
 *  Give your information about this HillAI here.
 */
@AIInformation(creator = "Anonymous", name = "Anonymous's AI")
public class HillAI extends wota.gameobjects.HillAI {

	/*
	 * your Hill is not able to move but can
	 * communicate and create new ants. 
	 * 
	 * You can create new ants with				createAnt(caste, antAIClass)		
	 * e.g. if you want a gatherer and the AI
	 * you want use is called SuperGathererAI	createAnt(Caste.Gatherer, SuperGathererAI.class)
	 * 
	 */
	@Override
	public void tick() throws Exception {
		
		/* 
		 * try to create an Ant using the TemplateAI in every tick
		 * if you don't have enough food to create the ant your call
		 * will be ignored
		 */
		
		createAnt(Caste.Gatherer, TemplateAI.class);
		
	}

}
