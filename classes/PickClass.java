import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;

public class PickClass extends HttpServlet
{



    protected void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
	doRequest(req, res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException
    {
	doRequest(req, res);
      
    }
}


