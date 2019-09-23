import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.Timer;

/**
 * The PuzzlePrisonBoard class. Sets up the JFrame that the JPanel and
 * everything else is put into.
 * 
 * @author Philip Radojcic
 * @version last updated 6/15/2014
 */
public class PuzzlePrisonBoard extends JFrame {

	// Fonts for the multiple screens.
	static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 16);
	static final Font TIME_FONT = new Font("Arial", Font.PLAIN, 45);
	static final Font LABEL_FONT2 = new Font("Arial", Font.PLAIN, 16);
	static final Font TIME_FONT2 = new Font("Arial", Font.PLAIN, 48);
	static final Font LABEL_FONT3 = new Font("Arial", Font.PLAIN, 16);
	static final Font TIME_FONT3 = new Font("Arial", Font.PLAIN, 22);

	public PuzzlePrisonBoard() {
		// Sets up the frame and the grid
		super("Puzzle Prison");
		// Sets the JFrame to the middle of the screen. (at least for me)
		setLocation(400, 160);
		// Set code up for the game board, creating and centering the border
		// from the middle of the setLocation.
		Container contentPane = getContentPane();
		contentPane.add(new GameBoard(), BorderLayout.CENTER);
	}

	// Inner class for the maze area
	/**
	 * The GameBoard class. The Gameboard class honestly does 98% of this entire
	 * program. It loads in the images, the grids, the screens, the
	 * interactions, the character movement, the new levels, literally everything
	 * but making the JFrame.
	 * 
	 * @author Philip Radojcic
	 * @version last updated 6/15/2014
	 */
	private class GameBoard extends JPanel {

		// The "Image" here is the JPanel which goes into the JFrame.
		private final int IMAGE_WIDTH;
		private final int IMAGE_HEIGHT;

		// Different game screens.
		private final int LEVEL_WON_SCREEN = -5;
		private final int CREDITS_SCREEN = -4;
		private final int HIGH_SCORE_SCREEN = -3;
		private final int CONTROL_SCREEN = -2;
		private final int LEVEL_SCREEN = -1;
		private final int MAIN_MENU = 0;
		private final int LEVEL1 = 1;
		private final int LEVEL2 = 2;
		private final int LEVEL3 = 3;
		private int screen = MAIN_MENU;

		// Creates all the non grid related images. (buttons, screens)
		private Image[] gridImages;
		private Image playerImage;
		private Image inventoryImage = new ImageIcon("Inventory Button.PNG")
				.getImage();
		private Image timeImage = new ImageIcon("Time Button 2.PNG").getImage();;
		private Image backToMenuImage = new ImageIcon(
				"Back to Main Menu Button.PNG").getImage();;
		private Image highScoreImage = new ImageIcon("High Score Screen.png")
				.getImage();
		private Image creditsImage = new ImageIcon("Credits Screen.png")
				.getImage();
		private Image menuImage = new ImageIcon("Main Menu 2.png").getImage();
		private Image levelMenuImage = new ImageIcon(
				"Level Buttons in Level Menu.png").getImage();
		private Image ControlsImage = new ImageIcon("Control Menu 2.png")
				.getImage();
		private Image afterLevelScreen = new ImageIcon("afterLevelScreen.png")
				.getImage();

		// Variables to keep track of the grid and the players position on the
		// grid.
		private int[][] grid;
		private int currentRow;
		private int currentColumn;

		// Top 5 names and scores for the high scores menu.
		private String[][] top5Names = new String[3][5];
		private int[][] top5Scores = new int[3][5];
		// The level_score is the final score at the end of each level once
		// calculated.
		private int level_score;
		// The name of the player to be inputed. (if they beat a high score)
		private String currentPlayerName = "";

		// Stores the buttons pressed, whether or not you have a key, and how
		// many coins were collected. (Used during calculating score)
		private int buttonsPressed = 0;
		private int KeyInInventory = 0;
		private int coinsInInventory = 0;

		// Time is the time in seconds, and time2 is the time in milliseconds.
		private Timer timer;
		private Timer timer2;
		private double time = 0;
		private double time2 = 0;
		// The time when the level is beaten.
		private double currentTime = 0;

		// These 2 variables help switching between levels, currentLevelName for
		// txts, currentLevelNumber for arrays.
		private String currentLevelName = "Level 1.txt";
		private int currentLevelNumber = 0;

		private boolean doYouHaveTheKey = false;		
		
		/**
		 * The GameBoard method. The Gameboard method creates the grid images
		 * and sets up the grid when the newGame method is called.
		 * 
		 * @author Philip Radojcic
		 * @version last updated 6/15/2014
		 */
		public GameBoard() {
			// Actually counts for seconds and milliseconds.
			timer = new Timer(1000, new TimerEventHandler());
			timer2 = new Timer(100, new TimerEventHandler());
			// Create an array for the gridImages and load them up
			// Also load up the player image
			gridImages = new Image[] {
					new ImageIcon("2D Blank Space 3.png").getImage(), // a
					new ImageIcon("Block Tile.png").getImage(), // b
					new ImageIcon("Button Active Sprite 3.png").getImage(), // c
					new ImageIcon("Button Unactive Sprite 3.png").getImage(), // d
					new ImageIcon("Button Wall Sprite 3.png").getImage(), // e
					new ImageIcon("Lowered Wall 2.png").getImage(), // f
					new ImageIcon("Key 3.PNG").getImage(), // g
					new ImageIcon("Final Door 3.PNG").getImage(), // h
					new ImageIcon("Final Door Open.PNG").getImage(), // i
					new ImageIcon("Coin 3.PNG").getImage(), // j
			};

			// Creates your player image.
			playerImage = new ImageIcon(
					"2D Sprite General on blank space 4.png").getImage();

			// For saving the values of the high score so that they remain even
			// after the file is closed.
			try {
				Scanner input = new Scanner(new File("savefile.txt"));
				for (int level = 0; level < 3; level++) {
					for (int index = 0; index < 5; index++) {
						top5Names[level][index] = input.nextLine();
						top5Scores[level][index] = input.nextInt();
						input.nextLine();
					}
				}
				input.close();
			} catch (FileNotFoundException e) {
				for (int level = 0; level < 3; level++) {
					Arrays.fill(top5Scores[level], 0);
					Arrays.fill(top5Names[level], "");
				}
			}

			// Makes a new game with the current level txt.
			newGame(currentLevelName);

			// Set the image height and width based on the path/blank walkable
			// space's image size.
			// Also sizes the JPanel based on the image and the grid size.
			IMAGE_WIDTH = gridImages[0].getWidth(this);
			IMAGE_HEIGHT = gridImages[0].getHeight(this);
			Dimension size = new Dimension(grid[0].length * IMAGE_WIDTH,
					grid.length * IMAGE_HEIGHT + 97);
			this.setPreferredSize(size);

			// Sets up for keyboard input (arrow keys) on this panel and it sets
			// up for mouse input.
			this.setFocusable(true);
			this.addKeyListener(new KeyHandler());
			this.addMouseListener(new MouseHandler());
			this.requestFocusInWindow();
		}

		// Does the actually saving function by writing into the savefile.txt
		// and putting the values in it.
		public void save() {
			PrintWriter writer;
			try {
				writer = new PrintWriter("savefile.txt");
				for (int level = 0; level < 3; level++) {
					for (int index = 0; index < 5; index++) {
						writer.println(top5Names[level][index]);
						writer.println(top5Scores[level][index]);
					}
				}
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		/**
		 * The coinCollecting method. This method was just for a bit of
		 * convenience, it simply counts every coin you collect.
		 * 
		 * @author Philip Radojcic
		 * @version last updated 6/15/2014
		 */
		public void coinCollecting() {
			if (screen == LEVEL1 || screen == LEVEL2 || screen == LEVEL3) {
				if (grid[currentRow][currentColumn] == 9) {
					grid[currentRow][currentColumn] = 0;
					coinsInInventory++;
				}
			}
		}

		/**
		 * The level_1 method. This method takes care of the specific
		 * interactions for level 1. (When each button is pressed, what walls
		 * are changed)
		 * 
		 * @author Philip Radojcic
		 * @version last updated 6/15/2014
		 */
		public void level_1() {
			// Row (Up -> Down) Column ( Left -> Right)
			if (grid[currentRow][currentColumn] == grid[11][7]) {
				grid[9][7] = 4;
				grid[13][12] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[11][16]) {
				grid[13][12] = 4;
				grid[7][18] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[6][16]) {
				grid[4][21] = 4;
				grid[7][18] = 4;
				grid[1][15] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[2][14]) {
				grid[1][15] = 4;
				grid[4][14] = 0;
				grid[4][13] = 0;
				grid[4][21] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[2][23]) {
				grid[4][14] = 0;
				grid[4][13] = 0;
				grid[5][22] = 0;
				grid[5][23] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[9][22]) {
				grid[7][18] = 0;
				grid[12][20] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[8][11]) {
				grid[9][7] = 4;
				grid[7][10] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[2][11]) {
				grid[1][6] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[6][1]) {
				grid[5][6] = 0;
				grid[6][6] = 0;
				grid[8][6] = 0;
				grid[12][1] = 0;
			}
		}

		/**
		 * The level_2 method. This method takes care of the specific
		 * interactions for level 2. (When each button is pressed, what walls
		 * are changed)
		 * 
		 * @author Philip Radojcic
		 * @version last updated 6/15/2014
		 */
		public void level_2() {
			// Row (Up -> Down) Column ( Left -> Right)
			if (grid[currentRow][currentColumn] == grid[7][4]) {
				grid[3][9] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[11][8]) {
				grid[12][20] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[9][14]) {
				grid[6][1] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[7][15]) {
				grid[4][15] = 0;
				grid[7][6] = 4;
			}
			if (grid[currentRow][currentColumn] == grid[6][22]) {
				grid[11][19] = 0;
				grid[6][21] = 4;
			}
			if (grid[currentRow][currentColumn] == grid[6][17]) {
				grid[7][21] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[3][7]) {
				grid[1][6] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[10][17]) {
				grid[4][10] = 0;
				grid[4][21] = 0;
			}
		}

		/**
		 * The level_3 method. This method takes care of the specific
		 * interactions for level 3. (When each button is pressed, what walls
		 * are changed)
		 * 
		 * @author Philip Radojcic
		 * @version last updated 6/15/2014
		 */
		public void level_3() {
			// Row (Up -> Down) Column ( Left -> Right)
			if (grid[currentRow][currentColumn] == grid[8][2]) {
				grid[12][19] = 0;
				grid[7][4] = 0;
				grid[12][14] = 4;
			}
			if (grid[currentRow][currentColumn] == grid[7][2]) {
				grid[13][15] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[6][2]) {
				grid[11][7] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[8][7]) {
				grid[12][8] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[8][8]) {
				grid[13][11] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[8][9]) {
				grid[11][12] = 0;
				grid[13][6] = 4;
			}
			if (grid[currentRow][currentColumn] == grid[1][1]) {
				grid[12][20] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[1][2]) {
				grid[11][8] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[1][3]) {
				grid[12][12] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[1][5]) {
				grid[12][4] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[1][7]) {
				grid[11][16] = 0;
				grid[4][2] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[3][11]) {
				grid[12][7] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[3][15]) {
				grid[12][11] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[1][21]) {
				grid[13][12] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[7][17]) {
				grid[11][15] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[5][23]) {
				grid[11][11] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[1][23]) {
				grid[13][16] = 0;
			}
			// Secret Path (Was just for speed while creating the level, but I
			// thought it would be cool to leave it there.
			if (grid[8][2] == 3 && grid[7][2] == 3 && grid[6][2] == 2
					&& grid[8][7] == 3 && grid[8][8] == 2 && grid[8][9] == 3) {
				grid[4][2] = 0;
			}
			if (grid[currentRow][currentColumn] == grid[1][23]
					&& grid[8][2] == 3 && grid[7][2] == 3 && grid[6][2] == 2
					&& grid[8][7] == 3 && grid[8][8] == 2 && grid[8][9] == 3
					&& grid[1][1] == 3 && grid[1][2] == 3 && grid[1][3] == 2) {
				grid[4][2] = 0;
				grid[12][7] = 0;
				grid[12][8] = 0;
				grid[12][11] = 0;
				grid[12][12] = 0;
				grid[12][15] = 0;
				grid[12][16] = 0;
				grid[12][20] = 0;
				grid[12][21] = 0;
			}
		}

		/**
		 * The paintComponent method. This method creates all the graphics,
		 * draws all the screens when asked, and draws the images into the grid
		 * when called for it.
		 * @param g stands for the graphical images, and is needed to draw every image.
		 * @author Philip Radojcic
		 * @version last updated 6/15/2014
		 */
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			// Credits Screen
			if (screen == CREDITS_SCREEN) {
				g.drawImage(creditsImage, 0, 0, this);
			}

			// High Score Screen
			if (screen == HIGH_SCORE_SCREEN) {
				g.drawImage(highScoreImage, 0, 0, this);
				g.setFont(LABEL_FONT3);
				g.setFont(TIME_FONT3);
				g.setColor(Color.BLACK);
				// Starting positions for y and x coordinates.
				int scoreSpotStart1 = 437;
				int scoreSpotStart2 = 463;
				int lettersXCoordinates = 90;				
				// Goes for 5 times since the high scores are only for top 5.
				for (int levelsIndex = 0; levelsIndex < 3; levelsIndex ++)
				{	
					// Selects the X coordinates corresponding to the level scores.
					if (levelsIndex == 0)
					lettersXCoordinates = 90;
					if (levelsIndex == 1)
					lettersXCoordinates = 430;
					if (levelsIndex == 2)
					lettersXCoordinates	= 820;
					for (int index = 0; index < 5; index++) 
					{
						g.drawString(
								("The #" + (index + 1) + " Player is: " + top5Names[levelsIndex][index]),
								lettersXCoordinates, scoreSpotStart1);
						g.drawString(
								("The #" + (index + 1) + " Score is:  " + top5Scores[levelsIndex][index]),
								lettersXCoordinates, scoreSpotStart2);
						scoreSpotStart1 += 65;
						scoreSpotStart2 += 65;
					}
					scoreSpotStart1 = 437;
					scoreSpotStart2 = 463;
				}
			}

			// Main Menu Screen
			if (screen == MAIN_MENU) {
				g.drawImage(menuImage, 0, 0, this);
				// Level Selection Screen
			} else if (screen == LEVEL_SCREEN) {
				g.drawImage(levelMenuImage, 0, 0, this);
				// The screen that appears when you beat a level, showing score
				// and stats.
			} else if (screen == LEVEL_WON_SCREEN) {
				g.setFont(LABEL_FONT2);
				g.setFont(TIME_FONT2);
				g.setColor(Color.BLACK);
				g.drawImage(afterLevelScreen, 0, 0, this);
				// The next 3 lines are for the time.
				currentTime = (int) (currentTime * 1.0);
				currentTime = ((double) time + time2) / 20;
				double level_Final_Time = currentTime;
				// The final score at the end of each level.
				level_score = ((coinsInInventory * 20)
						+ (1000 - (int) (level_Final_Time * 20)) - (buttonsPressed * 14));
				// These next 2 ifs are to raise the scores of level 2 and 3, to
				// make them around the same as level 1. Just looks better.
				if (currentLevelName == "Level 2.txt")
					level_score += 300;
				if (currentLevelName == "Level 3.txt")
					level_score += 700;
				g.drawString("" + ("You have " + coinsInInventory + " coins!"),
						167, 350);
				g.drawString(
						""
								+ ("Your final time is " + level_Final_Time + " seconds!"),
						167, 450);
				g.drawString(""
						+ ("You have pressed " + buttonsPressed + " buttons!"),
						167, 550);
				g.drawString("" + ("Your Final Score Is: " + level_score), 167,
						650);
				g.drawImage(gridImages[9], 590, 310, this);
				g.drawImage(gridImages[3], 810, 510, this);
			// Draws the controls screen.	
			} else if (screen == CONTROL_SCREEN) {
				g.drawImage(ControlsImage, 0, 0, this);
		    // The only screens greater then 0 are the level 1, 2 and 3 screens for the games.
			} else if (screen > 0) {				
				// Redraws the grid with the current images that are already loaded in.
				for (int row = 0; row < grid.length; row++)
					for (int column = 0; column < grid[0].length; column++) {
						// Put a path underneath everywhere
						g.drawImage(gridImages[0], column * IMAGE_WIDTH, row
								* IMAGE_HEIGHT, this);
						int imageNo = grid[row][column];
						g.drawImage(gridImages[imageNo], column * IMAGE_WIDTH,
								row * IMAGE_HEIGHT, this);
					}

				// Draw the moving player on top of the grid
				g.drawImage(playerImage, currentColumn * IMAGE_WIDTH,
						currentRow * IMAGE_HEIGHT, this);

				// These 3 are the images for the 3 buttons for all levels to use, 
				// 1 to keep track of the time, 1 to show the inventory and 1 to go back to the main menu.
				g.drawImage(timeImage, 664, 691, this);
				g.drawImage(backToMenuImage, 899, 691, this);
				g.drawImage(inventoryImage, 0, 691, this);

				g.setFont(LABEL_FONT);
				g.setFont(TIME_FONT);
				g.setColor(Color.RED);
				currentTime = (int) (currentTime * 1.0);
				currentTime = ((double) time + time2) / 20;
				// Actually shows the values of what appears in the 3 buttons for all the levels.
				g.drawString("" + (currentTime), 785, 750);
				g.drawString("" + (coinsInInventory), 337, 750);
				g.drawString("" + (buttonsPressed), 480, 750);
				g.drawString("" + (KeyInInventory), 600, 750);

			}

		} // paint component method.

		/** The newGame method.
		 * This method creates a new game by resetting all score related values and by loading in new images into the grid.
		 * @author Philip Radojcic
		 * @version last updated 6/15/2014
		 */
		public void newGame(String mazeFileName) {

			// Reset all the score, button and after level screen related variables.
			time = 0;
			time2 = 0;
			KeyInInventory = 0;
			coinsInInventory = 0;
			buttonsPressed = 0;
			currentTime = 0;
			level_score = 0;
			currentPlayerName = "";
			doYouHaveTheKey = false;

			// reinitializes the Initial position of the player on the grid.
			currentRow = 13;
			currentColumn = 1;

			// Load up the file for the maze. (try catch, is for file io errors)
			try {
				// Find the size of the file first to size the array
				// Standard Java file input (better than hsa.TextInputFile)
				BufferedReader mazeFile = new BufferedReader(new FileReader(
						mazeFileName));

				// Assume file has at least 1 line.
				int noOfRows = 1;
				String rowStr = mazeFile.readLine();
				int noOfColumns = rowStr.length();

				// Read and count the rest of rows until the end of the file.
				String line;
				while ((line = mazeFile.readLine()) != null) {
					noOfRows++;
				}
				mazeFile.close();

				// Set up the array
				grid = new int[noOfRows][noOfColumns];

				// Load in the file data into the grid (Need to re-open first)
				// The maze uses letters for the grid by subtracting by a.
				mazeFile = new BufferedReader(new FileReader(mazeFileName));
				for (int row = 0; row < grid.length; row++) {
					rowStr = mazeFile.readLine();
					for (int column = 0; column < grid[0].length; column++) {
						grid[row][column] = (int) (rowStr.charAt(column) - 'a');
					}
				}
				mazeFile.close();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, mazeFileName
						+ " not a valid level file",
						"Message - Invalid Level File",
						JOptionPane.WARNING_MESSAGE);
				System.exit(0);
			}

		}

		// Inner class to handle key events.
		/** The keyHandler class.
		 * This class takes care of any occurrences involving the keyboard.
		 * @author Philip Radojcic
		 * @version last updated 6/15/2014
		 */
		private class KeyHandler extends KeyAdapter {
			/** The keyPressed method.
			 * This method takes care of all events that occur when any of the arrow keys are pressed.
			 * @author Philip Radojcic
			 * @version last updated 6/15/2014
			 */
			public void keyPressed(KeyEvent event) {
				// Change the currentRow and currentColumn of the player based on the key pressed.
				// When an arrow key is pressed, if the character sprite would not be moving onto a wall space.
				// If the space the player moves on is a button, the button image is changed into a used button image.
				if (event.getKeyCode() == KeyEvent.VK_LEFT) {
					playerImage = new ImageIcon(
							"Character Sprite Facing Left 3.png").getImage();
					if (grid[currentRow][currentColumn - 1] != 1
							&& grid[currentRow][currentColumn - 1] != 4) {
						currentColumn--;
						if (grid[currentRow][currentColumn] == 3) {
							grid[currentRow][currentColumn] = 2;
							buttonsPressed++;
						}
						coinCollecting();
					}
				} else if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
					playerImage = new ImageIcon(
							"Character Sprite Facing Right 3.png").getImage();
					if (grid[currentRow][currentColumn + 1] != 1
							&& grid[currentRow][currentColumn + 1] != 4) {
						currentColumn++;
						if (grid[currentRow][currentColumn] == 3) {
							grid[currentRow][currentColumn] = 2;
							buttonsPressed++;
						}
						coinCollecting();
					}
				} else if (event.getKeyCode() == KeyEvent.VK_UP) {
					playerImage = new ImageIcon(
							"Character Sprite Facing Backwards 3.png")
							.getImage();
					if (grid[currentRow - 1][currentColumn] != 1
							&& grid[currentRow - 1][currentColumn] != 4) {
						currentRow--;
						if (grid[currentRow][currentColumn] == 3) {
							grid[currentRow][currentColumn] = 2;
							buttonsPressed++;
						}
						coinCollecting();
					}
				} else if (event.getKeyCode() == KeyEvent.VK_DOWN) {
					playerImage = new ImageIcon(
							"2D Sprite General on blank space 4.png")
							.getImage();
					if (grid[currentRow + 1][currentColumn] != 1
							&& grid[currentRow + 1][currentColumn] != 4) {
						currentRow++;
						if (grid[currentRow][currentColumn] == 3) {
							grid[currentRow][currentColumn] = 2;
							buttonsPressed++;
						}
						coinCollecting();						
					}										
				}

				// Specifies which level interaction specific method is to be used. The correct one corresponding with the txt file.
				if (currentLevelName == "Level 1.txt")
					level_1();
				if (currentLevelName == "Level 2.txt")
					level_2();
				if (currentLevelName == "Level 3.txt")
					level_3();

				// When the grid image is a key, the key is collected and the image is changed to a blank tile image.
				if (grid[currentRow][currentColumn] == 6) {
					doYouHaveTheKey = true;
					grid[currentRow][currentColumn] = 0;
					KeyInInventory = 1;
				}
				
				// When you have the key and go onto the door image you beat the level and go to the winner's screen.
				if (grid[currentRow][currentColumn] == 7
						&& doYouHaveTheKey == true) {
					// YOU BEAT THE LEVEL.
					grid[currentRow][currentColumn] = 8;
					// Since the level is beaten, it stops the time.
					timer.stop();
					timer2.stop();
					currentTime = (int) (currentTime * 1.0);
					currentTime = ((double) time + time2) / 20;
					double level_Final_Time = currentTime;
					level_score = ((coinsInInventory * 20)
							+ (1000 - (int) (level_Final_Time * 20)) - (buttonsPressed * 14));
					// These next 2 ifs are to raise the scores of level 2 and 3, to
					// make them around the same as level 1. Just looks better.
					if (currentLevelName == "Level 2.txt")
						level_score += 300;
					if (currentLevelName == "Level 3.txt")
						level_score += 700;
					doYouHaveTheKey = false;
					// Goes to the winner's screen.
					screen = LEVEL_WON_SCREEN;
					// According to which txt file, when you beat any of the 5 spots
					// on the high scores for the level you beat, it asks for your name.
					if (currentLevelName == "Level 1.txt") {
						if (level_score > top5Scores[0][4])
							currentPlayerName = JOptionPane
									.showInputDialog("Please enter your name: ");
					}
					if (currentLevelName == "Level 2.txt") {
						if (level_score > top5Scores[1][4])
							currentPlayerName = JOptionPane
									.showInputDialog("Please enter your name: ");
					}
					if (currentLevelName == "Level 3.txt") {
						if (level_score > top5Scores[2][4])
							currentPlayerName = JOptionPane
									.showInputDialog("Please enter your name: ");
					}
					// This is the main part of the high score code, it places your winning score in the high scores
					// putting it ahead of the scores it beats and kicks out the 5th place score.
					int tempScore = 0;
					String tempPlayer = "";
					for (int scoreList = 4; scoreList >= 0
							&& level_score > top5Scores[currentLevelNumber][scoreList]; scoreList--) {
						tempScore = top5Scores[currentLevelNumber][scoreList];
						tempPlayer = top5Names[currentLevelNumber][scoreList];
						top5Names[currentLevelNumber][scoreList] = currentPlayerName;
						top5Scores[currentLevelNumber][scoreList] = level_score;
						if (scoreList != 4) {
							top5Names[currentLevelNumber][scoreList + 1] = tempPlayer;
							top5Scores[currentLevelNumber][scoreList + 1] = tempScore;
							// tempScore ++;
						}
					}
					//Saves the newly arranged high scores.
					save();
				}
				repaint();
			}
		}


		// Inner class to handle mouse events
		/** The mouseHandler class.
		 * This class takes care of any occurrences involving the mouse.
		 * @author Philip Radojcic
		 * @version last updated 6/15/2014
		 */
		private class MouseHandler extends MouseAdapter {
			/** The mousePressed method.
			 * This method takes care of all events that occur when you click with the mouse.
			 * Specifically all instances where you click with the mouse it is for buttons and screens.
			 * @author Philip Radojcic
			 * @version last updated 6/15/2014
			 */
			public void mousePressed(MouseEvent event) {

				int nextScreen = screen;

				// While you are on a level screen the main menu button works.
				if (screen == LEVEL1 || screen == LEVEL2 || screen == LEVEL3) {
					Rectangle inGameBackToMainMenu = new Rectangle(899, 691,
							300, 300);
					if (inGameBackToMainMenu.contains(event.getPoint())) {
						nextScreen = MAIN_MENU;
					}
				}

				// The main menu screen draws all the main menu's buttons and what screens you go to when they are clicked.
				if (screen == MAIN_MENU) {
					Rectangle mainMenusLevelsButton = new Rectangle(43, 256,
							245, 160);
					Rectangle mainMenusControlsButton = new Rectangle(850, 256,
							245, 160);

					Rectangle mainMenusHighScoreButton = new Rectangle(46, 454,
							256, 160);
					Rectangle mainMenusCreditsButton = new Rectangle(850, 454,
							245, 160);

					if (mainMenusControlsButton.contains(event.getPoint())) {
						nextScreen = CONTROL_SCREEN;
					}

					if (mainMenusLevelsButton.contains(event.getPoint())) {
						nextScreen = LEVEL_SCREEN;
					}

					if (mainMenusCreditsButton.contains(event.getPoint())) {
						nextScreen = CREDITS_SCREEN;
					}

					if (mainMenusHighScoreButton.contains(event.getPoint())) {
						nextScreen = HIGH_SCORE_SCREEN;
					}

				// Within the level screen to select what level to play.	
				} else if (screen == LEVEL_SCREEN) {
					Rectangle Level_1_Button = new Rectangle(69, 341, 265, 425);
					Rectangle Level_2_Button = new Rectangle(420, 341, 280, 425);
					Rectangle Level_3_Button = new Rectangle(780, 341, 280, 425);

					// For each button you are sent to the corresponding level (level 1 button goes to level 1)
					if (Level_1_Button.contains(event.getPoint())) {
						nextScreen = LEVEL1;
						currentLevelNumber = 0;
						newGame(currentLevelName = "Level 1.txt");
						timer.start();
						timer2.start();
						// Starts a new game and loads up the grid (sets size of
						// grid array)
					}
					if (Level_2_Button.contains(event.getPoint())) {
						nextScreen = LEVEL2;
						currentLevelNumber = 1;
						newGame(currentLevelName = "Level 2.txt");
						timer.start();
						timer2.start();
					}
					if (Level_3_Button.contains(event.getPoint())) {
						nextScreen = LEVEL3;
						currentLevelNumber = 2;
						newGame(currentLevelName = "Level 3.txt");
						timer.start();
						timer2.start();
					}
				}
				//On all screens with the back to main menu picture on the top right corner, the button will send you back to the main menu.
				if (screen == CONTROL_SCREEN || screen == LEVEL_SCREEN
						|| screen == HIGH_SCORE_SCREEN
						|| screen == CREDITS_SCREEN
						|| screen == LEVEL_WON_SCREEN) {
					Rectangle goBackToMainMenu = new Rectangle(700, 0, 1300,
							400);
					if (goBackToMainMenu.contains(event.getPoint())) {
						nextScreen = MAIN_MENU;
					}
				}
				screen = nextScreen;
				repaint();
			}
		}

		/** The timeEventHandler class.
		 * This class takes care of all the timer events.
		 * @author Philip Radojcic
		 * @version last updated 6/15/2014
		 */
		private class TimerEventHandler implements ActionListener {
			// The following method is called each time a timer event is
			// generated (every 100 milliseconds in this example)
			// Put your code here that handles this event
			public void actionPerformed(ActionEvent event) {
				time++;
				time2++;
				repaint();
			}
		}
	}
	
	// Sets up the main frame for the Game.
	public static void main(String[] args) {
		PuzzlePrisonBoard frame = new PuzzlePrisonBoard();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	} // main method
} // BoardGame class
