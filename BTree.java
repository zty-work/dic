import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

public class BTree {
    private static final int T = 4;
    private Node root;
    private static final int LEFT_CHILD_NODE = 0;
    private static final int RIGHT_CHILD_NODE = 1;

    class Node {
        public int nukeys = 0;
        public String[] keys = new String[2 * T - 1];
        public String[] value = new String[2 * T - 1];
        public Node[] childNodes = new Node[2 * T];
        public boolean isLeafNode;

        int binarySearch(String key) {
            int leftIndex = 0;
            int rightIndex = nukeys - 1;

            while (leftIndex <= rightIndex) {
                final int middleIndex = leftIndex + ((rightIndex - leftIndex) / 2);
                if (keys[middleIndex].compareTo(key) < 0) {
                    leftIndex = middleIndex + 1;
                } else if (keys[middleIndex].compareTo(key) > 0) {
                    rightIndex = middleIndex - 1;
                } else {
                    return middleIndex;
                }
            }

            return -1;
        }

        boolean contains(String key) {
            return binarySearch(key) != -1;
        }

        // Remove an element from a node and also the left (0) or right (+1) child.
        void remove(int index, int leftOrRightChild) {
            if (index >= 0) {
                int i;
                for (i = index; i < nukeys - 1; i++) {
                    keys[i] = keys[i + 1];
                    value[i] = value[i + 1];
                    if (!isLeafNode) {
                        if (i >= index + leftOrRightChild) {
                            childNodes[i] = childNodes[i + 1];
                        }
                    }
                }
                keys[i] = "a";
                value[i] = null;
                if (!isLeafNode) {
                    if (i >= index + leftOrRightChild) {
                        childNodes[i] = childNodes[i + 1];
                    }
                    childNodes[i + 1] = null;
                }
                nukeys--;
            }
        }

        void shiftRightByOne() {
            if (!isLeafNode) {
                childNodes[nukeys + 1] = childNodes[nukeys];
            }
            for (int i = nukeys - 1; i >= 0; i--) {
                keys[i + 1] = keys[i];
                value[i + 1] = value[i];
                if (!isLeafNode) {
                    childNodes[i + 1] = childNodes[i];
                }
            }
        }

        int subtreeRootNodeIndex(String key) {
            for (int i = 0; i < nukeys; i++) {
                if (key.compareTo(keys[i]) < 0) {
                    return i;
                }
            }
            return nukeys;
        }
    }


    public BTree() {
        root = new Node();
        root.isLeafNode = true;
    }

    public void insert(String key, String value) {
        Node rootNode = root;
        if (!update(root, key, value)) {
            if (rootNode.nukeys == (2 * T - 1)) {
                Node newRootNode = new Node();
                root = newRootNode;
                newRootNode.isLeafNode = false;
                root.childNodes[0] = rootNode;
                splitChildNode(newRootNode, 0, rootNode); // Split rootNode and move its median (middle) key up into newRootNode.
                insertIntoNonFullNode(newRootNode, key, value); // Insert the key into the B-Tree with root newRootNode.
            } else {
                insertIntoNonFullNode(rootNode, key, value); // Insert the key into the B-Tree with root rootNode.
            }
        }
    }


