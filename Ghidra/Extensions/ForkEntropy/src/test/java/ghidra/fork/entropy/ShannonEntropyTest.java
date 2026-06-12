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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Unit tests for the dependency-free Shannon entropy calculation. */
public class ShannonEntropyTest {

	private static final double EPS = 1e-9;

	@Test
	public void allIdenticalBytesIsZero() {
		assertEquals(0.0, ShannonEntropy.ofBytes(new byte[1024]), EPS);
	}

	@Test
	public void uniformOverAllByteValuesIsEight() {
		byte[] data = new byte[256];
		for (int i = 0; i < 256; i++) {
			data[i] = (byte) i;
		}
		assertEquals(8.0, ShannonEntropy.ofBytes(data), EPS);
	}

	@Test
	public void twoEquallyLikelyValuesIsOne() {
		byte[] data = new byte[100];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) (i % 2);
		}
		assertEquals(1.0, ShannonEntropy.ofBytes(data), EPS);
	}

	@Test
	public void emptyNullAndZeroTotalAreZero() {
		assertEquals(0.0, ShannonEntropy.ofBytes(new byte[0]), EPS);
		assertEquals(0.0, ShannonEntropy.ofBytes(null), EPS);
		assertEquals(0.0, ShannonEntropy.ofHistogram(new long[256], 0), EPS);
		assertEquals(0.0, ShannonEntropy.ofHistogram(null, 10), EPS);
	}

	@Test
	public void offsetAndLengthAreRespected() {
		byte[] data = { 0, 0, 1, 1 };
		assertEquals(0.0, ShannonEntropy.ofBytes(data, 0, 2), EPS); // all zeros
		assertEquals(1.0, ShannonEntropy.ofBytes(data, 0, 4), EPS); // 50/50
	}
}
