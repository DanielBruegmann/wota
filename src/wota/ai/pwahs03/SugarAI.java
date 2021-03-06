/**
 * 
 */
package wota.ai.pwahs03; /* <-- change this to wota.ai.YOUR_AI_NAME
 							  * make sure your file is in the folder /de/wota/ai/YOUR_AI_NAME
 							  * and has the same name as the class (change TemplateAI to
 							  * the name of your choice) 
 							  */

import wota.gamemaster.AIInformation;
import wota.gameobjects.*;
import wota.utility.SeededRandomizer;
import wota.utility.Vector;

import java.util.List;
import java.util.LinkedList;


/**
 * Put a description of you AI here.
 */
// Here, you may use spaces, etc., unlike in the package path wota.ai.YOUR_AI_NAME:
@AIInformation(creator = "Anonymous", name = "Anonymous's AI")
public class SugarAI extends AntAI {
	/* 
	 * tick() gets called in every step of the game.
	 * You have to call methods of AntAI to specify
	 * the desired action.
	 * 
 	 * you have access to
	 * the Ants you see: 								visibleAnts
	 * the sources of sugar you see: 					visibleSugar
	 * the Hills you see: 								visibleHills
	 * 
	 * you can move using one of the methods starting
	 * with move. For example to move ahead (in the
	 * direction of the last tick) call					moveAhead()
	 * 
	 * to attack other ants use methods starting with	attack(otherAnt)
	 * attack, e.g.		
	 * 
	 * if you want a List containing only the hostile	
	 * ants you can see, call							visibleEnemies()
	 * 
	 * communication is possible with					talk(content)
	 * where content is an integer value with is
	 * contained in the message
	 * 
	 * To measure the distance between two objects
	 * (you must be able to see both of them), call		vectorBetween(start, end).length()
	 * 
	 * to get information about yourself, for example
	 * your health points								self.health
	 * 
	 * to obtain random numbers use	SeededRandomizer
	 * e.g. a random elment of {0,1,2}					SeededRandomizer.getInt(3)
	 * 
	 * to iterate over a list (e.g. visibleAnts) use	for (Ant ant : visibleAnts) {
	 * 														// ant is an element of visibleAnts
	 * 													}
	 * 
	 * A full list of possible actions and how to get information is available at 
	 * doc/de/wota/gameobjects/AntAI.html
	 */
	
	int time = 0;
	int initialTime = 0;
	double dir = -1;
	
	boolean fight = false;
	boolean run = false;
	
	double rundir = 0;
	
	List<LinkedList<Snapshot>> hills = new LinkedList<LinkedList<Snapshot>>();
	int []indices = new int[HillAI.NR_HILLS];
	int nr_hill = 0;
	
	double last_health = -1;
	
	@Override
	public void tick() throws Exception {
				
		time++;
		
		if (!fight){
			
			//dont fight:
			
			first_cry();
			
			listen();
			
			handle_sugar();

			have_a_look_around();
			
			shout();
			
			//distract();
			
			run_away();
			
			decide_fighting();
			
		}else{ //FIGHT!
			ATTACK1111111111111111111111();
		}
	}
	
	public void listen(){
		for(AntMessage m: audibleAntMessages){
			switch(m.content){
			case HillAI.SUGAR:
				if (!HillAI.contained(hills.get(HillAI.NOSUGAR_IND),m.contentSugar)){
					HillAI.add_if_not_contained(hills.get(HillAI.SUGAR_IND),m.contentSugar);
				}
				break;
			case HillAI.FULLSUGAR:
				if (!HillAI.contained(hills.get(HillAI.NOSUGAR_IND),m.contentSugar)){
					if (HillAI.add_if_not_contained(hills.get(HillAI.FULLSUGAR_IND),m.contentSugar)){
						HillAI.delete(hills.get(HillAI.SUGAR_IND),m.contentSugar);
					}
				}
				break;
			case HillAI.NOSUGAR:
				if (HillAI.add_if_not_contained(hills.get(HillAI.NOSUGAR_IND),m.contentSugar)){
					HillAI.delete(hills.get(HillAI.SUGAR_IND),m.contentSugar);
					HillAI.delete(hills.get(HillAI.FULLSUGAR_IND),m.contentSugar);
				}
				break;
			case HillAI.HILL:
				HillAI.add_if_not_contained(hills.get(HillAI.HILL_IND),m.contentHill);
				break;
			}
			//System.out.printf("%d: %d\n", self.id, m.content);
		}
	}

	public void have_a_look_around(){
		for(Hill hill: visibleHills){
			HillAI.add_if_not_contained(hills.get(HillAI.HILL_IND),hill);
		}
	}
	
