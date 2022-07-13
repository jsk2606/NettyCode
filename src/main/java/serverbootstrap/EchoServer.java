package serverbootstrap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/** NonBlocking*/
public class EchoServer {

	public static void main(String[] args){
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);    //스레드를 1개 쓰는 Nio 이벤트 루프다.
		EventLoopGroup workerGroup = new NioEventLoopGroup();   //인자가 없을경우 CPU 에 따른 스레드를 쓰게된다.
		try {
			ServerBootstrap b = new ServerBootstrap();  //ServerBootstrap 을 빌더패턴으로 설정한다.
			b.group(bossGroup, workerGroup)     //첫번째 아규먼트는 클라이언트 연결요청을 담당한다. 두번째 아규먼트는 연결된 소켓에 I/O를 담당한다.
			.channel(NioServerSocketChannel.class)  // 네트워크 입출력 모드를 설정한다. 거의 NIO 로 설정한다.
			.childHandler(// 연결된 채널에대한 초기화 방법을 지정한다.
				new ChannelInitializer<SocketChannel>(){  //ChannelInitializer 클래스를 이용하면 채널초기화 이벤트를 작성할수있다. 
					@Override
					public void initChannel(SocketChannel ch){
						ChannelPipeline p = ch.pipeline();  //파이프라인
						p.addLast(new ChannelHandler() {    //데이터 처리에 대한 이벤트를 작성할수있다.
							@Override
							public void handlerAdded(ChannelHandlerContext ctx) {
							}
	
							@Override
							public void handlerRemoved(ChannelHandlerContext ctx) {
							}
	
							@Override
							public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
							}
						});
					}
			});

		}

		finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
}

