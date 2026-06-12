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

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.TableModelListener;

import docking.ActionContext;
import docking.WindowPosition;
import docking.action.DockingAction;
import docking.action.ToolBarData;
import generic.theme.GIcon;
import ghidra.app.context.ProgramActionContext;
import ghidra.app.context.ProgramLocationActionContext;
import ghidra.fork.ux.theme.ForkUxColors;
import ghidra.framework.plugintool.ComponentProviderAdapter;
import ghidra.framework.plugintool.PluginTool;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Program;
import ghidra.program.util.ProgramLocation;
import ghidra.util.table.GhidraTable;
import ghidra.util.table.GhidraTableFilterPanel;
import ghidra.util.table.GhidraThreadedTablePanel;
import resources.Icons;

/**
 * Dockable, read-only view listing the program's Binary Entropy findings in a sortable,
 * filterable table with entropy heat-scale rendering. Double-click (or Enter) navigates to the
 * finding's address via the tool's navigation service. The view never modifies the program.
 */
public class EntropyFindingsProvider extends ComponentProviderAdapter {

	private final EntropyFindingsPlugin plugin;

	private JPanel panel;
	private JLabel hintLabel;
	private GhidraTable table;
	private EntropyFindingsTableModel model;
	private GhidraThreadedTablePanel<EntropyFinding> threadedPanel;
	private GhidraTableFilterPanel<EntropyFinding> filterPanel;
	private TableModelListener modelListener;

	private Program program;

	EntropyFindingsProvider(PluginTool tool, EntropyFindingsPlugin plugin) {
		super(tool, "Entropy Findings", plugin.getName(), ProgramActionContext.class);
		this.plugin = plugin;

		setIcon(new GIcon("icon.information"));
		setDefaultWindowPosition(WindowPosition.WINDOW);
		setTitle("Entropy Findings");

		buildPanel();
		createActions();
		addToTool();
	}

	private void buildPanel() {
		model = new EntropyFindingsTableModel(tool, null);
		threadedPanel = new GhidraThreadedTablePanel<>(model);
		table = threadedPanel.getTable();
		table.setAccessibleNamePrefix("Entropy Findings");
		table.installNavigation(tool);

		hintLabel = new JLabel();
		hintLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
		hintLabel.setForeground(ForkUxColors.muted());

		filterPanel = new GhidraTableFilterPanel<>(table, model);

		panel = new JPanel(new BorderLayout());
		panel.add(hintLabel, BorderLayout.NORTH);
		panel.add(threadedPanel, BorderLayout.CENTER);
		panel.add(filterPanel, BorderLayout.SOUTH);

		modelListener = e -> updateHint();
		table.getModel().addTableModelListener(modelListener);
		updateHint();
	}

	private void createActions() {
		DockingAction refresh = new DockingAction("Refresh Entropy Findings", plugin.getName()) {
			@Override
			public void actionPerformed(ActionContext context) {
				reload();
			}
		};
		refresh.setToolBarData(new ToolBarData(Icons.REFRESH_ICON));
		refresh.setDescription("Reload findings from the program's Binary Entropy bookmarks");
		addLocalAction(refresh);
	}

	private void updateHint() {
		String text;
		if (program == null) {
			text = "No program open.";
		}
		else {
			int rows = table.getRowCount();
			if (rows == 0) {
				text = "No entropy findings. Enable the “Binary Entropy (fork)” analyzer " +
					"in Analysis options, then run analysis.";
			}
			else {
				text = rows + " entropy finding" + (rows == 1 ? "" : "s") +
					" — double-click a row to navigate.";
			}
		}
		hintLabel.setText(text);
		setSubTitle(program == null ? "" : "(" + table.getRowCount() + ")");
	}

	void setProgram(Program newProgram) {
		this.program = newProgram;
		if (isVisible()) {
			model.setProgram(newProgram);
			model.reload();
		}
		updateHint();
	}

	void reload() {
		if (isVisible()) {
			model.setProgram(program);
			model.reload();
		}
	}

	@Override
	public void componentShown() {
		model.setProgram(program);
		model.reload();
		updateHint();
	}

	@Override
	public ActionContext getActionContext(MouseEvent event) {
		if (program == null) {
			return null;
		}
		ProgramLocation location = null;
		int row = table.getSelectedRow();
		if (row >= 0) {
			Address addr = model.getAddress(row);
			if (addr != null) {
				location = new ProgramLocation(program, addr);
			}
		}
		return new ProgramLocationActionContext(this, program, table, location);
	}

	void dispose() {
		if (table != null) {
			table.getModel().removeTableModelListener(modelListener);
		}
		if (filterPanel != null) {
			filterPanel.dispose();
		}
		if (threadedPanel != null) {
			threadedPanel.dispose();
		}
		table = null;
		model = null;
		program = null;
	}

	@Override
	public JComponent getComponent() {
		return panel;
	}
}
