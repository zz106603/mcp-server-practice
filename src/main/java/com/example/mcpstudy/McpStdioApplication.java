package com.example.mcpstudy;

import java.util.Map;

import com.example.mcpstudy.mcp.StdioMcpServerRunner;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

public class McpStdioApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(McpstudyApplication.class);
		application.setWebApplicationType(WebApplicationType.NONE);
		application.setBannerMode(Banner.Mode.OFF);
		application.setLogStartupInfo(false);
		application.setDefaultProperties(Map.of(
			"logging.level.root", "OFF",
			"spring.main.web-application-type", "none",
			"spring.main.banner-mode", "off"
		));

		try (ConfigurableApplicationContext context = application.run(args)) {
			context.getBean(StdioMcpServerRunner.class).run();
		}
		catch (RuntimeException e) {
			System.err.println("Stdio MCP server failed: " + e.getMessage());
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
