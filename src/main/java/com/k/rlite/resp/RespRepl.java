package com.k.rlite.resp;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class RespRepl {
    public static void main(String[] args) throws Exception {
        System.out.println("Paste RESP frame lines (no escapes). End the frame with an empty line.");
        System.out.println("Examples:");
        System.out.println("*2");
        System.out.println("$4");
        System.out.println("ECHO");
        System.out.println("$5");
        System.out.println("hello");
        System.out.println("(blank line to submit)");

        var console = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;

        while (true) {
            line = console.readLine();
            if (line == null) break;          // EOF (Ctrl+Z/Ctrl+D)
            if (line.isEmpty()) {             // blank line -> parse accumulated
                if (sb.length() == 0) break;  // two blanks in a row: exit
                String frame = sb.toString();
                try {
                    var reader = new RespReader(new ByteArrayInputStream(frame.getBytes(StandardCharsets.UTF_8)));
                    var msg = reader.read();
                    print(msg, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sb.setLength(0);
                System.out.println("\n--- Next frame ---");
            } else {
                if (sb.length() > 0) sb.append("\r\n");
                sb.append(line);
            }
        }
    }

    static void print(RespMessage m, int indent) {
        String pad = "  ".repeat(indent);
        System.out.println(pad + m.type);
        switch (m.type) {
            case SIMPLE_STRING, ERROR -> System.out.println(pad + "  \"" + m.simple + "\"");
            case INTEGER -> System.out.println(pad + "  " + m.integer);
            case BULK_STRING -> System.out.println(pad + "  bulk[" + m.bulk.length + "]");
            case NULL_BULK_STRING, NULL_ARRAY -> { /* nothing */ }
            case ARRAY -> {
                for (var item : m.array) print(item, indent + 1);
            }
        }
    }
}