    void splitChildNode(Node parentNode, int i, Node node) {
        Node newNode = new Node();
        newNode.isLeafNode = node.isLeafNode;
        newNode.nukeys = T - 1;
        for (int j = 0; j < T - 1; j++) { // Copy the last T-1 elements of node into newNode.
            newNode.keys[j] = node.keys[j + T];
            newNode.value[j] = node.value[j + T];
        }
        if (!newNode.isLeafNode) {
            for (int j = 0; j < T; j++) { // Copy the last T pointers of node into newNode.
                newNode.childNodes[j] = node.childNodes[j + T];
            }
            for (int j = T; j <= node.nukeys; j++) {
                node.childNodes[j] = null;
            }
        }
        for (int j = T; j < node.nukeys; j++) {
            node.keys[j] = "a";
            node.value[j] = null;
        }
        node.nukeys = T - 1;

        // Insert a (child) pointer to node newNode into the parentNode, moving other keys and pointers as necessary.
        for (int j = parentNode.nukeys; j >= i + 1; j--) {
            parentNode.childNodes[j + 1] = parentNode.childNodes[j];
        }
        parentNode.childNodes[i + 1] = newNode;
        for (int j = parentNode.nukeys - 1; j >= i; j--) {
            parentNode.keys[j + 1] = parentNode.keys[j];
            parentNode.value[j + 1] = parentNode.value[j];
        }
        parentNode.keys[i] = node.keys[T - 1];
        parentNode.value[i] = node.value[T - 1];
        node.keys[T - 1] = "a";
        node.value[T - 1] = null;
        parentNode.nukeys++;
    }
    void insertIntoNonFullNode(Node node, String key, String value) {
        int i = node.nukeys - 1;
        if (node.isLeafNode) {
            while (i >= 0 && key.compareTo(node.keys[i]) < 0) {
                node.keys[i + 1] = node.keys[i];
                node.value[i + 1] = node.value[i];
                i--;
            }
            i++;
            node.keys[i] = key;
            node.value[i] = value;
            node.nukeys++;
        } else {
            while (i >= 0 && key.compareTo(node.keys[i]) < 0) {
                i--;
            }
            i++;
            if (node.childNodes[i].nukeys == (2 * T - 1)) {
                splitChildNode(node, i, node.childNodes[i]);
                if (key.compareTo(node.keys[i]) > 0) {
                    i++;
                }
            }
            insertIntoNonFullNode(node.childNodes[i], key, value);
        }
    }

    public void delete(String key) {
        delete(root, key);
    }

    public void delete(Node node, String key) {
        if (node.isLeafNode) { // 1. If the key is in node and node is a leaf node, then delete the key from node.
            int i;
            if ((i = node.binarySearch(key)) != -1) { // key is i-th key of node if node contains key.
                node.remove(i, LEFT_CHILD_NODE);
            }
        } else {
            int i;
            if ((i = node.binarySearch(key)) != -1) {
                Node leftChildNode = node.childNodes[i];
                Node rightChildNode = node.childNodes[i + 1];
                if (leftChildNode.nukeys >= T) {
                    Node predecessorNode = leftChildNode;
                    Node erasureNode = predecessorNode;
                    while (!predecessorNode.isLeafNode) {  erasureNode = predecessorNode;
                        predecessorNode = predecessorNode.childNodes[node.nukeys - 1];
                    }
                    node.keys[i] = predecessorNode.keys[predecessorNode.nukeys - 1];
                    node.value[i] = predecessorNode.value[predecessorNode.nukeys - 1];
                    delete(erasureNode, node.keys[i]);
                } else if (rightChildNode.nukeys >= T) {
                    Node successorNode = rightChildNode;
                    Node erasureNode = successorNode; // Make sure not to delete a key from a node with only T - 1 elements.
                    while (!successorNode.isLeafNode) { // Therefore only descend to the previous node (erasureNode) of the predecessor node and delete the key using 3.
                        erasureNode = successorNode;
                        successorNode = successorNode.childNodes[0];
                    }
                    node.keys[i] = successorNode.keys[0];
                    node.value[i] = successorNode.value[0];
                    delete(erasureNode, node.keys[i]);
                } else {
                    int medianKeyIndex = mergeNodes(leftChildNode, rightChildNode);
                    moveKey(node, i, RIGHT_CHILD_NODE, leftChildNode, medianKeyIndex); // Delete i's right child pointer from node.
                    delete(leftChildNode, key);
                }
            } else {
                i = node.subtreeRootNodeIndex(key);
                Node childNode = node.childNodes[i]; // childNode is i-th child of node.
                if (childNode.nukeys == T - 1) {
                    Node leftChildSibling = (i - 1 >= 0) ? node.childNodes[i - 1] : null;
                    Node rightChildSibling = (i + 1 <= node.nukeys) ? node.childNodes[i + 1] : null;
                    if (leftChildSibling != null && leftChildSibling.nukeys >= T) {
                        childNode.shiftRightByOne();
                        childNode.keys[0] = node.keys[i - 1]; // i - 1 is the key index in node that is smaller than childNode's smallest key.
                        childNode.value[0] = node.value[i - 1];
                        if (!childNode.isLeafNode) {
                            childNode.childNodes[0] = leftChildSibling.childNodes[leftChildSibling.nukeys];
                        }
                        childNode.nukeys++;

                        // Move a key from the left sibling into the subtree's root node.
                        node.keys[i - 1] = leftChildSibling.keys[leftChildSibling.nukeys - 1];
                        node.value[i - 1] = leftChildSibling.value[leftChildSibling.nukeys - 1];

                        // Remove the key from the left sibling along with its right child node.
                        leftChildSibling.remove(leftChildSibling.nukeys - 1, RIGHT_CHILD_NODE);
                    } else if (rightChildSibling != null && rightChildSibling.nukeys >= T) {
                        childNode.keys[childNode.nukeys] = node.keys[i]; // i is the key index in node that is bigger than childNode's biggest key.
                        childNode.value[childNode.nukeys] = node.value[i];
                        if (!childNode.isLeafNode) {
                            childNode.childNodes[childNode.nukeys + 1] = rightChildSibling.childNodes[0];
                        }
                        childNode.nukeys++;
                        // Move a key from the right sibling into the subtree's root node.
                        node.keys[i] = rightChildSibling.keys[0];
                        node.value[i] = rightChildSibling.value[0];

                        // Remove the key from the right sibling along with its left child node.
                        rightChildSibling.remove(0, LEFT_CHILD_NODE);
                    } else { // 3b. Both of childNode's siblings have only T - 1 keys each...
                        if (leftChildSibling != null) {
                            int medianKeyIndex = mergeNodes(childNode, leftChildSibling);
                            moveKey(node, i - 1, LEFT_CHILD_NODE, childNode, medianKeyIndex); // i - 1 is the median key index in node when merging with the left sibling.
                        } else if (rightChildSibling != null) {
                            int medianKeyIndex = mergeNodes(childNode, rightChildSibling);
                            moveKey(node, i, RIGHT_CHILD_NODE, childNode, medianKeyIndex); // i is the median key index in node when merging with the right sibling.
                        }
                    }
                }
                delete(childNode, key);
            }
        }
    }

