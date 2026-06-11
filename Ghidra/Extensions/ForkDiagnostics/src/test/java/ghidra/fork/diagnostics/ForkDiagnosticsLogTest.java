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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for the dependency-free diagnostics logger core. These exercise the JSON
 * building, escaping, and flag logic without touching Ghidra or the filesystem.
 */
public class ForkDiagnosticsLogTest {

	@Test
	public void recordContainsRequiredFields() {
		String json = ForkDiagnosticsLog.buildRecord("program_activated", "a.exe", "/P/a.exe",
			"x86:LE:64:default", "gcc");
		assertTrue(json.startsWith("{"));
		assertTrue(json.endsWith("}"));
		assertTrue(json.contains("\"event\":\"program_activated\""));
		assertTrue(json.contains("\"active_program_name\":\"a.exe\""));
		assertTrue(json.contains("\"active_program_language\":\"x86:LE:64:default\""));
		assertTrue(json.contains("\"active_program_compiler\":\"gcc\""));
		assertTrue(json.contains("\"max_memory\":"));
		assertTrue(json.contains("\"total_memory\":"));
		assertTrue(json.contains("\"free_memory\":"));
		assertTrue(json.contains("\"timestamp_utc\":"));
		assertTrue(json.contains("\"java_runtime_version\":"));
	}

	@Test
	public void nullProgramFieldsBecomeJsonNull() {
		String json = ForkDiagnosticsLog.buildRecord("plugin_constructed", null, null, null, null);
		assertTrue(json.contains("\"active_program_name\":null"));
		assertTrue(json.contains("\"active_program_path\":null"));
	}

	@Test
	public void escapeHandlesQuotesBackslashesAndControls() {
		assertEquals("a\\\"b", ForkDiagnosticsLog.escape("a\"b"));
		assertEquals("a\\\\b", ForkDiagnosticsLog.escape("a\\b"));
		assertEquals("a\\nb", ForkDiagnosticsLog.escape("a\nb"));
		assertEquals("a\\tb", ForkDiagnosticsLog.escape("a\tb"));
	}

	@Test
	public void flagEnablesViaSystemProperty() {
		String prev = System.getProperty(ForkDiagnosticsLog.PROP_FLAG);
		try {
			System.clearProperty(ForkDiagnosticsLog.PROP_FLAG);
			// Only assert the disabled case when the env var is also unset.
			if (System.getenv(ForkDiagnosticsLog.ENV_FLAG) == null) {
				assertFalse(ForkDiagnosticsLog.isEnabled());
			}
			System.setProperty(ForkDiagnosticsLog.PROP_FLAG, "true");
			assertTrue(ForkDiagnosticsLog.isEnabled());
		}
		finally {
			if (prev != null) {
				System.setProperty(ForkDiagnosticsLog.PROP_FLAG, prev);
			}
			else {
				System.clearProperty(ForkDiagnosticsLog.PROP_FLAG);
			}
		}
	}
}
