import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Scanner;


public class RBTree {

    private RBTNode root;

    private static final boolean RED = false;
    private static final boolean BLACK = true;

    public class RBTNode {
        boolean color;        // 颜色
        String key;// 关键字(键值)
        String value;
        RBTNode left;    // 左孩子
        RBTNode right;    // 右孩子
        RBTNode parent;    // 父结点

        public RBTNode(String key, String value, boolean color, RBTNode parent, RBTNode left, RBTNode right) {
            this.key = key;
            this.value = value;
            this.color = color;
            this.parent = parent;
            this.left = left;
            this.right = right;
        }

        public String getKey() {
            return key;
        }

        public String toString() {
            return "" + key + (this.color == RED ? "(R)" : "B");
        }
    }

    public RBTree() {
        root = null;
    }

    private void setvalue(RBTNode node, String value) {
        if (node != null)
            node.value = value;
    }

    private RBTNode parentOf(RBTNode node) {
        return node != null ? node.parent : null;
    }

    private boolean colorOf(RBTNode node) {
        return node != null ? node.color : BLACK;
    }

    private boolean isRed(RBTNode node) {
        return ((node != null) && (node.color == RED)) ? true : false;
    }

    private boolean isBlack(RBTNode node) {
        return !isRed(node);
    }

    private void setBlack(RBTNode node) {
        if (node != null)
            node.color = BLACK;
    }

    private void setRed(RBTNode node) {
        if (node != null)
            node.color = RED;
    }

    private void setParent(RBTNode node, RBTNode parent) {
        if (node != null)
            node.parent = parent;
    }

    private void setColor(RBTNode node, boolean color) {
        if (node != null)
            node.color = color;
    }


    private void dump(RBTNode tree) {
        if (tree != null) {
            dump(tree.left);
            System.out.println(tree.key + "means " + tree.value + " ");
            dump(tree.right);
        }
    }

    public void dump() {
        dump(root);
    }


    /*
     * (递归实现)查找"红黑树x"中键值为key的节点
     */
    private RBTNode search(RBTNode x, String key) {
        if (x == null) {
            System.out.print("key mising");
            return x;
        }
        int cmp = key.compareTo(x.key);
        if (cmp < 0)
            return search(x.left, key);
        else if (cmp > 0)
            return search(x.right, key);
        else
            return x;
    }

    public RBTNode search(String key) {
        return search(root, key);
    }

    /*
     * (非递归实现)查找"红黑树x"中键值为key的节点
     */
    private RBTNode iterativeSearch(RBTNode x, String key) {
        while (x != null) {
            int cmp = key.compareTo(x.key);

            if (cmp < 0)
                x = x.left;
            else if (cmp > 0)
                x = x.right;
            else
                return x;
        }

        return x;
    }

    public RBTNode iterativeSearch(String key) {
        return iterativeSearch(root, key);
    }

    /*
     * 查找最小结点：返回tree为根结点的红黑树的最小结点。
     */
    private RBTNode minimum(RBTNode tree) {
        if (tree == null)
            return null;

        while (tree.left != null)
            tree = tree.left;
        return tree;
    }


    /*
     * 查找最大结点：返回tree为根结点的红黑树的最大结点。
     */
    private RBTNode maximum(RBTNode tree) {
        if (tree == null)
            return null;

        while (tree.right != null)
            tree = tree.right;
        return tree;
    }

    public String maximum() {
        RBTNode p = maximum(root);
        if (p != null)
            return p.key;

        return null;
    }


    private void leftRotate(RBTNode x) {
        RBTNode y = x.right;
        x.right = y.left;
        if (y.left != null)
            y.left.parent = x;

        y.parent = x.parent;

        if (x.parent == null) {
            this.root = y;
        } else {
            if (x.parent.left == x)
                x.parent.left = y;
            else
                x.parent.right = y;
        }
        y.left = x;
        x.parent = y;
    }


    private void rightRotate(RBTNode y) {
        // 设置x是当前节点的左孩子。
        RBTNode x = y.left;

        // 将 “x的右孩子” 设为 “y的左孩子”；
        // 如果"x的右孩子"不为空的话，将 “y” 设为 “x的右孩子的父亲”
        y.left = x.right;
        if (x.right != null)
            x.right.parent = y;

        // 将 “y的父亲” 设为 “x的父亲”
        x.parent = y.parent;

        if (y.parent == null) {
            this.root = x;            // 如果 “y的父亲” 是空节点，则将x设为根节点
        } else {
            if (y == y.parent.right)
                y.parent.right = x;    // 如果 y是它父节点的右孩子，则将x设为“y的父节点的右孩子”
            else
                y.parent.left = x;    // (y是它父节点的左孩子) 将x设为“x的父节点的左孩子”
        }

        // 将 “y” 设为 “x的右孩子”
        x.right = y;

        // 将 “y的父节点” 设为 “x”
        y.parent = x;
    }

