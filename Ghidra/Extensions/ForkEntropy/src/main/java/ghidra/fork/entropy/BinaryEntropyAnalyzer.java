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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import ghidra.app.services.AbstractAnalyzer;
import ghidra.app.services.AnalysisPriority;
import ghidra.app.services.AnalyzerType;
import ghidra.app.util.importer.MessageLog;
import ghidra.framework.options.Options;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSetView;
import ghidra.program.model.listing.BookmarkType;
import ghidra.program.model.listing.CommentType;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.Memory;
import ghidra.program.model.mem.MemoryAccessException;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.util.exception.CancelledException;
import ghidra.util.task.TaskMonitor;

/**
 * Computes Shannon entropy per initialized memory block and flags high-entropy blocks
 * (a common indicator of packed, compressed, or encrypted data).
 *
 * <p>Fork Phase 4 — the first fork analyzer. Behavior:
 * <ul>
 *   <li><b>Disabled by default</b> ({@code setDefaultEnablement(false)}): it is a
 *       specialized, opt-in analyzer and does not change stock auto-analysis unless a
 *       user enables it.</li>
 *   <li><b>Additive annotations only</b>: for blocks at/above the threshold it adds an
 *       Analysis bookmark and a plate comment at the block start. It does not alter
 *       disassembly, data, functions, names, or types.</li>
 *   <li><b>No network.</b> An optional CSV report is written only if a report path is
 *       configured (empty by default).</li>
 * </ul>
 *
 * <p>NOTE: the class name ends in "Analyzer" as required by the ClassSearcher.
 */
public class BinaryEntropyAnalyzer extends AbstractAnalyzer {

	private static final String NAME = "Binary Entropy (fork)";
	private static final String DESCRIPTION =
		"Computes Shannon entropy per initialized memory block and bookmarks/comments " +
			"high-entropy blocks (possible packed/compressed/encrypted data). Optionally " +
			"writes a CSV report. Disabled by default; additive annotations only.";

	private static final String OPT_THRESHOLD = "High-entropy threshold (bits/byte)";
	private static final String OPT_MIN_SIZE = "Minimum block size (bytes)";
	private static final String OPT_REPORT = "Entropy report file (optional)";

	private static final double DEFAULT_THRESHOLD = 7.0;
	private static final int DEFAULT_MIN_SIZE = 256;

	private double threshold = DEFAULT_THRESHOLD;
	private int minBlockSize = DEFAULT_MIN_SIZE;
	private String reportFile = "";

	public BinaryEntropyAnalyzer() {
		super(NAME, DESCRIPTION, AnalyzerType.BYTE_ANALYZER);
		setDefaultEnablement(false);
		setPriority(AnalysisPriority.LOW_PRIORITY);
		setSupportsOneTimeAnalysis();
	}

	@Override
	public boolean canAnalyze(Program program) {
		return program != null && program.getMemory() != null;
	}

	@Override
	public void registerOptions(Options options, Program program) {
		options.registerOption(OPT_THRESHOLD, DEFAULT_THRESHOLD, null,
			"Blocks whose Shannon entropy (0-8 bits/byte) is at or above this value are flagged.");
		options.registerOption(OPT_MIN_SIZE, DEFAULT_MIN_SIZE, null,
			"Initialized blocks smaller than this many bytes are skipped.");
		options.registerOption(OPT_REPORT, "", null,
			"If non-empty, a CSV entropy report is written to this file path.");
	}

	@Override
	public void optionsChanged(Options options, Program program) {
		threshold = options.getDouble(OPT_THRESHOLD, threshold);
		minBlockSize = options.getInt(OPT_MIN_SIZE, minBlockSize);
		reportFile = options.getString(OPT_REPORT, reportFile);
	}

	@Override
	public boolean added(Program program, AddressSetView set, TaskMonitor monitor, MessageLog log)
			throws CancelledException {

		Memory memory = program.getMemory();
		boolean processAll = (set == null || set.isEmpty());

		List<String> report = new ArrayList<>();
		report.add("block,start,size_bytes,entropy_bits,flagged");

		int flagged = 0;
		for (MemoryBlock block : memory.getBlocks()) {
			monitor.checkCancelled();
			if (!block.isInitialized() || block.getSize() < minBlockSize) {
				continue;
			}
			if (!processAll && !set.intersects(block.getStart(), block.getEnd())) {
				continue;
			}

			double entropy = computeBlockEntropy(block, monitor);
			boolean high = entropy >= threshold;

			log.appendMsg(NAME, String.format("block '%s' @ %s size %d entropy %.3f%s",
				block.getName(), block.getStart(), block.getSize(), entropy, high ? "  [HIGH]" : ""));
			report.add(String.join(",", csv(block.getName()), block.getStart().toString(),
				Long.toString(block.getSize()), String.format("%.4f", entropy), Boolean.toString(high)));

			if (high) {
				annotate(program, block, entropy);
				flagged++;
			}
		}

		if (!reportFile.isEmpty()) {
			writeReport(reportFile, report, log);
		}
		log.appendMsg(NAME,
			String.format("flagged %d high-entropy block(s) at threshold %.2f", flagged, threshold));
		return true;
	}

	/** Read the block's bytes in chunks and compute entropy over a byte-value histogram. */
	private double computeBlockEntropy(MemoryBlock block, TaskMonitor monitor)
			throws CancelledException {
		long[] counts = new long[256];
		long total = 0;
		long size = block.getSize();
		long offset = 0;
		byte[] buf = new byte[64 * 1024];
		Address start = block.getStart();

		while (offset < size) {
			monitor.checkCancelled();
			int toRead = (int) Math.min(buf.length, size - offset);
			int read;
			try {
				read = block.getBytes(start.add(offset), buf, 0, toRead);
			}
			catch (MemoryAccessException e) {
				break; // unreadable region; stop scanning this block
			}
			if (read <= 0) {
				break;
			}
			for (int i = 0; i < read; i++) {
				counts[buf[i] & 0xFF]++;
			}
			total += read;
			offset += read;
		}
		return ShannonEntropy.ofHistogram(counts, total);
	}

	/** Add an additive bookmark + plate comment marking a high-entropy block. Never throws. */
	private void annotate(Program program, MemoryBlock block, double entropy) {
		Address start = block.getStart();
		String msg = String.format(
			"High entropy %.2f/%.2f in block '%s' (possible packed/compressed/encrypted data)",
			entropy, ShannonEntropy.MAX_BITS_PER_BYTE, block.getName());
		try {
			program.getBookmarkManager().setBookmark(start, BookmarkType.ANALYSIS,
				"Binary Entropy", msg);
		}
		catch (Exception e) {
			// annotation is best-effort; never disrupt analysis
		}
		try {
			program.getListing().setComment(start, CommentType.PLATE, msg);
		}
		catch (Exception e) {
			// best-effort
		}
	}

	private void writeReport(String path, List<String> rows, MessageLog log) {
		try {
			Files.write(Paths.get(path), rows, StandardCharsets.UTF_8);
			log.appendMsg(NAME, "wrote entropy report: " + path);
		}
		catch (IOException e) {
			log.appendMsg(NAME, "could not write entropy report '" + path + "': " + e.getMessage());
		}
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
