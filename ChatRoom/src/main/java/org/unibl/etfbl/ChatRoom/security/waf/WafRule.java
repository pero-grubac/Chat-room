package org.unibl.etfbl.ChatRoom.security.waf;

import java.util.regex.Pattern;

/**
 * Attack patterns the WAF rejects. Each rule matches the shape of an attack rather than a single
 * word, so ordinary sentences ("rock and roll", "please update the list") pass. JPA parameter binding
 * and React output escaping remain the primary defences; these rules stop obvious probes early and
 * feed the security log.
 */
public enum WafRule {
    RESERVED_NAME("\\banonymousUser\\b"),

    // SQL injection
    SQL_TAUTOLOGY("['\"]\\s*(or|and)\\s+['\"\\w]+\\s*(=|like)\\s*['\"\\w]+|\\bor\\s+1\\s*=\\s*1\\b"),
    SQL_UNION_SELECT("\\bunion(\\s+all)?\\s+select\\b"),
    SQL_STACKED_QUERY(";\\s*(drop|delete|insert|update|alter|truncate|exec|create|shutdown)\\b"),
    SQL_DDL("\\b(drop|truncate|alter)\\s+(table|database|schema|user)\\b"),
    SQL_DML("\\b(delete\\s+from|insert\\s+into)\\s+\\w+"),
    SQL_COMMENT_AFTER_QUOTE("['\"]\\s*(--|#|/\\*)"),
    SQL_TIME_BASED("\\b(sleep|benchmark|pg_sleep|waitfor\\s+delay)\\s*\\("),

    // XSS and HTML
    HTML_TAG("<\\s*/?\\s*[a-z][a-z0-9-]*(\\s[^>]*)?/?\\s*>|<\\s*(script|iframe|object|embed|svg|img)\\b"),
    EVENT_HANDLER("\\bon[a-z]{3,}\\s*="),
    SCRIPT_URL("\\b(javascript|vbscript)\\s*:|\\bdata\\s*:\\s*text/html"),
    JS_SINK("\\b(document\\s*\\.\\s*(cookie|write|domain)|window\\s*\\.\\s*location|eval\\s*\\(|localStorage|sessionStorage)"),

    // Template, expression and server-side code injection
    TEMPLATE_EXPRESSION("\\$\\{|#\\{|\\{\\{|<%"),
    JAVA_REFLECTION("\\b(Runtime\\s*\\.\\s*(getRuntime|exec)|ProcessBuilder|java\\.lang\\.reflect|System\\s*\\.\\s*exit|Class\\s*\\.\\s*forName)\\b"),

    // Files, protocols and commands
    PATH_TRAVERSAL("\\.\\.[/\\\\]|%2e%2e[%/\\\\]|/etc/passwd"),
    URL("\\b(https?|ftp|file)://"),
    COMMAND_INJECTION("[;&|`]\\s*(rm|cat|curl|wget|nc|bash|sh|cmd|powershell)\\b|\\$\\([^)]*\\)|\\brm\\s+-rf\\b"),

    // LDAP and XML
    LDAP_INJECTION("\\*\\)\\s*\\(|\\)\\s*\\(\\s*[|&!]"),
    XML_ENTITY("<!\\s*(entity|doctype)|<!\\[cdata\\["),

    // Email header injection: a header only counts after a line break
    MAIL_HEADER_INJECTION("[\\r\\n]\\s*(to|cc|bcc|from|subject|reply-to)\\s*:");

    private final Pattern pattern;

    WafRule(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public boolean matches(String input) {
        return pattern.matcher(input).find();
    }
}
