import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Ryan Peters
 * @date 3/9/2017
 */
public class TreeList<E extends Comparable> implements ISortedList<E> {

	private int size;

	private BinaryTreeNode root;
	private BinaryTreeNode targetableNode;

	private enum NavigationFlags {SEEK, FOUND, IDLE, HIDENODE, NODEHIDDEN, DESTROY, DESTROYED}

	private NavigationFlags shouldSeek = NavigationFlags.IDLE;
	private NavigationFlags shouldHide = NavigationFlags.IDLE;
	private NavigationFlags shouldDestroy = NavigationFlags.IDLE;



	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public E getHead() {
		return root.data;
	}

	@Override
	public E getTail() {
		return tailChaser(root).data;
	}

	private BinaryTreeNode tailChaser(BinaryTreeNode node) {
		return (node.right != null) ? tailChaser(node.right) : node;
	}

	@Override
	public int indexOf(E value) {
		shouldSeek = NavigationFlags.SEEK;
		int pos = seeker(value, root, 0);
		if (shouldSeek != NavigationFlags.FOUND) pos = -1;
		shouldSeek = NavigationFlags.IDLE;
		return pos;


	}

	/**
	 * @param value
	 * @param node
	 * @param pos
	 * @return
	 */
	private int seeker(E value, BinaryTreeNode node, int pos) {
		if (node != null) {
			if (shouldSeek == NavigationFlags.SEEK && node.left != null) {
				pos = seeker(value, node.left, pos);
			}
			pos++;
			if (shouldSeek == NavigationFlags.SEEK && node.data.compareTo(value) == 0) {
				shouldSeek = NavigationFlags.FOUND;
				if (shouldDestroy == NavigationFlags.DESTROY) {
					targetableNode = node;
					shouldDestroy = NavigationFlags.DESTROYED;
				}
			}
			if (shouldSeek == NavigationFlags.SEEK && node.right != null) {
				pos = seeker(value, node.right, pos);
			}
		}

		return pos;
	}

	private void hideLikeNinja(E value, BinaryTreeNode node) {
		if (node != null) {
			if (shouldHide == NavigationFlags.HIDENODE) {
				if (node.data.compareTo(value) > 0 && node.left == null) {
					node.left = new BinaryTreeNode(value);
					shouldHide = NavigationFlags.NODEHIDDEN;
				} else if (node.data.compareTo(value) <= 0 && node.right == null) {
					node.right = new BinaryTreeNode(value);
					shouldHide = NavigationFlags.NODEHIDDEN;
				} else hideLikeNinja(value, (node.data.compareTo(value) <= 0) ? node.left : node.right);

			}
		}
	}

	@Override
	public boolean contains(E value) {
		return indexOf(value) >= 0;
	}

	@Override
	public void add(E value) {
		shouldHide = NavigationFlags.HIDENODE;
		hideLikeNinja(value, root);
		shouldHide = NavigationFlags.IDLE;
		size++;
	}

	@Override
	public void addAll(ISortedList other) {
		Iterator otherIter = other.iterator();
		while (otherIter.hasNext()) {
			add((E) otherIter.next());
		}
	}

	@Override
	public E removeHead() {
		E data = root.data;
		shiftAndDestroy(root);
		size--;
		return data;
	}

	/**
	 * This method will shift the left child node of any given tree or sub tree into it's parent's postion then
	 * recursively call itself to fill that new gap in it's old position; it will do this until there is no left child
	 * node to draw from, at which time it will pull the lone right node child into the gap and be finished, if a node
	 * with no children is reached before it reaches a node with only a right child, then the node is destroyed.
	 *
	 * @param node this parameter will accept either the tree's root, or the left child node of any given parent node.
	 */
	private void shiftAndDestroy(BinaryTreeNode node) {
		if (node.left != null) {
			node.data = node.left.data;
		} else if (node.right != null) {
			node.data = node.right.data;
		} else if (node.parent.left == node) {
			node.parent.left = null; // oops, parent's left node is destroyed
		} else {
			node.parent.right = null;// oops, parent's right node is destroyed
		}
	}

