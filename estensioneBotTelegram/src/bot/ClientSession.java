package bot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class ClientSession {
	static final String MENU = "MENU";
	static final String WAITING_TABLE_NAME = "WAITING_TABLE_NAME";
	static final String WAITING_ARCHIVE_NAME = "WAITING_ARCHIVE_NAME";
	static final String WAITING_PREDICTION_CHOICE = "WAITING_PREDICTION_CHOICE";
	static final String WAITING_REPEAT_PREDICTION = "WAITING_REPEAT_PREDICTION";

	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String state;
	private String lastQuestion;
	private List<Integer> predictionChoices = new ArrayList<>();

	ClientSession(String serverAddress, int serverPort) throws IOException {
		socket = new Socket(serverAddress, serverPort);
		out = new ObjectOutputStream(socket.getOutputStream());
		out.flush();
		in = new ObjectInputStream(socket.getInputStream());
		state = MENU;
	}

	String getState() {
		return state;
	}

	void setState(String state) {
		this.state = state;
	}

	String getLastQuestion() {
		return lastQuestion;
	}

	String loadTrainingSet(String tableName) throws IOException, ClassNotFoundException {
		out.writeObject(0);
		out.writeObject(tableName);
		out.flush();
		return in.readObject().toString();
	}

	String learnTree() throws IOException, ClassNotFoundException {
		out.writeObject(1);
		out.flush();
		return in.readObject().toString();
	}

	String loadTree(String archiveName) throws IOException, ClassNotFoundException {
		out.writeObject(2);
		out.writeObject(archiveName);
		out.flush();
		return in.readObject().toString();
	}

	ServerResponse startPrediction() throws IOException, ClassNotFoundException {
		predictionChoices.clear();
		out.writeObject(3);
		out.flush();
		return readPredictionResponse();
	}

	ServerResponse sendPredictionChoice(int choice) throws IOException, ClassNotFoundException {
		out.writeObject(choice);
		out.flush();

		ServerResponse response = readPredictionResponse();

		if(response.getType().equals(ServerResponse.ERROR) && lastQuestion != null)
			return recoverPrediction();

		if(!response.getType().equals(ServerResponse.ERROR))
			predictionChoices.add(choice);

		return response;
	}

	private ServerResponse recoverPrediction() throws IOException, ClassNotFoundException {
		out.writeObject(3);
		out.flush();

		ServerResponse response = readPredictionResponse();

		for(Integer choice : predictionChoices) {
			if(!response.getType().equals(ServerResponse.QUERY))
				return response;

			out.writeObject(choice);
			out.flush();
			response = readPredictionResponse();
		}

		return response;
	}

	private ServerResponse readPredictionResponse() throws IOException, ClassNotFoundException {
		String answer = in.readObject().toString();

		if(answer.equals("QUERY")) {
			String question = in.readObject().toString();
			lastQuestion = question;
			return ServerResponse.query(question);
		}

		if(answer.equals("OK")) {
			String predictedClass = in.readObject().toString();
			return ServerResponse.prediction(predictedClass);
		}

		return ServerResponse.error(answer);
	}

	void close() {
		try {
			if(socket != null)
				socket.close();
		} catch(IOException ignored) {
		}
	}

	static class ServerResponse {
		static final String QUERY = "QUERY";
		static final String PREDICTION = "PREDICTION";
		static final String ERROR = "ERROR";

		private String type;
		private String message;

		private ServerResponse(String type, String message) {
			this.type = type;
			this.message = message;
		}

		static ServerResponse query(String question) {
			return new ServerResponse(QUERY, question);
		}

		static ServerResponse prediction(String predictedClass) {
			return new ServerResponse(PREDICTION, predictedClass);
		}

		static ServerResponse error(String errorMessage) {
			return new ServerResponse(ERROR, errorMessage);
		}

		String getType() {
			return type;
		}

		String getMessage() {
			return message;
		}
	}
}