	public void handle_sugar(){
		if (self.sugarCarry>0) {	//have sugar => go home:
			moveHome();
		} else {					//dont have sugar:
			if (visibleSugar.size() == 0) {		//dont see sugar: 
				
				//move, where you were going:
				moveInDirection(dir);
				//if you know about sugar:
				//if (hills.get(0) == null) System.out.print("null\n");
				if (hills.get(HillAI.SUGAR_IND).size()>0 && time + initialTime > HillAI.STOP_LOOKING){
					Snapshot target = find_optimal_sugar_hill();
					if (target != null){
						moveToward(target);
						if (vectorBetween(self, target).length() < self.caste.SIGHT_RANGE) {
							HillAI.add_if_not_contained(hills.get(HillAI.NOSUGAR_IND), target);
							HillAI.delete(hills.get(HillAI.SUGAR_IND), target);
							HillAI.delete(hills.get(HillAI.FULLSUGAR_IND), target);
						}
					}
				}
			}
			else {		//see sugar:
				Sugar sugar = closest(visibleSugar);
				moveToward(sugar); // otherwise move to the first element in the List of visible sugar
				think_about_picking_up(sugar);
				if (!HillAI.contained(hills.get(HillAI.FULLSUGAR_IND), sugar)){
					HillAI.add_if_not_contained(hills.get(HillAI.SUGAR_IND), sugar);
				}
			}
		}
	}
	
	public void think_about_picking_up(Sugar sugar){
		int count = 0;
		for(Ant a: visibleFriends()){
			if (a.caste == Caste.Gatherer){
				if (a.id < self.id) count++;
			}
		}
		if (count < HillAI.QUEUELENGTH) {
			pickUpSugar(sugar);
		}
		else{
			if (count < sugar.amount / (Caste.Gatherer.MAX_SUGAR_CARRY)){
				Vector pos = sugar.position;
				pos = Vector.add(pos, 
								(new Vector(Math.sin(dir),Math.cos(dir)).scale(
										parameters.ATTACK_RANGE* HillAI.COMBAT_RADIUS)));
				moveToward(pos);
				if (visibleEnemies().size()>0) {	
					attack(closest(visibleEnemies()));
				}
			}else{
				if (HillAI.add_if_not_contained(hills.get(HillAI.FULLSUGAR_IND), sugar)){
					HillAI.delete(hills.get(HillAI.SUGAR_IND), sugar);
				}
				Snapshot target = find_optimal_sugar_hill();
				if (target != null) {
					moveToward(target);
				}
			}
		}
	}
	
	public void shout(){
		do{
			nr_hill = (nr_hill + 1) % HillAI.NR_HILLS;
		}
		while (hills.get(nr_hill).size() == 0);
		indices[nr_hill]++;
		if (indices[nr_hill]>=hills.get(nr_hill).size()) indices[nr_hill] = 0;
		
		if (nr_hill==HillAI.NOSUGAR_IND
				&& indices[nr_hill] < hills.get(nr_hill).size() - HillAI.SHOUT_LAST_NOSUGAR){
			indices[nr_hill] = hills.get(nr_hill).size() - HillAI.SHOUT_LAST_NOSUGAR;
		}
		
		talk(nr_hill+HillAI.HILL_OFFSET,
				hills.get(nr_hill).get(indices[nr_hill]));
	}
	
	public Snapshot find_optimal_sugar_hill(){
		if (hills.get(HillAI.SUGAR_IND).size()==0) return null;
		
		if (self.id % 2 == 0){
			return hills.get(HillAI.SUGAR_IND).get(
					(self.id/2)%hills.get(HillAI.SUGAR_IND).size());
					
		}
		//if (hills.get(QueenAI.SUGAR_IND).size()==1) return hills.get(QueenAI.SUGAR_IND).get(0);
		
		int count = 0;
		for(Ant a: visibleFriends()){
			if (a.caste == Caste.Gatherer){
				if (a.id < self.id) count++;
			}
		}
		
		if (count<HillAI.WAITLENGTH){
			Snapshot best_sugar = hills.get(HillAI.SUGAR_IND).get(0);
			
			for(Snapshot sugar: hills.get(HillAI.SUGAR_IND)){
				if (walking_distance(best_sugar) > walking_distance(sugar)){
					//second_sugar = best_sugar;
					best_sugar = sugar;
				}
			}
			return best_sugar;
		}
		
		return null;
	}
	
	
	public double walking_distance(Snapshot sugarhill){
		double d = vectorBetween(self, sugarhill).length() * self.caste.SPEED;
		d += vectorBetween(sugarhill, hills.get(HillAI.HILL_IND).getFirst()).length()
				* self.caste.SPEED_WHILE_CARRYING_SUGAR;
		return d;
	}
	
