package pt.up.fe.comp;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.List;


import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;


import pt.up.fe.specs.util.SpecsIo;


public class TestUtils {
	
	private static final Properties PARSER_CONFIG = TestUtils.loadProperties("parser.properties");
	
	public static Properties loadProperties(String filename) {
   		try {
			Properties props = new Properties();
			props.load(new StringReader(SpecsIo.read(filename)));
			return props;
		} catch(IOException e) {
			throw new RuntimeException("Error while loading properties file '"+filename+"'", e);
		}
	}

	public static String getJmmCode(final String filename) throws IOException {
		File file = new File("test/fixtures/public/" + filename);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();

		return new String(data, StandardCharsets.UTF_8);
	}

	public static void writeJson(final String pathname, final JmmParserResult result) throws IOException {
		String astJson = result.getRootNode().toJson();

		File json = new File(pathname);

		FileOutputStream fos = new FileOutputStream(json);
		fos.write(astJson.getBytes(StandardCharsets.UTF_8));
		fos.close();
	}

	public static JmmParserResult parse(String code) {
		try {
			// Get Parser class
			String parserClassName = PARSER_CONFIG.getProperty("ParserClass");

            // Get class with main
            Class<?> parserClass = Class.forName(parserClassName);

            // It is expected that the Parser class can be instantiated without arguments
			JmmParser parser = (JmmParser) parserClass.getConstructor().newInstance();
			
			return parser.parse(code);

        } catch (Exception e) {
			throw new RuntimeException("Could not parse code", e);
        }		
		
	}

	/**
	 * Checks if there are no Error reports. Throws exception if there is at least one Report of type Error.
	 */
	public static void noErrors(List<Report> reports) {
		reports.stream()
			.filter(report -> report.getType() == ReportType.ERROR)
			.findFirst()
			.ifPresent(report -> {
				throw new RuntimeException("Found at least one error report: " + report);
			});
	}
	
	/**
	 * Checks if there are Error reports. Throws exception is there are no reports of type Error.
	 */
	public static void mustFail(List<Report> reports) {	
		boolean noReports = reports.stream()
			.filter(report -> report.getType() == ReportType.ERROR)
			.findFirst()
			.isEmpty();
			
		if(noReports) {
			throw new RuntimeException("Could not find any Error report");			
		}			
	}
	
	public static long getNumReports(List<Report> reports, ReportType type) {	
		return reports.stream()
			.filter(report -> report.getType() == type)
			.count();
	}
	
	public static long getNumErrors(List<Report> reports) {	
		return getNumReports(reports, ReportType.ERROR);
	}

}