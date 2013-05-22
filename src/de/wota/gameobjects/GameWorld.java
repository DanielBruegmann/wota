package de.wota.gameobjects;

import java.lang.Math;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.wota.gamemaster.AILoader;
import de.wota.gamemaster.AbstractLogger;
import de.wota.gameobjects.Parameters;


import de.wota.utility.Vector;

/**
 * Enthält alle Elemente der Spielwelt.
 * 
 * @author pascal
 */
public class GameWorld {
	private final List<Player> players = new LinkedList<Player>();
	private final LinkedList<SugarObject> sugarObjects = new LinkedList<SugarObject>();
	
	private List<AbstractLogger> registeredLoggers = new LinkedList<AbstractLogger>();

	private SpacePartioning spacePartioning = new SpacePartioning(
			Parameters.SIZE_X, Parameters.SIZE_Y,
			maximumSight());
	
	private static double maximumSight() {
		double maximum = 0.0;
		for (Caste caste : Caste.values()) {
			if (caste.SIGHT_RANGE > maximum) {
				maximum = caste.SIGHT_RANGE;
			}
			if (caste.HEARING_RANGE > maximum) {
				maximum = caste.HEARING_RANGE;
			}
		}
		return maximum;
	}
	
	public void addSugarObject(SugarObject sugarObject) {
		sugarObjects.add(sugarObject);
		spacePartioning.addSugarObject(sugarObject);
	}
	
	/** Do not modify the list! Use addSugarObject instead */
	public List<SugarObject> getSugarObjects() {
		return sugarObjects;
	}
		
	public void addPlayer(Player player) {
		notifyLoggers(AbstractLogger.LogEventType.PLAYER_REGISTERED);

		players.add(player);
	}

	private static int nextPlayerId = 0; // TODO can this somehow go into Player?
	
	public class Player {
		public final List<AntObject> antObjects = new LinkedList<AntObject>();
		public final HillObject hillObject;
		public final QueenObject queenObject;

		public final String name;

		private final int id;

		public int getId() {
			return id;
		}

		// TODO make this private and change addPlayer
		public Player(Vector position, Class<? extends QueenAI> queenAIClass) {
			hillObject = new HillObject(position, this);
			queenObject = new QueenObject(position, queenAIClass, this);
			
			addAntObject(queenObject);
		
			name = AILoader.getAIName(queenAIClass);

			// TODO fail early w.r.t. to ants, too, by creating one to test ant
			// creation
			id = nextPlayerId;
			nextPlayerId++;
		}
		
		public void addAntObject(AntObject antObject) {
			antObjects.add(antObject);
			spacePartioning.addAntObject(antObject);
		}
	}
	
	public void tick() {		
		notifyLoggers(AbstractLogger.LogEventType.TICK);
		
		// can be removed as soon as SpacePartitioning is well tested!
		if (Parameters.DEBUG) {
			System.out.println("SpacePartitioning: " + spacePartioning.totalNumberOfAntObjects());
			System.out.println("Total number: " + totalNumberOfAntObjects());
		}

		// create Ants for all AntObjects and the QueenObject and sets them in
		// the AntAI (the latter happens in AntObject.createAnt() )
		// also create Sugar for SugarObjects
		for (Player player : players) {
			for (AntObject antObject : player.antObjects) {
				antObject.createAnt();
			}
			player.hillObject.createHill();
		}

		for (SugarObject sugarObject : sugarObjects) {
			sugarObject.createSugar();
		}
		
		// The MessageObjects don't need a "createMessage", because one can
		// construct the Message instance when the
		// MessageObject instance is constructed.

		// call tick for all AntObjects
		for (Player player : players) {			
			for (AntObject antObject : player.antObjects) {
				List<Ant> visibleAnts = new LinkedList<Ant>();
				List<Sugar> visibleSugar = new LinkedList<Sugar>();
				List<Hill> visibleHills = new LinkedList<Hill>();
				List<Message> audibleMessages = new LinkedList<Message>();

				for (AntObject visibleAntObject : 
					spacePartioning.antObjectsInsideCircle(antObject.getCaste().SIGHT_RANGE, antObject.getPosition())) {
					if (visibleAntObject != antObject) {
						visibleAnts.add(visibleAntObject.getAnt());
					}
				}

				for (SugarObject visibleSugarObject : 
					spacePartioning.sugarObjectsInsideCircle(antObject.getCaste().SIGHT_RANGE, antObject.getPosition())) {
					visibleSugar.add(visibleSugarObject.getSugar());
				}
				
				for (HillObject visibleHillObject :
					spacePartioning.hillObjectsInsideCircle(antObject.getCaste().SIGHT_RANGE, antObject.getPosition())) {
					visibleHills.add(visibleHillObject.getHill());
				}				
				
				for (MessageObject audibleMessageObject : 
					spacePartioning.messageObjectsInsideCircle(antObject.getCaste().HEARING_RANGE, antObject.getPosition())) {
					audibleMessages.add(audibleMessageObject.getMessage());
				}
				antObject.tick(visibleAnts, visibleSugar, visibleHills, audibleMessages);
			}
		}

		// execute all actions, ants get created
		for (Player player : players) {
			for (AntObject antObject : player.antObjects) {
				executeActionExceptMovement(antObject);
			} 
		}
		
		for (Player player : players) {
			for (AntObject antObject : player.antObjects) {
				executeMovement(antObject);
			}

			// order does matter since the queen creates new ants!
			executeAntOrders(player.queenObject);
		}
		
		// Includes discarding the MessageObject instances.
		spacePartioning.update(); 
				

		// Let ants die!
		for (Player player : players) {
			for (Iterator<AntObject> antObjectIter = player.antObjects.iterator();
					antObjectIter.hasNext();) {
				AntObject maybeDead = antObjectIter.next();
				if (maybeDead.isDead()) {
					// hat neue Aktionen erzeugt.
					//executeLastWill(maybeDead);
					antObjectIter.remove();
					spacePartioning.removeAntObject(maybeDead);
				}
			}
		}
		
		// remove empty sugar sources and decrease sugarObject.ticksToWait
		for (Iterator<SugarObject> sugarObjectIter = sugarObjects.iterator();
				sugarObjectIter.hasNext();) {
			SugarObject sugarObject = sugarObjectIter.next();
			
			sugarObject.tick();
			
			// remove if empty 
			if (sugarObject.getAmount() <= 0) {
				sugarObjectIter.remove();
				spacePartioning.removeSugarObject(sugarObject);
			}
		}
		
	}

