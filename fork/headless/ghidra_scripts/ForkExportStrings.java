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
// Fork headless export: writes discovered (defined) strings to strings.csv.
// READ-ONLY: does not modify the program. Output dir is script arg[0] (default ".").
//@category Fork.Headless

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Data;
import ghidra.program.model.listing.DataIterator;

public class ForkExportStrings extends GhidraScript {

	@Override
	public void run() throws Exception {
		if (currentProgram == null) {
			printerr("ForkExportStrings: no current program; nothing to export.");
			return;
		}

		File outFile = new File(resolveOutDir(), "strings.csv");
		int count = 0;
		try (BufferedWriter w = Files.newBufferedWriter(outFile.toPath(), StandardCharsets.UTF_8)) {
			w.write("address,length,value\n");

			// Conservative: iterate already-defined data and emit those with string
			// values. This does not run a fresh string search (no program mutation).
			DataIterator it = currentProgram.getListing().getDefinedData(true);
			while (it.hasNext()) {
				if (monitor.isCancelled()) {
					break;
				}
				Data data = it.next();
				if (!data.hasStringValue()) {
					continue;
				}
				Object value = data.getValue();
				String text = (value != null) ? value.toString() : "";
				w.write(String.join(",",
					csv(data.getAddress() != null ? data.getAddress().toString() : ""),
					Integer.toString(data.getLength()),
					csv(text)));
				w.write("\n");
				count++;
			}
		}
		println("ForkExportStrings: wrote " + count + " string(s) to " + outFile.getAbsolutePath());
	}

	private File resolveOutDir() {
		String[] args = getScriptArgs();
		File dir = (args != null && args.length > 0 && args[0] != null && !args[0].isEmpty())
				? new File(args[0]) : new File(".");
		dir.mkdirs();
		return dir;
	}

	private static String csv(String s) {
		if (s == null) {
			return "";
		}
		boolean quote = s.indexOf(',') >= 0 || s.indexOf('"') >= 0
				|| s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0;
		String v = s.replace("\"", "\"\"");
		return quote ? "\"" + v + "\"" : v;
	}
}
