package rover;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public final String label;
    public final List<Node> children;

    public Node(String label) {
        this.label = label;
        this.children = new ArrayList<>();
    }

    public Node(String label, Node... children) {
        this.label = label;
        this.children = new ArrayList<>();
        for (Node child : children) {
            if (child != null) {
                this.children.add(child);
            }
        }
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"label\":\"").append(escape(label)).append("\",\"children\":[");
        for (int i = 0; i < children.size(); i++) {
            sb.append(children.get(i).toJson());
            if (i < children.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
