package fr.faylixe.googlecodejam.cli;

import fr.faylixe.googlecodejam.client.CodeJamSession;
import fr.faylixe.googlecodejam.client.Contest;
import fr.faylixe.googlecodejam.client.Round;
import fr.faylixe.googlecodejam.client.common.HTMLConstant;
import fr.faylixe.googlecodejam.client.common.NamedObject;
import fr.faylixe.googlecodejam.client.executor.HttpRequestExecutor;
import fr.faylixe.googlecodejam.client.executor.Request;
import fr.faylixe.googlecodejam.client.webservice.Problem;
import fr.faylixe.googlecodejam.client.webservice.ProblemInput;
import fr.faylixe.googlecodejam.client.webservice.SubmitResponse;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.SerializationUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Supplier;

import static fr.faylixe.googlecodejam.cli.ApplicationConstant.*;
import static java.lang.System.err;
import static java.lang.System.out;

/**
 * <p>This class contains static method that are
 * executed from parsed command line argument in
 * {@link Application}.</p>
 * 
 * @author fv
 */
public final class ApplicationCommand {

	/** Path of the serialized round file to use. **/
	private static final String ROUND_PATH = ".cjs-round";

	/** Path of the serialized cookie file to use. **/
	private static final String COOKIE_PATH = ".cjs-cookie";

	/** Classname of the DIV that contains our testing dataset. **/
	private static final String IO_CLASSNAME = "problem-io-wrapper";

	/** Number of row used for dataset extraction. **/
	private static final int DATASET_ROW = 2;

	/** File extension for sample dataset. **/
	private static final String TEST_EXTENSION = ".test";

	/**
	 * Prompts users for selecting a valid {@link Round}
	 * instance that will be used as a contextual round.
	 * If the given <tt>contest</tt> identifier is not null, then
	 * it will be used for creating the round instance.
	 * 
	 * @param contest Contest identifier to use.
	 * @param cookie Cookie value to use for retrieving given round.
	 * @throws IOException If any error occurs while downloading contest page.
	 * @throws GeneralSecurityException If any error occurs while creating HTTP client.
	 */
	private static Optional<Round> selectRound(final String contest, final String cookie) throws IOException, GeneralSecurityException {
		if (contest != null) {
			return Optional.of(Round.fromIdentifier(contest, cookie));
		}
		out.println("Enter contest id ('-' if you don't have a contest id):\n> ");
		final Scanner reader = new Scanner(System.in);
		final String contestId = reader.next();
		if(!contestId.trim().equals("-")) {
			if(isValidContestID(contestId)) {
				return Optional.of(Round.fromIdentifier(contestId.trim(), cookie));
			}
			return Optional.empty();
		}

		out.println("[Round selection] Extracting contest list.");
		final HttpRequestExecutor executor = HttpRequestExecutor.create(Request.getHostname());
		final List<Contest> contests = Contest.get(executor);
		final Optional<Contest> selectedContest = select(contests, reader);
		if (selectedContest.isPresent()) {
			return select(selectedContest.get().getRounds(), reader);
		}
		return Optional.empty();
	}