    /*
     * 红黑树插入修正函数
     *
     * 在向红黑树中插入节点之后(失去平衡)，再调用该函数；
     * 目的是将它重新塑造成一颗红黑树。
     *
     * 参数说明：
     *     node 插入的结点        // 对应《算法导论》中的z
     */
    private void insertFixUp(RBTNode node) {
        RBTNode parent, gparent;

        // 若“父节点存在，并且父节点的颜色是红色”
        while (((parent = parentOf(node)) != null) && isRed(parent)) {
            gparent = parentOf(parent);

            //若“父节点”是“祖父节点的左孩子”
            if (parent == gparent.left) {
                // Case 1条件：叔叔节点是红色
                RBTNode uncle = gparent.right;
                if ((uncle != null) && isRed(uncle)) {
                    setBlack(uncle);
                    setBlack(parent);
                    setRed(gparent);
                    node = gparent;
                    continue;
                }

                // Case 2条件：叔叔是黑色，且当前节点是右孩子
                if (parent.right == node) {
                    RBTNode tmp;
                    leftRotate(parent);
                    tmp = parent;
                    parent = node;
                    node = tmp;
                }

                // Case 3条件：叔叔是黑色，且当前节点是左孩子。
                setBlack(parent);
                setRed(gparent);
                rightRotate(gparent);
            } else {    //若“z的父节点”是“z的祖父节点的右孩子”
                // Case 1条件：叔叔节点是红色
                RBTNode uncle = gparent.left;
                if ((uncle != null) && isRed(uncle)) {
                    setBlack(uncle);
                    setBlack(parent);
                    setRed(gparent);
                    node = gparent;
                    continue;
                }

                // Case 2条件：叔叔是黑色，且当前节点是左孩子
                if (parent.left == node) {
                    RBTNode tmp;
                    rightRotate(parent);
                    tmp = parent;
                    parent = node;
                    node = tmp;
                }

                // Case 3条件：叔叔是黑色，且当前节点是右孩子。
                setBlack(parent);
                setRed(gparent);
                leftRotate(gparent);
            }
        }

        // 将根节点设为黑色
        setBlack(this.root);
    }

    /*
     * 将结点插入到红黑树中
     *
     * 参数说明：
     *     node 插入的结点        // 对应《算法导论》中的node
     */
    private void insert(RBTNode node) {
        int cmp;
        RBTNode y = null;
        RBTNode x = this.root;

        // 1. 将红黑树当作一颗二叉查找树，将节点添加到二叉查找树中。
        while (x != null) {
            y = x;
            cmp = node.key.compareTo(x.key);
            if (cmp < 0)
                x = x.left;
            else
                x = x.right;
        }

        node.parent = y;
        if (y != null) {
            cmp = node.key.compareTo(y.key);
            if (cmp < 0)
                y.left = node;
            else
                y.right = node;
        } else {
            this.root = node;
        }

        // 2. 设置节点的颜色为红色
        node.color = RED;

        // 3. 将它重新修正为一颗二叉查找树
        insertFixUp(node);
    }


    public void insert(String key, String value) {
        if (search(key) != null) {
            System.out.println("already exist");
            return;
        }
        RBTNode node = new RBTNode(key, value, BLACK, null, null, null);

        // 如果新建结点失败，则返回。
        if (node != null)
            insert(node);
    }


    private void deleteFixUp(RBTNode node, RBTNode parent) {
        RBTNode other;

        while ((node == null || isBlack(node)) && (node != this.root)) {
            if (parent.left == node) {
                other = parent.right;
                if (isRed(other)) {
                    // Case 1: x的兄弟w是红色的
                    setBlack(other);
                    setRed(parent);
                    leftRotate(parent);
                    other = parent.right;
                }

                if ((other.left == null || isBlack(other.left)) &&
                        (other.right == null || isBlack(other.right))) {
                    // Case 2: x的兄弟w是黑色，且w的俩个孩子也都是黑色的
                    setRed(other);
                    node = parent;
                    parent = parentOf(node);
                } else {

                    if (other.right == null || isBlack(other.right)) {
                        // Case 3: x的兄弟w是黑色的，并且w的左孩子是红色，右孩子为黑色。
                        setBlack(other.left);
                        setRed(other);
                        rightRotate(other);
                        other = parent.right;
                    }
                    // Case 4: x的兄弟w是黑色的；并且w的右孩子是红色的，左孩子任意颜色。
                    setColor(other, colorOf(parent));
                    setBlack(parent);
                    setBlack(other.right);
                    leftRotate(parent);
                    node = this.root;
                    break;
                }
            } else {

                other = parent.left;
                if (isRed(other)) {
                    // Case 1: x的兄弟w是红色的
                    setBlack(other);
                    setRed(parent);
                    rightRotate(parent);
                    other = parent.left;
                }

                if ((other.left == null || isBlack(other.left)) &&
                        (other.right == null || isBlack(other.right))) {
                    // Case 2: x的兄弟w是黑色，且w的俩个孩子也都是黑色的
                    setRed(other);
                    node = parent;
                    parent = parentOf(node);
                } else {

                    if (other.left == null || isBlack(other.left)) {
                        // Case 3: x的兄弟w是黑色的，并且w的左孩子是红色，右孩子为黑色。
                        setBlack(other.right);
                        setRed(other);
                        leftRotate(other);
                        other = parent.left;
                    }

                    // Case 4: x的兄弟w是黑色的；并且w的右孩子是红色的，左孩子任意颜色。
                    setColor(other, colorOf(parent));
                    setBlack(parent);
                    setBlack(other.left);
                    rightRotate(parent);
                    node = this.root;
                    break;
                }
            }
        }

        if (node != null)
            setBlack(node);
    }

