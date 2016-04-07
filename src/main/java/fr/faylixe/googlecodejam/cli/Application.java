package fr.faylixe.googlecodejam.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * <p>Main class that could be used as a command line program.</p>
 * 
 * @author fv
 */
public final class Application {

	/**
	 * 
	 */
	private static boolean verbose;

	/**
	 * 
	 * @return
	 */
	public static boolean isVerbose() {
		return verbose;
	}

	/**
	 * Private constructor for avoiding instantiation.
	 */
	private Application() {
		// Do nothing.
	}
	
	/**
	 * Command line application entry point.
	 * 
	 * @param args Command line parameters provided.
	 */
	public static void main(final String [] args) {
		final Options options = ApplicationConstant.createOptions();
		final HelpFormatter formatter = new HelpFormatter();
		final CommandLineParser parser = new DefaultParser();
		try {
			final CommandLine command = parser.parse(options, args);
			verbose = command.hasOption(ApplicationConstant.VERBOSE);
			boolean success = false;
			if (command.hasOption(ApplicationConstant.INIT)) {
				success = ApplicationCommand.init(command);
			}
			else if (command.hasOption(ApplicationConstant.DOWNLOAD)) {
				success = ApplicationCommand.download(command);
			}
			else if (command.hasOption(ApplicationConstant.SUBMIT)) {
				success = ApplicationCommand.submit(command);
			}
			if (!success) {
				formatter.printHelp(ApplicationConstant.SYNTAX, options);
			}
		}
		catch (final ParseException e) {
			System.out.println("An error occurs while parsing command line arguments : " + e.getMessage());
			formatter.printHelp(ApplicationConstant.SYNTAX, options);
		}
	}

}
