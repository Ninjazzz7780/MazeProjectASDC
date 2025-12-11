import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MazePanel extends JPanel {
    private static final int CELL_SIZE = 20; // Ukuran disesuaikan agar muat di layar
    private static final int MARGIN = 20;

    private Cell[][] grid;
    private int rows;
    private int cols;
    private Set<Cell> exploredCells;
    private List<Cell> shortestPath;
    private List<Cell> destinations; // List untuk menampung 3 titik finish

    public MazePanel(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.exploredCells = new HashSet<>();
        this.shortestPath = new ArrayList<>();
        this.destinations = new ArrayList<>();

        int width = cols * CELL_SIZE + (MARGIN * 2);
        int height = rows * CELL_SIZE + (MARGIN * 2);
        setPreferredSize(new Dimension(width, height));
        setBackground(new Color(245, 245, 245));
    }

    public void setGrid(Cell[][] grid) { this.grid = grid; }
    public void setExploredCells(Set<Cell> exploredCells) { this.exploredCells = exploredCells; }
    public void setShortestPath(List<Cell> shortestPath) { this.shortestPath = shortestPath; }
    public void setDestinations(List<Cell> destinations) { this.destinations = destinations; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (grid == null) return;

        Graphics2D g2d = (Graphics2D) g;
        // Anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.translate(MARGIN, MARGIN);

        // 1. Terrain & Path
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = grid[i][j];
                int x = j * CELL_SIZE;
                int y = i * CELL_SIZE;

                g2d.setColor(cell.getTerrainColor());
                g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);

                if (exploredCells.contains(cell) && !shortestPath.contains(cell)) {
                    g2d.setColor(new Color(65, 105, 225, 100));
                    g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                }
                if (shortestPath.contains(cell)) {
                    g2d.setColor(new Color(220, 20, 60, 150));
                    g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        // 2. Walls (Round Cap)
        g2d.setColor(new Color(50, 50, 50));
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = grid[i][j];
                int x = j * CELL_SIZE;
                int y = i * CELL_SIZE;

                if (cell.hasTopWall()) g2d.drawLine(x, y, x + CELL_SIZE, y);
                if (cell.hasLeftWall()) g2d.drawLine(x, y, x, y + CELL_SIZE);
                if (i == rows - 1 && cell.hasBottomWall()) g2d.drawLine(x, y + CELL_SIZE, x + CELL_SIZE, y + CELL_SIZE);
                if (j == cols - 1 && cell.hasRightWall()) g2d.drawLine(x + CELL_SIZE, y, x + CELL_SIZE, y + CELL_SIZE);
            }
        }

        // 3. Markers
        drawMarker(g2d, 0, 0, new Color(50, 205, 50)); // Start (Green)

        // Loop gambar 3 finish lines
        for(Cell dest : destinations) {
            drawMarker(g2d, dest.getCol(), dest.getRow(), new Color(255, 215, 0)); // Gold
        }
    }

    private void drawMarker(Graphics2D g2d, int col, int row, Color color) {
        int x = col * CELL_SIZE;
        int y = row * CELL_SIZE;
        int padding = 5;

        g2d.setColor(color);
        g2d.fillOval(x + padding, y + padding, CELL_SIZE - (padding * 2), CELL_SIZE - (padding * 2));
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(x + padding, y + padding, CELL_SIZE - (padding * 2), CELL_SIZE - (padding * 2));
    }
}