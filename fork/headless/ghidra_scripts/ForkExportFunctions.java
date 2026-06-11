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
// Fork headless export: writes basic function metadata to functions.csv.
// READ-ONLY: does not modify the program. Output dir is script arg[0] (default ".").
//@category Fork.Headless

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.AddressSetView;
import ghidra.program.model.data.DataType;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionIterator;

public class ForkExportFunctions extends GhidraScript {

	@Override
	public void run() throws Exception {
		if (currentProgram == null) {
			printerr("ForkExportFunctions: no current program; nothing to export.");
			return;
		}

		File outFile = new File(resolveOutDir(), "functions.csv");
		int count = 0;
		try (BufferedWriter w = Files.newBufferedWriter(outFile.toPath(), StandardCharsets.UTF_8)) {
			w.write("name,entry_point,min_address,max_address,size_bytes,param_count," +
				"return_type,calling_convention,is_external,is_thunk\n");

			FunctionIterator it = currentProgram.getFunctionManager().getFunctions(true);
			while (it.hasNext()) {
				if (monitor.isCancelled()) {
					break;
				}
				Function f = it.next();
				AddressSetView body = f.getBody();
				String minAddr = (body != null && body.getMinAddress() != null)
						? body.getMinAddress().toString() : "";
				String maxAddr = (body != null && body.getMaxAddress() != null)
						? body.getMaxAddress().toString() : "";
				long size = (body != null) ? body.getNumAddresses() : 0L;
				DataType ret = f.getReturnType();
				String retName = (ret != null) ? ret.getDisplayName() : "";

				w.write(String.join(",",
					csv(f.getName(true)),
					csv(f.getEntryPoint() != null ? f.getEntryPoint().toString() : ""),
					csv(minAddr),
					csv(maxAddr),
					Long.toString(size),
					Integer.toString(f.getParameterCount()),
					csv(retName),
					csv(f.getCallingConventionName()),
					Boolean.toString(f.isExternal()),
					Boolean.toString(f.isThunk())));
				w.write("\n");
				count++;
			}
		}
		println("ForkExportFunctions: wrote " + count + " function(s) to " + outFile.getAbsolutePath());
	}

	private File resolveOutDir() {
		String[] args = getScriptArgs();
		File dir = (args != null && args.length > 0 && args[0] != null && !args[0].isEmpty())
				? new File(args[0]) : new File(".");
		dir.mkdirs();
		return dir;
	}

	// Minimal RFC-4180-ish CSV field escaping (handles commas, quotes, newlines).
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
