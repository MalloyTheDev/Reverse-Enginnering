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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Unit tests for the dependency-free entropy heat-bucket and comment-parse helpers. */
public class EntropyHeatScaleTest {

	@Test
	public void bucketsSpanLowToHigh() {
		// floor(bits / 8 * 6), clamped to [0,5]
		assertEquals(0, EntropyHeatScale.bucket(0.0));
		assertEquals(0, EntropyHeatScale.bucket(1.0));
		assertEquals(1, EntropyHeatScale.bucket(2.0));
		assertEquals(2, EntropyHeatScale.bucket(3.0));
		assertEquals(3, EntropyHeatScale.bucket(4.0));
		assertEquals(4, EntropyHeatScale.bucket(6.0));
		assertEquals(5, EntropyHeatScale.bucket(7.0));
		assertEquals(5, EntropyHeatScale.bucket(8.0));
	}

	@Test
	public void bucketClampsAndHandlesNaN() {
		assertEquals(0, EntropyHeatScale.bucket(-5.0));
		assertEquals(5, EntropyHeatScale.bucket(99.0));
		assertEquals(0, EntropyHeatScale.bucket(Double.NaN));
	}

	@Test
	public void parsesPhase4CommentFormat() {
		String comment =
			"High entropy 7.93/8.00 in block '.text' (possible packed/compressed/encrypted data)";
		assertEquals(7.93, EntropyHeatScale.parseEntropyBits(comment), 1e-9);
	}

	@Test
	public void parseFallsBackAndDegradesGracefully() {
		assertEquals(6.5, EntropyHeatScale.parseEntropyBits("entropy 6.5 something"), 1e-9);
		assertTrue(Double.isNaN(EntropyHeatScale.parseEntropyBits(null)));
		assertTrue(Double.isNaN(EntropyHeatScale.parseEntropyBits("no numbers here")));
	}

	@Test
	public void formatsValueAndDash() {
		assertEquals("7.93", EntropyHeatScale.format(7.931));
		assertEquals("—", EntropyHeatScale.format(null));
		assertEquals("—", EntropyHeatScale.format(Double.NaN));
	}
}
