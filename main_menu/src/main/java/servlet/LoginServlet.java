package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.MenuUser;
import service.MenuUserService;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LoginServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 设置编码
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		// 设置参数值
		String name = request.getParameter("name");
		String passwd = request.getParameter("passwd");

		// 调用Service获取Student
		MenuUserService service = new MenuUserService();
		MenuUser student = service.getMenuUserByName(name);
		
		// 根据student返回值判断,
//		返回值： 1:用户不存在；2:密码错误；0:用户名密码正确
		PrintWriter out = response.getWriter();
		if (student == null) {
			out.write("1");
		} else if (student.getPassword().equals(passwd)) {
			out.write("0");
		}else{
			out.write("2");
		}
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
