package Client;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ClientChat extends Application {

	private Socket socket;
	private BufferedReader socketReader;
	private BufferedWriter socketWriter;
	private TextField adressServer;
	private TextField portNumber;
	private TextArea windowChat;
	private TextField message;
	private TextField nameUser;
	private boolean isConnect = false;
	
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		
		primaryStage.setTitle("Онлайн чат");
		windowChat = new TextArea();
		adressServer = new TextField();
	    portNumber = new TextField();
		nameUser = new TextField();
		message = new TextField();
		Button sendMessages = new Button("Отправить сообщение");
		Button connect = new Button("Подключиться");
		GridPane root = new GridPane();
		root.setVgap(5);
		root.setHgap(5);
		root.add(new Label("Настройки соединения"), 1, 0);
		root.add(new Label("Адрес сервера:"), 1, 1);
		root.add(adressServer, 1, 2);
		root.add(new Label("Порт:"), 1, 3);
		root.add(portNumber, 1, 4);
		root.add(new Label("Имя пользователя:"), 1, 5);
		root.add(nameUser, 1, 6);
		root.add(connect, 1, 7);
		root.add(windowChat, 0, 0, 1, 9);
		root.add(message, 0, 9, 1, 1);
		root.add(sendMessages, 1, 9,2,1);
	
		sendMessages.setOnAction(e->{
			if (isConnect){
			sendMessages(getTime()+" "+nameUser.getText()+": "+message.getText());
			} else {errorMsg("Ошибка подключения","Попробуйте подключиться заново");}
			});
		
		connect.setOnAction(e -> {
			connection();
			if (isConnect){
				new Thread(new writeMessages()).start();
			} else {errorMsg("Ошибка подключения","Попробуйте подключиться заново");}
		});
		
		primaryStage.setScene(new Scene(root));
	
		primaryStage.setOnHiding(e->{
			windowChat.appendText(getTime()+" Пользователь "+nameUser.getText()+" отключился \n");
			//closeConnection();
			System.exit(0);
		});
		primaryStage.show();
	}

	
	private String getTime(){
        SimpleDateFormat format1 = new SimpleDateFormat("dd.MM.yyyy hh:mm");
		return format1.format(new Date());
	}

	void connection(){
		try {
			if (nameUser.getText().isEmpty()||adressServer.getText().isEmpty()||portNumber.getText().isEmpty()){
				errorMsg("Ошибка данных", "Вы ввели не все данные, попробуйте ещё раз");
				isConnect=false;
			} else {
			socket = new Socket(adressServer.getText(), Integer.parseInt(portNumber.getText()));
			socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
			isConnect=true;
			sendMessages(getTime()+" Пользователь "+nameUser.getText()+" подключился");
			nameUser.setEditable(false); adressServer.setEditable(false); portNumber.setEditable(false);
			}
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			isConnect=false;
		}
	}
	
	void errorMsg(String header, String context){
		Alert error = new Alert(AlertType.ERROR);
		error.setTitle("Ошибка");
		error.setHeaderText(header);
		error.setContentText(context);
		error.show();
	}
	
	void sendMessages(String msg){
		if (msg.length()!=0 || !socket.isClosed()){
			try {
				socketWriter.write(msg);
				socketWriter.newLine();
				socketWriter.flush();
				message.setText("");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			
			}
		} else {
			closeConnection();
		}
	}
	
	public synchronized void closeConnection(){
		if (!socket.isClosed()){
			try {
				windowChat.appendText(getTime()+" Пользователь "+nameUser.getText()+" отключился \n");
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	
	 class writeMessages implements Runnable {
			// TODO Auto-generated method stub
		
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (!socket.isClosed()){
					String readLine = null;
					try {
						readLine = socketReader.readLine();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						closeConnection();
					}
					if (readLine!=null) {
						windowChat.appendText(readLine+"\n");
					} else {
						closeConnection();
					}
				}
				}
			}
}
