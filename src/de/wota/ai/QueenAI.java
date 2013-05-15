package de.wota.ai;

import java.util.LinkedList;
import java.util.List;
import de.wota.AntOrder;
import de.wota.gameobjects.AntAI;
import de.wota.gameobjects.Caste;

/**
 * QueenAI ist wie AntAI + hat die Möglichkeit Einheiten zu ordern
 * @author pascal
 */
public abstract class QueenAI extends AntAI {
	private List<AntOrder> antOrders;
	
	protected void createAnt(Caste caste, Class<? extends AntAI> antAIClass) {
		AntOrder antOrder = new AntOrder(caste, antAIClass);
		antOrders.add(antOrder);
	}
	
	/** CAUTION! THIS METHOD DELETES THE AntOrders */
	public List<AntOrder> popAntOrders() {
		List<AntOrder> returnAntOrders = antOrders;
		antOrders = new LinkedList<AntOrder>();
		return returnAntOrders;
	}
	
	/*
	 *  Pascal: The constructor must not have any parameters or
	 *  newInstance() will not work. 
	 */
	public QueenAI() { 
		antOrders = new LinkedList<AntOrder>();
	}
	
}
