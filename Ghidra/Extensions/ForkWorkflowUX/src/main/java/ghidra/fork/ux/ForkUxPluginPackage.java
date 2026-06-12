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
package ghidra.fork.ux;

import generic.theme.GIcon;
import ghidra.framework.plugintool.util.PluginPackage;

/**
 * Groups the fork's Workflow-UX plugins under a single package in the tool's
 * "Configure Plugins" dialog. Phase 5 adds plugins into this package incrementally.
 */
public class ForkUxPluginPackage extends PluginPackage {

	public static final String NAME = "Fork Workflow UX";

	public ForkUxPluginPackage() {
		super(NAME, new GIcon("icon.information"),
			"MalloyRE fork workflow UX surfaces — read-only views over fork analysis output.",
			MISCELLANIOUS_PRIORITY);
	}
}
