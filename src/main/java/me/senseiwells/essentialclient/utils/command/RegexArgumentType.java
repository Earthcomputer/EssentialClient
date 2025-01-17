package me.senseiwells.essentialclient.utils.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import me.senseiwells.essentialclient.utils.render.Texts;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

// Taken from ClientCommands
public class RegexArgumentType implements ArgumentType<Pattern> {

	private static final DynamicCommandExceptionType EXPECTED_REGEX_EXCEPTION = new DynamicCommandExceptionType(arg -> Texts.translatable("Invalid regex %s", arg));

	private final RegexType type;

	private RegexArgumentType(RegexType type) {
		this.type = type;
	}

	@SuppressWarnings("unused")
	public static RegexArgumentType wordRegex() {
		return new RegexArgumentType(RegexType.SINGLE_WORD);
	}

	@SuppressWarnings("unused")
	public static RegexArgumentType slashyRegex() {
		return new RegexArgumentType(RegexType.SLASHY_PHRASE);
	}

	@SuppressWarnings("unused")
	public static RegexArgumentType greedyRegex() {
		return new RegexArgumentType(RegexType.GREEDY_PHRASE);
	}

	@SuppressWarnings("unused")
	public static Pattern getRegex(CommandContext<?> context, String name) {
		return context.getArgument(name, Pattern.class);
	}

	@Override
	public Pattern parse(StringReader reader) throws CommandSyntaxException {
		final int start = reader.getCursor();
		if (this.type == RegexType.GREEDY_PHRASE) {
			String text = reader.getRemaining();
			try {
				Pattern pattern = Pattern.compile(text);
				reader.setCursor(reader.getTotalLength());
				return pattern;
			}
			catch (PatternSyntaxException e) {
				reader.setCursor(start);
				throw EXPECTED_REGEX_EXCEPTION.createWithContext(reader, text);
			}
		}
		if (this.type == RegexType.SINGLE_WORD) {
			String text = reader.readUnquotedString();
			try {
				return Pattern.compile(text);
			}
			catch (PatternSyntaxException e) {
				reader.setCursor(start);
				throw EXPECTED_REGEX_EXCEPTION.createWithContext(reader, text);
			}
		}
		return parseSlashyRegex(reader);
	}

	public static Pattern parseSlashyRegex(StringReader reader) throws CommandSyntaxException {
		final int start = reader.getCursor();

		boolean slashy = reader.canRead() && reader.peek() == '/';
		if (!slashy) {
			String text = reader.readUnquotedString();
			try {
				return Pattern.compile(text);
			}
			catch (PatternSyntaxException e) {
				reader.setCursor(start);
				throw EXPECTED_REGEX_EXCEPTION.createWithContext(reader, text);
			}
		}

		reader.skip(); // /

		StringBuilder regex = new StringBuilder();
		boolean escaped = false;
		while (true) {
			if (!reader.canRead()) {
				reader.setCursor(start);
				throw EXPECTED_REGEX_EXCEPTION.createWithContext(reader, reader.getString().substring(start));
			}

			if (reader.peek() == '/') {
				if (!escaped) {
					reader.skip();
					try {
						return Pattern.compile(regex.toString());
					}
					catch (PatternSyntaxException e) {
						int end = reader.getCursor();
						reader.setCursor(start);
						throw EXPECTED_REGEX_EXCEPTION.createWithContext(reader, reader.getString().substring(start, end));
					}
				}
				regex.deleteCharAt(regex.length() - 1); // the backslash which escaped this slash
			}

			escaped = reader.peek() == '\\' && !escaped;
			regex.append(reader.peek());
			reader.skip();
		}
	}

	@Override
	public Collection<String> getExamples() {
		return this.type.getExamples();
	}

	public enum RegexType {
		SINGLE_WORD("word", "\\w+"),
		SLASHY_PHRASE("/\\w+/", "word", "//"),
		GREEDY_PHRASE("word", "words with spaces", "/and symbols/"),
		;

		private final Collection<String> examples;

		RegexType(final String... examples) {
			this.examples = Arrays.asList(examples);
		}

		public Collection<String> getExamples() {
			return this.examples;
		}
	}
}
