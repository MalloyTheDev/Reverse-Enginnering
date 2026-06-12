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

import ghidra.app.plugin.PluginCategoryNames;
import ghidra.app.plugin.ProgramPlugin;
import ghidra.app.services.GoToService;
import ghidra.fork.ux.ForkUxPluginPackage;
import ghidra.framework.plugintool.PluginInfo;
import ghidra.framework.plugintool.PluginTool;
import ghidra.framework.plugintool.util.PluginStatus;
import ghidra.program.model.listing.Program;

/**
 * Provides the read-only Entropy Findings view (Phase 5A), a dockable table that surfaces the
 * Binary Entropy analyzer's findings with heat-scale rendering and navigation. The plugin is
 * additive and read-only: it only reads existing "Binary Entropy" bookmarks.
 */
//@formatter:off
@PluginInfo(
	status = PluginStatus.RELEASED,
	packageName = ForkUxPluginPackage.NAME,
	category = PluginCategoryNames.ANALYSIS,
	shortDescription = "Entropy Findings view (fork)",
	description = "Read-only, dockable table of the Binary Entropy analyzer's findings " +
		"(high-entropy memory blocks), with entropy heat-scale rendering and navigation. " +
		"Additive and read-only; surfaces existing bookmarks only.",
	servicesRequired = { GoToService.class }
)
//@formatter:on
public class EntropyFindingsPlugin extends ProgramPlugin {

	private EntropyFindingsProvider provider;

	public EntropyFindingsPlugin(PluginTool tool) {
		super(tool);
		provider = new EntropyFindingsProvider(tool, this);
	}

	@Override
	protected void programActivated(Program program) {
		provider.setProgram(program);
	}

	@Override
	protected void programDeactivated(Program program) {
		provider.setProgram(null);
	}

	@Override
	protected void dispose() {
		if (provider != null) {
			provider.dispose();
		}
		super.dispose();
	}
}
