import java.awt.Color;
import java.util.Objects;

public class Cell {

    public enum TerrainType {
        DEFAULT(0, new Color(255, 255, 255)),      // Putih
        GRASS(1, new Color(144, 238, 144)),        // Hijau rumput
        MUD(5, new Color(139, 119, 101)),          // Coklat lumpur
        WATER(10, new Color(100, 149, 237));       // Biru air

        private final int penalty;
        private final Color color;

        TerrainType(int penalty, Color color) {
            this.penalty = penalty;
            this.color = color;
        }

        public int getPenalty() { return penalty; }
        public Color getColor() { return color; }
    }

    private int row;
    private int col;
    private boolean topWall = true;
    private boolean rightWall = true;
    private boolean bottomWall = true;
    private boolean leftWall = true;
    private TerrainType terrainType = TerrainType.DEFAULT;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    public boolean hasTopWall() { return topWall; }
    public boolean hasRightWall() { return rightWall; }
    public boolean hasBottomWall() { return bottomWall; }
    public boolean hasLeftWall() { return leftWall; }

    public TerrainType getTerrainType() { return terrainType; }
    public int getTerrainPenalty() { return terrainType.getPenalty(); }
    public Color getTerrainColor() { return terrainType.getColor(); }

    public void setTopWall(boolean topWall) { this.topWall = topWall; }
    public void setRightWall(boolean rightWall) { this.rightWall = rightWall; }
    public void setBottomWall(boolean bottomWall) { this.bottomWall = bottomWall; }
    public void setLeftWall(boolean leftWall) { this.leftWall = leftWall; }
    public void setTerrainType(TerrainType terrainType) { this.terrainType = terrainType; }

    public void removeWallBetween(Cell neighbor) {
        int rowDiff = this.row - neighbor.getRow();
        int colDiff = this.col - neighbor.getCol();

        if (rowDiff == 1) {
            this.topWall = false;
            neighbor.setBottomWall(false);
        } else if (rowDiff == -1) {
            this.bottomWall = false;
            neighbor.setTopWall(false);
        } else if (colDiff == 1) {
            this.leftWall = false;
            neighbor.setRightWall(false);
        } else if (colDiff == -1) {
            this.rightWall = false;
            neighbor.setLeftWall(false);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cell)) return false;
        Cell cell = (Cell) o;
        return row == cell.row && col == cell.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return "Cell(" + row + ", " + col + ")";
    }
}