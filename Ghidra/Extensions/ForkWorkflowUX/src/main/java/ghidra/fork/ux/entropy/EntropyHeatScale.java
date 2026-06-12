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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pure (Ghidra-free) helpers for the Entropy Findings view:
 * <ul>
 *   <li>mapping an entropy value (bits/byte, 0..8) to one of six heat buckets, and</li>
 *   <li>recovering the entropy value the Phase-4 {@code BinaryEntropyAnalyzer} embedded in a
 *       "Binary Entropy" bookmark comment.</li>
 * </ul>
 *
 * <p>Deliberately dependency-free so the bucket and parse logic can be unit-tested in isolation
 * (the same pattern as {@code ShannonEntropy} in the ForkEntropy module). Parsing degrades
 * gracefully to {@link Double#NaN} when a comment is missing or in an unexpected format.
 */
public final class EntropyHeatScale {

	/** Maximum entropy for byte data, in bits/byte. */
	public static final double MAX_BITS = 8.0;

	/** Number of heat buckets (ids {@code color.fork.ux.entropy.0 .. .5}). */
	public static final int BUCKET_COUNT = 6;

	/** Matches "7.93/8.00" (the analyzer's "value/max" form) and captures the value. */
	private static final Pattern RATIO = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*/\\s*[0-9]");

	/** Fallback: matches "entropy 7.93". */
	private static final Pattern AFTER =
		Pattern.compile("(?i)entropy[^0-9]*([0-9]+(?:\\.[0-9]+)?)");

	private EntropyHeatScale() {
		// utility class
	}

	/**
	 * Map an entropy value to a heat bucket in {@code [0, BUCKET_COUNT)}.
	 *
	 * @param bits entropy in bits/byte; {@code NaN} maps to bucket 0
	 * @return bucket index 0 (low/cool) .. 5 (high/hot)
	 */
	public static int bucket(double bits) {
		if (Double.isNaN(bits)) {
			return 0;
		}
		double clamped = bits < 0 ? 0 : (bits > MAX_BITS ? MAX_BITS : bits);
		int idx = (int) Math.floor(clamped / MAX_BITS * BUCKET_COUNT);
		return idx >= BUCKET_COUNT ? BUCKET_COUNT - 1 : idx;
	}

	/**
	 * Recover the entropy value embedded in a Binary Entropy bookmark comment.
	 *
	 * @param comment the bookmark comment (may be null)
	 * @return entropy in bits/byte, or {@link Double#NaN} if not found
	 */
	public static double parseEntropyBits(String comment) {
		if (comment == null) {
			return Double.NaN;
		}
		Matcher m = RATIO.matcher(comment);
		if (m.find()) {
			return toDouble(m.group(1));
		}
		Matcher m2 = AFTER.matcher(comment);
		if (m2.find()) {
			return toDouble(m2.group(1));
		}
		return Double.NaN;
	}

	/**
	 * Format an entropy value for display, using an em dash for unknown/NaN.
	 *
	 * @param bits entropy value (may be null/NaN)
	 * @return e.g. {@code "7.93"}, or {@code "—"} when unknown
	 */
	public static String format(Double bits) {
		if (bits == null || bits.isNaN()) {
			return "—";
		}
		return String.format("%.2f", bits);
	}

	private static double toDouble(String s) {
		try {
			return Double.parseDouble(s);
		}
		catch (NumberFormatException e) {
			return Double.NaN;
		}
	}
}
