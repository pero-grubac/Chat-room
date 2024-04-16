package org.unibl.etfbl.ChatRoom.enums;

import lombok.Getter;

@Getter
public enum BannedWordsEnum {
    USERNAME("anonymousUser"),
    // SQL
    SELECT("select"),
   // DELETE("delete"),
    DROP("drop"),
    INSERT("insert"),
    UPDATE("update"),
    TRUNCATE("truncate"),
    UNION("union"),
    JOIN("join"),
    OR("or"),
    AND("and"),
    EXEC("exec"),
    EXECUTE("execute"),
    ALTER("alter"),
    CREATE("create"),
    GRANT("grant"),
    REVOKE("revoke"),

    // XSS
    XSS_SCRIPT("<script>"),
    XSS_EVENT_ONERROR("onerror"),
    XSS_EVENT_ONLOAD("onload"),

    // HTML/JS
    HTML_IFRAME("<iframe>"),
    HTML_EVENT_ONCLICK("onclick"),
    HTML_JAVASCRIPT_ALERT("alert()"),

    // Sensitive Data Keywords
    SENSITIVE_DATA_PASSWORD("password"),
    SENSITIVE_DATA_CREDIT_CARD("credit card"),
    SENSITIVE_DATA_SSN("ssn"),

    // LDAP
    LDAP_INJECTION("*"),
    LDAP_INJECTION_OPEN_PAREN("("),
    LDAP_INJECTION_CLOSE_PAREN(")"),
    LDAP_INJECTION_BACKSLASH("\\"),

    // CSRF
    CSRF_TOKEN("csrf_token"),
    CSRF_NONCE("nonce"),


    PATH_ADMIN("/admin/"),
    PATH_AUTH("/auth/"),
    PATH_COMMENT("/comments/"),
    PATH_FORUM_ROOM("/forumrooms/"),
    PATH_USER("/users/"),


    CODE_INJECTION_ASP_OPEN("<%"),
    CODE_INJECTION_ASP_OPEN_OUTPUT("<%="),
    CODE_INJECTION_EL_EXPRESSION("${"),

    // NP
    NETWORK_PROTOCOL_HTTP("http://"),
    NETWORK_PROTOCOL_HTTPS("https://"),
    NETWORK_PROTOCOL_FTP("ftp://"),
    NETWORK_PROTOCOL_FILE("file://"),

    // FS
    FILE_TRAVERSAL(".."),
    RM_RF("rm -rf"),
    DEL("del"),
    ERASE("erase"),
    RMDIR("rmdir"),
    UNLINK("unlink"),
    SYSTEM_EXIT("System.exit()"),
    RUNTIME_EXEC("Runtime.exec()"),

    // Java Reflection
    JAVA_LANG_REFLECT("java.lang.reflect"),
    GETCLASS("getClass()"),
    NEWINSTANCE("newInstance()"),
    GETMETHOD("getMethod()"),
    GETDECLARED_METHOD("getDeclaredMethod()"),
    INVOKE("invoke()"),
    SETACCESSIBLE("setAccessible()"),

    // Regex
    PATTERN("Pattern"),
    MATCHER("Matcher"),
    COMPILE("compile()"),
    FIND("find()"),
    REPLACE_ALL("replaceAll()"),

    // CMD
    SYSTEM("system()"),
    POPEN("popen()"),
    SHELL_EXEC("shell_exec()"),
    PASSTHRU("passthru()"),
    PROC_OPEN("proc_open()"),

    // XML
    ENTITY("<!ENTITY"),
    DOCTYPE("<!DOCTYPE"),
    CDATA("CDATA"),

    // XPath
    SELECT_XPATH("SELECT"),
    NOT("NOT"),
    COUNT("COUNT"),
    XPATH("//"),

    // Email
    CC("CC:"),
    BCC("BCC:"),
    FROM("From:"),
    TO("To:"),
    REPLY_TO("Reply-To:"),
    SUBJECT("Subject:"),

    // JS
    JAVASCRIPT_ALERT("alert()"),
    JAVASCRIPT_PROMPT("prompt()"),
    JAVASCRIPT_CONFIRM("confirm()"),
    JAVASCRIPT_SETTIMEOUT("setTimeout()"),
    JAVASCRIPT_SETINTERVAL("setInterval()"),
    JAVASCRIPT_EVAL("eval()"),
    JAVASCRIPT_DOCUMENT_WRITE("document.write()"),
    JAVASCRIPT_DOCUMENT_COOKIE("document.cookie"),
    JAVASCRIPT_LOCATION("location."),
    JAVASCRIPT_WINDOW("window."),
    JAVASCRIPT_LOCAL_STORAGE("localStorage."),
    JAVASCRIPT_SESSION_STORAGE("sessionStorage."),
    JAVASCRIPT_FETCH("fetch()"),
    JAVASCRIPT_XMLHTTPREQUEST("XMLHttpRequest()"),
    JAVASCRIPT_ONCLICK("onclick"),
    JAVASCRIPT_ONMOUSEOVER("onmouseover"),
    JAVASCRIPT_ONMOUSEOUT("onmouseout"),
    JAVASCRIPT_ONLOAD("onload"),
    JAVASCRIPT_ONERROR("onerror"),
    JAVASCRIPT_ONSUBMIT("onsubmit"),
    JAVASCRIPT_ONCHANGE("onchange"),
    JAVASCRIPT_ONKEYPRESS("onkeypress"),
    JAVASCRIPT_ONFOCUS("onfocus"),
    JAVASCRIPT_ONBLUR("onblur"),
    JAVASCRIPT_ONSELECT("onselect"),
    JAVASCRIPT_ONRESIZE("onresize"),
    JAVASCRIPT_ONSCROLL("onscroll"),
    JAVASCRIPT_ONUNLOAD("onunload"),
    JAVASCRIPT_ONDBLCLICK("ondblclick"),
    JAVASCRIPT_ONLOADSTART("onloadstart"),
    JAVASCRIPT_ONPROGRESS("onprogress"),
    JAVASCRIPT_ONLOADEND("onloadend"),
    JAVASCRIPT_ONABORT("onabort"),
    JAVASCRIPT_ONTIMEOUT("ontimeout");

    private final String word;

    BannedWordsEnum(String word) {
        this.word = word;
    }

}