    // Merge two nodes and keep the median key (element) empty.
    int mergeNodes(Node dstNode, Node srcNode) {
        int medianKeyIndex;
        if (srcNode.keys[0].compareTo(dstNode.keys[dstNode.nukeys - 1]) < 0) {
            int i;
            // Shift all elements of dstNode right by srcNode.nukeys + 1 to make place for the srcNode and the median key.
            if (!dstNode.isLeafNode) {
                dstNode.childNodes[srcNode.nukeys + dstNode.nukeys + 1] = dstNode.childNodes[dstNode.nukeys];
            }
            for (i = dstNode.nukeys; i > 0; i--) {
                dstNode.keys[srcNode.nukeys + i] = dstNode.keys[i - 1];
                dstNode.value[srcNode.nukeys + i] = dstNode.value[i - 1];
                if (!dstNode.isLeafNode) {
                    dstNode.childNodes[srcNode.nukeys + i] = dstNode.childNodes[i - 1];
                }
            }

            // Clear the median key (element).
            medianKeyIndex = srcNode.nukeys;
            dstNode.keys[medianKeyIndex] = "a";
            dstNode.value[medianKeyIndex] = null;

            // Copy the srcNode's elements into dstNode.
            for (i = 0; i < srcNode.nukeys; i++) {
                dstNode.keys[i] = srcNode.keys[i];
                dstNode.value[i] = srcNode.value[i];
                if (!srcNode.isLeafNode) {
                    dstNode.childNodes[i] = srcNode.childNodes[i];
                }
            }
            if (!srcNode.isLeafNode) {
                dstNode.childNodes[i] = srcNode.childNodes[i];
            }
        } else {
            // Clear the median key (element).
            medianKeyIndex = dstNode.nukeys;
            dstNode.keys[medianKeyIndex] = "a";
            dstNode.value[medianKeyIndex] = null;

            // Copy the srcNode's elements into dstNode.
            int offset = medianKeyIndex + 1;
            int i;
            for (i = 0; i < srcNode.nukeys; i++) {
                dstNode.keys[offset + i] = srcNode.keys[i];
                dstNode.value[offset + i] = srcNode.value[i];
                if (!srcNode.isLeafNode) {
                    dstNode.childNodes[offset + i] = srcNode.childNodes[i];
                }
            }
            if (!srcNode.isLeafNode) {
                dstNode.childNodes[offset + i] = srcNode.childNodes[i];
            }
        }
        dstNode.nukeys += srcNode.nukeys;
        return medianKeyIndex;
    }

