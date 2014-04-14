/**
 *
 * Copyright the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.smack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.junit.Test;

public class PacketCollectorTest
{

	@Test
	public void verifyRollover()
	{
		TestPacketCollector collector = new TestPacketCollector(null, new OKEverything(), 5);
		
		for (int i=0; i<6; i++)
		{
			Packet testPacket = new TestPacket(i);
			collector.processPacket(testPacket);
		}
		
		// Assert that '0' has rolled off
		assertEquals("1", collector.nextResultBlockForever().getPacketID());
		assertEquals("2", collector.nextResultBlockForever().getPacketID());
		assertEquals("3", collector.nextResultBlockForever().getPacketID());
		assertEquals("4", collector.nextResultBlockForever().getPacketID());
		assertEquals("5", collector.pollResult().getPacketID());
		assertNull(collector.pollResult());
		
		for (int i=10; i<15; i++)
		{
			Packet testPacket = new TestPacket(i);
			collector.processPacket(testPacket);
		}
		
		assertEquals("10", collector.nextResultBlockForever().getPacketID());
		assertEquals("11", collector.nextResultBlockForever().getPacketID());
		assertEquals("12", collector.nextResultBlockForever().getPacketID());
		assertEquals("13", collector.nextResultBlockForever().getPacketID());
		assertEquals("14", collector.pollResult().getPacketID());
		assertNull(collector.pollResult());
		
		assertNull(collector.nextResult(1000));
	}

	/**
	 * Although this doesn't guarentee anything due to the nature of threading, it can 
	 * potentially catch problems.
	 */
	@Test
	public void verifyThreadSafety()
	{
		int insertCount = 500;
		final TestPacketCollector collector = new TestPacketCollector(null, new OKEverything(), insertCount);
		
		Thread consumer1 = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					while (true)
					{
						try
						{
							Thread.sleep(3);
						}
						catch (InterruptedException e)
						{
						}
						@SuppressWarnings("unused")
						Packet packet = collector.nextResultBlockForever();
//						System.out.println(Thread.currentThread().getName() + "  packet: " + packet);
					}
				}
				catch (RuntimeException re)
				{
					if (re.getCause() instanceof InterruptedException)
					{
//						System.out.println(Thread.currentThread().getName() + " has been interupted");
					}
				}
			}
		});
		consumer1.setName("consumer 1");

		Thread consumer2 = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Packet p = null;
				
				do
				{
					try
					{
						Thread.sleep(3);
					}
					catch (InterruptedException e)
					{
					}
					p = collector.nextResult(1);
//					System.out.println(Thread.currentThread().getName() + "  packet: " + p);
				}
				while (p != null);
			}
		});
		consumer2.setName("consumer 2");

		Thread consumer3 = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Packet p = null;
				
				do
				{
					try
					{
						Thread.sleep(3);
					}
					catch (InterruptedException e)
					{
					}
					p = collector.pollResult();
//					System.out.println(Thread.currentThread().getName() + "  packet: " + p);
				}
				while (p != null);
			}
		});
		consumer3.setName("consumer 3");

		consumer1.start();
		consumer2.start();
		consumer3.start();
		
		for(int i=0; i<insertCount; i++)
		{
			collector.processPacket(new TestPacket(i));
		}

		try
		{
			Thread.sleep(5000);
			consumer3.join();
			consumer2.join();
			consumer1.interrupt();
		}
		catch (InterruptedException e)
		{
		}
		//We cannot guarantee that this is going to pass due to the possible issue of timing between consumer 1 
		// and main, but the probability is extremely remote.
		assertNull(collector.pollResult());
	}

	class OKEverything implements PacketFilter
	{
		@Override
		public boolean accept(Packet packet)
		{
			return true;
		}
		
	}
	
	class TestPacketCollector extends PacketCollector 
	{
		protected TestPacketCollector(XMPPConnection conection, PacketFilter packetFilter, int size)
		{
			super(conection, packetFilter, size);
		}
	}

	class TestPacket extends Packet
	{
		public TestPacket(int i)
		{
			setPacketID(String.valueOf(i));
		}

		@Override
		public String toString()
		{
			return toXML();
		}

		@Override
		public String toXML()
		{
			return "<packetId>" + getPacketID() + "</packetId>";
		}
	}
}