	public void first_cry(){
		if (time == 1){		//was just born, figure out time and directions:
			
			//find time:
			if (audibleHillMessage != null) {
				if (audibleHillMessage.content < 0) {
					initialTime = audibleHillMessage.content - HillAI.initTime;
				}
			}
			if (initialTime < 5) {
				//in the beginning, figure out direction:
				int count = 0;
				int total = 0;
				for(Ant a: visibleFriends()){
					if (a.caste == Caste.Gatherer){
						total++;
						if (a.id < self.id) count++;
					}
				}
				
				dir = (360*count)/(total+1);
				moveInDirection(dir);
			}else{//later, just go some random direction:
				dir = SeededRandomizer.nextDouble()*360;
			}
			
			//initialize hills:
			for(int i = 0 ; i < HillAI.NR_HILLS ; ++i) {
				hills.add(new LinkedList<Snapshot>());
				indices[i] = 0;
			}
			
			//add my hill:
			for(Hill h: visibleHills){
				if (h.playerID == self.playerID){
					hills.get(HillAI.HILL_IND).add(visibleHills.get(0));					
				}
			}
			
			nr_hill = 0;
			
		}
	}
	
	public void distract(){
		if (!run){
			for(Ant enemy: visibleEnemies()){
				if (enemy.caste == Caste.Soldier){
					int count = 0;
					for(Ant friend: visibleFriends()){
						if (friend.caste == Caste.Gatherer && friend.health < self.health) {
							count++;
							break;
						}
					}
					if (count == 0){
						dropSugar();
						rundir = vectorBetween(self,enemy).angle();
						run = true;
					}
					break;
				}
			}
		}
		
		if (run){
			if (visibleEnemies().size()==0) run = false;
			else {
				if (hills.get(HillAI.HILL_IND).size() > 1){
					double newdir = vectorBetween(self, hills.get(HillAI.HILL_IND).get(1)).angle();
					rundir = adjust_in_direction(rundir, newdir);
				}
				moveInDirection(rundir);
			}
		}
	}
	
	public static double adjust_in_direction(double dir1, double dir2){
		return 0.6 * dir1 + 0.4 * dir2;
	}
	
	public void run_away(){
		/*
		
		run = false;
		for(Ant enemy: visibleEnemies()){
			if (enemy.caste == Caste.Soldier){
				dropSugar();
				moveInDirection((-1) * vectorBetween(self,enemy).angle());
				//moveHome();
				run = true;
			}
		}
		
		if (run) talk(QueenAI.FIGHT);*/
		
		Ant enemy = getClosest(visibleEnemies());
		
		if (enemy == null){
			run = false;
		}else{
			if (run){
				moveInDirection((-1) * vectorBetween(self,enemy).angle());
				dropSugar();
			}else if (last_health > self.health + HillAI.EPS){
				if (self.sugarCarry > 0
					&& self.health/(last_health-self.health) > vectorToHome().length() / self.caste.SPEED){
					//keep going
					run = false;
				}else{
					moveInDirection((-1) * vectorBetween(self,enemy).angle());
					run = true;
					dropSugar();
				}
			}
		}
		
		last_health = self.health;
	}
	
	public void decide_fighting(){
		int count_fighter = 0;
		
		for(AntMessage m: audibleAntMessages){
			if (m.content == HillAI.FIGHT) {
				count_fighter++;
			}
		}
		
		if (count_fighter >= HillAI.number_fighters) fight = true;
	}
	
	public void ATTACK1111111111111111111111(){
		if (visibleEnemies().size()==0) fight = false;
		else{
			Ant enemy = getWeakest(visibleEnemies());
			moveToward(enemy);
			attack(enemy);
			talk(HillAI.FIGHT);
		}
	}
	
	public Ant getWeakest(List<Ant> enemies) {
		Ant weakest = null;
		double health = Double.MAX_VALUE;
		for (Ant enemy : enemies) {
			double factor = 1;
			if (enemy.sugarCarry > 0) factor = 1.0 / parameters.VULNERABILITY_WHILE_CARRYING;
			if (enemy.health * factor < health) {
				health = enemy.health * factor;
				weakest = enemy;
			}
		}
		return weakest;
	}
	
	public Ant getClosest(List<Ant> enemies) {
		Ant closest = null;
		double distance = Double.MAX_VALUE;
		for (Ant enemy : enemies) {
			double d = vectorBetween(self,enemy).length();
			if (d < distance && enemy.caste == Caste.Soldier) {
				distance = d;
				closest = enemy;
			}
		}
		if (closest == null){
			for (Ant enemy : enemies) {
				double d = vectorBetween(self,enemy).length();
				if (d < distance) {
					distance = d;
					closest = enemy;
				}
			}
		}
		return closest;
	}
}
