package hk.edu.polyu.comp.comp2021.crobs.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Logs commands to an HTML file, completely rewriting the file on each log entry.
 */
public class HtmlLogger implements CommandLogger {
    private final String path;
    private final List<String> commands = new ArrayList<>();

    public HtmlLogger(String path) {
        this.path = path;
        flushToFile();
    }

    @Override
    public synchronized void log(String command) {
        commands.add(command);
        flushToFile();
    }

    @Override
    public synchronized void close() {
        flushToFile();
    }

    private void flushToFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><table border=\"1\">");
        sb.append("<tr><th>Index</th><th>Command</th></tr>");
        for (int i = 0; i < commands.size(); i++) {
            sb.append("<tr><td>").append(i + 1).append("</td><td>")
                    .append(escape(commands.get(i))).append("</td></tr>");
        }
        sb.append("</table></body></html>");

        try {
            Path filePath = Paths.get(path);
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            Files.write(filePath, sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}