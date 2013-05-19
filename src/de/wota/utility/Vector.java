package de.wota.utility;

/**
 * Vector with two double components.
 */
public class Vector {
	
	/** x Coordinate */
	public double x;
	
	/** y Coordinate */
	public double y;
	
	/** Constructs a vector with x and y Coordinate */
	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector(Vector v) {
		this.x = v.x;
		this.y = v.y;
	}
	
	/** returns the lengths of the vector in L_2 norm */
	public double length() {
		return Math.sqrt(x*x + y*y);
	}
	
	/** returns the sum of p1 and p2 */
	public static Vector add(final Vector p1, final Vector p2) {
		return new Vector(p1.x + p2.x, p1.y + p2.y);
	}
	
	/** returns the vector p, scaled by scaling_factor */
	public static Vector scale(double scalingFactor, final Vector p) {
		return new Vector(p.x * scalingFactor, p.y * scalingFactor);
	}
	
	/** creates a vector from polar coordinates. 
	 *  
	 * @param amplitude length of the vector
	 * @param direction phi-component. direction = 0 generates a vector along the x-axis.
	 * @return
	 */
	public static Vector fromPolar(double amplitude, double direction) {
		return new Vector(amplitude*Math.cos(direction/360.*2*Math.PI), amplitude*Math.sin(direction/360.*2*Math.PI));
	}
	
	/**
	 * returns the result of v1 - v2
	 */
	public static Vector subtract(Vector v1, Vector v2) {
		return new Vector(v1.x - v2.x, v1.y - v2.y);
	}
	
	/** does not change instance */
	public Vector scale(double a) {
		return Vector.scale(a, this);
	}

	/** returns this Vector scaled to specified length
	 * does not change instance */
	public Vector scaleTo(double length) {
		return Vector.scale(length / this.length(), this);
	}
	
	/** L_2 distance between two vectors = length of v1 - v2 */
	public static double distanceBetween(Vector v1, Vector v2) {
		return subtract(v1, v2).length();
	}
	
	/** prints vector like: "Vector: x = 5; y = 3" */
	public String toString() {
		return "Vector: x = " + x + "; y = " + y;
	}
}