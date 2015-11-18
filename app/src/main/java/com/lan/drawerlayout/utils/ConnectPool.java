package com.lan.drawerlayout.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
/**
 * ʹ��Semaphoreģ�����ݿ����ӳص�ʹ��
 * @author zhy
 *
 */
public class ConnectPool
{
	private final List<Conn> pool = new ArrayList<Conn>(3);
	private final Semaphore semaphore = new Semaphore(3);

	/**
	 * ��ʼ������3������
	 */
	public ConnectPool()
	{
		pool.add(new Conn());
		pool.add(new Conn());
		pool.add(new Conn());
	}

	/**
	 * �����������
	 * @return
	 * @throws InterruptedException
	 */
	public Conn getConn() throws InterruptedException
	{
		semaphore.acquire();
		Conn c = null  ;
		synchronized (pool)
		{
			c = pool.remove(0);
		}
		System.out.println(Thread.currentThread().getName()+" get a conn " + c);
		return c ;
	}
	
	/**
	 * �ͷ�����
	 * @param c
	 */
	public void release(Conn c)
	{
		pool.add(c);
		System.out.println(Thread.currentThread().getName()+" release a conn " + c);
		semaphore.release();
	}

	public static void main(String[] args)
	{

		final ConnectPool pool = new ConnectPool();
		
		/**
		 * ��һ���߳�ռ��1������3��
		 */
		new Thread()
		{
			public void run()
			{
				try
				{
					Conn c = pool.getConn();
					Thread.sleep(3000);
					pool.release(c);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			};
		}.start();
		/**
		 * ����3���߳������������
		 */
		for (int i = 0; i < 3; i++)
		{
			new Thread()
			{
				public void run()
				{
					try
					{
						Conn c = pool.getConn();
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				};
			}.start();
		}

	}

	private class Conn
	{
	}

}
