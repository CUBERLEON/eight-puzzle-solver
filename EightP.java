import java.util.*;

public class EightP
{
    public static enum SearchType {
        BREADTH_FIRST,
        ASTAR
    };
    public static enum HeuristicType {
        COUNT_MISPLACED_TILES
    };

    public static void main(String[] args) {
        SearchType searchType = SearchType.BREADTH_FIRST; //default search type is breadth first
        HeuristicType heuristicType = HeuristicType.COUNT_MISPLACED_TILES; //default heuristic type is count misplaced tiles
        int maxNodes = Integer.MAX_VALUE; //default limit of processed states is infinity

        //parsing command arguments
        try {
            for (int i = 0; i+1 < args.length; i += 2) { //loop through arguments (pairs: -type value)
                if (args[i].equals("-s")) {
                    if (args[i+1].equals("BreadthFirst"))
                        searchType = SearchType.BREADTH_FIRST;
                    else if (args[i+1].equals("ASTAR"))
                        searchType = SearchType.ASTAR;
                    else
                        throw new Exception(args[i+1] + " is unknown search type."); //throwing excepting that specified search type is unknown
                } else if (args[i].equals("-h")) {
                    if (args[i+1].equals("CountMisplacedTiles"))
                        heuristicType = HeuristicType.COUNT_MISPLACED_TILES;
                    else
                        throw new Exception(args[i+1] + " is unknown heuristic type.");
                } else if (args[i].equals("-m")) {
                    maxNodes = Integer.parseInt(args[i+1]); //parsing limit of nodes
                } else
                    throw new Exception(args[i] + " is unknown argument."); //throwing exception that specified argument type is unknown
            }
        } catch (Exception e) {
            System.out.println("Invalid arguments! " + e.getMessage()); //writing error message and exiting with error code 1
            System.exit(1);
        }

        //reading start board
        int[] startBoard = new int[9];
        Scanner scanner = new Scanner(System.in);
        for (int i = 0; i < 9; ++i) {
            if (scanner.hasNextInt())
                startBoard[i] = scanner.nextInt();
            else {
                System.out.println("Invalid board data!"); //if file doesn't contain required 9 integers print error message and exit
                System.exit(1);
            }
        }
        Node start = new Node(startBoard);

        //building finish node
        int[] finishBoard = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        Node finish = new Node(finishBoard);

        //checking for solvability
        if (!start.isSolvable()) { //checking if board is solvable using parity of its inversions
            System.out.println("No solution!"); //printing message and exiting
            System.exit(0);
        }

        //solving puzzle
        int startIndex = start.getIndex(), finishIndex = finish.getIndex(); //start and finish indices (index is a number between 0..362,879, mapped from permutation)
        boolean maxNodesReached = false, solutionFound = false;

        Node[] parentNode = new Node[363000]; //array of references to parent nodes by their indices
        int finishDirection = -1; //required to record last move's direction (there is no information about the last state in parentNode)

        int visitedNodes = 0; //number of processed states

        if (searchType == SearchType.BREADTH_FIRST) {
            Queue<Node> queue = new LinkedList<>(); //queue of states
            queue.add(start);

            boolean[] visited = new boolean[363000];
            visitedNodes = 1;
            visited[startIndex] = true; //marking starting state as visited

            while (!queue.isEmpty() && !maxNodesReached && !solutionFound) { //while there are some states to process, limit of states isn't reached and solution isn't found
                Node current = queue.remove(); //getting state from the head of the queue

                for (Node child : current.getChildren()) {
                    if (visited[child.getIndex()]) //if child state is visited, optimal way to it is already found
                        continue;

                    queue.add(child); //putting child state to the tail of the queue

                    parentNode[child.getIndex()] = current;

                    if (child.equals(finish)) { //checking if child state is finishing state
                        solutionFound = true;
                        finishDirection = child.getDirection(); //saving last move's direction
                    }

                    visited[child.getIndex()] = true;
                    visitedNodes++;
                    if (visitedNodes >= maxNodes) //checking if limit of states isn reached
                        maxNodesReached = true;
                }
            }
        } else if (searchType == SearchType.ASTAR) {
            if (heuristicType == HeuristicType.COUNT_MISPLACED_TILES) {
                Queue<Node> queue = new PriorityQueue<>(new Comparator<Node>() { //creating priority queue of states
                    @Override
                    public int compare(Node n1, Node n2) { //custom comparator for priority queue (sorts by cost of the state #cost = #number_of_moves + #number_of_misplaced_tiles)
                        return n1.getCost() - n2.getCost();
                    }
                });

                queue.add(start);
                visitedNodes = 0;

                int[] distance = new int[363000];
                Arrays.fill(distance, Integer.MAX_VALUE);

                while (!queue.isEmpty() && !maxNodesReached && !solutionFound) {
                    Node current = queue.remove();

                    if (current.equals(finish)) { //checking if current is finishing state
                        solutionFound = true;
                        finishDirection = current.getDirection(); //saving last move's direction
                    }

                    visitedNodes++;
                    if (visitedNodes >= maxNodes) //checking if limit of states is reached
                        maxNodesReached = true;

                    for (Node child : current.getChildren()) {
                        if (child.getDistance() < distance[child.getIndex()]) { //if distance to child state can be improved
                            queue.add(child);

                            parentNode[child.getIndex()] = current;
                            distance[child.getIndex()] = child.getDistance();
                        }
                    }
                }
            }
        }

        //limit of nodes reached
        if (maxNodesReached || !solutionFound) { //checking if limit of state is reached
            System.out.println("Not Found");
            System.exit(0);
        }

        //solution was found
        if (solutionFound) { //checking if solution is found
            //getting moves in solution
            LinkedList<Node> solution = new LinkedList<>(); //list that stores order of states in the solution

            Node curNode = new Node(finish.getBoard(), finishDirection, -1);
            solution.addFirst(curNode);
            while (curNode.getIndex() != startIndex) { //while current state isn't starting state
                curNode = parentNode[curNode.getIndex()]; //go to the parent state
                solution.addFirst(curNode); //add it to the list
            }

            //printing solution
            for (Node node : solution) {
                if (node.getDirection() != -1)
                    System.out.println(node.getDirectionString());
                for (int i = 0; i < 9; ++i) {
                    System.out.print(node.getBoard()[i] + " ");
                    if (i % 3 == 2)
                        System.out.println();
                }
            }
        }
//        System.out.println(visitedNodes);
    }
}
