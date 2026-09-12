/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package org.antlr.v4.test.tool;

import org.antlr.v4.Tool;
import org.antlr.v4.test.runtime.ErrorQueue;
import org.antlr.v4.tool.ANTLRMessage;
import org.antlr.v4.tool.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestHeaderOutputDirectory {
	private static final String GRAMMAR =
		"grammar T;\n" +
		"s : ID ;\n" +
		"ID : [a-z]+ ;\n" +
		"WS : [ \\t\\r\\n]+ -> skip ;\n";

	@TempDir
	Path tempDir;

	Path grammarFile;
	Path gen;
	Path inc;

	@BeforeEach
	public void beforeEach() throws IOException {
		grammarFile = tempDir.resolve("T.g4");
		Files.write(grammarFile, GRAMMAR.getBytes());
		gen = tempDir.resolve("gen");
		inc = tempDir.resolve("inc");
	}

	@Test
	public void testOutputDirectoryOnlyKeepsLayout() throws IOException {
		ErrorQueue errors = new ErrorQueue();
		generate(errors, "-Dlanguage=Cpp", "-o", gen.toString(), grammarFile.toString());

		assertEquals(0, errors.errors.size(), errors.toString());
		assertEquals(set(
				"gen/T.interp", "gen/T.tokens",
				"gen/TBaseListener.cpp", "gen/TBaseListener.h",
				"gen/TLexer.cpp", "gen/TLexer.h", "gen/TLexer.interp", "gen/TLexer.tokens",
				"gen/TListener.cpp", "gen/TListener.h",
				"gen/TParser.cpp", "gen/TParser.h"),
			generatedFiles());
	}

	@Test
	public void testHeadersGoToHeaderDirectory() throws IOException {
		ErrorQueue errors = new ErrorQueue();
		generate(errors, "-Dlanguage=Cpp", "-o", gen.toString(), "-header-dir", inc.toString(), grammarFile.toString());

		assertEquals(0, errors.errors.size(), errors.toString());
		assertEquals(set(
				"gen/T.interp", "gen/T.tokens",
				"gen/TBaseListener.cpp", "gen/TLexer.cpp", "gen/TLexer.interp", "gen/TLexer.tokens",
				"gen/TListener.cpp", "gen/TParser.cpp",
				"inc/TBaseListener.h", "inc/TLexer.h", "inc/TListener.h", "inc/TParser.h"),
			generatedFiles());
	}

	@Test
	public void testListenerAndVisitorHeadersGoToHeaderDirectory() throws IOException {
		ErrorQueue errors = new ErrorQueue();
		generate(errors, "-Dlanguage=Cpp", "-visitor", "-listener",
			"-o", gen.toString(), "-header-dir", inc.toString(), grammarFile.toString());

		assertEquals(0, errors.errors.size(), errors.toString());
		assertEquals(set(
				"gen/T.interp", "gen/T.tokens",
				"gen/TBaseListener.cpp", "gen/TBaseVisitor.cpp", "gen/TLexer.cpp", "gen/TLexer.interp",
				"gen/TLexer.tokens", "gen/TListener.cpp", "gen/TParser.cpp", "gen/TVisitor.cpp",
				"inc/TBaseListener.h", "inc/TBaseVisitor.h", "inc/TLexer.h", "inc/TListener.h",
				"inc/TParser.h", "inc/TVisitor.h"),
			generatedFiles());
	}

	@Test
	public void testTargetWithoutHeadersIgnoresHeaderDirectory() throws IOException {
		Path plain = tempDir.resolve("plain");
		ErrorQueue errors = new ErrorQueue();
		generate(errors, "-o", plain.toString(), grammarFile.toString());
		generate(errors, "-o", gen.toString(), "-header-dir", inc.toString(), grammarFile.toString());

		assertEquals(0, errors.errors.size(), errors.toString());
		assertFalse(Files.exists(inc));
		Set<String> generated = generatedFiles();
		Set<String> withOption = new TreeSet<>();
		Set<String> withoutOption = new TreeSet<>();
		for (String name : generated) {
			if (name.startsWith("gen/")) withOption.add(name.substring("gen/".length()));
			else if (name.startsWith("plain/")) withoutOption.add(name.substring("plain/".length()));
		}
		assertEquals(withoutOption, withOption);
		assertTrue(withOption.contains("TParser.java"), withOption.toString());
		assertEquals(generated.size(), withOption.size() + withoutOption.size(), generated.toString());
	}

	@Test
	public void testTrailingSeparatorIsAccepted() {
		ErrorQueue errors = new ErrorQueue();
		Tool tool = createTool(errors, "-Dlanguage=Cpp", "-o", gen.toString(),
			"-header-dir", inc + File.separator, grammarFile.toString());

		assertEquals(0, errors.errors.size(), errors.toString());
		assertEquals(inc.toFile(), tool.getHeaderOutputDirectory(grammarFile.toString()));
	}

	@Test
	public void testHeaderDirectoryNamingAFileIsAnErrorLikeOutputDirectory() throws IOException {
		Path file = tempDir.resolve("not-a-directory");
		Files.write(file, new byte[0]);
		ErrorQueue errors = new ErrorQueue();
		Tool tool = generate(errors, "-Dlanguage=Cpp", "-o", gen.toString(),
			"-header-dir", file.toString(), grammarFile.toString());

		assertEquals(Arrays.asList(ErrorType.OUTPUT_DIR_IS_FILE), errorTypes(errors));
		assertEquals(gen.toFile(), tool.getHeaderOutputDirectory(grammarFile.toString()));
		// as with -o naming a file, the error stops code generation
		assertEquals(set(), generatedFiles());
	}

	private Tool generate(ErrorQueue errors, String... args) {
		Tool tool = createTool(errors, args);
		tool.processGrammarsOnCommandLine();
		return tool;
	}

	/** Attaches the listener before argument handling, so option errors are captured too. */
	private static Tool createTool(ErrorQueue errors, String... args) {
		return new Tool(args) {
			@Override
			protected void handleArgs() {
				addListener(errors);
				super.handleArgs();
			}
		};
	}

	private static List<ErrorType> errorTypes(ErrorQueue errors) {
		List<ErrorType> types = new ArrayList<>();
		for (ANTLRMessage message : errors.errors) {
			types.add(message.getErrorType());
		}
		return types;
	}

	/** Every file created under the temporary directory apart from inputs, with '/' as separator. */
	private Set<String> generatedFiles() throws IOException {
		try (Stream<Path> paths = Files.walk(tempDir)) {
			return paths
				.filter(Files::isRegularFile)
				.map(tempDir::relativize)
				.map(p -> p.toString().replace(File.separatorChar, '/'))
				.filter(name -> !name.equals("T.g4") && !name.equals("not-a-directory"))
				.collect(Collectors.toCollection(TreeSet::new));
		}
	}

	private static Set<String> set(String... names) {
		return new TreeSet<>(Arrays.asList(names));
	}
}
