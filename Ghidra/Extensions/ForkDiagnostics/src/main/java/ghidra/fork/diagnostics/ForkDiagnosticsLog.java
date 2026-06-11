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

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Observation-only diagnostics logger for the fork (Phase 2 MVP).
 *
 * <p>This class is deliberately <b>free of any Ghidra dependency</b> so it can be
 * compiled and unit-tested in isolation. {@link ForkDiagnosticsPlugin} is the thin
 * bridge that feeds it Ghidra lifecycle events.
 *
 * <p>Design rules (do not relax without an ADR):
 * <ul>
 *   <li><b>Disabled by default.</b> Nothing is written unless the feature flag is set
 *       via the {@value #ENV_FLAG} environment variable or the {@value #PROP_FLAG}
 *       system property.</li>
 *   <li><b>Local only.</b> Writes a single append-only JSONL file. No network, ever.</li>
 *   <li><b>Never disruptive.</b> All logging failures are swallowed; diagnostics must
 *       never throw into Ghidra.</li>
 *   <li><b>Observation only.</b> It reads runtime/program metadata and writes its own
 *       log. It never mutates a program or analysis state.</li>
 * </ul>
 */
public final class ForkDiagnosticsLog {

	/** Environment variable that enables diagnostics when set to a truthy value. */
	public static final String ENV_FLAG = "GHIDRA_FORK_DIAGNOSTICS";

	/** System property that enables diagnostics when set to a truthy value. */
	public static final String PROP_FLAG = "ghidra.fork.diagnostics";

	/** Optional system property to override the log file location with an absolute path. */
	public static final String PROP_LOGFILE = "ghidra.fork.diagnostics.logfile";

	/**
	 * Default log path, relative to {@code user.dir}. Suitable for repo / development
	 * runs. NOTE: for an installed distribution, {@code user.dir} may not be writable;
	 * set {@link #PROP_LOGFILE} to a user-writable location (e.g. under the user's home
	 * or config directory) in that scenario. See fork/docs/DIAGNOSTICS.md.
	 */
	public static final String DEFAULT_RELATIVE_LOG = "fork/logs/diagnostics.jsonl";

	private static final Object LOCK = new Object();

	private ForkDiagnosticsLog() {
		// utility class; not instantiable
	}

	/**
	 * @return true if diagnostics are enabled via env var or system property.
	 */
	public static boolean isEnabled() {
		return isTruthy(System.getenv(ENV_FLAG)) || isTruthy(System.getProperty(PROP_FLAG));
	}

	private static boolean isTruthy(String value) {
		if (value == null) {
			return false;
		}
		String v = value.trim();
		return v.equalsIgnoreCase("true") || v.equals("1") || v.equalsIgnoreCase("yes") ||
			v.equalsIgnoreCase("on");
	}

	/**
	 * Resolve the JSONL log file location. Honors {@link #PROP_LOGFILE} if set, otherwise
	 * uses {@link #DEFAULT_RELATIVE_LOG} relative to {@code user.dir}.
	 *
	 * @return the resolved log file path
	 */
	public static Path resolveLogFile() {
		String override = System.getProperty(PROP_LOGFILE);
		if (override != null && !override.trim().isEmpty()) {
			return Paths.get(override.trim());
		}
		return Paths.get(System.getProperty("user.dir", "."), DEFAULT_RELATIVE_LOG);
	}

	/**
	 * Log a lifecycle event that has no associated program.
	 *
	 * @param event the event name (e.g. {@code "plugin_constructed"})
	 */
	public static void log(String event) {
		logProgram(event, null, null, null, null);
	}

	/**
	 * Log an event, optionally with active-program context. No-op when disabled. Never
	 * throws.
	 *
	 * @param event          the event name
	 * @param programName    active program name, or null if none/unavailable
	 * @param programPath    active program project path, or null if none/unavailable
	 * @param languageId     active program language id, or null if none/unavailable
	 * @param compilerSpecId active program compiler-spec id, or null if none/unavailable
	 */
	public static void logProgram(String event, String programName, String programPath,
			String languageId, String compilerSpecId) {
		if (!isEnabled()) {
			return;
		}
		try {
			String line = buildRecord(event, programName, programPath, languageId, compilerSpecId);
			append(line);
		}
		catch (Throwable t) {
			// Diagnostics must NEVER disrupt Ghidra. Swallow everything, including IO
			// and unexpected runtime errors.
		}
	}

	/**
	 * Build a single JSONL record. Package-private for unit testing. Does not check the
	 * enabled flag (the caller does that).
	 */
	static String buildRecord(String event, String programName, String programPath,
			String languageId, String compilerSpecId) {
		Runtime rt = Runtime.getRuntime();
		Map<String, Object> rec = new LinkedHashMap<>();
		rec.put("event", event);
		rec.put("timestamp_utc", Instant.now().toString());
		rec.put("java_runtime_version", System.getProperty("java.runtime.version", "unknown"));
		rec.put("max_memory", rt.maxMemory());
		rec.put("total_memory", rt.totalMemory());
		rec.put("free_memory", rt.freeMemory());
		rec.put("used_memory", rt.totalMemory() - rt.freeMemory());
		rec.put("active_program_name", programName);
		rec.put("active_program_path", programPath);
		rec.put("active_program_language", languageId);
		rec.put("active_program_compiler", compilerSpecId);
		if (programName == null && !event.startsWith("plugin_")) {
			rec.put("note", "no active program available (program fields are null)");
		}
		return toJson(rec);
	}

	private static void append(String jsonLine) throws IOException {
		Path file = resolveLogFile();
		synchronized (LOCK) {
			Path parent = file.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
				w.write(jsonLine);
				w.write(System.lineSeparator());
			}
		}
	}

	// ---- minimal JSON serialization (standard library only) ------------------

	/** Serialize a string-keyed map to a compact JSON object. Package-private for tests. */
	static String toJson(Map<String, Object> map) {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		boolean first = true;
		for (Map.Entry<String, Object> e : map.entrySet()) {
			if (!first) {
				sb.append(',');
			}
			first = false;
			sb.append('"').append(escape(e.getKey())).append('"').append(':');
			Object v = e.getValue();
			if (v == null) {
				sb.append("null");
			}
			else if (v instanceof Number || v instanceof Boolean) {
				sb.append(v.toString());
			}
			else {
				sb.append('"').append(escape(v.toString())).append('"');
			}
		}
		sb.append('}');
		return sb.toString();
	}

	/** JSON-escape a string. Package-private for tests. */
	static String escape(String s) {
		StringBuilder sb = new StringBuilder(s.length() + 8);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '"':
					sb.append("\\\"");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\t':
					sb.append("\\t");
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\f':
					sb.append("\\f");
					break;
				default:
					if (c < 0x20) {
						sb.append(String.format("\\u%04x", (int) c));
					}
					else {
						sb.append(c);
					}
			}
		}
		return sb.toString();
	}
}