    // Move the key from srcNode at index into dstNode at medianKeyIndex. Note that the element at index is already empty.
    void moveKey(Node srcNode, int srcKeyIndex, int childIndex, Node dstNode, int medianKeyIndex) {
        dstNode.keys[medianKeyIndex] = srcNode.keys[srcKeyIndex];
        dstNode.value[medianKeyIndex] = srcNode.value[srcKeyIndex];
        dstNode.nukeys++;

        srcNode.remove(srcKeyIndex, childIndex);

        if (srcNode == root && srcNode.nukeys == 0) {
            root = dstNode;
        }
    }

    public String search(String key) {
        return search(root, key);
    }


    public String search(Node node, String key) {
        int i = 0;
        while (i < node.nukeys && key.compareTo(node.keys[i]) > 0) {
            i++;
        }
        if (i < node.nukeys && key == node.keys[i]) {
            return node.value[i];
        }
        if (node.isLeafNode) {
            return null;
        } else {
            return search(node.childNodes[i], key);
        }
    }


    private boolean update(Node node, String key, String value) {
        while (node != null) {
            int i = 0;
            while (i < node.nukeys && key.compareTo(node.keys[i]) > 0) {
                i++;
            }
            if (i < node.nukeys && key == node.keys[i]) {
                node.value[i] = value;
                return true;
            }
            if (node.isLeafNode) {
                return false;
            } else {
                node = node.childNodes[i];
            }
        }
        return false;
    }

    String dump(Node node) {
        String string = "";
        if (node != null) {
            if (node.isLeafNode) {
                for (int i = 0; i < node.nukeys; i++) {
                    string += node.value[i] + ", ";
                }
            } else {
                int i;
                for (i = 0; i < node.nukeys; i++) {
                    string += dump(node.childNodes[i]);
                    string += node.value[i] + ", ";
                }
                string += dump(node.childNodes[i]);
            }
        }
        return string;
    }

    public String toString() {
        return dump(root);
    }

    public void load(BTree bTree) {
        try {
            File filename = new File("unix.txt");
            InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
            BufferedReader br = new BufferedReader(reader);
            String line;
            line = br.readLine();
            while (line != null) {
                String key = line;
                line = br.readLine();
                String value = line;
                bTree.put(key, value, bTree);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void put(String key, String value, BTree bTree) {
        if (bTree.search(key) == null) {
            insert(key, value);
        } else {
            bTree.delete(bTree.root, key);
            insert(key, value);
        }
    }

    public static void main(String args[]) {
        BTree tree = new BTree();
        tree.insert("c", "e");
        tree.insert("b", "d");
        tree.insert("a", "f");
        tree.insert("m", "h");
        System.out.print(tree.search("c"));
        System.out.print(tree.dump(tree.root));
        System.out.println("please enter the order:'INSERT','PUT','GET','DEL','LOAD','DUMP'，'END'");
        Scanner scanner2 = new Scanner(System.in);
        String order = scanner2.next();

        while (!order.equals("END")) {
            switch (order) {
                case "INSERT": {
                    System.out.println("key,plz");
                    Scanner scanner3 = new Scanner(System.in);
                    String thekey = scanner3.next();
                    System.out.println("value,plz");
                    Scanner scanner4 = new Scanner(System.in);
                    String value = scanner4.next();
                    break;
                }
                case "PUT": {
                    System.out.println("key,plz");
                    Scanner scanner3 = new Scanner(System.in);
                    String thekey = scanner3.next();
                    System.out.println("value,plz");
                    Scanner scanner4 = new Scanner(System.in);
                    String value = scanner4.next();
                    tree.put(thekey, value, tree);
                    break;
                }
                case "GET": {
                    System.out.println("key,plz");
                    Scanner scanner3 = new Scanner(System.in);
                    String thekey = scanner3.next();
                    System.out.println(tree.search(thekey));
                    break;
                }
                case "DEL": {
                    System.out.println("key,plz");
                    Scanner scanner3 = new Scanner(System.in);
                    String thekey = scanner3.next();
                    tree.delete(thekey);
                    break;
                }
                case "LOAD": {
                    tree.load(tree);
                    break;
                }
                case "DUMP": {
                    System.out.println(tree.dump(tree.root));
                    break;
                }
                default: {
                    System.out.println("please enter 'INSERT','PUT','GET','DEL','LOAD' or 'DUMP'");
                    break;
                }
            }
            System.out.println("please enter the order:'INSERT','PUT','GET','DEL','LOAD','DUMP'，'END'");
            scanner2 = new Scanner(System.in);
            order = scanner2.next();
        }
    }
}
