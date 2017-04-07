import java.util.ArrayList;
import java.util.Arrays;

public class Node {
    private int[] board; //order of tiles
    private int direction; //last move's direction that was made to get to this state
    private int distance; //number of moves that was made to get to this state

    public Node(int[] board) {
        this(board, -1, 0);
    }

    public Node(int[] board, int direction, int distance) {
        this.board = board.clone();
        this.direction = direction;
        this.distance = distance;
    }

    public int getIndex() { //computes a number between 0..362,879, mapped from permutation
        int[] invertions = boardToInvertions(board);

        int index = 0, tmp = 1;
        for (int i = 8; i >= 0; --i) {
            index += invertions[i] * tmp;
            tmp *= 9-i;
        }
        return index;
    }

    public int[] getBoard() {
        return board;
    }

    public int getDirection() {
        return direction;
    }

    public String getDirectionString() { //returns direction of the move as a letter
        switch (direction) {
            case 0: return "L";
            case 1: return "D";
            case 2: return "U";
            case 3: return "R";
        }
        return "NA";
    }

    public int getDistance() {
        return distance;
    }

    public int getNumberOfMisplacedTiles() { //computes number of misplaced tiles
        int res = 0;
        for (int i = 0; i < 9; ++i)
            if (board[i] != 9 && board[i] != i+1)
                res++;
        return res;
    }

    public int getCost() { //computes cost of the state required for comparator of priority queue in ASTAR
        return  getDistance() + getNumberOfMisplacedTiles();
    }

    public boolean isSolvable() { //check if board is solvable (number of inversions is even or not)
        int[] invertions = boardToInvertions(board);
        int invertionsCnt = 0;
        for (int i = 0; i < 9; ++i)
            if (board[i] != 9)
                invertionsCnt += invertions[i];
        return invertionsCnt % 2 == 0;
    }

    public ArrayList<Node> getChildren() {
        int blankX = -10, blankY = -10; //coordinates of the blank tile (x, y)
        for (int i = 0; i < 9; ++i)
            if (board[i] == 9) {
                blankY = i/3;
                blankX = i%3;
            }
        int[] dx = {-1, 0, 0, 1}; //offsets for left, down, up, right moves
        int[] dy = {0, 1, -1, 0};

        ArrayList<Node> children = new ArrayList<>(); //array of possible child states

        for (int i = 0; i < 4; ++i) {
            int nextX = blankX + dx[i], nextY = blankY + dy[i]; //new position of the blank tile after move in direction 'i'
            if (nextX < 0 || nextX > 2 ||
                nextY < 0 || nextY > 2) //if the position is invalid, skip this child
                continue;

            int[] nextBoard = board.clone();
            nextBoard[blankY*3 + blankX] = board[nextY*3 + nextX]; //moving tile (not blank) to the old position of the blank tile (blankX, blankY)
            nextBoard[nextY*3 + nextX] = 9; //moving the blank tile

            children.add(new Node(nextBoard, i, distance+1)); //adding new state to the list of children
        }

        return children;
    }

    @Override
    public boolean equals(Object o) { //checks if two states are equal (equality of boards)
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return Arrays.equals(board, node.board);
    }

    private int[] boardToInvertions(int[] board) { //converts board to its inversions
        int[] invertions = new int[9];
        for (int i = 0; i < 9; ++i) {
            invertions[i] = 0;
            for (int j = i+1; j < 9; ++j)
                if (board[j] < board[i])
                    invertions[i]++;
        }
        return invertions;
    }
}
