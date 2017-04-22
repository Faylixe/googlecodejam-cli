package fr.faylixe.googlecodejam.cli;

import org.apache.commons.cli.Options;

/**
 * <p>Constants used for managing command
 * line parameters.</p>
 * 
 * @author fv
 */
public final class ApplicationConstant {

	/** Syntax of the command line script. **/
	public static final String SYNTAX = "codejamclient.sh action parameter";

	/** Short option for the contest parameter. **/
	public static final String CONTEST = "c";

	/** Long option for the contest parameter. **/
	public static final String CONTEST_LONG = "contest";

	/** Description for the contest parameter. **/
	public static final String CONTEST_DESCRIPTION = "Id of the target contest to work with.";

	/** Short option for the initialization action. **/
	public static final String INIT = "i";

	/** Long option for the initialization action**/
	public static final String INIT_LONG = "init";

	/** Description of the initialization action. **/
	public static final String INIT_DESCRIPTION = "Initializes code jam context by logging user in, and selects an active contest and round.";

	/** Short option for the initialization method. **/
	public static final String INIT_METHOD = "m";

	/** Long option for initialization method. **/
	public static final String INIT_METHOD_LONG = "method";

	/** Description of the method parameter. **/
	public static final String INIT_METHOD_DESCRIPTION = "Specify the initialization method to use. Supported method are firefox, or text";

	/** Parameter value for firefox initialization method. **/
	public static final String FIREFOX_METHOD = "firefox";

	/** Parameter value for chrome initialization method. **/
	public static final String CHROME_METHOD = "chrome";

	/** Parameter value for text initialization method. **/
	public static final String TEXT_METHOD = "text";

	/** Short option for the download action. **/
	public static final String DOWNLOAD = "d";

	/** Long option for the download action. **/
	public static final String DOWNLOAD_LONG = "download";

	/** Description of the download action. **/
	public static final String DOWNLOAD_DESCRIPTION = "Downloads an input file for a given problem and target dataset.";

	/** Short option for the attempt parameter. **/
	public static final String DOWNLOAD_ATTEMPT = "a";

	/** Long option for the attempt parameter. **/
	public static final String DOWNLOAD_ATTEMPT_LONG = "attempt";

	/** Description of the attemp parameter. **/
	public static final String DOWNLOAD_ATTEMPT_DESCRIPTION = "Attempt number to use for the download action";

	/** Short option for the submit action. **/
	public static final String SUBMIT = "s";

	/** Long option for the submit action. **/
	public static final String SUBMIT_LONG = "submit";

	/** Description of the submission action. **/
	public static final String SUBMIT_DESCRIPTION = "Submits an output file as solution source as well for a given problem and target dataset.";

	/** Short option for the problem parameter. **/
	public static final String PROBLEM = "p";

	/** Long option for the problem parameter. **/
	public static final String PROBLEM_LONG = "problem";

	/** Description of the problem parameter. **/
	public static final String PROBLEM_DESCRIPTION = "Problem to download or submit problem from, consists in a letter or the problem list index.";

	/** Short option for the input type parameter. **/
	public static final String INPUT_TYPE = "t";

	/** Long option for the input type parameter. **/
	public static final String INPUT_TYPE_LONG = "inputtype";

	/** Description of the input type parameter. **/
	public static final String INPUT_TYPE_DESCRIPTION = "Dataset input type, usually small or large.";

	/** Short option for the output parameter. **/
	public static final String OUTPUT = "o";

	/** Long option for the output parameter. **/
	public static final String OUTPUT_LONG = "output";

	/** Description of the output parameter. **/
	public static final String OUTPUT_DESCRIPTION = "Output file to submit.";

	/** Short option for the source parameter. **/
	public static final String SOURCE = "f";

	/** Long option for the source parameter. **/
	public static final String SOURCE_LONG = "sourcefile";

	/** Description of the source parameter. **/
	public static final String SOURCE_DESCRIPTION = "This parameter specifies the source file to upload.";

	/** Short option for the verbose mode. **/
	public static final String VERBOSE = "v";

	/** Long option for the verbose mode.**/
	public static final String VERBOSE_LONG = "verbose";

	/** Description for the verbose mode. **/
	public static final String VERBOSE_DESCRIPTION = "Verbose mode for debugging purpose only";

	/**
	 * Static factory method that creates the {@link Options} list
	 * for the command line arguments parsing.
	 * 
	 * @return Created options list.
	 */
	public static Options createOptions() {
		final Options options = new Options();
		options.addOption(INIT, INIT_LONG, false, INIT_DESCRIPTION);
		options.addOption(DOWNLOAD, DOWNLOAD_LONG, false, DOWNLOAD_DESCRIPTION);
		options.addOption(SUBMIT, SUBMIT_LONG, false, SUBMIT_DESCRIPTION);
		options.addOption(PROBLEM, PROBLEM_LONG, true, PROBLEM_DESCRIPTION);
		options.addOption(INIT_METHOD, INIT_METHOD_LONG, true, INIT_METHOD_DESCRIPTION);
		options.addOption(CONTEST, CONTEST_LONG, true, CONTEST_DESCRIPTION);
		options.addOption(DOWNLOAD_ATTEMPT, DOWNLOAD_ATTEMPT_LONG, true, DOWNLOAD_ATTEMPT_DESCRIPTION);
		options.addOption(INPUT_TYPE, INPUT_TYPE_LONG, true, INPUT_TYPE_DESCRIPTION);
		options.addOption(OUTPUT, OUTPUT_LONG, true, OUTPUT_DESCRIPTION);
		options.addOption(SOURCE, SOURCE_LONG, true, SOURCE_DESCRIPTION);
		options.addOption(VERBOSE, VERBOSE_LONG, false, VERBOSE_DESCRIPTION);
		return options;
	}

	/**
	 * Private constructor for avoiding instantiation.
	 */
	private ApplicationConstant() {
		// Do nothing.
	}

}