    /*
     * 删除结点(node)，并返回被删除的结点
     *
     * 参数说明：
     *     node 删除的结点
     */
    private void delete(RBTNode node) {
        RBTNode child, parent;
        boolean color;

        // 被删除节点的"左右孩子都不为空"的情况。
        if ((node.left != null) && (node.right != null)) {
            // 被删节点的后继节点。(称为"取代节点")
            // 用它来取代"被删节点"的位置，然后再将"被删节点"去掉。
            RBTNode replace = node;

            // 获取后继节点
            replace = replace.right;
            while (replace.left != null)
                replace = replace.left;

            // "node节点"不是根节点(只有根节点不存在父节点)
            if (parentOf(node) != null) {
                if (parentOf(node).left == node)
                    parentOf(node).left = replace;
                else
                    parentOf(node).right = replace;
            } else {
                // "node节点"是根节点，更新根节点。
                this.root = replace;
            }

            // child是"取代节点"的右孩子，也是需要"调整的节点"。
            // "取代节点"肯定不存在左孩子！因为它是一个后继节点。
            child = replace.right;
            parent = parentOf(replace);
            // 保存"取代节点"的颜色
            color = colorOf(replace);

            // "被删除节点"是"它的后继节点的父节点"
            if (parent == node) {
                parent = replace;
            } else {
                // child不为空
                if (child != null)
                    setParent(child, parent);
                parent.left = child;

                replace.right = node.right;
                setParent(node.right, replace);
            }

            replace.parent = node.parent;
            replace.color = node.color;
            replace.left = node.left;
            node.left.parent = replace;

            if (color == BLACK)
                deleteFixUp(child, parent);

            node = null;
            return;
        }

        if (node.left != null) {
            child = node.left;
        } else {
            child = node.right;
        }

        parent = node.parent;
        // 保存"取代节点"的颜色
        color = node.color;

        if (child != null)
            child.parent = parent;

        // "node节点"不是根节点
        if (parent != null) {
            if (parent.left == node)
                parent.left = child;
            else
                parent.right = child;
        } else {
            this.root = child;
        }

        if (color == BLACK)
            deleteFixUp(child, parent);
        node = null;
    }

    /*
     * 删除结点(z)，并返回被删除的结点
     *
     * 参数说明：
     *     tree 红黑树的根结点
     *     z 删除的结点
     */
    public void delete(String key) {
        RBTNode node;
        if ((node = search(root, key)) != null) {
            delete(node);
        } else {
            System.out.println("error:key missing");
        }
    }


    public void load(RBTree rbTree) {
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
                rbTree.put(key, value, rbTree);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void put(String key, String value, RBTree rbTree) {
        if (rbTree.search(key) == null) {
            insert(key, value);
        } else {
            rbTree.setvalue(rbTree.search(key), value);
        }
    }

    public static void main(String args[]) {
        RBTree tree = new RBTree();
        tree.insert("c", "e");
        tree.insert("b", "d");
        tree.insert("a", "s");
        tree.insert("h", "f");
        tree.insert("d", "e");
        //  tree.delete(20);
        System.out.println(tree.search("a").value);
        tree.put("a", "c", tree);
        tree.delete("a");
        tree.dump();

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
                    tree.insert(thekey, value);
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
                    System.out.println(tree.search(thekey).value);
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
                    tree.dump();
                    break;
                }
                default: {
                    System.out.println("please enter 'INSERT','PUT','GET','DEL','LOAD' or 'DUMP，'END'");
                    break;
                }
            }
            System.out.println("please enter the order:'INSERT','PUT','GET','DEL','LOAD','DUMP'，'END'");
            scanner2 = new Scanner(System.in);
            order = scanner2.next();
        }
    }
}