	@Override
	public E removeTail() {
		BinaryTreeNode tail = tailChaser(root);
		E data = tail.data;
		tail.parent.right = null;
		size--;
		return data;
	}

	@Override
	public boolean remove(E value) {

		shouldDestroy = NavigationFlags.DESTROY;
		seeker(value, root, 0);
		boolean destroyer = shouldDestroy == NavigationFlags.DESTROYED;
		shouldDestroy = NavigationFlags.IDLE;
		if (destroyer) size--;
		return destroyer;
	}

	@Override
	public void clear() {
		root = null;
		size = 0;
	}

	/**
	 * Returns an iterator over elements of type {@code T}.
	 *
	 * @return an Iterator.
	 */
	@Override
	public Iterator iterator() {
		return new MyIter();
	}

	/**
	 * Returns a string representation of the object. In general, the
	 * {@code toString} method returns a string that
	 * "textually represents" this object. The result should
	 * be a concise but informative representation that is easy for a
	 * person to read.
	 * It is recommended that all subclasses override this method.
	 * <p>
	 * The {@code toString} method for class {@code Object}
	 * returns a string consisting of the name of the class of which the
	 * object is an instance, the at-sign character `{@code @}', and
	 * the unsigned hexadecimal representation of the hash code of the
	 * object. In other words, this method returns a string equal to the
	 * value of:
	 * <blockquote>
	 * <pre>
	 * getClass().getName() + '@' + Integer.toHexString(hashCode())
	 * </pre></blockquote>
	 *
	 * @return a string representation of the object.
	 */
	@Override
	public String toString() {
		return super.toString();
	}

	private class MyIter implements Iterator {
		private int pos;
		private BinaryTreeNode target;
		private boolean canRemove;
		private boolean returnedSelf;

		MyIter() {
			this.pos = 0;
			this.target = root;
			canRemove = false;
			returnedSelf = false;
			while (target.left != null) target = target.left;
		}

		/**
		 * Returns {@code true} if the iteration has more elements.
		 * (In other words, returns {@code true} if {@link #next} would
		 * return an element rather than throwing an exception.)
		 *
		 * @return {@code true} if the iteration has more elements
		 */
		@Override
		public boolean hasNext() {
			return pos < size + 1;
		}

		/**
		 * Returns the next element in the iteration.
		 *
		 * @return the next element in the iteration
		 * @throws NoSuchElementException if the iteration has no more elements
		 */
		@Override
		public E next() throws NoSuchElementException {
			if (!hasNext()) throw new NoSuchElementException("There are no more nodes to traverse");
			if (target.data == null) throw new NoSuchElementException("there appears to be no data here");
			return target.data;
		}

	}

	/** This private inner class is used to represent the data nodes within the BinaryTree, each node can hold a
	 * a generic data value along with left, right, and parent BinaryTreeNode(BTN) pointers.
	 *
	 * The left BTN pointer should always hold values that satisfy: (btn.data.compareTo(otherData) <= 0)
	 *
	 * The right BTN pointer should always hold values that satisfy: (btn.data.compareTo(otherData) > 0)
	 *
	 * The parent BTN pointer is a convenience pointer for tracing back to the root from any given node in the tree.
	 */
	private class BinaryTreeNode {
		private E data;
		private BinaryTreeNode left;
		private BinaryTreeNode right;
		private BinaryTreeNode parent;

		/** creates a basic node that holds data and instantiates the left and right node pointers to null.
		 *
		 * @param data a generic data type that the client declares at the instantiation of the binary tree.
		 */
		BinaryTreeNode(E data) {
			this(data, null, null, null);
		}

		BinaryTreeNode(E data, BinaryTreeNode parent) {
			this(data, null, null, parent);
		}

		/**
		 * @param data
		 * @param left
		 * @param right
		 */
		BinaryTreeNode(E data, BinaryTreeNode left, BinaryTreeNode right, BinaryTreeNode parent) {
			this.data = data;
			this.left = left;
			this.right = right;
			this.parent = parent;
		}
	}
}