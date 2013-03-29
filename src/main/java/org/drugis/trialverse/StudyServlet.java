package org.drugis.trialverse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Servlet implementation class StudyServlet
 */
public class StudyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public StudyServlet() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getPathInfo();

		Long id = Long.valueOf(path.substring(1));
		
		String url = "jdbc:postgresql://localhost/trialverse";
		Properties props = new Properties();
		props.setProperty("user","trialverse");
		props.setProperty("password","develop");
		props.setProperty("ssl","true");
		try {
			Connection conn = DriverManager.getConnection(url, props);
			Statement stmt = conn.createStatement() ;
			ResultSet rs = stmt.executeQuery("SELECT name FROM studies WHERE id = " + id) ;

			if (rs.next()) {
				Map<String, String> myMap = new HashMap<>();
				myMap.put("id", id.toString());
				myMap.put("name", rs.getString(1));
				
				PrintWriter writer = response.getWriter();
				ObjectMapper mapper = new ObjectMapper();
				mapper.writeValue(writer, myMap);
			}

			rs.close() ;
			stmt.close() ;
			conn.close() ;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
