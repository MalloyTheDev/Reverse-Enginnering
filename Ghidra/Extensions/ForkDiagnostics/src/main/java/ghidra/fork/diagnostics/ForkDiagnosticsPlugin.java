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
package ghidra.fork.diagnostics;

import ghidra.MiscellaneousPluginPackage;
import ghidra.app.plugin.PluginCategoryNames;
import ghidra.app.plugin.ProgramPlugin;
import ghidra.framework.plugintool.PluginInfo;
import ghidra.framework.plugintool.PluginTool;
import ghidra.framework.plugintool.util.PluginStatus;
import ghidra.program.model.listing.Program;

/**
 * Observation-only diagnostics plugin (fork Phase 2 MVP).
 *
 * <p>This plugin is a thin bridge: it forwards Ghidra plugin/program lifecycle events to
 * {@link ForkDiagnosticsLog}, which writes a local JSONL log <b>only</b> when the
 * feature flag is enabled (env {@code GHIDRA_FORK_DIAGNOSTICS=true} or system property
 * {@code ghidra.fork.diagnostics=true}). When the flag is off, this plugin does nothing
 * observable.
 *
 * <p>It is intentionally:
 * <ul>
 *   <li><b>Opt-in twice</b> — the extension must be installed AND the plugin added to a
 *       tool, AND the flag must be set.</li>
 *   <li><b>UI-less</b> — no actions, no windows (MVP). UI is deferred.</li>
 *   <li><b>Non-mutating</b> — it only reads program metadata via public APIs.</li>
 *   <li><b>Local</b> — no network calls.</li>
 * </ul>
 *
 * <p>Analyzer timing and decompiler timing are intentionally NOT implemented here; they
 * are deferred until they can be done through clean public hooks (see
 * fork/docs/DIAGNOSTICS.md).
 */
//@formatter:off
@PluginInfo(
	status = PluginStatus.STABLE,
	packageName = MiscellaneousPluginPackage.NAME,
	category = PluginCategoryNames.COMMON,
	shortDescription = "Fork diagnostics (observation-only)",
	description = "Observation-only fork diagnostics. When enabled via the " +
		"GHIDRA_FORK_DIAGNOSTICS environment variable (or ghidra.fork.diagnostics system " +
		"property), logs basic plugin/program lifecycle events and JVM memory snapshots " +
		"to a local JSONL file. Disabled by default. Does not mutate programs or call the " +
		"network."
)
//@formatter:on
public class ForkDiagnosticsPlugin extends ProgramPlugin {

	/**
	 * Construct the plugin.
	 *
	 * @param tool the plugin tool
	 */
	public ForkDiagnosticsPlugin(PluginTool tool) {
		super(tool);
		ForkDiagnosticsLog.log("plugin_constructed");
	}

	@Override
	protected void programActivated(Program program) {
		logProgramEvent("program_activated", program);
	}

	@Override
	protected void programDeactivated(Program program) {
		logProgramEvent("program_deactivated", program);
	}

	@Override
	protected void dispose() {
		ForkDiagnosticsLog.log("plugin_disposed");
		super.dispose();
	}

	/**
	 * Extract program metadata defensively (each field independently) and forward to the
	 * logger. Never throws.
	 */
	private void logProgramEvent(String event, Program program) {
		String name = null;
		String path = null;
		String languageId = null;
		String compilerSpecId = null;
		if (program != null) {
			try {
				name = program.getName();
			}
			catch (Throwable t) {
				// ignore; leave null
			}
			try {
				path = program.getDomainFile().getPathname();
			}
			catch (Throwable t) {
				// ignore; leave null
			}
			try {
				languageId = program.getLanguageID().getIdAsString();
			}
			catch (Throwable t) {
				// ignore; leave null
			}
			try {
				compilerSpecId = program.getCompilerSpec().getCompilerSpecID().getIdAsString();
			}
			catch (Throwable t) {
				// ignore; leave null
			}
		}
		ForkDiagnosticsLog.logProgram(event, name, path, languageId, compilerSpecId);
	}
}
