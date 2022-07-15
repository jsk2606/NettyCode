package A_block;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class NonBlockingServer {
	private Map<SocketChannel, List<byte[]>> keepDataTrack = new HashMap<>();
	private ByteBuffer buffer = ByteBuffer.allocate(2 * 1024);

	private void startEchoServer(){
		try (
			// 1.셀렉터는 N개의 클라이언트 연결을 하나의 스레드로 처리한다.
			Selector selector = Selector.open();
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		){

			if((serverSocketChannel.isOpen()) && (selector.isOpen())){  //소켓채널과 셀렉터 생성여부 확인
				serverSocketChannel.configureBlocking(false);   //소켓채널의 논블락킹 설정
				serverSocketChannel.bind(new InetSocketAddress(8888));  //소켓채널에 포트 할당

				//2. 셀렉터가 등록되었다.
				serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); //소켓채널에 셀렉터 등록
				System.out.println("접속 대기중..");

				while(true){
					selector.select();  //소켓채널에 변경사항이 있는지 확인
					/** 변경사항이 없다면  이때  Blocking 발생. 이를 해결하려면 .selectNow() 메서드를 사용하면된다. */

					Iterator<SelectionKey> keys = selector.selectedKeys().iterator();   //변경사항이 발생한 채널들을 조회

					while (keys.hasNext()){
						 
						SelectionKey key = (SelectionKey) keys.next();
						keys.remove();//논블락킹소켓이므로 확인된 채널은 바로 지워준다.

						if(!key.isValid())
							continue;
						if(key.isAcceptable())  //채널에 발생한 I/O 이벤트가 연결요청인지 확인한다.
							this.accepOP(key, selector);
						else if(key.isReadable())  //채널에 발생한 I/O 이벤트가 데이터수신 인지 확인한다.
							this.readOP(key);
						else if(key.isWritable())  //채널에 발생한 I/O 이벤트가 데이터쓰기 인지 확인한다.
							this.writeOP(key);
					}
				}
			}
			else
				System.out.println("서버 소켓 생성에 실패했습니다.");
		}catch (Exception e){
			System.out.println(e.toString());
		}
	}

	//연결요청  처리  메서드
	private void accepOP(SelectionKey key, Selector selector) throws  IOException {
		ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();    //셀렉터에서 감지된 채널을  Netty 클래스로 캐스팅 한다.
		SocketChannel socketChannel = serverChannel.accept();    //클라이언트 연결을 수락하고 소켓채널을 가져온다.
		socketChannel.configureBlocking(false); //가져온 소캣채널을 논블락킹 으로 설정한다.

		System.out.println("클라이언트 연결: " + socketChannel.getRemoteAddress());

		keepDataTrack.put(socketChannel, new ArrayList<byte[]>());
		socketChannel.register(selector, SelectionKey.OP_READ);    //셀렉터에게 연결이 됬다고 알려준다.
	}

	private void readOP(SelectionKey key){
		try {
			SocketChannel socketChannel = (SocketChannel) key.channel();
			buffer.clear();
			int numRead  = -1;

			try{
				numRead = socketChannel.read(buffer);
			}
			catch (IOException e){
				System.out.println("데이터 읽기 에러!");
			}

			if(numRead == 1){
				this.keepDataTrack.remove(socketChannel);
				System.out.println("클라이언트 연결 종료: " + socketChannel.getRemoteAddress());
				socketChannel.close();
				key.cancel();
				return;
			}
			byte[] data = new byte[numRead];
			System.arraycopy(buffer.array(), 0, data, 0, numRead);
			System.out.println(new String(data, "UTF-8") + "from " + socketChannel.getRemoteAddress());

			doEchoJob(key, data);
		}
		catch(Exception e){
			System.err.println(e.toString());
		}
	}

	private void writeOP(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		List<byte[]> channelData = keepDataTrack.get(socketChannel);
		Iterator<byte[]> its = channelData.iterator();

		while (its.hasNext()){
			byte[] it = its.next();
			its.remove();
			socketChannel.write(ByteBuffer.wrap(it));
		}

		key.interestOps(SelectionKey.OP_READ);
	}

	private void doEchoJob(SelectionKey key, byte[] data) {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		List<byte[]> channelData = keepDataTrack.get(socketChannel);
		channelData.add(data);

		key.interestOps(SelectionKey.OP_WRITE);
	}

	public static void main(String[] args) throws Exception {
		NonBlockingServer main = new NonBlockingServer();
		main.startEchoServer();
	}

}
