package org.antlr.v4.runtime.programanalysis.cfg;

import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BasicBlock extends Node implements Iterable<SimpleNode> {

  private final Deque<SimpleNode> nodes;
  private final Set<SimpleNode> ignoredNodes;
  private final boolean isReversed;

  public BasicBlock(Deque<SimpleNode> nodes) {
	this.nodes = nodes;
	this.ignoredNodes = new HashSet<>(nodes.size());
	this.isReversed = false;
  }

  @Override
  public Iterator<SimpleNode> iterator() {
	if (isReversed) {
	  return nodes.descendingIterator();
	}
	return nodes.iterator();
  }

  public SimpleNode first() {
	if (isReversed) {
	  return nodes.peekLast();
	}
	return nodes.peekFirst();
  }

  public SimpleNode last() {
	if (isReversed) {
	  return nodes.peekFirst();
	}
	return nodes.peekLast();
  }

  public boolean isIgnored(SimpleNode node) {
	return ignoredNodes.contains(node);
  }

  public void ignore(SimpleNode node) {
	ignoredNodes.add(node);
  }

  public int size() {
	return nodes.size();
  }
}
