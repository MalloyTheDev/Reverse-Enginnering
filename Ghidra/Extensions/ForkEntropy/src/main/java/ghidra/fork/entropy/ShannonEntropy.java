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
package ghidra.fork.entropy;

import java.util.Objects;

/**
 * Shannon entropy calculation in bits per byte (range 0.0 .. 8.0).
 *
 * <p>Deliberately <b>free of any Ghidra dependency</b> so it can be compiled and
 * unit-tested in isolation. {@link BinaryEntropyAnalyzer} feeds it bytes read from
 * memory blocks.
 *
 * <ul>
 *   <li>A block of identical bytes has entropy 0.0.</li>
 *   <li>A uniform distribution over all 256 byte values has entropy 8.0 (the max).</li>
 *   <li>High entropy (~7.5+) often indicates packed, compressed, or encrypted data.</li>
 * </ul>
 */
public final class ShannonEntropy {

	/** Maximum possible entropy for byte data, in bits per byte. */
	public static final double MAX_BITS_PER_BYTE = 8.0;

	private static final double LOG2 = Math.log(2.0);

	/** Required length of a byte-value histogram: one bin per unsigned byte value. */
	private static final int HISTOGRAM_SIZE = 256;

	private ShannonEntropy() {
		// utility class
	}

	/**
	 * Compute Shannon entropy from a 256-element byte-value histogram.
	 *
	 * <p>A {@code null} histogram or non-positive {@code total} is treated as empty (returns
	 * 0.0). Otherwise {@code counts} must have exactly {@value #HISTOGRAM_SIZE} bins; a
	 * different length is a programming error and fails fast.
	 *
	 * @param counts histogram indexed by unsigned byte value (length 256)
	 * @param total  total number of samples (sum of counts)
	 * @return entropy in bits/byte, or 0.0 if counts is null or total &lt;= 0
	 * @throws IllegalArgumentException if {@code total > 0} and {@code counts.length != 256}
	 */
	public static double ofHistogram(long[] counts, long total) {
		if (counts == null || total <= 0) {
			return 0.0;
		}
		if (counts.length != HISTOGRAM_SIZE) {
			throw new IllegalArgumentException(
				"counts histogram must have length " + HISTOGRAM_SIZE + " (got " + counts.length + ")");
		}
		double entropy = 0.0;
		for (long c : counts) {
			if (c <= 0) {
				continue;
			}
			double p = (double) c / total;
			entropy -= p * (Math.log(p) / LOG2);
		}
		return entropy;
	}

	/**
	 * Compute Shannon entropy over a byte range.
	 *
	 * <p>Treats {@code null} data or non-positive {@code len} as empty (returns 0.0). For a
	 * positive {@code len}, the range {@code [off, off + len)} must lie within {@code data};
	 * an out-of-range request is a programming error and fails fast.
	 *
	 * @param data the bytes
	 * @param off  start offset
	 * @param len  number of bytes
	 * @return entropy in bits/byte, or 0.0 if data is null or len &lt;= 0
	 * @throws IndexOutOfBoundsException if {@code len > 0} and {@code [off, off + len)} is not a
	 *             valid range within {@code data}
	 */
	public static double ofBytes(byte[] data, int off, int len) {
		if (data == null || len <= 0) {
			return 0.0;
		}
		Objects.checkFromIndexSize(off, len, data.length);
		long[] counts = new long[HISTOGRAM_SIZE];
		int end = off + len;
		for (int i = off; i < end; i++) {
			counts[data[i] & 0xFF]++;
		}
		return ofHistogram(counts, len);
	}

	/**
	 * Compute Shannon entropy over an entire byte array.
	 *
	 * @param data the bytes
	 * @return entropy in bits/byte
	 */
	public static double ofBytes(byte[] data) {
		return (data == null) ? 0.0 : ofBytes(data, 0, data.length);
	}
}
