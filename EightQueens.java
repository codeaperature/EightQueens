/***************************************************************************
*
*  File Name: EightQueens
*  Author: Stephan Warren
*  
*  Description: A program to solve the 8 queens chess problem where
*  the 8 queens will peacefully coexist. This program specifically
*  uses recursive techniques to solve this problem.
*
*  Special Notes: (if any)
*    - Doesn't preform error checking or other points of interest.
*    - Will the program only work if compiled on certain platforms.
*    - etc.
*
*  Time estimates (optional)			Actual Time Spent:
*    Design:		60 min				120
*    Coding:		90					120
*    Testing:		30					120
*    Documentation: 10					20
*    Total Time:	190					380
*
*  NOTE: Most of the time was spent optimizing the process & finding
*  a way to look for unique solutions.
*****************************************************************************/
/*
*  PLANNING:
*
*  I decided to research algorithms & found 3 basic versions:
*
*  a. see if queen can be attacked by backtracking
*  b. mark attack paths & have the next queen avoid them
*  c. look ahead at final attack positions and avoid them
*
*	I found the 3rd to be the easiest to implement, fastest to execute,
*   uses the least stack & I could adapt it to be even faster.
*
*  SCHEMA: The following schema is used for all of the methods
*  in this file -- This is fixed in our problem definition.
*
*  	- Rows & Columns are labelled as 1-8
* 	- Diagnals can be back or forward (as backslash and foreslash) &
*     these are may be expressed as row+col or col-row (with offsets
*	  for convenient array storage
*
*
*  STRATEGY: I used Niklaus Wirth's algorithm with a self-designed
*  adaptation that enhances solving speed / iterations by avoiding
*  solutions of left <> right mirrors. Furthermore, I eliminate all
*  solutions that are rotations (90, 180 or 270 degrees) in addition to
*  up <> down mirrors. I used an Isomorphicity check to determine if
*  solution is unique. (For more info on the algorithm, see ref http:
*  http://www.mactech.com/articles/mactech/Vol.13/13.12/
*  TheEightQueensProblem/)
*
*
*  1) It is impossible to have 2 queens on the same row so I
*  put a queen on each row & thus I only need to iterate for
*  each row. This reduces the search down fom 64! to 8^8 searches.
*  Furthermore this reduces my stack depth. (This means only one
*  queen can exist per row.)
*
*  2) Rather than placing a queen to see if it is under attack,
*  I mark attack path ends as I lay down queens -- avoiding
*  redundant checks of whether a queen can be attacked.
*  This saves time & code.
*
*  3) It is seemingly logical to keep track of which columns where
*  used, but this can be a tedious task & my attack paths (#2) idea
*  essentially does this. Yes - I iterate across the attack-able
*  columns, but these are quickly discounted (as they are not free).
*  (Further reducing our search from 8 ^ 8 to 8!.)
*
*  4) Any solutions found will be mirrored (left to right) so I
*  will only search the first 4 squares of the 1st row and thus
*  reduce the number of searches to 8!/2 or 4 * 7!.
*
*  NOTE: I wonder if I can cut more corners on top/bottom or diagonal
*  mirrors... (Obviously this board is so symetric that top &
*  bottom can be folded, board can be rotated 90, 180 or 270 degrees, as
*  well as folded along each diagonal.)
*
*  5) Since solutions are found in increasing order, I know that if
*  solution array can be rotated/mirrored to a lesser vector, the
*  solution was already established (printed). See paper by Paul Cull
*  & Rajeev Pandey -- Isomorphism & the N-Queens Problem. (Ref Http:
*  http://www.cs.orst.edu/~rpandey/papers/nqueens.ps )
*
*
*****************************************************************************/


  //   EightQueens - This is the main class to solve the problem
  //  pre:  no inputs - solves a sepcific problem
  //
  //  post: changes nothing
  //
  //  returns: All solutions
  //

public class EightQueens
{
  //  main only initializes the main board & then recurses to
  //  placeQueen
  //
  //  pre:  Args are accepted but nothing is done with them
  //
  //  post: no arg changes
  //
  //  returns: exits with 0
  //
  //
	public static void main(String args[])
	{
		// track - pun name keep track of our queen placements
		// and attack 'tracks'

		// track[0]     => unique solutions
		// track[1-8]   => col values (& our final solutions)
		// track[9-23]  => fore diagonal
		// track[24-38] => back diagonal
		// track[39]    => empty entry
		int track[] = new int[40];
		// set track to empty
		for(int j = 0; j < 40; j++) track[j] = 0;

		// Assume we have solutions
		System.out.println("Stephan Warren's Non-Isomorphic solutions " +
		            "to the 8 queens chess problem:\n");
		System.out.println("Problem: Arrange 8 queens so that no queen is under attack.\n");
		System.out.println("Note: Rotated & mirrored solutions will not "
		 		+ "be shown.\n");

	    // recurse starting with queen #1 on row #1
		placeQueen(1, track);
        System.out.println("Authored by Stephan Warren");
   
        System.exit(0);

	}

