import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class Maze extends JFrame {
    private static final int ROWS = 30;
    private static final int COLS = 40;

    private MazeGenerator generator;
    private MazeSolver solver;
    private MazePanel panel;
    private MazeController controller;

    private JLabel penaltyLabel;
    private JLabel algorithmLabel;
    private JSlider speedSlider;

    public Maze() {
        setTitle("Maze Project - Multi Finish Lines (3 Targets)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        generator = new MazeGenerator(ROWS, COLS);
        generator.generateMaze();

        solver = new MazeSolver(generator.getGrid(), ROWS, COLS);

        panel = new MazePanel(ROWS, COLS);
        panel.setGrid(generator.getGrid());
        panel.setDestinations(generator.getDestinations());

        controller = new MazeController(generator, solver, panel, this);

        setupUI();
        controller.setAnimationSpeed(speedSlider.getValue() / 100.0);

        pack();
        setLocationRelativeTo(null);
    }

    private void setupUI() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 12));
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 12));

        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel mazeContainer = new JPanel(new BorderLayout());
        mazeContainer.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        mazeContainer.add(panel, BorderLayout.CENTER);
        add(mazeContainer, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));

        buttonPanel.add(createStyledButton("BFS", e -> controller.solveBFSAnimated()));
        buttonPanel.add(createStyledButton("DFS", e -> controller.solveDFSAnimated()));
        buttonPanel.add(createStyledButton("Dijkstra", e -> controller.solveDijkstraAnimated()));
        buttonPanel.add(createStyledButton("A*", e -> controller.solveAStarAnimated()));

        JButton resetButton = createStyledButton("New Maze", e -> controller.generateNewMaze());
        resetButton.setBackground(new Color(240, 240, 255));
        buttonPanel.add(resetButton);

        add(buttonPanel, BorderLayout.SOUTH);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        infoPanel.setPreferredSize(new Dimension(180, 0));

        JPanel speedPanel = new JPanel(new BorderLayout());
        speedPanel.setBorder(BorderFactory.createTitledBorder("Animation Speed"));
        speedPanel.setMaximumSize(new Dimension(200, 80));

        speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 80);
        speedSlider.setFocusable(false);
        speedSlider.addChangeListener(e -> controller.setAnimationSpeed(speedSlider.getValue() / 100.0));
        speedPanel.add(speedSlider, BorderLayout.CENTER);
        infoPanel.add(speedPanel);
        infoPanel.add(Box.createVerticalStrut(15));

        JPanel legendPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        legendPanel.setBorder(BorderFactory.createTitledBorder("Legend"));
        legendPanel.setMaximumSize(new Dimension(200, 180));
        legendPanel.add(createLegendLabel("Start", new Color(50, 205, 50)));
        legendPanel.add(createLegendLabel("Finish (x3)", new Color(255, 215, 0)));
        legendPanel.add(createLegendLabel("Grass", new Color(144, 238, 144)));
        legendPanel.add(createLegendLabel("Mud", new Color(139, 119, 101)));
        legendPanel.add(createLegendLabel("Water", new Color(100, 149, 237)));
        infoPanel.add(legendPanel);
        infoPanel.add(Box.createVerticalStrut(15));

        JPanel statsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Statistics"));
        statsPanel.setMaximumSize(new Dimension(200, 80));
        algorithmLabel = new JLabel("Algorithm: -");
        penaltyLabel = new JLabel("Total Penalty: 0");
        statsPanel.add(algorithmLabel);
        statsPanel.add(penaltyLabel);
        infoPanel.add(statsPanel);

        infoPanel.add(Box.createVerticalGlue());
        add(infoPanel, BorderLayout.EAST);
    }

    private JButton createStyledButton(String text, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.addActionListener(action);
        return btn;
    }

    private JLabel createLegendLabel(String text, Color color) {
        JLabel label = new JLabel("  " + text);
        label.setOpaque(true);
        label.setBackground(color);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        return label;
    }

    public void updatePenaltyDisplay(int penalty, String algorithm) {
        penaltyLabel.setText("Total Penalty: " + penalty);
        algorithmLabel.setText("Algorithm: " + algorithm);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> { new Maze().setVisible(true); });
    }

    // ==================== INNER CLASSES ====================

    // --- CONTROLLER ---
    public static class MazeController {
        private MazeGenerator generator;
        private MazeSolver solver;
        private MazePanel panel;
        private Maze mainFrame;
        private Timer currentTimer;
        private int currentDelay = 30;

        public MazeController(MazeGenerator generator, MazeSolver solver, MazePanel panel, Maze mainFrame) {
            this.generator = generator;
            this.solver = solver;
            this.panel = panel;
            this.mainFrame = mainFrame;
        }

        public void setAnimationSpeed(double speedRatio) {
            int minDelay = 1; int maxDelay = 150;
            this.currentDelay = maxDelay - (int)(speedRatio * (maxDelay - minDelay));
            if (currentTimer != null && currentTimer.isRunning()) currentTimer.setDelay(currentDelay);
        }

        private void stopCurrentAnimation() {
            if (currentTimer != null && currentTimer.isRunning()) currentTimer.stop();
        }

        private Cell getStart() { return generator.getGrid()[0][0]; }
        private List<Cell> getTargets() { return generator.getDestinations(); }

        public void solveDijkstraAnimated() {
            prepareSolve();
            MazeSolver.SolveResult result = solver.solveDijkstra(getStart(), getTargets());
            if(result.reachedTarget != null) solver.reconstructPath(result.parent, result.reachedTarget);
            animateExploration(result.explorationOrder, "Dijkstra");
        }

        public void solveAStarAnimated() {
            prepareSolve();
            MazeSolver.SolveResult result = solver.solveAStar(getStart(), getTargets());
            if(result.reachedTarget != null) solver.reconstructPath(result.parent, result.reachedTarget);
            animateExploration(result.explorationOrder, "A*");
        }

        public void solveBFSAnimated() {
            prepareSolve();
            MazeSolver.SolveResult result = solver.solveBFS(getStart(), getTargets());
            if(result.reachedTarget != null) solver.reconstructPath(result.parent, result.reachedTarget);
            animateExploration(result.explorationOrder, "BFS");
        }

        public void solveDFSAnimated() {
            prepareSolve();
            MazeSolver.SolveResult result = solver.solveDFS(getStart(), getTargets());
            if(result.reachedTarget != null) solver.reconstructPath(result.parent, result.reachedTarget);
            animateExploration(result.explorationOrder, "DFS");
        }

        private void prepareSolve() {
            stopCurrentAnimation();
            panel.setExploredCells(new HashSet<>());
            panel.setShortestPath(new ArrayList<>());
            panel.repaint();
            solver.reset();
        }

        private void animateExploration(List<Cell> explorationOrder, String algorithmName) {
            final int[] index = {0};
            Set<Cell> currentExplored = new HashSet<>();
            currentTimer = new Timer(currentDelay, null);
            currentTimer.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    currentTimer.setDelay(currentDelay);
                    int stepsPerFrame = (currentDelay < 5) ? 5 : 1;
                    for(int k=0; k<stepsPerFrame; k++) {
                        if (index[0] < explorationOrder.size()) {
                            currentExplored.add(explorationOrder.get(index[0]));
                            index[0]++;
                        } else {
                            currentTimer.stop();
                            panel.setShortestPath(solver.getShortestPath());
                            panel.repaint();
                            mainFrame.updatePenaltyDisplay(solver.getTotalPenalty(), algorithmName);
                            return;
                        }
                    }
                    panel.setExploredCells(new HashSet<>(currentExplored));
                    panel.repaint();
                }
            });
            currentTimer.start();
        }

        public void generateNewMaze() {
            stopCurrentAnimation();
            generator.reset();
            generator.generateMaze();
            solver.reset();
            panel.setGrid(generator.getGrid());
            panel.setDestinations(generator.getDestinations());
            panel.setExploredCells(new HashSet<>());
            panel.setShortestPath(new ArrayList<>());
            panel.repaint();
            mainFrame.updatePenaltyDisplay(0, "-");
        }
    }

    // --- GENERATOR ---
    public static class MazeGenerator {
        private int rows;
        private int cols;
        private Cell[][] grid;
        private List<Cell> destinations;

        public MazeGenerator(int rows, int cols) {
            this.rows = rows;
            this.cols = cols;
            this.grid = new Cell[rows][cols];
            this.destinations = new ArrayList<>();
            initializeGrid();
        }

        private void initializeGrid() {
            Random rand = new Random();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    grid[i][j] = new Cell(i, j);
                    if (!(i == 0 && j == 0)) {
                        int terrainChance = rand.nextInt(100);
                        if (terrainChance < 10) grid[i][j].setTerrainType(Cell.TerrainType.GRASS);
                        else if (terrainChance < 20) grid[i][j].setTerrainType(Cell.TerrainType.MUD);
                        else if (terrainChance < 30) grid[i][j].setTerrainType(Cell.TerrainType.WATER);
                    }
                }
            }
        }

        public void generateMaze() {
            UnionFind uf = new UnionFind(rows * cols);
            List<Edge> edges = createAllEdges();
            Collections.shuffle(edges);
            for (Edge edge : edges) {
                Cell c1 = edge.getCell1();
                Cell c2 = edge.getCell2();
                int id1 = c1.getRow() * cols + c1.getCol();
                int id2 = c2.getRow() * cols + c2.getCol();
                if (!uf.isConnected(id1, id2)) {
                    uf.union(id1, id2);
                    c1.removeWallBetween(c2);
                }
            }
            generateDestinations();
        }

        private void generateDestinations() {
            destinations.clear();
            Random rand = new Random();
            Set<Cell> used = new HashSet<>();
            used.add(grid[0][0]);

            while (destinations.size() < 3) {
                int r = rand.nextInt(rows);
                int c = rand.nextInt(cols);
                if (r < 5 && c < 5) continue;

                Cell candidate = grid[r][c];
                if (!used.contains(candidate)) {
                    used.add(candidate);
                    destinations.add(candidate);
                }
            }
        }

        private List<Edge> createAllEdges() {
            List<Edge> edges = new ArrayList<>();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (i < rows - 1) edges.add(new Edge(grid[i][j], grid[i + 1][j]));
                    if (j < cols - 1) edges.add(new Edge(grid[i][j], grid[i][j + 1]));
                }
            }
            return edges;
        }

        public void reset() { initializeGrid(); }
        public Cell[][] getGrid() { return grid; }
        public int getRows() { return rows; }
        public int getCols() { return cols; }
        public List<Cell> getDestinations() { return destinations; }
    }

    // --- SOLVER ---
    public static class MazeSolver {
        private Cell[][] grid;
        private int rows, cols;
        private boolean[][] visited;
        private Set<Cell> exploredCells = new HashSet<>();
        private List<Cell> shortestPath = new ArrayList<>();
        private int totalPenalty;

        public MazeSolver(Cell[][] grid, int rows, int cols) {
            this.grid = grid; this.rows = rows; this.cols = cols;
            this.visited = new boolean[rows][cols];
        }

        public SolveResult solveDijkstra(Cell start, List<Cell> targets) {
            resetVisited();
            PriorityQueue<CellDistance> pq = new PriorityQueue<>();
            Map<Cell, Cell> parent = new HashMap<>();
            Map<Cell, Integer> distance = new HashMap<>();
            List<Cell> explOrder = new ArrayList<>();

            for(int i=0; i<rows; i++) for(int j=0; j<cols; j++) distance.put(grid[i][j], Integer.MAX_VALUE);
            distance.put(start, 0); pq.offer(new CellDistance(start, 0)); parent.put(start, null);

            while (!pq.isEmpty()) {
                Cell current = pq.poll().cell;
                if (visited[current.getRow()][current.getCol()]) continue;
                visited[current.getRow()][current.getCol()] = true;
                explOrder.add(current);

                if (targets.contains(current)) {
                    return new SolveResult(parent, explOrder, distance.get(current), current);
                }

                for (Cell neighbor : getAccessibleNeighbors(current)) {
                    if (!visited[neighbor.getRow()][neighbor.getCol()]) {
                        int newDist = distance.get(current) + neighbor.getTerrainPenalty();
                        if (newDist < distance.get(neighbor)) {
                            distance.put(neighbor, newDist);
                            parent.put(neighbor, current);
                            pq.offer(new CellDistance(neighbor, newDist));
                        }
                    }
                }
            }
            return new SolveResult(parent, explOrder, 0, null);
        }

        public SolveResult solveAStar(Cell start, List<Cell> targets) {
            resetVisited();
            PriorityQueue<AStarNode> pq = new PriorityQueue<>();
            Map<Cell, Cell> parent = new HashMap<>();
            Map<Cell, Integer> gScore = new HashMap<>();
            List<Cell> explOrder = new ArrayList<>();

            for(int i=0; i<rows; i++) for(int j=0; j<cols; j++) gScore.put(grid[i][j], Integer.MAX_VALUE);
            gScore.put(start, 0);
            pq.offer(new AStarNode(start, 0, minHeuristic(start, targets)));
            parent.put(start, null);

            while (!pq.isEmpty()) {
                Cell current = pq.poll().cell;
                if (visited[current.getRow()][current.getCol()]) continue;
                visited[current.getRow()][current.getCol()] = true;
                explOrder.add(current);

                if (targets.contains(current)) {
                    return new SolveResult(parent, explOrder, gScore.get(current), current);
                }

                for (Cell neighbor : getAccessibleNeighbors(current)) {
                    if (!visited[neighbor.getRow()][neighbor.getCol()]) {
                        int tentative = gScore.get(current) + neighbor.getTerrainPenalty();
                        if (tentative < gScore.get(neighbor)) {
                            parent.put(neighbor, current);
                            gScore.put(neighbor, tentative);
                            pq.offer(new AStarNode(neighbor, tentative, tentative + minHeuristic(neighbor, targets)));
                        }
                    }
                }
            }
            return new SolveResult(parent, explOrder, 0, null);
        }

        public SolveResult solveBFS(Cell start, List<Cell> targets) {
            resetVisited();
            Queue<Cell> queue = new LinkedList<>();
            Map<Cell, Cell> parent = new HashMap<>();
            List<Cell> explOrder = new ArrayList<>();
            queue.offer(start); visited[start.getRow()][start.getCol()] = true; parent.put(start, null);

            while (!queue.isEmpty()) {
                Cell current = queue.poll();
                explOrder.add(current);
                if (targets.contains(current)) return new SolveResult(parent, explOrder, 0, current);

                for (Cell neighbor : getAccessibleNeighbors(current)) {
                    if (!visited[neighbor.getRow()][neighbor.getCol()]) {
                        visited[neighbor.getRow()][neighbor.getCol()] = true;
                        parent.put(neighbor, current);
                        queue.offer(neighbor);
                    }
                }
            }
            return new SolveResult(parent, explOrder, 0, null);
        }

        public SolveResult solveDFS(Cell start, List<Cell> targets) {
            resetVisited();
            Stack<Cell> stack = new Stack<>();
            Map<Cell, Cell> parent = new HashMap<>();
            List<Cell> explOrder = new ArrayList<>();
            stack.push(start); visited[start.getRow()][start.getCol()] = true; parent.put(start, null);

            while (!stack.isEmpty()) {
                Cell current = stack.pop();
                explOrder.add(current);
                if (targets.contains(current)) return new SolveResult(parent, explOrder, 0, current);

                for (Cell neighbor : getAccessibleNeighbors(current)) {
                    if (!visited[neighbor.getRow()][neighbor.getCol()]) {
                        visited[neighbor.getRow()][neighbor.getCol()] = true;
                        parent.put(neighbor, current);
                        stack.push(neighbor);
                    }
                }
            }
            return new SolveResult(parent, explOrder, 0, null);
        }

        private int minHeuristic(Cell a, List<Cell> targets) {
            int minH = Integer.MAX_VALUE;
            for(Cell t : targets) {
                int dist = Math.abs(a.getRow() - t.getRow()) + Math.abs(a.getCol() - t.getCol());
                if (dist < minH) minH = dist;
            }
            return minH;
        }

        private List<Cell> getAccessibleNeighbors(Cell cell) {
            List<Cell> n = new ArrayList<>();
            int r=cell.getRow(), c=cell.getCol();
            if(!cell.hasTopWall() && r>0) n.add(grid[r-1][c]);
            if(!cell.hasRightWall() && c<cols-1) n.add(grid[r][c+1]);
            if(!cell.hasBottomWall() && r<rows-1) n.add(grid[r+1][c]);
            if(!cell.hasLeftWall() && c>0) n.add(grid[r][c-1]);
            return n;
        }

        public void reconstructPath(Map<Cell, Cell> parent, Cell reachedTarget) {
            shortestPath.clear(); totalPenalty=0; Cell curr=reachedTarget;
            while(curr!=null) { shortestPath.add(curr); totalPenalty+=curr.getTerrainPenalty(); curr=parent.get(curr); }
            Collections.reverse(shortestPath);
        }

        private void resetVisited() { for(int i=0; i<rows; i++) Arrays.fill(visited[i], false); }
        public void reset() { resetVisited(); exploredCells.clear(); shortestPath.clear(); totalPenalty=0; }
        public List<Cell> getShortestPath() { return shortestPath; }
        public int getTotalPenalty() { return totalPenalty; }

        static class SolveResult {
            Map<Cell, Cell> parent;
            List<Cell> explorationOrder;
            int algorithmPenalty;
            Cell reachedTarget;

            SolveResult(Map<Cell, Cell> p, List<Cell> e, int ap, Cell rt) {
                parent=p; explorationOrder=e; algorithmPenalty=ap; reachedTarget=rt;
            }
        }
        static class CellDistance implements Comparable<CellDistance> {
            Cell cell; int distance;
            CellDistance(Cell c, int d) { cell=c; distance=d; }
            public int compareTo(CellDistance o) { return Integer.compare(distance, o.distance); }
        }
        static class AStarNode implements Comparable<AStarNode> {
            Cell cell; int gScore, fScore;
            AStarNode(Cell c, int g, int f) { cell=c; gScore=g; fScore=f; }
            public int compareTo(AStarNode o) { return Integer.compare(fScore, o.fScore); }
        }
    }
}