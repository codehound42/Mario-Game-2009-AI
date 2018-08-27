package competition.cig.tickbased;

import competition.cig.tickbased.game.world.LevelScene;

public class SearchNode implements Comparable<SearchNode>{
	SearchNode parent;
	boolean[] action;
	public float x, y;
	public float xa, ya;
	
	public double gScore, fScore;
	public int ticksInFuture;
	public LevelScene levelScene;
	
	public static final int TIME_DIFF = 5;
	public static final double X_DIFF = 2;
	public static final double Y_DIFF = 2;
	public static final double XA_DIFF = 0.05;
	public static final double YA_DIFF = 0.0;
	
	public SearchNode(SearchNode parent, boolean[] action, LevelScene levelScene, int ticksInFuture) {
		this.parent = parent;
		this.action = action;
		this.levelScene = levelScene;
		this.ticksInFuture = ticksInFuture;
		
		this.x = levelScene.mario.x;
		this.y = levelScene.mario.y;
		this.xa = levelScene.mario.xa;
		this.ya = levelScene.mario.ya;
	}
	
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof SearchNode) {
			SearchNode oo = (SearchNode) o;
			return (Math.abs(oo.x - this.x) <= X_DIFF &&
					Math.abs(oo.y - this.y) <= Y_DIFF &&
					Math.abs(oo.ticksInFuture - this.ticksInFuture) <= TIME_DIFF &&
					Math.abs(oo.xa - this.xa) <= XA_DIFF &&
					Math.abs(oo.ya - this.ya) <= YA_DIFF);
		}
		return false;
	}
	
//	public boolean equals(Object o) {
//		if (o == null) return false;
//		if (o instanceof SearchNode) {
//			SearchNode oo = (SearchNode) o;
//			return (oo.ticksInFuture == this.ticksInFuture && oo.action.equals(this.action) && oo.parent.equals(this.parent));
//		}
//		return false;
//	}
	
	public int compareTo(SearchNode o) {
		return (int) (this.fScore - o.fScore); // Note: Changed from orignal order
	}
	
	public String toString() {
		return "( x:" +  x + ", y:" + y + ", t:" + ticksInFuture + ", xa:" + xa + ", ya:" + ya + ")";
	}
}
