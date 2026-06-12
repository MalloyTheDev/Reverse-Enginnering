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
package ghidra.fork.ux.theme;

import generic.theme.GColor;

/**
 * Theme-aware accessors for the fork's scoped Workflow-UX colors, defined in
 * {@code data/forkux.theme.properties}. These ids are fork-owned and do not override any stock
 * Ghidra color, so the stock tool appearance is unchanged.
 */
public final class ForkUxColors {

	public static final String ENTROPY_PREFIX = "color.fork.ux.entropy.";
	public static final String FG_ON_HEAT_DARK = "color.fork.ux.fg.on-heat-dark";
	public static final String FG_ON_HEAT_LIGHT = "color.fork.ux.fg.on-heat-light";
	public static final String FG_MUTED = "color.fork.ux.fg.muted";
	public static final String SIGNAL = "color.fork.ux.signal";

	private ForkUxColors() {
		// utility class
	}

	/** Background color for an entropy heat bucket (0..5). */
	public static GColor entropyHeat(int bucket) {
		return new GColor(ENTROPY_PREFIX + clampBucket(bucket));
	}

	/** Readable foreground for text drawn on a heat-bucket background. */
	public static GColor onHeat(int bucket) {
		// cool buckets (deep blue/teal) read better with light text; warmer buckets with dark text
		return new GColor(clampBucket(bucket) >= 2 ? FG_ON_HEAT_DARK : FG_ON_HEAT_LIGHT);
	}

	/** Muted foreground for secondary/hint text in fork views. */
	public static GColor muted() {
		return new GColor(FG_MUTED);
	}

	/** The fork's single amber "signal" accent. */
	public static GColor signal() {
		return new GColor(SIGNAL);
	}

	private static int clampBucket(int b) {
		return b < 0 ? 0 : (b > 5 ? 5 : b);
	}
}
