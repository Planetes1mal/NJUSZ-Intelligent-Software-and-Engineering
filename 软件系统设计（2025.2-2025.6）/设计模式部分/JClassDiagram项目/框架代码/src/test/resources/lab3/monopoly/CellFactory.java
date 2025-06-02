// CellFactory.java
public class CellFactory {
    public static Cell createCell(String cellStr) {
        if (cellStr.equals("[]")) return new BlankCell();
        if (cellStr.startsWith("P")) {
            String[] parts = cellStr.substring(1).split("-");
            return new PropertyCell(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }
        if (cellStr.equals("S1")) return new WaitCell();
        if (cellStr.startsWith("M")) {
            return new MoneyCell(Integer.parseInt(cellStr.substring(1)));
        }
        if (cellStr.equals("S2")) return new RobCell();
        if (cellStr.equals("S3")) return new HouseCell();
        throw new IllegalArgumentException("Unknown cell type: " + cellStr);
    }
}