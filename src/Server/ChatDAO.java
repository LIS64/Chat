package Server;

import java.sql.Connection;

public interface ChatDAO {

	public Connection getConnection();
	public void insertMessages(MessageClient mc);
	
}
