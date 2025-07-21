package pers.di.common;

import java.util.Random;

public class CRandom {
	
	public static int randomInteger()
	{
		return m_random.nextInt();
	}
	public static int randomUnsignedInteger()
	{
		return Math.abs(m_random.nextInt());
	}

	public static float randomFloat()
	{
		return m_random.nextFloat();
	}
	
	private static Random m_random = new Random();
}
