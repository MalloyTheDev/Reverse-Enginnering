/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ghidra.fork.ux.entropy;

import java.awt.Component;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import docking.widgets.table.GTableCellRenderingData;
import docking.widgets.table.TableColumnDescriptor;
import ghidra.docking.settings.Settings;
import ghidra.fork.ux.theme.ForkUxColors;
import ghidra.framework.plugintool.ServiceProvider;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Bookmark;
import ghidra.program.model.listing.BookmarkManager;
import ghidra.program.model.listing.BookmarkType;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.util.datastruct.Accumulator;
import ghidra.util.exception.CancelledException;
import ghidra.util.table.AddressBasedTableModel;
import ghidra.util.table.column.AbstractGColumnRenderer;
import ghidra.util.table.column.GColumnRenderer;
import ghidra.util.table.field.AbstractProgramBasedDynamicTableColumn;
import ghidra.util.task.TaskMonitor;

/**
 * Read-only table model backing the Entropy Findings view. Rows are built from the program's
 * "Binary Entropy" bookmarks (written by the Phase-4 {@code BinaryEntropyAnalyzer}); this model
 * never modifies the program.
 */
public class EntropyFindingsTableModel extends AddressBasedTableModel<EntropyFinding> {

	/** Bookmark category written by the Phase-4 BinaryEntropyAnalyzer. */
	static final String CATEGORY = "Binary Entropy";

	public EntropyFindingsTableModel(ServiceProvider serviceProvider, Program program) {
		super("Entropy Findings", serviceProvider, program, null);
	}

	@Override
	protected void doLoad(Accumulator<EntropyFinding> accumulator, TaskMonitor monitor)
			throws CancelledException {
		Program p = getProgram();
		if (p == null) {
			return;
		}
		BookmarkManager bookmarks = p.getBookmarkManager();
		Iterator<Bookmark> it = bookmarks.getBookmarksIterator(BookmarkType.ANALYSIS);
		while (it.hasNext()) {
			monitor.checkCancelled();
			Bookmark bookmark = it.next();
			if (!CATEGORY.equals(bookmark.getCategory())) {
				continue;
			}
			Address addr = bookmark.getAddress();
			MemoryBlock block = p.getMemory().getBlock(addr);
			String blockName = (block != null) ? block.getName() : "?";
			long size = (block != null) ? block.getSize() : 0L;
			double bits = EntropyHeatScale.parseEntropyBits(bookmark.getComment());
			accumulator.add(new EntropyFinding(addr, blockName, size, bits, bookmark.getComment()));
		}
	}

	@Override
	public Address getAddress(int row) {
		EntropyFinding finding = getRowObject(row);
		return (finding != null) ? finding.getAddress() : null;
	}

	@Override
	protected TableColumnDescriptor<EntropyFinding> createTableColumnDescriptor() {
		TableColumnDescriptor<EntropyFinding> descriptor = new TableColumnDescriptor<>();
		descriptor.addVisibleColumn(new EntropyColumn(), 1, false); // sort by entropy, descending
		descriptor.addVisibleColumn(new BlockColumn());
		descriptor.addVisibleColumn(new SizeColumn());
		descriptor.addVisibleColumn(new LocationColumn());
		descriptor.addVisibleColumn(new DescriptionColumn());
		return descriptor;
	}

//==================================================================================================
// Columns
//==================================================================================================

	private class EntropyColumn
			extends AbstractProgramBasedDynamicTableColumn<EntropyFinding, Double> {
		private final EntropyHeatRenderer renderer = new EntropyHeatRenderer();

		@Override
		public String getColumnName() {
			return "Entropy";
		}

		@Override
		public Double getValue(EntropyFinding rowObject, Settings settings, Program p,
				ServiceProvider sp) {
			double bits = rowObject.getEntropyBits();
			return Double.isNaN(bits) ? null : Double.valueOf(bits);
		}

		@Override
		public GColumnRenderer<Double> getColumnRenderer() {
			return renderer;
		}

		@Override
		public int getColumnPreferredWidth() {
			return 80;
		}
	}

	private class BlockColumn
			extends AbstractProgramBasedDynamicTableColumn<EntropyFinding, String> {
		@Override
		public String getColumnName() {
			return "Block";
		}

		@Override
		public String getValue(EntropyFinding r, Settings s, Program p, ServiceProvider sp) {
			return r.getBlockName();
		}

		@Override
		public int getColumnPreferredWidth() {
			return 110;
		}
	}

	private class SizeColumn
			extends AbstractProgramBasedDynamicTableColumn<EntropyFinding, Long> {
		@Override
		public String getColumnName() {
			return "Size (bytes)";
		}

		@Override
		public Long getValue(EntropyFinding r, Settings s, Program p, ServiceProvider sp) {
			return Long.valueOf(r.getSizeBytes());
		}
	}

	private class LocationColumn
			extends AbstractProgramBasedDynamicTableColumn<EntropyFinding, String> {
		@Override
		public String getColumnName() {
			return "Location";
		}

		@Override
		public String getValue(EntropyFinding r, Settings s, Program p, ServiceProvider sp) {
			Address a = r.getAddress();
			return (a != null) ? a.toString() : null;
		}

		@Override
		public int getColumnPreferredWidth() {
			return 110;
		}
	}

	private class DescriptionColumn
			extends AbstractProgramBasedDynamicTableColumn<EntropyFinding, String> {
		@Override
		public String getColumnName() {
			return "Description";
		}

		@Override
		public String getValue(EntropyFinding r, Settings s, Program p, ServiceProvider sp) {
			return r.getDescription();
		}

		@Override
		public int getColumnPreferredWidth() {
			return 360;
		}
	}

//==================================================================================================
// Heat renderer
//==================================================================================================

	private class EntropyHeatRenderer extends AbstractGColumnRenderer<Double> {

		@Override
		public Component getTableCellRendererComponent(GTableCellRenderingData data) {
			JLabel label = (JLabel) super.getTableCellRendererComponent(data);
			Double bits = (Double) data.getValue();
			label.setText(EntropyHeatScale.format(bits));
			label.setHorizontalAlignment(SwingConstants.RIGHT);
			if (bits != null && !bits.isNaN() && !data.isSelected()) {
				int bucket = EntropyHeatScale.bucket(bits);
				label.setBackground(ForkUxColors.entropyHeat(bucket));
				label.setForeground(ForkUxColors.onHeat(bucket));
			}
			return label;
		}

		@Override
		public String getFilterString(Double t, Settings settings) {
			return EntropyHeatScale.format(t);
		}
	}
}
