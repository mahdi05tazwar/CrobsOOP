package hk.edu.polyu.comp.comp2021.crobs;

import hk.edu.polyu.comp.comp2021.crobs.controller.BookingController;
import hk.edu.polyu.comp.comp2021.crobs.controller.CommandController;
import hk.edu.polyu.comp.comp2021.crobs.controller.RoomController;
import hk.edu.polyu.comp.comp2021.crobs.controller.UserController;
import hk.edu.polyu.comp.comp2021.crobs.model.core.CrobsSystem;
import hk.edu.polyu.comp.comp2021.crobs.util.CommandLogger;
import hk.edu.polyu.comp.comp2021.crobs.util.HtmlLogger;
import hk.edu.polyu.comp.comp2021.crobs.util.TextLogger;
import hk.edu.polyu.comp.comp2021.crobs.view.CliView;

import java.util.ArrayList;
import java.util.List;

/**
 * Starts the CROBS application.
 */
public final class Application {

    private static final String USAGE = "Usage: java hk.edu.polyu.comp.comp2021.crobs.Application " + "-html <file> -txt <file>";

    /**
     * Private constructor to prevent creation of Application objects.
     */
    private Application() {}

    /**
     * Starts the application.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        String htmlPath = null;
        String textPath = null;
        for (int i = 0; i < args.length; i++) {
            if ("-html".equals(args[i]) && i + 1 < args.length) htmlPath = args[++i];
            else if ("-txt".equals(args[i]) && i + 1 < args.length) textPath = args[++i];
        }
        if (htmlPath == null || textPath == null) {
            System.out.println(USAGE);
            return;
        }

        CrobsSystem system = new CrobsSystem();

        List<CommandLogger> loggers = new ArrayList<>();
        loggers.add(new HtmlLogger(htmlPath));
        loggers.add(new TextLogger(textPath));
        CommandController controller = new CommandController(system, loggers);
        new UserController(system, controller);
        new RoomController(system, controller);
        new BookingController(system, controller);

        CliView view = new CliView(System.in, System.out, controller);
        view.start(system.getSuperAdminId());
    }
}