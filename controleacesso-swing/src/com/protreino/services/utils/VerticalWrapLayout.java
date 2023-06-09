package com.protreino.services.utils;

import java.awt.*;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * FlowLayout subclass that fully supports wrapping of components.
 */
public class VerticalWrapLayout extends VerticalFlowLayout {
	private Dimension preferredLayoutSize;

	/**
	 * Constructs a new <code>WrapLayout</code> with a left alignment and a
	 * default 5-unit horizontal and vertical gap.
	 */
	public VerticalWrapLayout() {
		super();
	}

	/**
	 * Constructs a new <code>FlowLayout</code> with the specified alignment and
	 * a default 5-unit horizontal and vertical gap. The value of the alignment
	 * argument must be one of <code>WrapLayout</code>, <code>WrapLayout</code>,
	 * or <code>WrapLayout</code>.
	 * 
	 * @param align
	 *            the alignment value
	 */
	public VerticalWrapLayout(int align) {
		super(align);
	}

	/**
	 * Creates a new flow layout manager with the indicated alignment and the
	 * indicated horizontal and vertical gaps.
	 * <p>
	 * The value of the alignment argument must be one of
	 * <code>WrapLayout</code>, <code>WrapLayout</code>, or
	 * <code>WrapLayout</code>.
	 * 
	 * @param align
	 *            the alignment value
	 * @param hgap
	 *            the horizontal gap between components
	 * @param vgap
	 *            the vertical gap between components
	 */
	public VerticalWrapLayout(int align, int hgap, int vgap) {
		super(align, hgap, vgap);
	}

	/**
	 * Returns the preferred dimensions for this layout given the <i>visible</i>
	 * components in the specified target container.
	 * 
	 * @param target
	 *            the component which needs to be laid out
	 * @return the preferred dimensions to lay out the subcomponents of the
	 *         specified container
	 */
	@Override
	public Dimension preferredLayoutSize(Container target) {
		return layoutSize(target, true);
	}

	/**
	 * Returns the minimum dimensions needed to layout the <i>visible</i>
	 * components contained in the specified target container.
	 * 
	 * @param target
	 *            the component which needs to be laid out
	 * @return the minimum dimensions to lay out the subcomponents of the
	 *         specified container
	 */
	@Override
	public Dimension minimumLayoutSize(Container target) {
		Dimension minimum = layoutSize(target, false);
		minimum.height -= (getVgap() + 1);
		return minimum;
	}

	/**
	 * Returns the minimum or preferred dimension needed to layout the target
	 * container.
	 *
	 * @param target
	 *            target to get layout size for
	 * @param preferred
	 *            should preferred size be calculated
	 * @return the dimension to layout the target container
	 */
	private Dimension layoutSize(Container target, boolean preferred) {
		synchronized (target.getTreeLock()) {
			// Each row must fit with the width allocated to the containter.
			// When the container width = 0, the preferred width of the
			// container
			// has not yet been calculated so lets ask for the maximum.

			int targetHeight = target.getSize().height;

			if (targetHeight == 0)
				targetHeight = Integer.MAX_VALUE;

			int hgap = getHgap();
			int vgap = getVgap();
			Insets insets = target.getInsets();
			int verticalInsetsAndGap = insets.top + insets.bottom + (vgap * 2);
			int maxHeight = targetHeight - verticalInsetsAndGap;

			// Fit components into the allowed height

			Dimension dim = new Dimension(0, 0);
			int rowWidth = 0;
			int rowHeight = 0;

			int nmembers = target.getComponentCount();

			for (int i = 0; i < nmembers; i++) {
				Component m = target.getComponent(i);

				if (m.isVisible()) {
					Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

					// Can't add the component to current row. Start a new row.

					if (rowHeight + d.height > maxHeight) {
						addColumn(dim, rowWidth, rowHeight);
						rowWidth = 0;
						rowHeight = 0;
					}

					// Add a horizontal gap for all components after the first

					if (rowHeight != 0) {
						rowHeight += vgap;
					}

					// ******************************************************
					rowHeight += d.height;
					rowWidth = Math.max(rowWidth, d.width);
				}
			}

			addColumn(dim, rowWidth, rowHeight);

			dim.height += verticalInsetsAndGap;
			dim.width += insets.left + insets.right + hgap * 2;

			// When using a scroll pane or the DecoratedLookAndFeel we need to
			// make sure the preferred size is less than the size of the
			// target containter so shrinking the container size works
			// correctly. Removing the horizontal gap is an easy way to do this.

			Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);

			if (scrollPane != null) {
				dim.height -= (vgap + 1);
			}

			return dim;
		}
	}

	/*
	 * A new row has been completed. Use the dimensions of this row to update
	 * the preferred size for the container.
	 *
	 * @param dim update the width and height when appropriate
	 * 
	 * @param rowWidth the width of the row to add
	 * 
	 * @param rowHeight the height of the row to add
	 */
	private void addColumn(Dimension dim, int rowWidth, int rowHeight) {
		dim.height = Math.max(dim.height, rowHeight);

		if (dim.width > 0) {
			dim.width += getHgap();
		}

		dim.width += rowWidth;
	}
}
