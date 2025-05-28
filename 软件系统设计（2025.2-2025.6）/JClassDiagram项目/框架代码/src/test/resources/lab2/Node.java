package lab2;

public class LinkList {
    Node head;
}

public class Node {
    public int data;
    public Node next;
}

public interface TreeNode {
    int getChildNum();
}

public class RedNode implements TreeNode {
    public int getChildNum() {
        return 1;
    }
}

public class GroupNode implements TreeNode {
    public List<TreeNode> children;

    protected void addChildren(LinkList list) {
        // do something
    }

    public int getChildNum() {
        int num = 1;
        for (TreeNode child : children) {
            num += child.getChildNum();
        }
        return num;
    }
}