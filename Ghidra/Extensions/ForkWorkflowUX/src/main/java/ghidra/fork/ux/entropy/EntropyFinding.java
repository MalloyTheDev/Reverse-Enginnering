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

import ghidra.program.model.address.Address;

/** Immutable row object for the Entropy Findings table, one per Binary Entropy bookmark. */
public class EntropyFinding {

	private final Address address;
	private final String blockName;
	private final long sizeBytes;
	private final double entropyBits;
	private final String description;

	public EntropyFinding(Address address, String blockName, long sizeBytes, double entropyBits,
			String description) {
		this.address = address;
		this.blockName = blockName;
		this.sizeBytes = sizeBytes;
		this.entropyBits = entropyBits;
		this.description = description;
	}

	public Address getAddress() {
		return address;
	}

	public String getBlockName() {
		return blockName;
	}

	public long getSizeBytes() {
		return sizeBytes;
	}

	/** Entropy in bits/byte, or {@link Double#NaN} if it could not be recovered. */
	public double getEntropyBits() {
		return entropyBits;
	}

	public String getDescription() {
		return description;
	}
}
