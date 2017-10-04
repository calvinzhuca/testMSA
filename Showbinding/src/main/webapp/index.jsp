<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Products</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<head>
<body>
  <%
    String userName = System.getenv("USER_NAME");
    String password = System.getenv("PASSWORD");
    String developrPortalUrl = System.getenv("DEVELOPER_PORTAL_URL");
    if (userName == null || ("".equals(userName))) {
  %>
      <h2>There is no information about Developer Portal!</h2>
  <%
    } else {
  %>
      <h2>User Name: </h2><p><%= userName %></p>
      <h2>Password : </h2><p><%= password %></p>
      <h2><a href="<%= developrPortalUrl %>"><h3>Developer Portal</h3></a></p>
  <%
    }
  %>
  <a href="<%= request.getRequestURI() %>"><h3>Try Again</h3></a>
</body>
</html>