	private static void executeAntOrders(QueenObject queenObject) {
		List<AntOrder> antOrders = queenObject.getAntOrders();
		Iterator<AntOrder> iterator = antOrders.iterator();
		final Player player = queenObject.player;
		
		while (iterator.hasNext()) {
			AntOrder antOrder = iterator.next();
		
			if (Parameters.ANT_COST <= player.hillObject.getStoredFood()) {
				player.hillObject.changeStoredFoodBy(-Parameters.ANT_COST);
				
				AntObject antObject = new AntObject(
						queenObject.player.hillObject.getPosition(),
						antOrder.getCaste(), antOrder.getAntAIClass(),
						queenObject.player);
				queenObject.player.addAntObject(antObject);
			}
		}
	}

	/** Führt die Aktion für das AntObject aus. 
	 * Beinhaltet Zucker abliefern.
	 *  */
	private void executeActionExceptMovement(AntObject actor) {
		Action action = actor.getAction();

		// Attack
		Ant targetAnt = action.attackTarget;
		actor.isAttacking = false;
		if (targetAnt != null) {
			if (Parameters.distance(targetAnt.antObject.getPosition(), actor.getPosition()) 
					<= Parameters.ATTACK_RANGE) {
				
				// main damage:
				AntObject target = targetAnt.antObject;
				target.takesDamage(actor.getCaste().ATTACK);
				actor.isAttacking = true;
				
				// collateral damage:
				for (AntObject closeAntObject : spacePartioning.antObjectsInsideCircle(Parameters.ATTACK_RANGE, target.getPosition())) {
					if (closeAntObject != target && closeAntObject.player != actor.player) {
						// TODO: Find a good rate, maybe depending on distance.
						closeAntObject.takesDamage(actor.getCaste().ATTACK*Parameters.COLLATERAL_DAMAGE_FACTOR);
					}
				}
			}
		}

		// Drop sugar at the hill.
		// TODO possible optimization: Use space partitioning for dropping sugar at the hill, don't test for all ants.
		if (Parameters.distance(actor.player.hillObject.getPosition(), actor.getPosition())
				<= Parameters.HILL_RADIUS) {
			actor.player.hillObject.changeStoredFoodBy(actor.getSugarCarry());
			actor.dropSugar();
		}
		
		// or drop sugar if desired
		if (action.dropItem == true) {
			actor.dropSugar();
		}
		
		// Pick up sugar
		Sugar sugar = action.sugarTarget;
		if (sugar != null) {
			if (Parameters.distance(actor.getPosition(),sugar.sugarObject.getPosition())
					<= sugar.sugarObject.getRadius()) {
				actor.pickUpSugar(sugar.sugarObject);
			}
		}
		
		// Messages
		handleMessages(actor, action);
	}
	
	private void executeMovement(AntObject actor) {
		Action action = actor.getAction();

		if (actor.isAttacking) {
			actor.move(action.movement.boundLengthBy(actor.getCaste().SPEED_WHILE_ATTACKING));
		} else if (actor.getSugarCarry() > 0) {
			actor.move(action.movement.boundLengthBy(actor.getCaste().SPEED_WHILE_CARRYING_SUGAR));
		} else {
			actor.move(action.movement.boundLengthBy(actor.getCaste().SPEED));
		}
	}

	/*
	/** wird nur aufgerufen bevor die Ant stirbt -> kein Angriff mehr
	private void executeLastWill(AntObject actor) {
		Action action = actor.getAction();

		// Messages
		handleMessages(actor, action);
	}
	*/
	
	/** tests if victory condition is fulfilled and notifies the Logger
	 * Victory condition: is the queen alive? */
	public Player checkVictoryCondition() {
		List<Player> possibleWinners = new LinkedList<Player>(players);
		for (Player player : players) {
			if (player.queenObject.isDead()) {
				possibleWinners.remove(player);
			}
		}
		if (possibleWinners.size() == 1) {
			return possibleWinners.get(0);
		}
		else
			return null;
	}

	private void handleMessages(AntObject actor, Action action) {
		if (action.messageObject != null) {
			spacePartioning.addMessageObject(action.messageObject);
			Message message = action.messageObject.getMessage();
			if (Parameters.DEBUG)
				System.out.println("\"" + message.content + "\" sagt "
						+ message.sender + ".");
		}
	}

	public void registerLogger(AbstractLogger logger) {
		registeredLoggers.add(logger);
	}

	private void notifyLoggers(AbstractLogger.LogEventType event) {
		for (AbstractLogger logger : registeredLoggers)
			logger.log(event);
	}

	public int totalNumberOfAntObjects() {
		int n = 0;
		for (Player player : players) {
			n += player.antObjects.size();
		}
		return n;
	}
	
	public List<Player> getPlayers() {
		return Collections.unmodifiableList(players); // TODO is it possible to ensure this statically?
	}
}
