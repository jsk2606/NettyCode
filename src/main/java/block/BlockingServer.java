package block;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BlockingServer {

	public static void main(String[] args) throws Exception {
		BlockingServer server = new BlockingServer();
		server.run();
	}

	private void run() throws IOException {
		ServerSocket server = new ServerSocket(8888);
		System.out.println("접속 대기중..");

		while(true){
			Socket sock = server.accept(); //BLOCKING 발생 - 수신대기
			System.out.println("클라이언트 연결");

			OutputStream out = sock.getOutputStream();
			InputStream in = sock.getInputStream();

			while(true){
				try {
					int request = in.read();    // BLOCKING 발생 - 입력대기
					out.write(request);
				}catch (IOException e){
					break;
				}
			}
		}
	}
}
