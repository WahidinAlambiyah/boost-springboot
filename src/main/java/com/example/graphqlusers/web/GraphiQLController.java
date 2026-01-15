package com.example.graphqlusers.web;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GraphiQLController {
    private static final String PAGE = """
            <!doctype html>
            <html>
            <head>
              <meta charset=\"utf-8\" />
              <title>GraphiQL</title>
              <style>
                body { font-family: Arial, sans-serif; margin: 0; padding: 0; }
                #container { display: flex; height: 100vh; }
                textarea { width: 50%; padding: 16px; font-family: monospace; }
                pre { width: 50%; margin: 0; padding: 16px; background: #f5f5f5; overflow: auto; }
                .toolbar { padding: 8px; background: #222; color: white; }
                button { margin-left: 8px; }
              </style>
            </head>
            <body>
              <div class=\"toolbar\">
                GraphiQL - POST /graphql
                <button onclick=\"runQuery()\">Run</button>
              </div>
              <div id=\"container\">
                <textarea id=\"query\">query {\n  users {\n    id\n    username\n    email\n    fullName\n  }\n}\n</textarea>
                <pre id=\"result\"></pre>
              </div>
              <script>
                async function runQuery() {
                  const query = document.getElementById('query').value;
                  const response = await fetch('/graphql', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ query })
                  });
                  const data = await response.json();
                  document.getElementById('result').textContent = JSON.stringify(data, null, 2);
                }
              </script>
            </body>
            </html>
            """;

    @GetMapping("/graphiql")
    public ResponseEntity<String> graphiql() {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(PAGE);
    }
}