  //  showQueensOnBoard is used for displaying results
  //
  //  pre: submit a title and the board for printing
  //
  //  post: no arg changes
  //
  //  returns: none
  //
  //
	public static void showQueensOnBoard(String title, int board[])
	{
		System.out.println(title);
		for(int i = 1; i <= 8 ; i++) {
	        // '*' - queen on square -- see markAttackPaths for info
	        // '.' - empty/available square
	        // add a black or \n if at the end of a row
	        for(int j = 1; j <= 8 ; j++) {
				System.out.print((board[i] == j) ? "* " : ". ");
			}
 			System.out.println("");
		}
		System.out.println("");
	}


  //  checkLesserArray -- recursive check if array is 'alphabetically less'
  //
  //  pre: submit both arrays (original vs transformed)
  //
  //  post: no arg changes
  //
  //  returns: true if lesser or identity was found
  //
  //
	public static boolean isArrayLesser(int queen, int orig[], int xform[])
	{
		if(queen == 9)	return(true); // identities match (equal)
		// if element is equal, recurse to next element
		if(orig[queen] == xform[queen])
			return(isArrayLesser(++queen, orig, xform));
		// less (true) or greater (false)
		return((orig[queen] < xform[queen]) ? true : false);
	}

  //  checkIsomorphic is used for check if solution is unique by rotating
  //  & mirroring (in an RRRMRRR sequence) to ensure we did not already
  //  select this array as a previous solution
  //
  //  pre: submit an array
  //
  //  post: no arg changes
  //
  //  returns: true if isomorphic
  //
  //

	public static boolean checkIsomorphic(int original[])
	{
	int tempboard[] = new int[9]; // temp board to avoid temp swap issues
	int translate[] = new int[9]; // translated board
	int i, j;

		// copy board -- need swappable space for translations
		for(j = 1; j <= 8; j++) tempboard[j] = original[j];

        // use: RRRMRRR scheme
		for(i = 0; i < 7; i++) {
			if(i == 3) { // mirror u <> d:
				for(j = 1; j <= 8; j++) translate[9 - j] = tempboard[j];
			}
			else {	// rotate on i = 0,1,2, 4,5,6
				for(j = 1; j <= 8; j++) translate[tempboard[j]] = 9 - j;
			}
			// did we find we already printed this solution?
			if(!isArrayLesser(1, original, translate)) {
				return(true); // we did
			}
			// make copy
			for(j = 1; j <= 8; j++) tempboard[j] = translate[j];

		}
		return(false); // nope a new solution
	}
  //  placeQueen to choose available squares to place queens
  //
  //  pre: queen number & last used board are passed (that has
  //  all attach paths marked.
  //
  //  post: none - recursive - no changes to var
  //
  //  returns: none
  //
  //
    // find a better way - static int within funct like C++???
 	public static void placeQueen(int queen, int track[])
	{
		int end_sq;   // last square to search on a row
		int fwd_diag; // forward diagonal mark positions
		int bck_diag; // back diagonal mark positions

	    // Have we reached the end / solved? -- print results
		if(queen == 9) {
			// Since our solution matrix was tracked by column
			// sequencing & we need it by rows in order to do our
			// check, we swap them for our isomorphicity check
			int row_col_swapped[] = new int[9];
			for(int i = 1; i <= 8; i++) row_col_swapped[track[i]] = i;
			// add to unique solutions if non-isomorphic
			if(!checkIsomorphic(row_col_swapped)) {
				showQueensOnBoard("Unique Solution #" + (++track[0]) + ": ",
						row_col_swapped);
			} // bracket for non-other than clarity
			return;
		}

		// start at the beginning of the row
		// end at 4th square if zero -- see strategy at top of file
		// or end at the end of the row
		end_sq = (queen == 1) ? 4 : 8;
		for(int i = 1; i <= end_sq; i++) {
			// If empty square ...
			if(track[i] == 0) {
				// look at far-side diagonals for possible attacks
				// row = queen, col = i
				fwd_diag = 31 + i - queen; // see definition of track
				bck_diag = 7 + i + queen;  // see definition of track

				// check for attacks along diagonals -- if none ...
				if((track[fwd_diag] == 0) && (track[bck_diag] == 0)) {
					// place queen & mark attacks
					track[i] = track[fwd_diag] = track[bck_diag] = queen;
					// then check next queen
					placeQueen(queen + 1, track);
					// if returned -- either time for next try or no queen
					// could be laid in a lower level square
					track[i] = track[fwd_diag] = track[bck_diag] = 0;
				}
			}
		} // where done iterating columns in this row
	} // placeQueen
} // EightQueens class def
