package pathfinder;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JComboBox;

public class PathFinderDemonstration {
	
	JFrame frame;

	private int cells = 20;
	private int delay = 30;
	private double dense = .5;
	private double density = (cells*cells)*.5;
	private int start_x_position = -1;
	private int start_y_position = -1;
	private int finish_x_position = -1;
	private int finish_y_position = -1;
	private int tool = 0;
	private int checks = 0;
	private int length = 0;
	private int curAlg = 0;
	private final int WIDTH = 850;
	private final int HEIGHT = 650;
	private final int MSIZE = 600;
	private int CSIZE = MSIZE/cells;
	
	private final String[] algorithms = {
            "Dijkstra",
            "A*"
        };
	private final String[] tools = {
            "Start", 
            "Finish", 
            "Wall", 
            "Eraser"
        };
	
	private boolean solving = false;
	
	Node[][] map;
	Algorithm Alg = new Algorithm();
	Random r = new Random();
	
	JSlider size = new JSlider(1,150,1);
	JSlider speed = new JSlider(0,500,delay);
	JSlider obstacles = new JSlider(1,100,50);
	
	JLabel algL = new JLabel("Algorithms");
	JLabel toolL = new JLabel("Toolbox");
	JLabel sizeL = new JLabel("Size:");
	JLabel cellsL = new JLabel(cells+"x"+cells);
	JLabel delayL = new JLabel("Delay:");
	JLabel msL = new JLabel(delay+"ms");
	JLabel obstacleL = new JLabel("Density: ");
	JLabel densityL = new JLabel(obstacles.getValue()+"%");
	JLabel checkL = new JLabel("Checks: "+checks);
	JLabel lengthL = new JLabel("Path Length: "+length);

	JButton searchB = new JButton("Start Search");
	JButton resetB = new JButton("Reset");
	JButton genMapB = new JButton("Generate Map");
	JButton clearMapB = new JButton("Clear Map");

	JComboBox algorithmsBx = new JComboBox(algorithms);
	JComboBox toolBx = new JComboBox(tools);

	JPanel toolP = new JPanel();

	Map canvas;
	
	Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

	public static void main(String[] args) {
            PathFinderDemonstration pathFinding;
            pathFinding = new PathFinderDemonstration();
	}

	public PathFinderDemonstration() {	//CONSTRUCTOR
		clearMap();
		initialize();
	}
        
	/**
         * Generate the map
         */
	public void generateMap() {
		clearMap();
		for(int i = 0; i < density; i++) {
			Node current;
			do {
				int x = r.nextInt(cells);
				int y = r.nextInt(cells);
				current = map[x][y];	//Find a random node at the grid
			} while(current.getType() == 2);	//If it is a wall, find another node.
			current.setType(2);//The node is a wall
		}
	}
	
        /**
         * Clear the map
         */
	public void clearMap() {
		finish_x_position = -1;
		finish_y_position = -1;
		start_x_position = -1;
		start_y_position = -1;
		map = new Node[cells][cells];
		for(int x = 0; x < cells; x++) {
			for(int y = 0; y < cells; y++) {
				map[x][y] = new Node(3,x,y);	//SET ALL NODES TO EMPTY
			}
		}
		reset();	//RESET SOME VARIABLES
	}
	
	public void resetMap() {	//RESET MAP
		for(int x = 0; x < cells; x++) {
			for(int y = 0; y < cells; y++) {
				Node current = map[x][y];
				if(current.getType() == 4 || current.getType() == 5)
					map[x][y] = new Node(3,x,y);
			}
		}
		if(start_x_position > -1 && start_y_position > -1) {
			map[start_x_position][start_y_position] = new Node(0,start_x_position,start_y_position);
			map[start_x_position][start_y_position].setHops(0);
		}
		if(finish_x_position > -1 && finish_y_position > -1)
			map[finish_x_position][finish_y_position] = new Node(1,finish_x_position,finish_y_position);
		reset();	//RESET SOME VARIABLES
	}

