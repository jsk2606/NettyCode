package C_channelpipeline;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class EchoServer {
	public static void main(String[] args) throws Exception{
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try{
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			//.childHandler() 메서드를 통해 서버와 클라이언트같에 연결될 파이프라인을 설정한다.
			.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				//initChannel() 메서드는 클라이언트 소켓이 생성될 때 호출된다.
				public void initChannel(SocketChannel ch){
					ChannelPipeline p = ch.pipeline();  // 클라이언트 파이프라인을 획득한다.
					//p.addLast(new EchoServerHandler());   //파이프라인에 이벤트 핸들러를 등록한다.
				}
			});
			//....
		}
		finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
}
