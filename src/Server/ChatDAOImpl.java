package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class ChatDAOImpl implements ChatDAO {
	
	public Connection getConnection(){
		Connection conn=null;
		try {
			conn = DriverManager.getConnection("urlBD", "login", "password");
			System.out.println("Подключение к БД произошло успешно");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("При подключении к БД произошла ошибка \nError: "+e.getMessage());
		}
		return conn;
	}

	@Override
	public void insertMessages(MessageClient mc) {
		// TODO Auto-generated method stub
		try(Connection conn = getConnection();){
			try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO table_name (user,messages,date) VALUES (?,?,?)"); ){
				stmt.setString(1, mc.getNameUser());
				stmt.setString(2, mc.getMessage());
				stmt.setString(3, mc.getDate());
				stmt.executeUpdate();
				System.out.println("Запись успешно вставлена в базу данных");
			}catch (SQLException e) {
				// TODO: handle exception
				System.out.println("Произошла ошибка при вставке данных "+ e.getMessage());
			}

		}catch (SQLException e) {
			// TODO: handle exception
			System.out.println("Произошла ошибка при вставке данных "+ e.getMessage());
		}
		
	}

}