	private void initialize() {
		frame = new JFrame();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setSize(WIDTH,HEIGHT);
		frame.setTitle("Path Finding");
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		toolP.setBorder(BorderFactory.createTitledBorder(loweredetched,"Controls"));
		int space = 25;
		int buff = 45;
		
		toolP.setLayout(null);
		toolP.setBounds(10,10,210,600);
		
		searchB.setBounds(40,space, 120, 25);
		toolP.add(searchB);
		space+=buff;
		
		resetB.setBounds(40,space,120,25);
		toolP.add(resetB);
		space+=buff;
		
		genMapB.setBounds(40,space, 120, 25);
		toolP.add(genMapB);
		space+=buff;
		
		clearMapB.setBounds(40,space, 120, 25);
		toolP.add(clearMapB);
		space+=40;
		
		algL.setBounds(40,space,120,25);
		toolP.add(algL);
		space+=25;
		
		algorithmsBx.setBounds(40,space, 120, 25);
		toolP.add(algorithmsBx);
		space+=40;
		
		toolL.setBounds(40,space,120,25);
		toolP.add(toolL);
		space+=25;
		
		toolBx.setBounds(40,space,120,25);
		toolP.add(toolBx);
		space+=buff;
		
		sizeL.setBounds(15,space,40,25);
		toolP.add(sizeL);
		size.setMajorTickSpacing(10);
		size.setBounds(50,space,100,25);
		toolP.add(size);
		cellsL.setBounds(160,space,40,25);
		toolP.add(cellsL);
		space+=buff;
		
		delayL.setBounds(15,space,50,25);
		toolP.add(delayL);
		speed.setMajorTickSpacing(5);
		speed.setBounds(50,space,100,25);
		toolP.add(speed);
		msL.setBounds(160,space,40,25);
		toolP.add(msL);
		space+=buff;
		
		obstacleL.setBounds(15,space,100,25);
		toolP.add(obstacleL);
		obstacles.setMajorTickSpacing(5);
		obstacles.setBounds(50,space,100,25);
		toolP.add(obstacles);
		densityL.setBounds(160,space,100,25);
		toolP.add(densityL);
		space+=buff;
		
		checkL.setBounds(15,space,100,25);
		toolP.add(checkL);
		space+=buff;
		
		lengthL.setBounds(15,space,100,25);
		toolP.add(lengthL);
		space+=buff;
		
		frame.getContentPane().add(toolP);
		
		canvas = new Map();
		canvas.setBounds(230, 10, MSIZE+1, MSIZE+1);
		frame.getContentPane().add(canvas);
		
		searchB.addActionListener((ActionEvent e) -> {
                    reset();
                    if((start_x_position > -1 && start_y_position > -1) && (finish_x_position > -1 && finish_y_position > -1))
                        solving = true;
                } //ACTION LISTENERS
                );
		resetB.addActionListener((ActionEvent e) -> {
                    resetMap();
                    Update();
                });
		genMapB.addActionListener((ActionEvent e) -> {
                    generateMap();
                    Update();
                });
		clearMapB.addActionListener((ActionEvent e) -> {
                    clearMap();
                    Update();
                });
		algorithmsBx.addItemListener((ItemEvent e) -> {
                    curAlg = algorithmsBx.getSelectedIndex();
                    Update();
                });
		toolBx.addItemListener((ItemEvent e) -> {
                    tool = toolBx.getSelectedIndex();
                });
		size.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				cells = size.getValue()*1;
				clearMap();
				reset();
				Update();
			}
		});
		speed.addChangeListener((ChangeEvent e) -> {
                    delay = speed.getValue();
                    Update();
                });
		obstacles.addChangeListener((ChangeEvent e) -> {
                    dense = (double)obstacles.getValue()/100;
                    Update();
                });
		startSearch();	//START STATE
	}
	
	public void startSearch() {	//START STATE
		if(solving) {
			switch(curAlg) {
				case 0:
					Alg.Dijkstra();
					break;
				case 1:
					Alg.AStar();
					break;
			}
		}
		pause();	//PAUSE STATE
	}
	
	public void pause() {	//PAUSE STATE
		int i = 0;
		while(!solving) {
			i++;
			if(i > 500)
				i = 0;
			try {
				Thread.sleep(1);
			} catch(Exception e) {}
		}
		startSearch();	//START STATE
	}
	
	public void Update() {	//UPDATE ELEMENTS OF THE GUI
		density = (cells*cells)*dense;
		CSIZE = MSIZE/cells;
		canvas.repaint();
		cellsL.setText(cells+"x"+cells);
		msL.setText(delay+"ms");
		lengthL.setText("Path Length: "+length);
		densityL.setText(obstacles.getValue()+"%");
		checkL.setText("Checks: "+checks);
	}
	
	public void reset() {	//RESET METHOD
		solving = false;
		length = 0;
		checks = 0;
	}
	
	public void delay() {	//DELAY METHOD
		try {
			Thread.sleep(delay);
		} catch(Exception e) {}
	}
	
	class Map extends JPanel implements MouseListener, MouseMotionListener{	//MAP CLASS
		
		public Map() {
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		
		public void paintComponent(Graphics g) {	//REPAINT
			super.paintComponent(g);
			for(int x = 0; x < cells; x++) {	//PAINT EACH NODE IN THE GRID
				for(int y = 0; y < cells; y++) {
					switch(map[x][y].getType()) {
						case 0:
							g.setColor(Color.GREEN);
							break;
						case 1:
							g.setColor(Color.RED);
							break;
						case 2:
							g.setColor(Color.BLACK);
							break;
						case 3:
							g.setColor(Color.WHITE);
							break;
						case 4:
							g.setColor(Color.CYAN);
							break;
						case 5:
							g.setColor(Color.YELLOW);
							break;
					}
					g.fillRect(x*CSIZE,y*CSIZE,CSIZE,CSIZE);
					g.setColor(Color.BLACK);
					g.drawRect(x*CSIZE,y*CSIZE,CSIZE,CSIZE);
					//DEBUG STUFF
					/*
					if(curAlg == 1)
						g.drawString(map[x][y].getHops()+"/"+map[x][y].getEuclidDist(), (x*CSIZE)+(CSIZE/2)-10, (y*CSIZE)+(CSIZE/2));
					else 
						g.drawString(""+map[x][y].getHops(), (x*CSIZE)+(CSIZE/2), (y*CSIZE)+(CSIZE/2));
					*/
				}
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			try {
				int x = e.getX()/CSIZE;	
				int y = e.getY()/CSIZE;
				Node current = map[x][y];
				if((tool == 2 || tool == 3) && (current.getType() != 0 && current.getType() != 1))
					current.setType(tool);
				Update();
			} catch(Exception z) {}
		}

		@Override
		public void mouseMoved(MouseEvent e) {}

		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {
			resetMap();	//RESET THE MAP WHENEVER CLICKED
			try {
				int x = e.getX()/CSIZE;	//GET THE X AND Y OF THE MOUSE CLICK IN RELATION TO THE SIZE OF THE GRID
				int y = e.getY()/CSIZE;
				Node current = map[x][y];
				switch(tool ) {
					case 0: {	//START NODE
						if(current.getType()!=2) {	//IF NOT WALL
							if(start_x_position > -1 && start_y_position > -1) {	//IF START EXISTS SET IT TO EMPTY
								map[start_x_position][start_y_position].setType(3);
								map[start_x_position][start_y_position].setHops(-1);
							}
							current.setHops(0);
							start_x_position = x;	//SET THE START X AND Y
							start_y_position = y;
							current.setType(0);	//SET THE NODE CLICKED TO BE START
						}
						break;
					}
					case 1: {//FINISH NODE
						if(current.getType()!=2) {	//IF NOT WALL
							if(finish_x_position > -1 && finish_y_position > -1)	//IF FINISH EXISTS SET IT TO EMPTY
								map[finish_x_position][finish_y_position].setType(3);
							finish_x_position = x;	//SET THE FINISH X AND Y
							finish_y_position = y;
							current.setType(1);	//SET THE NODE CLICKED TO BE FINISH
						}
						break;
					}
					default:
						if(current.getType() != 0 && current.getType() != 1)
							current.setType(tool);
						break;
				}
				Update();
			} catch(Exception z) {}	//EXCEPTION HANDLER
		}

		@Override
		public void mouseReleased(MouseEvent e) {}
	}
	
	class Algorithm {	
		public void Dijkstra() {
			ArrayList<Node> priority = new ArrayList<Node>(); //Priority queue
			priority.add(map[start_x_position][start_y_position]);	//Add the start to the queue
			while(solving) {
				if(priority.size() <= 0){//If the queue is 0, then no path can be found
					solving = false;
					break;
				}
				int hops = priority.get(0).getHops()+1;	//Increment hops
				ArrayList<Node> explored = exploreNeighbors(priority.get(0), hops);//Nodes that were exploded
				if(explored.size() > 0) {
					priority.remove(0); //Remove the node from the queue
					priority.addAll(explored);//Add all nodes to the queue
					Update();
					delay();
				} else {
					priority.remove(0);
				}
			}
		}
		
		//A STAR WORKS ESSENTIALLY THE SAME AS DIJKSTRA CREATING A PRIORITY QUE AND PROPAGATING OUTWARDS UNTIL IT FINDS THE END
		//HOWEVER ASTAR BUILDS IN A HEURISTIC OF DISTANCE FROM ANY NODE TO THE FINISH
		//THIS MEANS THAT NODES THAT ARE CLOSER TO THE FINISH WILL BE EXPLORED FIRST
		//THIS HEURISTIC IS BUILT IN BY SORTING THE QUE ACCORDING TO HOPS PLUS DISTANCE UNTIL THE FINISH
		public void AStar() {
			ArrayList<Node> priority = new ArrayList<Node>();
			priority.add(map[start_x_position][start_y_position]);
			while(solving) {
				if(priority.size() <= 0) {
					solving = false;
					break;
				}
				int hops = priority.get(0).getHops()+1;
				ArrayList<Node> explored = exploreNeighbors(priority.get(0),hops);
				if(explored.size() > 0) {
					priority.remove(0);
					priority.addAll(explored);
					Update();
					delay();
				} else {
					priority.remove(0);
				}
				sortQueue(priority);
			}
		}
		
		public ArrayList<Node> sortQueue(ArrayList<Node> sort) {
			int c = 0;
			while(c < sort.size()) {
				int sm = c;
				for(int i = c+1; i < sort.size(); i++) {
					if(sort.get(i).getEuclidDist()+sort.get(i).getHops() < sort.get(sm).getEuclidDist()+sort.get(sm).getHops())
						sm = i;
				}
				if(c != sm) {
					Node temp = sort.get(c);
					sort.set(c, sort.get(sm));
					sort.set(sm, temp);
				}	
				c++;
			}
			return sort;
		}
		
		public ArrayList<Node> exploreNeighbors(Node current, int hops) {	//EXPLORE NEIGHBORS
			ArrayList<Node> explored = new ArrayList<Node>();	//LIST OF NODES THAT HAVE BEEN EXPLORED
			for(int a = -1; a <= 1; a++) {
				for(int b = -1; b <= 1; b++) {
					int xbound = current.getX()+a;
					int ybound = current.getY()+b;
					if((xbound > -1 && xbound < cells) && (ybound > -1 && ybound < cells)) {	//MAKES SURE THE NODE IS NOT OUTSIDE THE GRID
						Node neighbor = map[xbound][ybound];
						if((neighbor.getHops()==-1 || neighbor.getHops() > hops) && neighbor.getType()!=2) {	//CHECKS IF THE NODE IS NOT A WALL AND THAT IT HAS NOT BEEN EXPLORED
							explore(neighbor, current.getX(), current.getY(), hops);	//EXPLORE THE NODE
							explored.add(neighbor);	//ADD THE NODE TO THE LIST
						}
					}
				}
			}
			return explored;
		}
		
		public void explore(Node current, int lastx, int lasty, int hops) {	//EXPLORE A NODE
			if(current.getType()!=0 && current.getType() != 1)	//CHECK THAT THE NODE IS NOT THE START OR FINISH
				current.setType(4);	//SET IT TO EXPLORED
			current.setLastNode(lastx, lasty);	//KEEP TRACK OF THE NODE THAT THIS NODE IS EXPLORED FROM
			current.setHops(hops);	//SET THE HOPS FROM THE START
			checks++;
			if(current.getType() == 1) {	//IF THE NODE IS THE FINISH THEN BACKTRACK TO GET THE PATH
				backtrack(current.getLastX(), current.getLastY(),hops);
			}
		}
		
		public void backtrack(int lx, int ly, int hops) {	//BACKTRACK
			length = hops;
			while(hops > 1) {	//BACKTRACK FROM THE END OF THE PATH TO THE START
				Node current = map[lx][ly];
				current.setType(5);
				lx = current.getLastX();
				ly = current.getLastY();
				hops--;
			}
			solving = false;
		}
	}
	
	class Node {
		
		// 0 = start, 1 = finish, 2 = wall, 3 = empty, 4 = checked, 5 = finalpath
		private int cellType = 0;
		private int hops;
		private int x;
		private int y;
		private int lastX;
		private int lastY;
		private double dToEnd = 0;
	
		public Node(int type, int x, int y) {	//CONSTRUCTOR
			cellType = type;
			this.x = x;
			this.y = y;
			hops = -1;
		}
		
		public double getEuclidDist() {		//CALCULATES THE EUCLIDIAN DISTANCE TO THE FINISH NODE
			int xdif = Math.abs(x-finish_x_position);
			int ydif = Math.abs(y-finish_y_position);
			dToEnd = Math.sqrt((xdif*xdif)+(ydif*ydif));
			return dToEnd;
		}
		
		public int getX() {return x;}		//GET METHODS
		public int getY() {return y;}
		public int getLastX() {return lastX;}
		public int getLastY() {return lastY;}
		public int getType() {return cellType;}
		public int getHops() {return hops;}
		
		public void setType(int type) {cellType = type;}		//SET METHODS
		public void setLastNode(int x, int y) {lastX = x; lastY = y;}
		public void setHops(int hops) {this.hops = hops;}
	}
}