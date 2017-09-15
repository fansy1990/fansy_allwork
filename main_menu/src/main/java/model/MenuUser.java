package model;
/**
 * 定义实体类
 * @author fanzhe
 *
 */
public class MenuUser {
	private int id;// 序号
	private String name;// 姓名
	private int age;// 年龄
	private String gender; // 性别
	private String password; // 密码
	
	public MenuUser() {}
	public MenuUser(String name, int age, String gender,
                     String password) {
		this.name = name;
		this.age = age;
		this.gender = gender;
		this.password = password;
	}
	public MenuUser(int id, String name, int age, String gender,
                     String password) {
		this(name, age, gender, password);
		this.id = id;
	}
	@Override
	public String toString() {
		return "id:"+this.id+",name:"+this.name+",age:"+this.age+",gender:"+this.gender+
				",password:"+this.password;
	}
	/**
	 * getter & setter
	 * @return
	 */
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
}
