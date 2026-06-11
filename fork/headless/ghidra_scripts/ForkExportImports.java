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
// Fork headless export: writes external/import symbols to imports.csv.
// READ-ONLY: does not modify the program. Output dir is script arg[0] (default ".").
// If no external symbols exist, writes a header-only file and notes it on stderr.
//@category Fork.Headless

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import ghidra.app.script.GhidraScript;
import ghidra.program.model.symbol.Namespace;
import ghidra.program.model.symbol.Symbol;
import ghidra.program.model.symbol.SymbolIterator;
import ghidra.program.model.symbol.SymbolTable;

public class ForkExportImports extends GhidraScript {

	@Override
	public void run() throws Exception {
		if (currentProgram == null) {
			printerr("ForkExportImports: no current program; nothing to export.");
			return;
		}

		File outFile = new File(resolveOutDir(), "imports.csv");
		int count = 0;
		try (BufferedWriter w = Files.newBufferedWriter(outFile.toPath(), StandardCharsets.UTF_8)) {
			w.write("name,address,library\n");

			SymbolTable st = currentProgram.getSymbolTable();
			SymbolIterator it = st.getExternalSymbols();
			while (it.hasNext()) {
				if (monitor.isCancelled()) {
					break;
				}
				Symbol sym = it.next();
				Namespace ns = sym.getParentNamespace();
				String library = (ns != null) ? ns.getName() : "";
				w.write(String.join(",",
					csv(sym.getName()),
					csv(sym.getAddress() != null ? sym.getAddress().toString() : ""),
					csv(library)));
				w.write("\n");
				count++;
			}
		}
		if (count == 0) {
			printerr("ForkExportImports: no external/import symbols found; wrote header only.");
		}
		println("ForkExportImports: wrote " + count + " import(s) to " + outFile.getAbsolutePath());
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
