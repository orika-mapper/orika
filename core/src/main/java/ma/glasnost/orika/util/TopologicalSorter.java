package ma.glasnost.orika.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ma.glasnost.orika.util.Ordering.OrderingRelation;

public class TopologicalSorter {

	private static class Node<T> {
		public final T value;
		public final LinkedHashSet<Edge<T>> inEdges;
		public final LinkedHashSet<Edge<T>> outEdges;

		public Node(T value) {
			this.value = value;
			inEdges = new LinkedHashSet<Edge<T>>();
			outEdges = new LinkedHashSet<Edge<T>>();
		}

		public Node<T> addEdge(Node<T> node) {
			Edge<T> e = new Edge<T>(this, node);
			outEdges.add(e);
			node.inEdges.add(e);
			return this;
		}

		public T getValue() {
			return value;
		}

		@Override
		public String toString() {
			return getValue().toString();
		}
	}

	private static class Edge<T> {
		public final Node<T> from;
		public final Node<T> to;

		public Edge(Node<T> from, Node<T> to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public boolean equals(Object obj) {
			Edge<?> e = (Edge<?>) obj;
			return e.from == from && e.to == to;
		}
		
		@Override
		public int hashCode() {
			return to.hashCode() ^ from.hashCode();
		}

		@Override
		public String toString() {
			return from.toString() + " -> " + to.toString();
		}
	}

	public static <T> List<T> sort(Collection<T> elements, Ordering<T> ordering) {
		List<Node<T>> nodes = new ArrayList<Node<T>>();
		for (T curElement : elements)
			nodes.add(new Node<T>(curElement));

		// Empty list that will contain the sorted elements
		ArrayList<T> result = new ArrayList<T>();

		// Set of all nodes with no incoming edges
		Set<Node<T>> openSet = new LinkedHashSet<Node<T>>();

		for (Node<T> to : nodes) {
			for (Node<T> from : nodes) {
				if (ordering.order(from.getValue(), to.getValue()) == OrderingRelation.AFTER) {
					from.addEdge(to);
				}
			}
			if (to.inEdges.isEmpty()) {
				openSet.add(to);
			}
		}

		// while openSet is non-empty do
		while (!openSet.isEmpty()) {
			// remove a node n from openSet
			Node<T> n = openSet.iterator().next();
			openSet.remove(n);

			// insert n into result
			result.add(n.getValue());

			// for each node m with an edge e from n to m do
			for (Iterator<Edge<T>> it = n.outEdges.iterator(); it.hasNext();) {
				// remove edge e from the graph
				Edge<T> e = it.next();
				Node<T> m = e.to;
				it.remove();// Remove edge from n
				m.inEdges.remove(e);// Remove edge from m

				// if m has no other incoming edges then insert m into S
				if (m.inEdges.isEmpty()) {
					openSet.add(m);
				}
			}
		}

		if (nodes.size() > result.size()) {
			throw new IllegalStateException("Ordering contains cycles");
		}
		return result;
	}
}