	private static boolean isValidContestID(final String contestId) {

		try {
			long id = Long.parseLong(contestId);
			return true;
		} catch(NumberFormatException e) {
			err.println("-> Number expected, abort");
			if (Application.isVerbose()) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Selects a named object from the given list using the
	 * given {@link Scanner} for reading user input.
	 * 
	 * @param objects {@link NamedObject} list that user should choose in.
	 * @return Optional selected {@link NamedObject} instance.
	 */
	private static <T extends NamedObject> Optional<T> select(final List<T> objects, final Scanner reader) {
		out.println("[Round selection] Please select a contest :");
		for (int i = 0; i < objects.size(); i++) {
			final StringBuilder builder = new StringBuilder('\t');
			builder.append(i + 1);
			builder.append(" - ");
			builder.append(objects.get(i).getName());
			out.println(builder.toString());
		}
		out.print("> ");
		final String input = reader.next();
		try {
			final int selected = Integer.valueOf(input);
			if (selected > 0 && selected <= objects.size()) {
				return Optional.of(objects.get(selected - 1));
			}
		}
		catch (final NumberFormatException e) {
			err.println("-> Number expected, abort");
			if (Application.isVerbose()) {
				e.printStackTrace();
			}
		}
		return Optional.empty();
	}

	/**
	 * Creates and saves a contextual session.
	 * 
	 * @param cookie Cookie value to use for initialization.
	 * @param contest Contest id to use. If <tt>null</tt> the contest selection menu will be shown.
	 * @return <tt>true</tt> if the init command was correctly executed, <tt>false</tt> otherwise.
	 * @throws IOException If any error occurs while saving contextual session.
	 * @throws GeneralSecurityException If any error occurs while creating session.
	 */
	private static CommandStatus init(final String cookie, final String contest) throws IOException, GeneralSecurityException {
		out.println("[Initialization] Cookie retrieved");
		final Optional<Round> round = selectRound(contest, cookie);
		if (round.isPresent()) {
			out.println("[Initialization] Writing " + COOKIE_PATH);
			SerializationUtils.serialize(cookie, new FileOutputStream(COOKIE_PATH));
			out.println("Cookie retrieved successfully.");
			out.println("[Initialization] Writing " + ROUND_PATH);
			SerializationUtils.serialize(round.get(), new FileOutputStream(ROUND_PATH));
			out.println("Round retrieved successfully, you can now download and submit in this directory.");
			return CommandStatus.SUCCESS;
		}
		err.println("-> No round selected, abort.");
		return CommandStatus.FAILED;
	}

	public static CommandStatus initDataSets() throws IOException, GeneralSecurityException {
		out.println("[Initialization] Creating input directory");
		Files.createDirectories(Paths.get(EXTRACTED_SAMPLE_INPUT_DIRECTORY));
		out.println("[Initialization] Creating output directory");
		Files.createDirectories(Paths.get(EXTRACTED_SAMPLE_OUTPUT_DIRECTORY));
		out.println("[Initialization] Generating sample dataset");
		final CodeJamSession session = getContextualSession();
		try {
			final List<Problem> problems = session
					.getContestInfo()
					.getProblems();
			for (int i = 0; i < problems.size(); i++) {
				extractDataset(problems.get(i), i);
			}
		} catch(Exception e) {
			e.printStackTrace();
			try {Thread.sleep(10);} catch (InterruptedException e1) {}
			err.println("-> Problem occurred while extracting sample data sets, abort.");
			clearTestDataSets();
			return CommandStatus.FAILED;
		}
		out.println("Sample data sets retrieved successfully.");
		return CommandStatus.SUCCESS;
	}

	/**
	 * Extracts and creates sample dataset from the given <tt>problem</tt>.
	 * 
	 * @param problem Problem to create sample dataset for.
	 * @throws IOException If any error occurs while creating sample dataset.
	 */
	private static void extractDataset(final Problem problem, final int id) throws IOException {
		final Document document = Jsoup.parse(problem.getBody());
		final Elements problemIO = document.getElementsByClass(IO_CLASSNAME);
		if (!problemIO.isEmpty()) {
			final Elements row = problemIO.first().getElementsByTag(HTMLConstant.TR);
			if (row.size() >= DATASET_ROW) {
				final Element dataset = row.get(1);
				final Elements io = dataset.getElementsByTag(HTMLConstant.TD);
				if (io.size() >= DATASET_ROW) {
					final char problemIdentifier = (char)('A' + id);
					final String path = new StringBuilder()
						.append(problemIdentifier)
						.append(TEST_EXTENSION)
						.toString();
					Files.write(
							Paths.get(EXTRACTED_SAMPLE_INPUT_DIRECTORY).resolve(path),
							io.first().text().getBytes());
					Files.write(
							Paths.get(EXTRACTED_SAMPLE_OUTPUT_DIRECTORY).resolve(path),
							io.get(1).text().getBytes());
				}
			}
		}
	}

	/**
	 * Starts chrome through selenium to retrieve
	 * cookie instance and process initialization.
	 * 
	 * @param driverSupplier Driver supplier to use.
	 * @param contest Contest identifier to use.
	 * @return <tt>true</tt> if the init command was correctly executed, <tt>false</tt> otherwise.
	 */
	private static CommandStatus browserInit(final Supplier<WebDriver> driverSupplier, final String contest) {
		out.println("[Initialization] Web browser will open, please authenticate to your Google account with it.");
		ChromeDriverManager.getInstance().setup();
		final SeleniumCookieSupplier supplier = new SeleniumCookieSupplier(Request.getHostname() + "/codejam", ChromeDriver::new);
		try {
			final String cookie = supplier.get();
			if (cookie == null) {
				err.println("-> Retrieved cookie instance is null, abort.");
			}
			else {
				return init(cookie, contest);
			}
		}
		catch (final IOException | UnreachableBrowserException | GeneralSecurityException e) {
			err.println("-> An error occurs while creating CodeJamSession");
			if (Application.isVerbose()) {
				e.printStackTrace();
			}
		}
		return CommandStatus.FAILED;
	}

	/**
	 * Initializes contextual session by asking user for SACSID cookie value.
	 * 
	 * @param contest Contest identifier to use.
	 * @return <tt>true</tt> if the init command was correctly executed, <tt>false</tt> otherwise.
	 */
	private static CommandStatus textInit(final String contest) {
		out.println("Please enter the SACSID cookie value to use :");
		final Scanner scanner = new Scanner(System.in);
		final String cookie = scanner.next();
		scanner.close();
		try {
			return init(cookie, contest);
		}
		catch (final IOException | GeneralSecurityException e) {
			err.println("-> An error occurs while creating CodeJamSession");
			if (Application.isVerbose()) {
				e.printStackTrace();
			}
		}
		return CommandStatus.FAILED;
	}

	/**
	 * Methods that is bound to the INIT command. 
	 * 
	 * @param command Command to retrieve method parameters from.
	 * @return <tt>true</tt> if the init command was correctly executed, <tt>false</tt> otherwise.
	 */
	public static CommandStatus init(final CommandLine command) {
		clearTestDataSets();
		final String contest = command.getOptionValue(CONTEST);
		if (command.hasOption(INIT_METHOD)) {
			final String method = command.getOptionValue(INIT_METHOD).toLowerCase();
			if (CHROME_METHOD.equals(method)) {
				return browserInit(ChromeDriver::new, contest);
			}
			else if (TEXT_METHOD.equals(method)) {
				return textInit(contest);
			}
			printInvalidFormatHelpWithError("-> Invalid method provided (only chrome or text supported");
			return CommandStatus.INVALID_FORMAT;
		}
		return browserInit(ChromeDriver::new, contest);
	}

	public static void clearTestDataSets() {
		removeAllUnderDir(EXTRACTED_SAMPLE_INPUT_DIRECTORY);
		removeAllUnderDir(EXTRACTED_SAMPLE_OUTPUT_DIRECTORY);
	}

	public static void removeAllUnderDir(String dirName) {
		File dir = new File(dirName);
		if(dir.exists()) {
			for(File f : dir.listFiles()) {
				f.delete();
			}
		}
	}

	/**
	 * If exists, deserializes the cookie and round file
	 * in order to create and returns a valid {@link CodeJamSession}.
	 * 
	 * @return Contextual session loaded if exist.
	 * @throws IOException If the session could not be loaded.
	 * @throws GeneralSecurityException 
	 */
	private static CodeJamSession getContextualSession() throws IOException, GeneralSecurityException {
		final String cookie = SerializationUtils.deserialize(new FileInputStream(COOKIE_PATH));
		if (cookie == null) {
			throw new IOException("Invalid cookie file, please initialize directory again.");
		}
		final HttpRequestExecutor executor = HttpRequestExecutor.create(Request.getHostname(), cookie);
		final Round round = SerializationUtils.deserialize(new FileInputStream(ROUND_PATH));
		if (round == null) {
			throw new IOException("Contextual session is broken, please initialize directory again.");
		}
		return CodeJamSession.createSession(executor, round);
	}

	/**
	 * Retrieves and returns the problem input from the given <tt>command</tt>
	 * using the given <tt>session</tt>.
	 * 
	 * @param command Command to retrieve input parameters from.
	 * @param session Session to retrieve input from.
	 * @return Retrieved {@link ProblemInput} instance if exists, <tt>null</tt> otherwise.
	 */
	private static ProblemInput getProblemInput(final CommandLine command, final CodeJamSession session) {
		if (!command.hasOption(PROBLEM) || !command.hasOption(INPUT_TYPE)) {
			printInvalidFormatHelpWithError("-> Download command requires problem and input type parameters.");
			return null;
		}
		final String problemArgument = command.getOptionValue(PROBLEM);
		final String inputArgument = command.getOptionValue(INPUT_TYPE);
		final Problem problem = session.getProblem(problemArgument);
		if (problem == null) {
			printInvalidFormatHelpWithError("-> Problem " + problemArgument + " not found.");
			return null;
		}
		final ProblemInput input = problem.getProblemInput(inputArgument);
		if (input == null) {
			String availableInputs = Arrays.toString(problem.getProblemInputs().stream().map((in) -> in.getName()).toArray());
			StringBuilder msg = new StringBuilder()
					.append("-> Input " + inputArgument + " not found for problem " + problemArgument + ".\n")
					.append("-> Available input types are: " + availableInputs);
			printInvalidFormatHelpWithError(msg.toString());
			return null;
		}
		return input;
	}

	/**
	 * Downloads an input file, from the given user <tt>command</tt>.
	 * Retrieves the contextual session if exist, and if so, then
	 * the download method is used on the loaded session. If the
	 * file already exist, it will be removed first before downloading.
	 * 
	 * @param command User command line.
	 * @return <tt>true</tt> if the command was executed successfully, <tt>false</tt> otherwise.
	 */
	public static CommandStatus download(final CommandLine command) {
		try {
			final CodeJamSession session = getContextualSession();
			final ProblemInput input = getProblemInput(command, session);
			if (input == null) {
				return CommandStatus.INVALID_FORMAT;
			}
			final String rawAttempt = command.getOptionValue(DOWNLOAD_ATTEMPT);
			final int attempt = (rawAttempt == null ? 0 : Integer.valueOf(rawAttempt));
			final InputStream stream = session.download(input, attempt);
			final Path target = Paths.get(session.buildFilename(input, attempt));
			Files.deleteIfExists(target);
			Files.copy(stream, target);
			out.println(target.toString());
		}
		catch (final IOException | GeneralSecurityException e) {
			err.println("-> An error occurs while downloading input file : " + e.getMessage());
			if (Application.isVerbose()) {
				e.printStackTrace();
			}
			return CommandStatus.FAILED;
		}
		return CommandStatus.SUCCESS;
	}

	/**
	 * Performs an output file submission using the <tt>SubmitAnswer</tt>
	 * command. Retrieves the contextual session if exist, and if so, then
	 * the submit method is used on the loaded session.
	 * 
	 * @param command User command line.
	 * @return <tt>true</tt> if the command was executed successfully, <tt>false</tt> otherwise.
	 */
	public static CommandStatus submit(final CommandLine command) {
		if (!command.hasOption(OUTPUT) || !command.hasOption(SOURCE)) {
			printInvalidFormatHelpWithError("-> Submit command requires output and source file parameters.");
			return CommandStatus.INVALID_FORMAT;
		}
		final String output = command.getOptionValue(OUTPUT);
		final String source = command.getOptionValue(SOURCE);
		try {
			final CodeJamSession session = getContextualSession();
			final ProblemInput input = getProblemInput(command, session);
			final SubmitResponse response = session.submit(input, new File(output), new File(source));
			if (response.isSuccess()) {
				out.println("Submission correct !");
				return CommandStatus.SUCCESS;
			}
			out.println("Submission failed : " + response.getMessage());
		}
		catch (final IOException | GeneralSecurityException e) {
			err.println("An error occurs while submitting output file : " + e.getMessage());
			if (Application.isVerbose()) {
				e.printStackTrace();
			}
			return CommandStatus.FAILED;
		}
		return CommandStatus.SUCCESS;
	}

	public static void printInvalidFormatHelpWithError(final String errorMsg) {
		final Options options = ApplicationConstant.createOptions();
		final HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(ApplicationConstant.SYNTAX, options);

		try {
			Thread.sleep(10);//to make sure standard output is printed before error output and prevent them from getting mixed up
		} catch (InterruptedException e) {}
		System.err.println(errorMsg);
	}

}
