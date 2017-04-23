package fr.faylixe.googlecodejam.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * <p>Main class that could be used as a command line program.</p>
 * 
 * @author fv
 */
public final class Application {

	/**
	 * Static flag that indicates if
	 * verbose mode if activated or not.
	 */
	private static boolean verbose;

	/**
	 * Indicates if this execution is in verbose mode.
	 * 
	 * @return <tt>true</tt> if execution should be verbose, <tt>false</tt> otherwise.
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
			CommandStatus status = CommandStatus.INVALID_FORMAT;
			if (command.hasOption(ApplicationConstant.INIT)) {
				status = ApplicationCommand.init(command);
			}
			else if(command.hasOption(ApplicationConstant.EXTRACT_SAMPLE_DATA_SETS)) {
				status = ApplicationCommand.initDataSets();
			}
			else if (command.hasOption(ApplicationConstant.DOWNLOAD)) {
				status = ApplicationCommand.download(command);
			}
			else if (command.hasOption(ApplicationConstant.SUBMIT)) {
				status = ApplicationCommand.submit(command);
			}

			if (!CommandStatus.SUCCESS.equals(status)) {
				System.exit(-1);
			}

		}
		catch (final ParseException e) {
			System.out.println("An error occurs while parsing command line arguments : " + e.getMessage());
			formatter.printHelp(ApplicationConstant.SYNTAX, options);
			System.exit(-1);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
