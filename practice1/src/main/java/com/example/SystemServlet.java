package com.example;
import java.io.*;

import java.lang.Runtime;
import java.lang.System;
import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "system", value = "/system")
public class SystemServlet extends HttpServlet {
    private String cpuInformation;
    private String osInformation;
    private String ramInformation;
    private String diskCapacityInformation;

    public void init() {
        Runtime runtime = Runtime.getRuntime();
        cpuInformation = runtime.availableProcessors() + " - ядра процесора";

        osInformation = System.getProperty("os.name") + ", " + System.getProperty("os.version") + ", "
                + System.getProperty("os.arch") + " - операційна система";

        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        ramInformation = osBean.getTotalMemorySize() / (1024 * 1024 * 1024) + " - GB RAM";

        File root = new File("/");
        diskCapacityInformation = root.getTotalSpace() / (1024 * 1024 * 1024)
                + " - GB ємність жорсткого диску, з них вільно - " + root.getUsableSpace() / (1024 * 1024 * 1024) + " GB";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1> Характеристики комп'ютера, на якому запущений сервер</h1>");
        out.println("<ul>");
        out.println("<li>" + cpuInformation + "</li>");
        out.println("<li>" + osInformation + "</li>");
        out.println("<li>" + ramInformation + "</li>");
        out.println("<li>" + diskCapacityInformation + "</li>");
        out.println("</ul>");
        out.println("</body></html>");
    }

    public void destroy() {}
}