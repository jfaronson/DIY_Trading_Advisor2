/**
 * 
 */
package net.aronsonhome.diyadvisor;

import java.net.http.HttpRequest.BodyPublisher;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow.Subscriber;

/**
 * @author trav3
 *
 */
public class MyBodyPublisher implements BodyPublisher
{

	@Override
	public void subscribe(Subscriber<? super ByteBuffer> subscriber)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public long contentLength()
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
