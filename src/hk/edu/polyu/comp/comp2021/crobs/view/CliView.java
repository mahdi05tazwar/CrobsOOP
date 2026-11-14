package hk.edu.polyu.comp.comp2021.crobs.view;

import hk.edu.polyu.comp.comp2021.crobs.controller.CommandController;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Objects;
import java.util.Scanner;

/**
 * Interactive command-line interface reading input lines and printing responses
 * returned by {@link CommandController}.
 */
public class CliView {
    private final Scanner in;
    private final PrintStream out;
    private final CommandController controller;

    /**
     * Constructs a {@code CliView}.
     *
     * @param in         input stream for user commands
     * @param out        output stream for prompts and responses
     * @param controller command controller handling command execution
     */
    public CliView(InputStream in, PrintStream out, CommandController controller) {
        Objects.requireNonNull(in, "Input stream cannot be null");
        Objects.requireNonNull(out, "Output stream cannot be null");
        Objects.requireNonNull(controller, "Command controller cannot be null");
        this.in = new Scanner(in);
        this.out = out;
        this.controller = controller;
    }

    /**
     * Starts the CLI loop, displaying the auto-created superadmin ID and executing
     * commands until the controller stops running or input ends. Always calls
     * {@link CommandController#shutdown()} on exit.
     *
     * @param superAdminId the superadmin user ID generated at startup
     */
    public void start(String superAdminId) {
        try {
            out.println("Welcome to CROBS. Superadmin ID: " + superAdminId);
            while (controller.isRunning()) {
                out.print("> ");
                if (!in.hasNextLine()) {
                    break;
                }
                String line = in.nextLine();
                String response = controller.execute(line);
                if (response != null && !response.isEmpty()) {
                    out.println(response);
                }
            }
        } finally {
            controller.shutdown();
        }
    }
}